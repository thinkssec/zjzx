
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
     <script type="text/javascript" src="<%=request.getContextPath() %>/static/easyui/datagrid-export.js"></script>
    <script type="text/javascript" src="<%=request.getContextPath() %>/static/easyui/locale/easyui-lang-zh_CN.js"></script>
</head>
<style>

 </style>
<script type="text/javascript">

</script>
<body>

<div>
    <input class="easyui-datetimebox" id="c1" data-options="required:true,showSeconds:false" style="width:150px">
    <font font-family="Sans-serif" font-size="2.5em">  --  </font> 
    <input class="easyui-datetimebox" id="c2"  data-options="required:true,showSeconds:false" style="width: 150px">
	<input id="submitBtn" class="btn btn-primary" type="button" value="查询"/>

	<table id="datatable" title="请求列表"></table>
</div>
<div style="padding-top:10px">
	<table id="datatable2" title="业务处理队列"></table>
</div>
<script type="text/javascript">
    var toolbar = [
    	{
	        text:'手动刷新',
	        iconCls:'icon-reload',
	        handler:function(){clearInterval(i1);$('#datatable').datagrid("load")}
	    },
        {
            text:'开启自动刷新',
            iconCls:'icon-reload',
            handler:function(){i1=setInterval("load1()",500);}
        },
        {
            text:'数据源监控',
            iconCls:'icon-ok',
            handler:function(){
                window.open('http://localhost:8080/druid/index.html');
            }
        },
        {
            text:'导出excle',
            iconCls:'icon-redo',
            handler:function(){
                    stockPrizeExport();
            }
        }
    ];
    var toolbar2 = [
    	{
	        text:'手动刷新',
	        iconCls:'icon-reload',
	        handler:function(){clearInterval(i2);$('#datatable2').datagrid("load")}
	    },
        {
            text:'开启自动刷新',
            iconCls:'icon-reload',
            handler:function(){i2=setInterval("load2()",500);}
        }
    ];

    var dgOptions = {
        rownumbers: true,
        //fit: true,
        height:300,
        border: true,
        url: '',
        method: 'post',
        cache: false,
        //oolbar: '#tb',
        /*pageSize: 20,
         pagination: true,*/
        multiSort: true,
        showFooter:true,
        loadMsg:false,
        toolbar:toolbar,
        columns: [[
            {field: 'ID',hidden: true},
            {field: 'USERNAME', title: '请求用户', width: 180},
            {field: 'DW', title: '单位', width: 180},
            /*{field: 'IP', title: 'IP地址', width: 180},*/
            {field: 'CALL', title: '请求指令', width: 180},
            {field: 'I', title: '接收时间', width: 180},
            {field: 'O', title: '接收完成时间', width: 180},
            /*{field: 'SESSIONID', title: '业务编号', width: 180},*/
            {field: 'S', title: '状态', width: 180}
        ]],
        onLoadSuccess:function(data){
            //console.log(data);
        }
    };
    var i1,i2;
    var dgOptions2 = {
        rownumbers: true,
        //fit: true,
        height:300,
        border: true,
        url: '<%=request.getContextPath()%>/em/getHandleMsgList',
        method: 'post',
        //oolbar: '#tb',
        /*pageSize: 20,
         pagination: true,*/
        multiSort: true,
        showFooter:true,
        loadMsg:false,
        toolbar:toolbar2,
        columns: [[
            {field: 'ID',hidden: true},
            {field: 'USERNAME', title: '请求用户', width: 180},
            {field: 'DW', title: '单位', width: 180},
            {field: 'CALL', title: '请求命令', width: 180},
            {field: 'I', title: '开始时间', width: 180},
            {field: 'O', title: '完成时间', width: 180},
            /*{field: 'SESSIONID', title: '业务编号', width: 180},*/
            {field: 'S', title: '状态', width: 180}
        ]],
        onLoadSuccess:function(data){
        
        }
    };

    function load1() {
         $('#datatable').datagrid(dgOptions);
         var c1 = $('#c1').datetimebox('getValue');	
         var c2 = $('#c2').datetimebox('getValue');	
    	 var op = $("#datatable").datagrid("options");//获取 option设置对象
         op.url = '<%=request.getContextPath()%>/em/getRequestList?c1='+ c1 +'&c2='+ c2;//设置url
         $("#datatable").datagrid("load");//重新加载
    }
    function load2() {
        $('#datatable2').datagrid(dgOptions2);
    }
     function stockPrizeExport(){
       $('#datatable').datagrid('toExcel','请求列表.xls');
    } 
     
    $("#submitBtn").click(function(){
    	c1 = $('#c1').val();
        c2 = $('#c2').val();
        load1();
    });
     
     $(document).ready(function(){
         //setInterval("load()",500);
          load1();
          load2();
     });
</script>
</body>
</html>