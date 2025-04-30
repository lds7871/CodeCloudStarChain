package com.mojian.controller;

import cn.hutool.json.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.mojian.config.properties.AlipayConfig;
import com.mojian.dto.user.AlipayOrderInfo;
import com.mojian.dto.AlipayVO;
import com.mojian.service.AlipayUserSerivce;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 支付宝支付的控制类
 */
@RestController
@RequestMapping("/alipay")
public class AliPayController {
    private static final String GATEWAY_URL = "https://openapi-sandbox.dl.alipaydev.com/gateway.do";
    private static final String FORMAT = "JSON";
    private static final String CHARSET = "UTF-8";
    //签名方式
    private static final String SIGN_TYPE = "RSA2";
    @Autowired
    private AlipayConfig aliPayConfig;
    @Autowired
    private AlipayUserSerivce alipaySerivce;
    @GetMapping("/pay")
    public void pay(AlipayVO aliPay, HttpServletResponse httpResponse) throws Exception {
        System.out.println("out_trade_no"+aliPay.getTraceNo());
        System.out.println("total_amount"+aliPay.getTotalAmount());
        System.out.println("subject"+aliPay.getSubject());

        // 1. 创建Client，通用SDK提供的Client，负责调用支付宝的API
        AlipayClient alipayClient = new DefaultAlipayClient(GATEWAY_URL, aliPayConfig.getAppId(),
                aliPayConfig.getAppPrivateKey(), FORMAT, CHARSET, aliPayConfig.getAlipayPublicKey(), SIGN_TYPE);
        // 2. 创建 Request并设置Request参数
        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();  // 发送请求的 Request类
        request.setNotifyUrl(aliPayConfig.getNotifyUrl());
        JSONObject bizContent = new JSONObject();
        bizContent.set("out_trade_no", aliPay.getTraceNo());  // 我们自己生成的订单编号
        bizContent.set("total_amount", aliPay.getTotalAmount()); // 订单的总金额
        bizContent.set("subject", aliPay.getSubject());   // 支付的名称
        bizContent.set("product_code", "FAST_INSTANT_TRADE_PAY");  // 固定配置
        request.setBizContent(bizContent.toString());
        System.out.println("付费文章"+aliPay.getSubject());
       if("付费文章".equals(aliPay.getSubject())){
            request.setReturnUrl(aliPayConfig.getReturnUrlTwo());
        }
        // 执行请求，拿到响应的结果，返回给浏览器
        System.out.println(1111);
        String form = "";
        try {
            form = alipayClient.pageExecute(request).getBody(); // 调用SDK生成表单
            System.out.println(22222);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }

        httpResponse.setContentType("text/html;charset=" + CHARSET);
        System.out.println(333);
        httpResponse.getWriter().write(form);// 直接将完整的表单html输出到页面

        httpResponse.getWriter().flush();
        httpResponse.getWriter().close();
    }
    @PostMapping("/notify")  // 注意这里必须是POST接口
    public String payNotify(HttpServletRequest request) throws Exception {
        System.out.println(11111);
        if (request.getParameter("trade_status").equals("TRADE_SUCCESS")) {
            System.out.println("=========支付宝异步回调========");
            Map<String, String> params = new HashMap<>();
            Map<String, String[]> requestParams = request.getParameterMap();
            for (String name : requestParams.keySet()) {
                params.put(name, request.getParameter(name));
                // System.out.println(name + " = " + request.getParameter(name));
            }
            String outTradeNo = params.get("out_trade_no");
            String gmtPayment = params.get("gmt_payment");
            String alipayTradeNo = params.get("trade_no");
            System.out.println("trade_no"+alipayTradeNo);

            String sign = params.get("sign");
            String content = AlipaySignature.getSignCheckContentV1(params);
            boolean checkSignature = AlipaySignature.rsa256CheckContent(content, sign, aliPayConfig.getAlipayPublicKey(), "UTF-8"); // 验证签名
            // 支付宝验签
            if (checkSignature) {
                // 验签通过
                System.out.println("交易名称: " + params.get("subject"));
                System.out.println("交易状态: " + params.get("trade_status"));
                System.out.println("支付宝交易凭证号: " + params.get("trade_no"));
                System.out.println("商户订单号: " + params.get("out_trade_no"));
                System.out.println("交易金额: " + params.get("total_amount"));
                System.out.println("买家在支付宝唯一id: " + params.get("buyer_id"));
                System.out.println("买家付款时间: " + params.get("gmt_payment"));
                System.out.println("买家付款金额: " + params.get("buyer_pay_amount"));

            }
        }
        return "success";
    }

    @PostMapping("/rhgeSuccess")
    public Boolean rhgeSuccess(@RequestBody  AlipayOrderInfo alipayOrderInfo) {
        System.out.println(111);
       return alipaySerivce.paySuccess(alipayOrderInfo);
    }

    @PostMapping("/rhgeFail")
    public Boolean rhgeFail(@RequestBody  AlipayOrderInfo alipayOrderInfo) {
        System.out.println(2222);
        return alipaySerivce.payFail(alipayOrderInfo);
    }
}


