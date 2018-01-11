
<%@ page language="java" contentType="text/html;charset=UTF-8"  isELIgnored="true" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp" %>
<html lang="zh">
<head>
    <meta charset="UTF-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title></title>
    <link rel="stylesheet" type="text/css" href="<%=request.getContextPath() %>/static/login/css1/normalize.css"/>
    <link rel="stylesheet" type="text/css" href="<%=request.getContextPath() %>/static/login/css1/default.css">
    <link rel="stylesheet" type="text/css" href="<%=request.getContextPath() %>/static/login/css1/styles.css">
    <script src="<%=request.getContextPath() %>/static/jquery/jquery-1.8.3.min.js" type="text/javascript"></script>
    <link href="<%=request.getContextPath() %>/static/jquery-validation/1.11.0/jquery.validate.min.css" type="text/css" rel="stylesheet"/>
    <script src="<%=request.getContextPath() %>/static/jquery-validation/1.11.0/jquery.validate.min.js" type="text/javascript"></script>
</head>
<style>
    input:-webkit-autofill {
        -webkit-box-shadow: 0 0 0 1000px white inset !important;
    }
    form {
        padding: 20px 0;
        position: relative;
        z-index: 2;
    }
    form input {
        -webkit-appearance: none;
        -moz-appearance: none;
        appearance: none;
        outline: 0;
        border: 1px solid rgba(140, 137, 255, 0.3);
        background-color: rgba(255, 255, 255, 0.2);
        width: 200px;
        border-radius: 3px;
        padding: 10px 15px;
        margin: 0 auto 10px auto;
        display: block;
        text-align: center;
        font-size: 18px;
        color: #45aeea;
        -webkit-transition-duration: 0.25s;
        transition-duration: 0.25s;
        font-weight: 300;
    }
    form input:hover {
        background-color: rgba(255, 255, 255, 0);
    }
    form input:focus {
        background-color: white;
        width: 200px;
        color: #53e3a6;
    }
    form button {
        -webkit-appearance: none;
        -moz-appearance: none;
        appearance: none;
        outline: 0;
        background-color: white;
        border: 0;
        padding: 10px 15px;
        color: #53e3a6;
        border-radius: 3px;
        width: 250px;
        cursor: pointer;
        font-size: 18px;
        -webkit-transition-duration: 0.25s;
        transition-duration: 0.25s;
    }
    .panel-lite.form-success .panel-lite h4 {
        -webkit-transform: translateY(385px);
        -ms-transform: translateY(385px);
        transform: translateY(385px);
    }
    .panel-lite h4 {
        font-weight: 400;
        font-size: 24px;
        text-align: center;
        color: #45aeea;
        margin: 15px auto;
        -webkit-transition-duration: 1s;
        transition-duration: 1s;
        -webkit-transition-timing-function: ease;
        transition-timing-function: ease;
        font-weight: 200;
    }

    </style>
<script type="text/javascript">
    $(document).ready(function () {
        $("#loginForm").validate({
            rules: {
                validateCode: {remote: "${pageContext.request.contextPath}/servlet/validateCodeServlet"}
            },
            messages: {
                username: {required: "请填写用户名."}, password: {required: "请填写密码."},
                validateCode: {remote: "验证码不正确.", required: "请填写验证码."}
            },
            errorLabelContainer: "#messageBox",
            errorPlacement: function (error, element) {
                error.appendTo($("#loginError").parent());
            }
        });
        //$('#username').addClass("form-control:valid");
        //$('#password').valid();
    });
    // 如果在框架或在对话框中，则弹出提示并跳转到首页
    if (self.frameElement && self.frameElement.tagName == "IFRAME" || $('#left').length > 0 || $('.jbox').length > 0) {
        alert('未登录或登录超时。请重新登录，谢谢！');
        top.location = "${ctx}";
    }
</script>
<body>
<div class="htmleaf-container">
    <header class="htmleaf-header">
        <h1></h1>
    </header>
    <div class="panel-lite">
        <div class="thumbur">
        <div class="icon-lock" id="dddd"></div>
    </div>
        <h4>服务器监控平台</h4>
        <form id="loginForm" class="form" action="<%=request.getContextPath() %>/em/login" method="post">
            <%--<div class="form-group">
                <input type="text"  id="username" name="username" required="required" class="form-control"
                       value="${username}">

                <label class="form-label">用户名 </label>
            </div>
            <div class="form-group">
                <input type="password" required="required" class="form-control" id="password" name="password">

                <label class="form-label">密　码</label>
            </div>
            <a href="#">忘记密码 ? </a>--%>
                <input type="text" id="username" name="username" placeholder="用户名" value="">
                <input type="password" placeholder="密码" id="password" name="password">
            <button class="floating-btn" id="login-button"><i class="icon-arrow"></i></button>
        </form>

    </div>


    <script>
        $('#login-button').click(function (event) {

            event.preventDefault();

            $('form').fadeOut(0);
            $('.panel-lite').addClass('form-success');
            $('form').submit();
        });
    </script>

</body>
</html>