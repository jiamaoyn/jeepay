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
    <link rel="stylesheet" href="https://stice.ilian8html.icu/pear.css" type="text/css"/>
    <link href="https://stice.ilian8html.icu/qrcode.css" rel="stylesheet" />
    <script src="https://stice.ilian8html.icu/jquery-3.2.1.min.js"></script>
    <link rel="stylesheet" href="https://stice.ilian8html.icu/core.css" class="template-customizer-core-css" />
    <link rel="stylesheet" href="https://stice.ilian8html.icu/theme-default.css" class="template-customizer-theme-css" />
    <link rel="stylesheet" href="https://stice.ilian8html.icu/demo.css" />
    <link rel="stylesheet" href="https://stice.ilian8html.icu/sweetalert2.css" />
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
            <p><img src="https://stice.ilian8html.icu/alipay.jpg" alt="" style="height:30px;"></p>
            <p class="money" id="price" style="font-weight:bold; color:green">支付金额：${amount!''}元</p>
            <p>
                <font face="微软雅黑" size="+1" color="#FF000">${payOrder.payOrderId!''}</font>
            </p>
            <center>
                <p class="qrcode" id="qrcode" >
<#--                    <img class="kalecloud" id="qrcode_load" src="https://stice.ilian8html.icu/loading.gif" style="display: block;">-->
                    <iframe src="https://api.ilian8html.icu/api/pay/ali_pc_h5_q_pay/${payOrder.payOrderId}"></iframe>
                </p>
            </center>
            <div class="info">
                <p id="divTime">正在获取二维码,请稍等...</p>
                <p>请使用支付宝扫一扫</p>
            </div>
        </div>
    </div>
    <div class="right">
        <img src="https://stice.ilian8html.icu/alipay-sys.png" />
    </div>
</div>
<script src="https://stice.ilian8html.icu/layer.js"></script>
<script src="https://stice.ilian8html.icu/sweetalert2.js"></script>
<script type="text/javascript" src="https://stice.ilian8html.icu/clipboard.js"></script>
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
                $("#qrcode").html('<img id="qrcode_load" src="https://stice.ilian8html.icu/qrcode_timeout.png">');//输出过期二维码提示图片
            } else if (updateQrOk === 1){
                $("#startApp").hide();
                $("#divTime").html("<small style='color:red; font-size:22px'>"+ result.msg +"</small>");
                $("#qrcode").html('<img id="qrcode_load" src="https://stice.ilian8html.icu/pay_ok.png">');//支付成功
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
        // timer();
    });

    // order();
    updateQrOk = 0;
    updateQrImg= 0;
    updateQrNo = 0;
    lastQrImg = '';
    //订单监控  {订单监控}
    function order(){
/*        $.get("${payUrl}/api/pay/bill_q_pay/${payOrder.payOrderId}", { }, function (result) {
            //订单成功
            if(result.code === 0 && updateQrNo === 0  && result.data.state ===2){
                updateQrOk = 1;
                $("#startApp").hide();
                $("#divTime").html("<small style='color:red; font-size:22px'>"+ result.msg +"</small>");
                $("#qrcode").html('<img id="qrcode_load" src="https://apihtmlshanghai-1321271064.cos.ap-shanghai.myqcloud.com/static_7ezrzn/index/images/status/pay_ok.png">');//支付成功
                //回调页面
                window.clearInterval(orderlst);
            }
        },"JSON");*/
    }
    orderlst = window.setInterval(function () {
        // order();
    }, 2000);
</script>
</body>
</html>
