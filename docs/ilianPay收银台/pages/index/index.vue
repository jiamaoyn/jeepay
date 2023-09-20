<template>
	<view class="page">
		<view class="content">
			<text v-if="isTest">仅供测试</text>
			<image class="logo" src="/static/logo_300.png"></image>
		</view>

		<view class="price-count-down">
			<view class="price">
				<text class="min">￥</text>
				<text class="max">{{amountSpan}}</text>
			</view>
		</view>
		<!-- 支付方式列表 -->
		<view class="pay-way">
			<view class="pay-list">
				<view class="list" v-for="(item,index) in PayList" @click="onPayWay(item,index)" :key="index">
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
	var CryptoJS = require("crypto-js");
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
				amountSpan: 0,
				PayPirce: '支付宝支付',
				isTest: true,
				appId: undefined,
				mchNo: undefined,
				currency: 'cny',
				version: '1.0',
				signType: 'MD5',
				wayCode: 'ALI_JSAPI',
				subject: undefined,
				body: undefined,
				userId: '2088722004845989',
				amount: undefined,
				appKey: "DmsMlIa7KXp6RkiYVlUcjGAGvUVK28WH4ICY9NBlucB2ThWqegnIoPh65YUJzNlMAMpYkS3Pjn1xVwQq7OeVr6I4E9WltN4NtYc6zr7MMbsznbKwDAAmPgrnMYab0wuL",
				sign: undefined,
				notifyUrl: undefined,
				returnUrl: 'https://render.alipay.com/p/404',
			};
		},
		onLoad() {
			let onLoadData = uni.getLaunchOptionsSync();
			const that = this
			if (onLoadData.query !== undefined &&
				typeof onLoadData.query.appId !== 'undefined' &&
				typeof onLoadData.query.notifyUrl !== 'notifyUrl' &&
				typeof onLoadData.query.mchNo !== 'undefined' &&
				typeof onLoadData.query.mchOrderNo !== 'undefined' &&
				typeof onLoadData.query.amount !== 'undefined' &&
				typeof onLoadData.query.reqTime !== 'undefined'
			) {
				if (Math.ceil(onLoadData.query.amount) !== parseInt(onLoadData.query.amount) || Math.ceil(onLoadData.query
						.amount) <
					0) {
					that.openAlipayApp("订单金额错误")
				} else if (typeof that.appKey === undefined && typeof onLoadData.query.appKey === 'undefined') {
					that.openAlipayApp("appKey参数错误")
				} else {
					//mchOrderNo=20230920093801&appId=64fc13cce4b0f7ca7a278b49&mchNo=M1694241740&amount=1&reqTime=123132
					if (typeof onLoadData.query.appKey !== 'undefined') {
						that.appKey = onLoadData.query.appKey
					}
					if (typeof onLoadData.query.returnUrl !== 'undefined') {
						that.returnUrl = onLoadData.query.returnUrl
					}
					this.isTest = false
					that.appId = onLoadData.query.appId
					that.mchNo = onLoadData.query.mchNo
					that.mchOrderNo = onLoadData.query.mchOrderNo
					that.amount = parseInt(onLoadData.query.amount)
					that.amountSpan = parseInt(onLoadData.query.amount) / 100
					that.PayPirce = '支付宝支付￥'+that.amountSpan
					that.reqTime = onLoadData.query.reqTime
					if (typeof onLoadData.query.subject !== 'undefined') {
						that.subject = onLoadData.query.subject
					} else {
						that.subject = '秒杀商品' + Math.ceil(Math.random() * 100);
					}
					if (typeof onLoadData.query.subject !== 'undefined') {
						that.body = onLoadData.query.body
					} else {
						that.body = '秒杀商品，请尽快付款';
					}
					my.getAuthCode({
						scopes: 'auth_base',
						success: res => {
							console.log(res.authCode)
							const authCode = res.authCode;
							uni.request({
								url: 'http://127.0.0.1:9216/api/pay/aliJsapiOrderToUserId', //仅为示例，并非真实接口地址。
								data: {
									authToken: authCode,
									appId: that.appId,
									mchNo: that.mchNo,
									version: that.version,
									signType: that.signType,
									sign: 'dsadsadasdsad',
									reqTime: '123456789'
								},
								method: 'POST',
								success: (res) => {
									if (res.data.code == 0) {
										that.userId = res.data.data
									} else {
										that.openAlipayApp(res.data.msg)
									}
								}
							});
						}
					})
				}

			} else {
				if (typeof onLoadData.query === 'undefined') {
					console.log("tradeNO")
					that.openAlipayApp("query参数错误")
				} else if (typeof onLoadData.query.notifyUrl === 'undefined') {
					console.log("notifyUrl")
					that.openAlipayApp("notifyUrl参数错误")
				} else if (typeof onLoadData.query.appId === 'undefined') {
					console.log("appId")
					that.openAlipayApp("appId参数错误")
				} else if (typeof onLoadData.query.mchNo === 'undefined') {
					console.log("mchNo")
					that.openAlipayApp("mchNo参数错误")
				} else if (typeof onLoadData.query.mchOrderNo === 'undefined') {
					console.log("mchOrderNo")
					that.openAlipayApp("mchOrderNo参数错误")
				} else if (typeof onLoadData.query.amount === 'undefined') {
					console.log("amount")
					that.openAlipayApp("amount参数错误")
				} else if (typeof onLoadData.query.reqTime === 'undefined') {
					console.log("reqTime")
					that.openAlipayApp("reqTime参数错误")
				} else {
					that.openAlipayApp("系统错误")
				}

			}

		},
		methods: {
			openAlipayApp(msg) {
				const that = this
				my.showLoading({
					content: msg,
					success: function(res) {
						setTimeout(() => {
							my.hideLoading();
							my.ap.openURL({
								url: that.returnUrl
							});
						}, 3000);
					},
					fail: function(err) {
						console.log(err);
					}
				});
			},
			openShowLoading(msg) {
				my.showLoading({
					content: msg,
				})
			},
			openHideLoading() {
				my.hideLoading();
			},
			/**
			 * 支付方式切换点击
			 */
			onPayWay(item, index) {
				this.PayWay = index;
				this.PayPirce = `${item.name}￥299.00`
			},
			/**
			 * 支付点击
			 */
			onSubmit() {
				const that = this
				that.openShowLoading("拼命支付中....")
				const sendData = {
					appId: that.appId,
					mchNo: that.mchNo,
					mchOrderNo: that.mchOrderNo,
					version: that.version,
					signType: that.signType,
					reqTime: Date.now(),
					currency: that.currency,
					version: that.version,
					notifyUrl: that.notifyUrl,
					returnUrl: that.returnUrl,
					signType: that.signType,
					channelExtra: "{\"buyerOpenId\":\""+that.userId+"\"}",
					wayCode: that.wayCode,
					amount: that.amount,
					subject: that.subject,
					body: that.body,
				}
				console.log(sendData)
				sendData.sign = that.paramArraySign(sendData, that.appKey)
				console.log(sendData)
				uni.request({
					url: 'http://127.0.0.1:9216/api/pay/unifiedOrder', //仅为示例，并非真实接口地址。
					data: sendData,
					method: 'POST',
					success: (res) => {
						if (res.data.code == 0) {
							that.openHideLoading()
							my.tradePay ({
							  // 调用统一收单交易创建接口（alipay.trade.create），获得返回字段支付宝交易号 trade_no
							  tradeNO: res.data.data.mchOrderNo,
							  success: res => {
								  if (res.resultCode === "9000") {
									that.openAlipayApp("订单处理成功，即将跳转")
								  } else {
									that.openAlipayApp(JSON.stringify (res))
								  }
							    
							  },
							  fail: res => {
							    that.openAlipayApp("请返回后重新拉起支付")
							  },
							});
						} else {
							that.openAlipayApp(res.data.msg)
						}
					}
				});
			},
			paramArraySign(paramArray, appKey) {
				const paramArraySorted = Object.entries(paramArray).sort(); // 字典排序
				console.log(paramArraySorted)
				let md5str = "";

				paramArraySorted.forEach(([key, val]) => {
					if (key && val) {
						md5str += key + "=" + val + "&";
					}
				});
				console.log(md5str);
				md5str += "key=" + appKey;
				console.log(md5str);
				const hash = CryptoJS.MD5(md5str);
				return hash.toString().toUpperCase(); // 签名
			}
		},
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