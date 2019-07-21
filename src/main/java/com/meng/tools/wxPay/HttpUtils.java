package com.meng.tools.wxPay;

import org.apache.http.*;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


public class HttpUtils {

    private static Logger logger = Logger.getLogger(HttpUtils.class);

    /**
     * get请求
     * @return
     */
    public static String doGet(String url) {
        try {
            HttpClient client = new DefaultHttpClient();
            //发送get请求
            HttpGet request = new HttpGet(url);
            HttpResponse response = client.execute(request);

            /**请求发送成功，并得到响应**/
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                /**读取服务器返回过来的json字符串数据**/
                String strResult = EntityUtils.toString(response.getEntity());

                return strResult;
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * post请求(用于key-value格式的参数)
     * @param url
     * @param params
     * @return
     */
    public static String doPost(String url, Map params){

        BufferedReader in = null;
        try {
            // 定义HttpClient  
            HttpClient client = new DefaultHttpClient();
            // 实例化HTTP方法  
            HttpPost request = new HttpPost();
            request.setURI(new URI(url));

            //设置参数
            List<NameValuePair> nvps = new ArrayList<NameValuePair>();
            for (Iterator iter = params.keySet().iterator(); iter.hasNext();) {
                String name = (String) iter.next();
                String value = String.valueOf(params.get(name));
                nvps.add(new BasicNameValuePair(name, value));

                //System.out.println(name +"-"+value);
            }
            request.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));

            HttpResponse response = client.execute(request);
            int code = response.getStatusLine().getStatusCode();
            if(code == 200){	//请求成功
                in = new BufferedReader(new InputStreamReader(response.getEntity()
                        .getContent(),"utf-8"));
                StringBuffer sb = new StringBuffer("");
                String line = "";
                String NL = System.getProperty("line.separator");
                while ((line = in.readLine()) != null) {
                    sb.append(line + NL);
                }

                in.close();

                return sb.toString();
            }
            else{	//
                System.out.println("状态码：" + code);
                return null;
            }
        }
        catch(Exception e){
            e.printStackTrace();

            return null;
        }
    }

    /**
     * post请求（用于请求json格式的参数）
     * @param url
     * @param params
     * @return
     */
    public static String doPost(String url, String params) throws Exception {

        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(url);// 创建httpPost
        httpPost.setHeader("Accept", "application/json");
        httpPost.setHeader("Content-Type", "application/json");
        String charSet = "UTF-8";
        StringEntity entity = new StringEntity(params, charSet);
        httpPost.setEntity(entity);
        CloseableHttpResponse response = null;

        try {

            response = httpclient.execute(httpPost);
            StatusLine status = response.getStatusLine();
            int state = status.getStatusCode();
            if (state == HttpStatus.SC_OK) {
                HttpEntity responseEntity = response.getEntity();
                String jsonString = EntityUtils.toString(responseEntity);
                return jsonString;
            }
            else{
                logger.error("请求返回:"+state+"("+url+")");
            }
        }
        finally {
            if (response != null) {
                try {
                    response.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            try {
                httpclient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * 提交http请求，获取响应数据字符串
     *
     * @param url 请求URL
     * @headerInfo 请求头信息
     * @param xml 请求数据字符串
     * @return
     * @throws ClientProtocolException
     * @throws IOException
     * @author SvenAugustus(蔡政滦)  e-mail: SvenAugustus@outlook.com modify by 2015年8月2日
     */
    public static String postXml(String url, Map<String, String> headerInfo, String xml) throws Exception {
        // httpclient 4.2.2
        //        String result = "";
        //
        //        HttpClient httpclient = new DefaultHttpClient();
        //        HttpPost pmethod = new HttpPost(url); // 设置响应头信息
        //        pmethod.addHeader("Connection", "keep-alive");
        //        pmethod.addHeader("Accept", "*/*");
        //        pmethod.addHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        //        pmethod.addHeader("Host", "api.mch.weixin.qq.com");
        //        pmethod.addHeader("X-Requested-With", "XMLHttpRequest");
        //        pmethod.addHeader("Cache-Control", "max-age=0");
        //        pmethod.addHeader("User-Agent", "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.0) ");
        //        pmethod.setEntity(new StringEntity(xml, "UTF-8"));
        //
        //        HttpResponse response = httpclient.execute(pmethod);
        //        if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
        //            result = EntityUtils.toString(response.getEntity(), "UTF-8");
        //        }
        //        return result;
        //return HttpsUtils.httpRequest(url, "POST", xml, "utf-8");

        // httpclient 4.3.4
        String result = null;
        HttpEntity postEntity = new StringEntity(xml, "utf-8");
        HttpPost httpPost = new HttpPost(url);
        //        httpPost.addHeader("Content-Type", "text/xml");
        //        httpPost.addHeader("Connection", "keep-alive");
        //        httpPost.addHeader("Accept", "*/*");
        //        httpPost.addHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        //        httpPost.addHeader("Host", "api.mch.weixin.qq.com");
        //        httpPost.addHeader("X-Requested-With", "XMLHttpRequest");
        //        httpPost.addHeader("Cache-Control", "max-age=0");
        //        httpPost.addHeader("User-Agent", "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.0) ");
        if (headerInfo != null) {
            Iterator it = headerInfo.keySet().iterator();
            while (it.hasNext()) {
                String name = (String) it.next();
                String value = headerInfo.get(name);
                httpPost.addHeader(name, value);
            }
        }
        httpPost.setEntity(postEntity);

        CloseableHttpClient httpclient = HttpClients.custom().build();
        try {
            HttpResponse response = httpclient.execute(httpPost);
            int statusCode = response.getStatusLine().getStatusCode();
            /*if (statusCode == HttpStatus.SC_MOVED_PERMANENTLY || statusCode == HttpStatus.SC_MOVED_TEMPORARILY) {
                String location = response.getFirstHeader("Location").getValue();
                return get(location);
            }
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                result = EntityUtils.toString(entity, "utf-8");
            }*/
            if (statusCode == HttpStatus.SC_OK) {
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    result = EntityUtils.toString(entity, "utf-8");
                }
            }
        } finally {
            httpclient.close();
        }
        return result;
    }
}
