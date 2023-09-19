<template>
  <view class="page">
	<view class="content">
		<text v-if="isTest">仅供测试</text>
		<image class="logo" src="/static/logo_300.png"></image>
	</view>
	
  	<view class="price-count-down">
  		<view class="price">
  			<text class="min">￥</text>
  			<text class="max">1</text>
  			<text class="min">.11</text>
  		</view>
  	</view> 
  	<!-- 支付方式列表 -->
  	<view class="pay-way">
  		<view class="pay-list">
  			<view class="list" v-for="(item,index) in PayList" 
  			@click="onPayWay(item,index)"
  			:key="index">
  				<view class="pay-type">
  					<image :src="item.icon" mode=""></image>
  					<text>{{item.name}}</text>
  				</view>
  				<view class="check">
  					<text class="iconfont" :class="PayWay === index ? 'icon-checked action':'icon-check'"></text>
  				</view>
  			</view>
  		</view>
  	</view>
  	<view class="pay-submit">
  		<view class="submit" @click="onSubmit">{{PayPirce}}</view>
  	</view>
  </view>
</template>

<script>
export default {
		data() {
			return {
				PayList: [
					// {
					// 	icon: '/static/wx_pay.png',
					// 	name: '微信支付',
					// },
					{
						icon: '/static/zfb_pay.png',
						name: '支付宝支付',
					},
					// {
					// 	icon: '/static/ye_pay.png',
					// 	name: '余额支付',
					// },
				],
				PayWay: 0,
				PayPirce: `支付宝支付￥1.11`,
				isTest: true
			};
		},
		onLoad(){
			let onLoadData = uni.getLaunchOptionsSync();
			console.log(onLoadData.path)
			console.log(onLoadData.query)
			if (onLoadData.query !== undefined && typeof onLoadData.query.tradeNO !== 'undefined'){
				this.isTest = false
				my.getAuthCode({
					success: res => {
						const authCode = res.authCode;
						console.log(authCode)
						uni.request({
						    url: 'http://127.0.0.1:9216/api/pay/aliJsapiOrderToUserId', //仅为示例，并非真实接口地址。
						    data: {
								authToken: authCode,
								appId: "64fc13cce4b0f7ca7a278b49",
								mchNo: 'M1694241740',
								version:'1.0',
								signType: 'MD5',
								sign: 'dsadsadasdsad',
								reqTime: '123456789'
						    },
							method: 'POST',
						    header: {
						        'custom-header': 'hello' //自定义请求头信息
						    },
						    success: (res) => {
						        console.log(res.data);
						        this.text = 'request success';
						    }
						});
					}
				})
			}
			
		},
		methods:{
			/**
			 * 支付方式切换点击
			 */
			onPayWay(item,index){
				this.PayWay = index;
				this.PayPirce = `${item.name}￥299.00`
			},
			/**
			 * 支付点击
			 */
			onSubmit(){
				uni.requestPayment({
				    provider: 'alipay',
				    orderInfo: 'orderInfo', //微信、支付宝订单数据 【注意微信的订单信息，键值应该全部是小写，不能采用驼峰命名】
				    success: function (res) {
				        console.log('success:' + JSON.stringify(res));
				    },
				    fail: function (err) {
				        console.log('fail:' + JSON.stringify(err));
				    }
				});
			}
		}
	}
</script>

<style>
.content {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.logo {
  height: 200rpx;
  width: 200rpx;
  margin-top: 30rpx;
  margin-left: auto;
  margin-right: auto;
  margin-bottom: 10rpx;
}

.text-area {
  display: flex;
  justify-content: center;
}

.title {
  font-size: 36rpx;
  color: #8f8f94;
}
</style>
<style scoped lang="scss">
	@import '../../style/FontStyle.css';
	@import 'index.scss';
</style>
