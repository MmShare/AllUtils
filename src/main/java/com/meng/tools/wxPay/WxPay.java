package com.meng.tools.wxPay;


import com.alibaba.fastjson.JSON;
import com.alipay.api.internal.util.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * @description:
 * @author: xiapq
 * @date: 2019-07-04 11:47
 */
public class WxPay {

    private Logger logger= LoggerFactory.getLogger(WxPay.class);


    public static String create(String appId,String realIp,String machId,String apiKey, String notifyUrl,String title, Map<String,Object> map){

        try {
            String community_name = (String) map.get("community_name");
            int months_of_payment = (int) map.get("months_of_payment");
            if(StringUtils.isEmpty(community_name)){
                community_name="";
            }
            Map<String, String> data = new HashMap<String, String>();
            data.put("body", community_name+months_of_payment+"个月"+title);
            String out_trade_no = (String) map.get("out_trade_no");
            data.put("out_trade_no", out_trade_no);
            data.put("device_info", "WEB");
            data.put("fee_type", "CNY");
            BigDecimal amount_paid = (BigDecimal) map.get("amount_paid");
            data.put("total_fee", amount_paid.toString());
            data.put("spbill_create_ip", realIp);
            data.put("notify_url", notifyUrl);
            data.put("trade_type", "APP");  // 此处指定为扫码支付
            data.put("appid",appId);
            data.put("mch_id", machId);
            String nonceStr = WXPayUtil.generateNonceStr();
            data.put("nonce_str",nonceStr);
            String sign = WXPayUtil.generateSignature(data, apiKey, WXPayConstants.SignType.MD5);
            data.put("sign",sign);
            String toXml = WXPayUtil.mapToXml(data);
            String xml = HttpUtils.postXml("https://"+WXPayConstants.DOMAIN_API + WXPayConstants.UNIFIEDORDER_URL_SUFFIX, null, toXml);
            Map<String, String> payMap = WXPayUtil.xmlToMap(xml);
            String orderString =payMap.get("orderString").toString();
            return orderString;
        } catch (Exception e) {
            e.printStackTrace();
        }
            return null;
    }

    public static String getIpAdrress(HttpServletRequest request) {
        String Xip = request.getHeader("X-Real-IP");
        String XFor = request.getHeader("X-Forwarded-For");
        if(StringUtils.isEmpty(XFor) && !"unKnown".equalsIgnoreCase(XFor)){
            //多次反向代理后会有多个ip值，第一个ip才是真实ip
            int index = XFor.indexOf(",");
            if(index != -1){
                return XFor.substring(0,index);
            }else{
                return XFor;
            }
        }
        XFor = Xip;
        if(StringUtils.isEmpty(XFor) && !"unKnown".equalsIgnoreCase(XFor)){
            return XFor;
        }
        if (StringUtils.isEmpty(XFor) || "unknown".equalsIgnoreCase(XFor)) {
            XFor = request.getHeader("Proxy-Client-IP");
        }
        if (StringUtils.isEmpty(XFor) || "unknown".equalsIgnoreCase(XFor)) {
            XFor = request.getHeader("WL-Proxy-Client-IP");
        }
        if (StringUtils.isEmpty(XFor) || "unknown".equalsIgnoreCase(XFor)) {
            XFor = request.getHeader("HTTP_CLIENT_IP");
        }
        if (StringUtils.isEmpty(XFor) || "unknown".equalsIgnoreCase(XFor)) {
            XFor = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (StringUtils.isEmpty(XFor) || "unknown".equalsIgnoreCase(XFor)) {
            XFor = request.getRemoteAddr();
        }
        return XFor;
    }

    /**
     * 微信获取用户信息
     * @param appId     应用的唯一标识
     * @param appSecret 应用的密钥
     * @param code
     */
    public static Map<String, String> loginAndUserInfo(String appId, String appSecret, String code) {
        Map<String, String> resultMap = new HashMap<>();
        try {
            //  对拿去token的url进行封装
            String codeUrl = "https://api.weixin.qq.com/sns/oauth2/access_token?appid=APPID&secret=SECRET&code=CODE&grant_type=authorization_code";
            codeUrl = codeUrl.replace("APPID", appId);
            codeUrl = codeUrl.replace("SECRET", appSecret);
            codeUrl = codeUrl.replace("CODE", code);
            //  获取返回的信息
            com.alibaba.fastjson.JSONObject codeObject = JSON.parseObject(get(codeUrl));
            //  封装三个基础返回值
            resultMap.put("access_token",codeObject.getString("access_token"));
            resultMap.put("expires_in",codeObject.getString("expires_in"));
            resultMap.put("open_id",codeObject.getString("openid"));
            //  对拿去详细信息的url进行封装
            String tokenUrl = "https://api.weixin.qq.com/sns/userinfo?access_token=ACCESS_TOKEN&openid=OPENID";
            tokenUrl = tokenUrl.replace("ACCESS_TOKEN", codeObject.getString("access_token")).replace("OPENID", codeObject.getString("openid"));
            //  获取返回的信息
            com.alibaba.fastjson.JSONObject tokenObject = JSON.parseObject(get(tokenUrl));
            //  封装另外两个基础返回值
            if (null!=tokenObject){
                resultMap.put("nick_name",tokenObject.getString("nickname"));
                resultMap.put("head_image",tokenObject.getString("headimgurl"));
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return resultMap;
    }

    public static String get(String url) {
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        StringBuffer sb = new StringBuffer();
        HttpGet httpGet = new HttpGet(url);
        try {
            HttpResponse response = httpClient.execute(httpGet);
            HttpEntity entity = response.getEntity();
            InputStreamReader reader = new InputStreamReader(entity.getContent(), "utf-8");
            char[] charbufer;
            while (0 < reader.read(charbufer = new char[10])) {
                sb.append(charbufer);
            }
        } catch (IOException e) {//1
            e.printStackTrace();
        } finally {
            httpGet.releaseConnection();
        }
        return sb.toString();
    }
}
