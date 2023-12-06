<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <meta http-equiv="Content-Language" content="zh-cn">
    <meta name="apple-mobile-web-app-capable" content="no">
    <meta name="apple-touch-fullscreen" content="yes">
    <meta name="format-detection" content="telephone=no,email=no">
    <meta name="apple-mobile-web-app-status-bar-style" content="white">
    <meta http-equiv="X-UA-Compatible" content="IE=Edge,chrome=1">
    <meta content="width=device-width, initial-scale=1, maximum-scale=1.2, user-scalable=0" name="viewport" />
    <title>在线支付 - 网上支付 安全快速！</title>
    <!-- 依 赖 样 式-->
    <link rel="stylesheet" href="https://static.ilian.icu/component/pear/css/pear.css" type="text/css"/>
    <link href="https://static.ilian.icu/index/pay/console/qrcode.css" rel="stylesheet" />
    <script src="https://static.ilian.icu/index/js/jquery-3.2.1.min.js"></script>
    <link rel="stylesheet" href="https://static.ilian.icu/index/user/assets/vendor/css/rtl/core.css" class="template-customizer-core-css" />
    <link rel="stylesheet" href="https://static.ilian.icu/index/user/assets/vendor/css/rtl/theme-default.css" class="template-customizer-theme-css" />
    <link rel="stylesheet" href="https://static.ilian.icu/index/user/assets/css/demo.css" />
    <link rel="stylesheet" href="https://static.ilian.icu/index/user/assets/vendor/libs/sweetalert2/sweetalert2.css" />
    <style>
        body {
            background: #f2f2f4;
        }

        body, html {
            width: 100%;
            height: 100%;
        }

        *, :after, :before {
            box-sizing: border-box;
        }


        * {
            margin: 0;
            padding: 0;
        }

        img {
            max-width: 100%;
        }

        #header {
            height: 60px;
            border-bottom: 2px solid #eee;
            background-color: #fff;
            text-align: center;
            line-height: 60px;
        }

        #header h1 {
            font-size: 20px;
        }

        #main {
            overflow: hidden;
            margin: 0 auto;
            padding: 20px;
            padding-top: 80px;
            width: 992px;
            max-width: 100%;
        }

        #main .left {
            float: left;
            width: 40%;
            box-shadow: 0 0 60px #b5f1ff;
        }

        .left p {
            margin: 10px auto;
        }

        .make {
            padding-top: 15px;
            border-radius: 10px;
            background-color: #fff;
            box-shadow: 0 3px 3px 0 rgba(0,0,0,.05);
            color: #666;
            text-align: center;
            transition: all .2s linear;
        }

        .make .qrcode {
            margin: auto;
        }

        .make .money {
            margin-bottom: 0;
            color: #f44336;
            font-weight: 600;
            font-size: 15px;
        }

        .info {
            margin-top: 15px;
            padding: 10px;
            width: 100%;
            border-radius: 0 0 10px 10px;
            background: #32343d;
            color: #f2f2f2;
            text-align: center;
            font-size: 14px;
        }

        #main .right {
            float: right;
            padding-top: 20px;
            width: 60%;
            color: #ccc;
            text-align: center;
        }

        @media (max-width:768px) {
            #main {
                padding-top: 20px;
            }

            #main .left {
                width: 100%;
            }

            #main .right {
                display: none;
            }
        }
    </style>
</head>
<body>
<div id="main">
    <div class="left">
        <div class="make">
            <p><img src="https://static.ilian.icu/index/pay/console/images/alipay.jpg" alt="" style="height:30px;"></p>
            <p class="money" id="price" style="font-weight:bold; color:green">支付金额：${amount!''}元</p>
            <#if aliName>
                <p><font face="微软雅黑" size="+1" color="#FF000">商户姓名：${aliName!''}</font></p>
            </#if>
            <p>
                <font face="微软雅黑" size="+1" color="#FF000">${payOrder.payOrderId!''}</font>
                <button id='copy' class="layui-btn layui-btn-default copy" data-clipboard-text="${payOrder.payOrderId}" >复制订单号</button>
            </p>
            <#if payHtmlWarn>
                <p><font face="微软雅黑" size="+1" color="#FF000">${payHtmlWarn!''}</font><br></p>
            </#if>
            <center><p class="qrcode" id="qrcode" ><img class="kalecloud" id="qrcode_load" src="https://static.ilian.icu/index/images/status/loading.gif" style="display: block;"></p></center>
            <center>
                <a id="startApp" type="button" class="btn btn-lg btn-block btn-danger" href="${payOrder.returnUrl}" style="font-size:13px;width:250px;display:none">一键启动APP支付</a>
            </center>
            <div class="info">
                <p id="divTime">正在获取二维码,请稍等...</p>
                <p>请使用支付宝扫一扫</p>
            </div>
        </div>
    </div>
    <div class="right">
        <img src="https://static.ilian.icu/index/pay/console/images/alipay-sys.png" />
    </div>
</div>
<script src="https://static.ilian.icu/component/layer/layer.js"></script>
<script src="https://static.ilian.icu/index/user/assets/vendor/libs/sweetalert2/sweetalert2.js"></script>
<script type="text/javascript" src="https://static.ilian.icu/index/user/assets/vendor/libs/clipboard/clipboard.js"></script>
<script type="text/javascript">
    var intDiff = parseInt('7200');//倒计时总秒数量
    function timer(){
        var timerId = window.setInterval(function(){
            var day=0,
                hour=0,
                minute=0,
                second=0;//时间默认值
            if(intDiff > 0){
                day = Math.floor(intDiff / (60 * 60 * 24));
                hour = Math.floor(intDiff / (60 * 60)) - (day * 24);
                minute = Math.floor(intDiff / 60) - (day * 24 * 60) - (hour * 60);
                second = Math.floor(intDiff) - (day * 24 * 60 * 60) - (hour * 60 * 60) - (minute * 60);
            }
            if (minute <= 9) minute = '0' + minute;
            if (second <= 9) second = '0' + second;
            if (hour <= 0 && minute <= 0 && second <= 0 && updateQrOk !== 1) {
                $("#startApp").hide();
                $("#divTime").html("<small style='color:red; font-size:26px'>订单二维码已过期</small>");
                $("#qrcode").html('<img id="qrcode_load" src="https://static.ilian.icu/index/images/status/qrcode_timeout.png">');//输出过期二维码提示图片
            } else if (updateQrOk === 1){
                $("#startApp").hide();
                $("#divTime").html("<small style='color:red; font-size:22px'>"+ result.msg +"</small>");
                $("#qrcode").html('<img id="qrcode_load" src="https://static.ilian.icu/index/images/status/pay_ok.png">');//支付成功
            } else {
                $("#divTime").html("二维码有效时间:<small style='color:red; font-size:24px'>" + minute + "</small>分<small style='color:red; font-size:24px'>" + second + "</small>秒,失效勿付");
            }
            if(intDiff < 0){
                clearInterval(timerId);
            }
            intDiff--

        }, 1000);
    }

    $(function(){
        timer();
    });

    order();
    updateQrOk = 0;
    updateQrImg= 0;
    updateQrNo = 0;
    lastQrImg = '';
    //订单监控  {订单监控}
    function order(){
        $.get("${payUrl}/api/pay/bill_q_pay/${payOrder.payOrderId}", { }, function (result) {
            //支付二维码
            if(result.code === 0 && (updateQrImg === 0 || (result.data.returnUrl && lastQrImg !== result.data.returnUrl))){
                updateQrImg = 1;
                lastQrImg = result.data.returnUrl;
                $("#qrcode").html('<img id="qrcode_load"  src="' + result.data.returnUrl + '">');
                //二维码获取成功
                if ("${payOrder.ifCode}" == 'alipay')
                {
                    if(isMobilCheck())
                    {
                        $("#startApp").show();
                    }
                }
            }
            // //订单已经超时
            if(result.code === 0 && updateQrOk===0 && result.data.state ===6){
                updateQrNo==1;
                intDiff = -1;
                window.clearInterval(orderlst);
                $("#startApp").hide();
                $("#divTime").html("<small style='color:red; font-size:26px'>订单二维码已过期</small>");
                $("#qrcode").html('<img id="qrcode_load" src="https://static.ilian.icu/index/images/status/qrcode_timeout.png">');//输出过期二维码提示图片
                Swal.fire({
                    title: '订单关闭',
                    text: result.msg,
                    icon: 'error',
                    customClass: {
                        confirmButton: 'btn btn-primary'
                    },
                    buttonsStyling: false
                });
            }
            //订单成功
            if(result.code === 0 && updateQrNo === 0  && result.data.state ===2){
                updateQrOk = 1;
                $("#startApp").hide();
                $("#divTime").html("<small style='color:red; font-size:22px'>"+ result.msg +"</small>");
                $("#qrcode").html('<img id="qrcode_load" src="https://static.ilian.icu/index/images/status/pay_ok.png">');//支付成功
                //回调页面
                window.clearInterval(orderlst);
            }
        },"JSON");
    }

    /**
     * 检验是否手机版，手机版直接跳转到APP支付
     * @@returns
     */
    function isMobilCheck() {
        var userAgentInfo = navigator.userAgent;

        var mobileAgents = ["Android", "iPhone", "SymbianOS", "Windows Phone", "iPad", "iPod"];

        var mobile_flag = false;

        //根据userAgent判断是否是手机
        for (var v = 0; v < mobileAgents.length; v++) {
            if (userAgentInfo.indexOf(mobileAgents[v]) > 0) {
                mobile_flag = true;
                break;
            }
        }
        var screen_width = window.screen.width;
        var screen_height = window.screen.height;

        //根据屏幕分辨率判断是否是手机
        if (screen_width > 325 && screen_height < 750) {
            mobile_flag = true;
        }

        return mobile_flag;
    }
    orderlst = window.setInterval(function () {
        order();
    }, 2000);
    $("#copy").click(function () {
        clipboard = new ClipboardJS('.copy');
        clipboard.on('success', function(e) {
            Swal.fire({
                icon: 'success',
                title: '复制成功',
                customClass: {
                    confirmButton: 'btn btn-success'
                }
            });
        });

        clipboard.on('error', function(e) {
            Swal.fire({
                icon: 'error',
                title: '复制失败,请手动复制',
                customClass: {
                    confirmButton: 'btn btn-danger'
                }
            });
        });
    });
</script>
</body>
</html>
