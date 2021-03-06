package com.zhuhao.utils;


import com.alibaba.fastjson.JSONObject;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.BasicHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import javax.net.ssl.*;
import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * http请求工具类
 * Created by zhu on 2017/3/15.
 */
public class HttpClientUtil {

    /** https */
    private static final String HTTPS_PROTOCOL = "https://";

    /** https 端口 */
    private static final int HTTPS_PROTOCOL_DEFAULT_PORT = 443;

    /** 默认编码格式 */
    private static final String DEFAULT_CHARSET = "UTF-8";

    /** 请求参数类型，POST表单 */
    public static final int REQUEST_PARAM_DATA_TYPE_POST = 1;

    /** 请求参数类型，JSON格式字符串 */
    public static final int REQUEST_PARAM_DATA_TYPE_JSON = 2;

    /** 日志对象 */
    private static final Logger LOG = Logger.getLogger(HttpClientUtil.class);


    /**
     * 发送Post请求  未测试
     * @param url
     * @param map
     * @param charset
     * @return
     */
    public static String doPost(String url,Map<String,String> map,String charset){
        CloseableHttpClient httpClient = null;
        HttpPost httpPost = null;
        String result = null;
        try{
            httpClient = HttpClients.createDefault();
            httpPost = new HttpPost(url);
            //设置参数
            List<NameValuePair> list = new ArrayList<>();
            Iterator iterator = map.entrySet().iterator();
            while(iterator.hasNext()){
                Map.Entry<String,String> elem = (Map.Entry<String, String>) iterator.next();
                list.add(new BasicNameValuePair(elem.getKey(),elem.getValue()));
            }
            if(list.size() > 0){
                UrlEncodedFormEntity entity = new UrlEncodedFormEntity(list,charset);
                httpPost.setEntity(entity);
            }
            HttpResponse response = httpClient.execute(httpPost);
            if(response != null){
                HttpEntity resEntity = response.getEntity();
                if(resEntity != null){
                    result = EntityUtils.toString(resEntity,charset);
                }
            }
        }catch(Exception ex){
            ex.printStackTrace();
        }
        return result;
    }

    /**
     * 发送POST请求
     * @param url
     * @param jsonStr
     * @return
     */
    public static String doPost(String url,String jsonStr){
        CloseableHttpClient httpClient = null;
        HttpPost httpPost = null;
        String result = null;
        try{
            httpClient = HttpClients.createDefault();
            httpPost = new HttpPost(url);
            httpPost.setEntity(new StringEntity(jsonStr, Charset.forName("UTF-8")));
            httpPost.addHeader("Content-type","application/json; charset=utf-8");
            httpPost.setHeader("Accept", "application/json");
            HttpResponse response = httpClient.execute(httpPost);
            if(response != null){
                HttpEntity resEntity = response.getEntity();
                if(resEntity != null){
                    result = EntityUtils.toString(resEntity, Charset.forName("UTF-8"));
                }
            }
        }catch(Exception ex){
            ex.printStackTrace();
        }
        return result;
    }

    /**
     * 发送 Get请求
     * @param url
     * @return
     */
    public static String doGet(String url) {
        CloseableHttpClient httpClient = null;
        HttpGet httpGet = null;
        String result = null;
        try {
            httpClient = HttpClients.createDefault();
            httpGet = new HttpGet(url);
            HttpResponse httpResponse = httpClient.execute(httpGet);
            if(httpResponse.getStatusLine().getStatusCode() == 200) {
                HttpEntity httpEntity = httpResponse.getEntity();
                result = EntityUtils.toString(httpEntity);
            }
        }  catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }



    public static final String sendPostRequest(String url, Map<String, String> params) {
        StringBuffer sb = new StringBuffer();
        for (String key : params.keySet()) {
            sb.append(key + "=" + params.get(key) + "&");
        }
        sb.deleteCharAt(sb.length() - 1);
        return sendPostRequest(url, sb.toString(), null, REQUEST_PARAM_DATA_TYPE_POST);
    }


    public static final String sendPostRequest(String url, String params, int dataType) {
        return sendPostRequest(url, params, null, dataType);
    }


    /**
     * 发送POST请求
     * @author 朱昊
     * @date Apr 29, 2016 4:36:28 PM
     * @param url        请求地址
     * @param params     请求参数
     * @param cookie     Cookie信息
     * @param dataType   数据类型，1：字符串，2：JSON
     * @return   请求结果内容
     */
    public static final String sendPostRequest(String url, String params, String cookie, int dataType) {
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        RequestConfig reqConf = RequestConfig.DEFAULT;
        HttpPost httpPost = new HttpPost(url);
        try {
            if (dataType == REQUEST_PARAM_DATA_TYPE_POST) {
                List<NameValuePair> pairs = geneNameValPairs(params);
                if (pairs != null) {
                    httpPost.setEntity(new UrlEncodedFormEntity(pairs, DEFAULT_CHARSET));
                }
            }
            else if (dataType == REQUEST_PARAM_DATA_TYPE_JSON) {
                StringEntity entity = new StringEntity(params, DEFAULT_CHARSET);
                entity.setContentEncoding("UTF-8");
                entity.setContentType("application/json");
                httpPost.setEntity(entity);
            }

            if (StringUtils.isNotBlank(cookie)) {
                httpPost.addHeader("Cookie", cookie);
            }

            // 对HTTPS请求进行处理
            if (url.toLowerCase().startsWith(HTTPS_PROTOCOL)) {
                initSSL(httpClient, getPort(url));
            }
            // 提交请求并以指定编码获取返回数据
            httpPost.setConfig(reqConf);
            HttpResponse httpResponse = httpClient.execute(httpPost);
            int statuscode = httpResponse.getStatusLine().getStatusCode();
            if ((statuscode == HttpStatus.SC_MOVED_TEMPORARILY) || (statuscode == HttpStatus.SC_MOVED_PERMANENTLY)
                    || (statuscode == HttpStatus.SC_SEE_OTHER) || (statuscode == HttpStatus.SC_TEMPORARY_REDIRECT)) {
                Header header = httpResponse.getFirstHeader("location");
                if (header != null) {
                    String newuri = header.getValue();
                    if ((newuri == null) || (newuri.equals(""))){
                        newuri = "/";
                    }
                    if (("/".equals(newuri))){
                        newuri = "http://" + httpPost.getURI().getHost();
                    }
                    try {
                        httpClient.close();
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                        httpClient = null;
                    }
                    return sendPostRequest(newuri, params, cookie, dataType);
                }
            }

            HttpEntity resEntity = httpResponse.getEntity();
            String res = EntityUtils.toString(resEntity, DEFAULT_CHARSET);
            LOG.info("请求地址：" + url + "; 请求结果：" + res);
            return res;
        }
        catch (UnsupportedEncodingException e) {
            LOG.error("不支持当前参数编码格式[" + DEFAULT_CHARSET + "],堆栈信息如下", e);
        }
        catch (ClientProtocolException e) {
            LOG.error("协议异常,堆栈信息如下", e);
        }
        catch (IOException e) {
            LOG.error("网络异常,堆栈信息如下", e);
        }
        finally {
            // 关闭连接，释放资源
            try {
                httpClient.close();
            }
            catch (Exception e) {
                e.printStackTrace();
                httpClient = null;
            }
        }
        return null;
    }


    public static String sendGetReq(final String url) {
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        HttpGet get = new HttpGet(url);
        if (url.toLowerCase().startsWith(HTTPS_PROTOCOL)) {
            initSSL(httpClient, getPort(url));
        }
        try {
            // 提交请求并以指定编码获取返回数据
            HttpResponse httpResponse = httpClient.execute(get);
            int statuscode = httpResponse.getStatusLine().getStatusCode();
            if ((statuscode == HttpStatus.SC_MOVED_TEMPORARILY) ||
                    (statuscode == HttpStatus.SC_MOVED_PERMANENTLY) ||
                    (statuscode == HttpStatus.SC_SEE_OTHER) ||
                    (statuscode == HttpStatus.SC_TEMPORARY_REDIRECT)) {
                Header header = httpResponse.getFirstHeader("location");
                if (header != null) {
                    String newuri = header.getValue();
                    if ((newuri == null) || (newuri.equals("")))
                        newuri = "/";
                    try {
                        httpClient.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                        httpClient = null;
                    }
                    LOG.info("重定向地址：" + newuri);
                    return sendGetReq(newuri);
                }
            }
            LOG.info("请求地址：" + url + "；响应状态：" + httpResponse.getStatusLine());
            HttpEntity entity = httpResponse.getEntity();
            return EntityUtils.toString(entity, DEFAULT_CHARSET);
        } catch (ClientProtocolException e) {
            LOG.error("协议异常,堆栈信息如下", e);
        } catch (IOException e) {
            LOG.error("网络异常,堆栈信息如下", e);
        } finally {
            // 关闭连接，释放资源
            try {
                httpClient.close();
            } catch (Exception e) {
                e.printStackTrace();
                httpClient = null;
            }
        }
        return null;
    }


    /**
     * 根据请求地址获取端口
     * @author 朱昊
     * @param url    请求地址
     * @return   端口
     */
    private static int getPort(String url) {
        int startIndex = url.indexOf("://") + "://".length();
        String host = url.substring(startIndex);
        if (host.indexOf("/") != -1) {
            host = host.substring(0, host.indexOf("/"));
        }
        int port = HTTPS_PROTOCOL_DEFAULT_PORT;
        if (host.contains(":")) {
            int i = host.indexOf(":");
            port = new Integer(host.substring(i + 1));
        }
        return port;
    }

    /**
     * 初始化HTTPS请求服务
     * @param httpClient HTTP客户端
     * @param port 端口
     */
    public static void initSSL(CloseableHttpClient httpClient, int port) {
        SSLContext sslContext = null;
        try {
            sslContext = SSLContext.getInstance("SSL");
            final X509TrustManager trustManager = new X509TrustManager() {
                public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
                }

                public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
                }

                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
            };
            // 使用TrustManager来初始化该上下文,TrustManager只是被SSL的Socket所使用
            sslContext.init(null, new TrustManager[] { trustManager }, null);
            ConnectionSocketFactory ssf = new SSLConnectionSocketFactory(sslContext);
            Registry<ConnectionSocketFactory> r = RegistryBuilder.<ConnectionSocketFactory> create().register("https", ssf).build();
            BasicHttpClientConnectionManager ccm = new BasicHttpClientConnectionManager(r);
            HttpClients.custom().setConnectionManager(ccm).build();
        }
        catch (KeyManagementException e) {
            e.printStackTrace();
        }
        catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }


    /**
     * 封装POST请求参数
     * @author 朱昊
     * @date Apr 29, 2016 4:43:03 PM
     * @param params     参数内容，a=1&b=2
     * @return   POST参数列表
     */
    public static List<NameValuePair> geneNameValPairs(String params) {
        if (StringUtils.isBlank(params)) {
            return null;
        }

        List<NameValuePair> pairs = new ArrayList<NameValuePair>();
        try {
            String[] paramArray = params.split("&");
            for (String param : paramArray) {
                pairs.add(new BasicNameValuePair(param.split("=")[0], param.split("=")[1]));
            }
        }
        catch (Exception e) {
            LOG.error("参数错误--> params : " + params, e);
            throw e;
        }
        return pairs;
    }


    /**
     * 封装POST请求参数
     * @author 朱昊
     * @date Apr 29, 2016 4:43:03 PM
     * @param params 参数内容
     * @return   POST参数列表
     */
    public static List<NameValuePair> geneNameValPairs(Map<String, ?> params) {
        List<NameValuePair> pairs = new ArrayList<NameValuePair>();
        if (params == null) {
            return pairs;
        }
        for (String name : params.keySet()) {
            if (params.get(name) == null) {
                continue;
            }
            pairs.add(new BasicNameValuePair(name, params.get(name).toString()));
        }
        return pairs;
    }

    /**
     * HTTPS 请求方式
     * @author 朱昊
     * @date 2015年8月10日 下午11:47:59
     * @param requestUrl 请求链接
     * @param requestMethod 请求方式
     * @param outputStr 请求数据
     * @return 返回json
     */
    public static JSONObject httpRequest(String requestUrl, String requestMethod, String outputStr) {
        JSONObject jsonObject = null;
        StringBuffer buffer = new StringBuffer();
        try {
            TrustManager[] tm = null;
            //{ new MyX509TrustManagerUtil() };
            SSLContext sslContext = SSLContext.getInstance("SSL", "SunJSSE");
            sslContext.init(null, tm, new java.security.SecureRandom());
            SSLSocketFactory ssf = sslContext.getSocketFactory();
            URL url = new URL(requestUrl);
            HttpsURLConnection httpUrlConn = (HttpsURLConnection) url.openConnection();
            httpUrlConn.setSSLSocketFactory(ssf);
            httpUrlConn.setDoOutput(true);
            httpUrlConn.setDoInput(true);
            httpUrlConn.setUseCaches(false);
            httpUrlConn.setRequestMethod(requestMethod);
            if ("GET".equalsIgnoreCase(requestMethod)) {
                httpUrlConn.connect();
            }
            if (null != outputStr) {
                OutputStream outputStream = httpUrlConn.getOutputStream();
                outputStream.write(outputStr.getBytes("UTF-8"));
                outputStream.close();
            }
            InputStream inputStream = httpUrlConn.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "utf-8");
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String str = null;
            while ((str = bufferedReader.readLine()) != null) {
                buffer.append(str);
            }
            bufferedReader.close();
            inputStreamReader.close();
            inputStream.close();
            httpUrlConn.disconnect();
            jsonObject = JSONObject.parseObject(buffer.toString());
        }
        catch (Exception ce) {
            ce.printStackTrace();
        }
        System.out.println(jsonObject.toString());
        return jsonObject;
    }
}
