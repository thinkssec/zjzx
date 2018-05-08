<%@ page import="com.common.utils.UserUtils" %>
<%@ page import="com.common.realm.StatelessRealm" %>
<%@ page contentType="text/html;charset=UTF-8" %><meta http-equiv="Content-Type" content="text/html;charset=utf-8" />
<%@ include file="/WEB-INF/views/include/taglib.jsp" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<html>
<head>
    <title>数据版本管理</title>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
    <meta name="robots" content="noindex, nofollow" />
    <script src="${ctxStatic}/jquery/jquery-1.8.3.min.js" type="text/javascript"></script>
    <link rel="stylesheet" type="text/css" href="${ctxStatic}/easyui/themes/gray/easyui.css">
    <link rel="stylesheet" type="text/css" href="${ctxStatic}/easyui/themes/icon.css">
    <link rel="stylesheet" type="text/css" href="${ctxStatic}/easyui/themes/color.css">
    <script type="text/javascript" src="${ctxStatic}/easyui/jquery.easyui.min.js"></script>
    <script type="text/javascript" src="${ctxStatic}/easyui/locale/easyui-lang-zh_CN.js"></script>
    <script type="text/javascript" src="ckfinder.js"></script>
    <style type="text/css">body, html, iframe, #ckfinder {margin:0;padding:0;border:0;width:100%;height:100%;overflow:hidden;}</style>
</head>
<body class="CKFinderFrameWindow">
<div style="padding-left: 9px;">
    <a href="#" class="easyui-linkbutton" data-options="iconCls:'icon-back'" onclick="document.location='${ctx}/bbgl/sjlist'">返回</a>

    <%
        StatelessRealm.Principal principal = (StatelessRealm.Principal) UserUtils.getPrincipal();
        principal.setRelationId(request.getParameter("ID"));
    %>
</div>
<div id="ckfinder"></div>
<script type="text/javascript">
    //<![CDATA[
    (function()
    {
        var config = {};
        var get = CKFinder.tools.getUrlParam;
        var getBool = function( v )
        {
            var t = get( v );

            if ( t === null )
                return null;

            return t == '0' ? false : true;
        };

        var tmp;
        if ( tmp = get( 'configId' ) )
        {
            var win = window.opener || window;
            try
            {
                while ( ( !win.CKFinder || !win.CKFinder._.instanceConfig[ tmp ] ) && win != window.top )
                    win = win.parent;

                if ( win.CKFinder._.instanceConfig[ tmp ] )
                    config = CKFINDER.tools.extend( {}, win.CKFinder._.instanceConfig[ tmp ] );
            }
            catch(e) {}
        }

        if ( tmp = get( 'basePath' ) )
            CKFINDER.basePath = tmp;

        if ( tmp = get( 'startupPath' ) || get( 'start' ) )
            config.startupPath = decodeURIComponent( tmp );

        config.id = get( 'id' ) || '';
        if ( ( tmp = getBool( 'rlf' ) ) !== null )
            config.rememberLastFolder = tmp;

        if ( ( tmp = getBool( 'dts' ) ) !== null )
            config.disableThumbnailSelection = tmp;
        if ( ( tmp = getBool( 'sm' ) ) !== null )
            config.selectMultiple = tmp;

        if ( tmp = get( 'data' ) )
            config.selectActionData = tmp;

        if ( tmp = get( 'tdata' ) )
            config.selectThumbnailActionData = tmp;

        if ( tmp = get( 'type' ) )
            config.resourceType = tmp;

        if ( tmp = get( 'skin' ) )
            config.skin = tmp;

        if ( tmp = get( 'langCode' ) )
            config.language = tmp;

        if ( typeof( config.selectActionFunction ) == 'undefined' )
        {
            // Try to get desired "File Select" action from the URL.
            var action;
            if ( tmp = get( 'CKEditor' ) )
            {
                if ( tmp.length )
                    action = 'ckeditor';
            }
            if ( !action )
                action = get( 'action' );

            var parentWindow = ( window.parent == window ) ? window.opener : window.parent;
            if (!parentWindow) parentWindow = window.parent.document;

            if ( tmp = get( 'pwMf' ) )
                parentWindow = parentWindow.mainFrame;

            switch ( action )
            {
                case 'js':
                    var actionFunction = get( 'func' );
                    if ( actionFunction && actionFunction.length > 0 )
                        config.selectActionFunction = parentWindow[ actionFunction ];
                    actionFunction = get( 'thumbFunc' );
                    if ( actionFunction && actionFunction.length > 0 )
                        config.selectThumbnailActionFunction = parentWindow[ actionFunction ];
                    break ;

                case 'ckeditor':
                    var funcNum = get( 'CKEditorFuncNum' );
                    if ( parentWindow['CKEDITOR'] )
                    {
                        config.selectActionFunction = function( fileUrl, data )
                        {
                            parentWindow['CKEDITOR'].tools.callFunction( funcNum, fileUrl, data );
                        };

                        config.selectThumbnailActionFunction = config.selectActionFunction;
                    }
                    break;

                default:
                    if ( parentWindow && parentWindow['FCK'] && parentWindow['SetUrl'] )
                    {
                        action = 'fckeditor' ;
                        config.selectActionFunction = parentWindow['SetUrl'];

                        if ( !config.disableThumbnailSelection )
                            config.selectThumbnailActionFunction = parentWindow['SetUrl'];
                    }
                    else
                        action = null ;
            }
            config.action = action;

            var callback = get( 'cb' );
            if ( callback && callback.length > 0 )
                config.callback = parentWindow[ callback ];

        }

        // Always use 100% width and height when nested using this middle page.
        config.width =  '100%';
        config.height = '95%';
        config.resourceType='data';
        var ckfinder = new CKFinder( config );
        ckfinder.replace( 'ckfinder', config );
    })();
    //]]>
    $(function(){
        $(document).ready(function () {
            /*console.log($(window.frames["CKFinder"]));
            var tt=$(window.frames["CKFinder"].document).find("#cke_11_label");
            //tt.css('display','none');;
            console.log(tt.html());*/
        });
    });
</script>
</body>
</html>
