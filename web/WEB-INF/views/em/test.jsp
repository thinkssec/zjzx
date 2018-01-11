
<%@ page language="java" contentType="text/html;charset=UTF-8"  isELIgnored="true" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp" %>
<html lang="zh">
<head>
    <meta charset="UTF-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title></title>
    <link rel="stylesheet" type="text/css" href="<%=request.getContextPath() %>/static/easyui/themes/gray/easyui.css">
    <link rel="stylesheet" type="text/css" href="<%=request.getContextPath() %>/static/easyui/themes/icon.css">
    <link rel="stylesheet" type="text/css" href="<%=request.getContextPath() %>/static/easyui/themes/color.css">
    <script type="text/javascript" src="<%=request.getContextPath() %>/static/easyui/jquery.min.js"></script>
    <script type="text/javascript" src="<%=request.getContextPath() %>/static/easyui/jquery.easyui.min.js"></script>
    <script type="text/javascript" src="<%=request.getContextPath() %>/static/easyui/locale/easyui-lang-zh_CN.js"></script>
</head>
<style>

 </style>
<script type="text/javascript">

</script>
<body>
<form id="importFileForm" method="post" enctype="multipart/form-data">
    <div style="margin-bottom:20px">
        <%--<input name="requestBody" value="{'username':'admin','password':'11111111','call':'getRequestList','params':{'a':1,'b':1}}"/>
--%>
        <input name="file" class="easyui-filebox" label="文件:" labelPosition="top" data-options="prompt:'选择一个文件...'" style="width:100%">
    </div>
    <input type="button" value="提交" onclick="doSub()">
</form>
<script>
    function doSub(){
        var formData = new FormData($("#importFileForm")[0]);
        console.log(formData);
        //调用apicontroller后台action方法，将form数据传递给后台处理。contentType必须设置为false,否则chrome和firefox不兼容
        $.ajax({
            url:'<%=request.getContextPath()%>/a/requestf?requestBody={%22type%22:%22f%22,%22username%22:%22admin%22,%22password%22:%2211111111%22,%22ip%22:%22127.0.0.1%22,%22sessionid%22:%22'+ Date.parse(new Date())+'%22,%22call%22:%22pancreateFile%22,%22params%22:%27{%22PATH%22:%22/root/d%22}%27}',
            type: 'POST',
            data: formData,
            async: false,
            cache: false,
            contentType: false,
            processData: false,
            success: function (returnInfo) {

            },
            error: function (returnInfo) {

            }
        });
    }

    </script>

</body>
</html>