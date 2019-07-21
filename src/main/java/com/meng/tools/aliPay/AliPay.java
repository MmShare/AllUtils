package com.meng.tools.aliPay;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.AlipayTradeAppPayModel;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.internal.util.StringUtils;
import com.alipay.api.request.AlipaySystemOauthTokenRequest;
import com.alipay.api.request.AlipayTradeAppPayRequest;
import com.alipay.api.request.AlipayUserInfoShareRequest;
import com.alipay.api.response.AlipaySystemOauthTokenResponse;
import com.alipay.api.response.AlipayTradeAppPayResponse;
import com.alipay.api.response.AlipayUserInfoShareResponse;
import org.slf4j.LoggerFactory;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AliPay {
    private static org.slf4j.Logger logger= LoggerFactory.getLogger(AliPay.class);

    private   static String SERVER_URL="";
    private   static String APP_ID="";
    private   static String FORMAT="json";
    private   static String CHARSET="utf-8";
    private   static String SIGN_TYPE="RSA2";
    private   static String APP_PRIVATE_KEY="";
    private   static String ALIPAY_PUBLIC_KEY="";

    /**
     * 静态代码块读取 公钥私钥
     */
    static {
        try {
//            Resource private_resource = new ClassPathResource("/META-INF/rsa/private_key.txt");
//            File private_file = private_resource.getFile();
//            System.out.println(private_file.getPath());
//            APP_PRIVATE_KEY = FileUtils.readFileToString(private_file, "UTF-8");
//            Resource public_resource = new ClassPathResource("/META-INF/rsa/public_key.txt");
//            File public_file = public_resource.getFile();
//            ALIPAY_PUBLIC_KEY = FileUtils.readFileToString(public_file, "UTF-8");
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws AlipayApiException {
        String inString = authCode("2018112162241925", "2088131840779127");
        System.out.println(inString);
    }

    public static String create(String appId, String serverUrl, String notifyUrl,String title,Integer user_id, Map<String,Object> map) throws AlipayApiException {
        //实例化客户端
        AlipayClient alipayClient = new DefaultAlipayClient(serverUrl,appId,APP_PRIVATE_KEY,FORMAT,CHARSET,ALIPAY_PUBLIC_KEY,SIGN_TYPE);
        //实例化具体API对应的request类,类名称和接口名称对应,当前调用接口名称：alipay.trade.app.pay
        AlipayTradeAppPayRequest request = new AlipayTradeAppPayRequest();
        //SDK已经封装掉了公共参数，这里只需要传入业务参数。以下方法为sdk的model入参方式(model和biz_content同时存在的情况下取biz_content)。
        AlipayTradeAppPayModel model = new AlipayTradeAppPayModel();
        model.setBody("移动家");
        String community_name = (String) map.get("community_name");
        int months_of_payment = (int) map.get("months_of_payment");
        if(StringUtils.isEmpty(community_name)){
            community_name="";
        }
        model.setSubject(community_name+months_of_payment+"个月"+title);
        String out_trade_no = (String) map.get("out_trade_no");
        model.setOutTradeNo(out_trade_no);
        model.setTimeoutExpress("1c");
        BigDecimal amount_paid = (BigDecimal) map.get("amount_paid");
        model.setTotalAmount(amount_paid.toString());
        String passparam="payerId="+user_id;
        try {
            String urlString = URLEncoder.encode(passparam, "GBK");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        model.setPassbackParams(passparam);
        //model.setTotalAmount("0.01");
        request.setBizModel(model);
        request.setNotifyUrl(notifyUrl);
        String return_url="";
        try {
            //这里和普通的接口调用不同，使用的是sdkExecute
            AlipayTradeAppPayResponse response = alipayClient.sdkExecute(request);
            return_url = response.getBody();
            System.out.println(return_url);
            //就是orderString 可以直接给客户端请求，无需再做处理。
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }

        return return_url;
    }

    public static boolean PayNotify(Map<String,String> params) {
        boolean flag=false;
        try {
            flag = AlipaySignature.rsaCheckV1(params, ALIPAY_PUBLIC_KEY, CHARSET,SIGN_TYPE);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        return flag;
    }


    public static Map<String, String> loginAndUserInfo(String appId, String serverUrl, String code) throws AlipayApiException {
        Map<String,String> map =new HashMap<>();
        //实例化客户端
        AlipayClient alipayClient = new DefaultAlipayClient(serverUrl,appId,APP_PRIVATE_KEY,FORMAT,CHARSET,ALIPAY_PUBLIC_KEY,SIGN_TYPE);
        //获取code换取token 和刷新的toekn
        AlipaySystemOauthTokenRequest request = new AlipaySystemOauthTokenRequest();
        //值为authorization_code时，代表用code换取；值为refresh_token时，代表用refresh_token换取
        request.setGrantType("authorization_code");
        //授权码，用户对应用授权后得到。
        request.setCode(code);
        //刷刷新令牌，上次换取访问令牌时得到。见出参的refresh_token字段
        //request.setRefreshToken("201208134b203fe6c11548bcabd8da5bb087a83b");
        AlipaySystemOauthTokenResponse response = alipayClient.execute(request);
        if(response.isSuccess()){
            //支付宝用户唯一id
            String userId = response.getUserId();
            //访问令牌。通过该令牌调用需要授权类接口
            String accessToken = response.getAccessToken();
            //token 获取用户详情的实例
            AlipayUserInfoShareRequest userInfoRequest = new AlipayUserInfoShareRequest();
            //获取用户详情
            AlipayUserInfoShareResponse userInfoResponse = alipayClient.execute(userInfoRequest,accessToken);
            if(userInfoResponse.isSuccess()){
                //调用成功解析
                map.put("user_id",userInfoResponse.getUserId());
                map.put("nick_name",userInfoResponse.getNickName());
                map.put("head_image",userInfoResponse.getAvatar());
                System.out.println("调用成功");
            } else {
                System.out.println("用户详情调用失败");
            }
        } else {
            System.out.println("用户换取token调用失败");
        }
        return map;
    }

    public static String authCode(String appId, String pid) throws AlipayApiException {
        // pid 商户id 2088131840779127
        // 随机数的换取code
        String replace = UUID.randomUUID().toString().replace("-", "");
        //拼装
        String param="apiname=com.alipay.account.auth&app_id="+appId+"&app_name=mc&auth_type=AUTHACCOUNT&biz_type=openservice&method=alipay.open.auth.sdk.code.get&pid="+pid+"&product_id=APP_FAST_LOGIN&scope=kuaijie&sign_type=RSA2&target_id="+replace;
        //rsa2 加密
        String rsa256Sign = AlipaySignature.rsa256Sign(param, APP_PRIVATE_KEY, CHARSET);
        try {
            // encode 加密
            rsa256Sign = URLEncoder.encode(rsa256Sign, "GBK");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        //拼装
        param+="&sign="+rsa256Sign;
        //返回
        return param;
    }

}
