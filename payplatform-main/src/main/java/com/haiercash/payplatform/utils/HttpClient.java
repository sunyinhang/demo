/*
 * 功  能：简单说明该类的功能
 *
 * 文件名：HttpClient.java
 *
 * 描  述：
 *
 * [变更履历]
 * Version   变更日         部署              作者           变更内容
 * -----------------------------------------------------------------------------
 * V1.00     2015年5月6日   haiercash    suyang      CREATE
 * -----------------------------------------------------------------------------
 *
 *
 * Copyright (c) 2015 haiercash All Rights Reserved.
 *┌─────────────────────────────────────────────────—────┐
 *│ 版权声明                               haiercash       │
 *└──────────────────────────────—————————————————————───┘
 */
package com.haiercash.payplatform.utils;

import com.bestvike.lang.Base64Utils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.json.JSONTokener;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;


/**
 * SignUtil.java
 *
 * @author suyang
 * @serial
 * @since 2014年5月23日 上午10:17:56
 */
@Deprecated
public class HttpClient {
    public static String HMAC_SHA1_ALGORITHM = "HmacSHA1";

    private URL url;// 目标地址

    private int connectionTimeout;// 通信连接超时时间

    private int readTimeOut;// 通信读超时时间

    /**
     * 通信结果
     */
    private String result;

    /**
     * 构造函数
     *
     * @param url               目标地址
     * @param connectionTimeout HTTP连接超时时间
     * @param readTimeOut       HTTP读写超时时间
     */
    public HttpClient(String url, int connectionTimeout, int readTimeOut) {
        try {
            this.url = new URL(url);
            this.connectionTimeout = connectionTimeout;
            this.readTimeOut = readTimeOut;
        } catch (MalformedURLException e) {
            e.getMessage();
        }
    }

    /**
     * 构造函数
     */
    public HttpClient() {
        super();
    }

    /**
     * 向指定 URL 发送POST方法的请求
     *
     * @param url   发送请求的 URL
     * @param param 请求参数，请求参数应该是 name1=value1&name2=value2 的形式。
     * @return 所代表远程资源的响应结果
     */
    public static String sendPost(String url, String param) {
        PrintWriter out = null;
        BufferedReader in = null;
        String result = "";
        try {
            URL realUrl = new URL(url);
            // 打开和URL之间的连接
            HttpURLConnection conn = (HttpURLConnection) realUrl.openConnection();
            // 设置通用的请求属性
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("connection", "Keep-Alive");
            conn.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            // 发送POST请求必须设置如下两行
            conn.setDoOutput(true);
            conn.setDoInput(true);
            // conn.setRequestProperty("Content-Type",
            // "application/json;charset=utf-8"); // 设置发送数据的格式
            OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream(), "UTF-8");
            writer.write(param);
            writer.flush();

            // // 获取URLConnection对象对应的输出流
            // out = new PrintWriter(conn.getOutputStream());
            // // 发送请求参数
            // out.print(param);
            // // flush输出流的缓冲
            // out.flush();
            // 定义BufferedReader输入流来读取URL的响应
            in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
        } catch (Exception e) {
            e.getMessage();
            System.out.println("发送 POST 请求出现异常！" + e);
            e.printStackTrace();
        }
        // 使用finally块来关闭输出流、输入流
        finally {
            try {
                if (out != null) {
                    out.close();
                }
                if (in != null) {
                    in.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        // 打印返回报文
        StringBuffer loggerBugffer = new StringBuffer();// 清空日志缓存
        loggerBugffer.append("*************客户登录返回报文************ /n");
        loggerBugffer.append(result + "/n");
        loggerBugffer.append("*************客户登录返回报文************ ");
        // 打印返回报文
        return result;
    }

    /**
     * DESCRIPTION:发送http请求 @author xuchao @date 2015年7月27日 sendPost 方法 @param
     * url @param param @param charset @return @return String @throws
     */
    public static String sendPost(String url, String param, String charset) {
        PrintWriter out = null;
        BufferedReader in = null;
        String result = "";
        try {
            URL realUrl = new URL(url);
            // 打开和URL之间的连接
            HttpURLConnection conn = (HttpURLConnection) realUrl.openConnection();
            // 设置通用的请求属性
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("connection", "Keep-Alive");
            conn.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            conn.setRequestProperty("Content-Type", "application/xml;charset=" + charset); // 设置发送数据的格式
            // 发送POST请求必须设置如下两行
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setRequestMethod("POST");

            OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream(), "UTF-8");
            writer.write(param);
            writer.flush();

            // // 获取URLConnection对象对应的输出流
            // out = new PrintWriter(conn.getOutputStream());
            // // 发送请求参数
            // out.print(param);
            // // flush输出流的缓冲
            // out.flush();
            // 定义BufferedReader输入流来读取URL的响应
            in = new BufferedReader(new InputStreamReader(conn.getInputStream(), charset));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
        } catch (Exception e) {
            e.getMessage();
            System.out.println("发送 POST 请求出现异常！" + e);
            e.printStackTrace();
        }
        // 使用finally块来关闭输出流、输入流
        finally {
            try {
                if (out != null) {
                    out.close();
                }
                if (in != null) {
                    in.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return result;
    }

    /**
     * DESCRIPTION:发送post参数请求 @author xuchao @date 2015年6月12日 sendPost 方法 @param
     * url @param param @return @return String @throws
     */
    public static String sendPostparam(String url, String param, String charset) {
        PrintWriter out = null;
        BufferedReader in = null;
        String result = "";
        try {
            URL realUrl = new URL(url);
            // 打开和URL之间的连接
            HttpURLConnection conn = (HttpURLConnection) realUrl.openConnection();
            // 设置通用的请求属性
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("connection", "Keep-Alive");
            conn.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=" + charset); // 设置发送数据的格式
            // 发送POST请求必须设置如下两行
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setRequestMethod("POST");

            OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream(), "UTF-8");
            writer.write(param);
            writer.flush();

            // // 获取URLConnection对象对应的输出流
            // out = new PrintWriter(conn.getOutputStream());
            // // 发送请求参数
            // out.print(param);
            // // flush输出流的缓冲
            // out.flush();
            // 定义BufferedReader输入流来读取URL的响应
            in = new BufferedReader(new InputStreamReader(conn.getInputStream(), charset));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
        } catch (Exception e) {
            e.getMessage();
            System.out.println("发送 POST 请求出现异常！" + e);
            e.printStackTrace();
        }
        // 使用finally块来关闭输出流、输入流
        finally {
            try {
                if (out != null) {
                    out.close();
                }
                if (in != null) {
                    in.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return result;
    }

    /**
     * DESCRIPTION:发送JSON @author xuchao @date 2015年6月12日 sendPost 方法 @param
     * url @param param @return @return String @throws
     */
    public static String sendJson(String url, String param) {
        try {

            URL add_url = new URL(url);

            HttpURLConnection connection = (HttpURLConnection) add_url.openConnection();

            connection.setDoInput(true);

            connection.setDoOutput(true);

            connection.setRequestMethod("POST");

            connection.setUseCaches(false);

            connection.setInstanceFollowRedirects(true);
            connection.setRequestProperty("Accept", "application/json"); // 设置接收数据的格式
            connection.setRequestProperty("Content-Type", "application/json;charset=utf-8"); // 设置发送数据的格式

            connection.connect();

            DataOutputStream out = new DataOutputStream(connection.getOutputStream());

            out.write(param.getBytes("utf-8"));

            out.flush();

            out.close();

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            String lines;

            StringBuffer sbf = new StringBuffer();

            while ((lines = reader.readLine()) != null) {

                lines = new String(lines.getBytes(), "utf-8");

                sbf.append(lines);

            }

            System.out.println(sbf);

            reader.close();

            // 断开连接

            connection.disconnect();
            return sbf.toString();

        } catch (MalformedURLException e) {
            e.getMessage();
            e.printStackTrace();

        } catch (IOException e) {
            e.getMessage();
            e.printStackTrace();

        }
        return null;
    }

    /**
     * @Title param DESCRIPTION: 调用appa后台
     * @author yu jianwei
     */
    public static String sendJsonForApp(String url, String param) {
        try {

            URL add_url = new URL(url);

            HttpURLConnection connection = (HttpURLConnection) add_url.openConnection();

            connection.setDoInput(true);

            connection.setDoOutput(true);

            connection.setRequestMethod("POST");

            connection.setUseCaches(false);

            connection.setInstanceFollowRedirects(true);
            JSONObject paramJb = new JSONObject(param);
            if (paramJb.has("channel")) {
                connection.setRequestProperty("channel", paramJb.getString("channel"));
            }
            if (paramJb.has("channelNo")) {
                connection.setRequestProperty("channel_no", paramJb.getString("channelNo"));
            }
            if (paramJb.has("token")) {
                connection.setRequestProperty("access_token", paramJb.getString("token"));
            }
            if (paramJb.has("access_token")) {
                connection.setRequestProperty("access_token", paramJb.getString("access_token"));
            }
            // if(paramJb.containsKey("token")){
            // connection.setRequestProperty("Authorization", "Bearer " +
            // paramJb.get("token").toString());
            // }
            connection.setRequestProperty("Accept", "application/json"); // 设置接收数据的格式
            connection.setRequestProperty("Content-Type", "application/json;charset=utf-8"); // 设置发送数据的格式

            connection.connect();

            DataOutputStream out = new DataOutputStream(connection.getOutputStream());

            out.write(param.getBytes("utf-8"));

            out.flush();

            out.close();

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            String lines;

            StringBuffer sbf = new StringBuffer();

            while ((lines = reader.readLine()) != null) {

                lines = new String(lines.getBytes(), "utf-8");

                sbf.append(lines);

            }

            System.out.println(sbf);

            reader.close();

            // 断开连接

            connection.disconnect();
            return sbf.toString();

        } catch (MalformedURLException e) {
            e.getMessage();
            e.printStackTrace();

        } catch (IOException e) {
            e.getMessage();
            e.printStackTrace();

        }
        return null;
    }

    /**
     * @Title param DESCRIPTION: 调用app后台(token验证)
     * @author yl
     */
    public static String sendPostAppToken(String url, String param) {
        try {

            URL add_url = new URL(url);

            HttpURLConnection connection = (HttpURLConnection) add_url.openConnection();

            connection.setDoInput(true);

            connection.setDoOutput(true);

            connection.setRequestMethod("POST");

            connection.setUseCaches(false);

            connection.setInstanceFollowRedirects(true);
            JSONObject paramJb = new JSONObject(param);
            if (paramJb.has("channel")) {
                connection.setRequestProperty("channel", paramJb.getString("channel"));
            }
            if (paramJb.has("channelNo")) {
                connection.setRequestProperty("channel_no", paramJb.getString("channelNo"));
            }
            if (paramJb.has("token")) {
                connection.setRequestProperty("Authorization", "Bearer " + paramJb.get("token").toString());
            }
            connection.setRequestProperty("Accept", "application/json"); // 设置接收数据的格式
            connection.setRequestProperty("Content-Type", "application/json;charset=utf-8"); // 设置发送数据的格式

            connection.connect();

            DataOutputStream out = new DataOutputStream(connection.getOutputStream());

            out.write(param.getBytes("utf-8"));

            out.flush();

            out.close();

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            String lines;

            StringBuffer sbf = new StringBuffer();

            while ((lines = reader.readLine()) != null) {

                lines = new String(lines.getBytes(), "utf-8");

                sbf.append(lines);

            }

            System.out.println(sbf);

            reader.close();

            // 断开连接

            connection.disconnect();
            return sbf.toString();

        } catch (MalformedURLException e) {
            e.getMessage();
            e.printStackTrace();

        } catch (IOException e) {
            e.getMessage();
            e.printStackTrace();

        }
        return null;
    }

    /**
     * DESCRIPTION:发送JSON @author yl @date 2016年8月23日 sendJsonRRS 方法 @param
     * url @param param @param @return @return String @throws
     */
    public static String sendJsonPut(String url, String param) {
        try {

            URL add_url = new URL(url);

            HttpURLConnection connection = (HttpURLConnection) add_url.openConnection();

            connection.setDoInput(true);

            connection.setDoOutput(true);

            connection.setRequestMethod("PUT");

            connection.setUseCaches(false);

            connection.setInstanceFollowRedirects(true);
            connection.setRequestProperty("Accept", "application/json"); // 设置接收数据的格式
            connection.setRequestProperty("Content-Type", "application/json;charset=utf-8"); // 设置发送数据的格式
            // connection.addRequestProperty("access_token", access_token);

            connection.connect();

            DataOutputStream out = new DataOutputStream(connection.getOutputStream());

            out.write(param.getBytes("utf-8"));

            out.flush();

            out.close();

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            String lines;

            StringBuffer sbf = new StringBuffer();

            while ((lines = reader.readLine()) != null) {

                lines = new String(lines.getBytes(), "utf-8");

                sbf.append(lines);

            }

            System.out.println(sbf);

            reader.close();

            // 断开连接

            connection.disconnect();
            return sbf.toString();

        } catch (MalformedURLException e) {
            e.getMessage();
            e.printStackTrace();

        } catch (IOException e) {
            e.getMessage();
            e.printStackTrace();

        }
        return null;
    }

    /**
     * @Title param DESCRIPTION: 调用app后台
     * @author yu jianwei
     */
    public static String sendJsonPutForApp(String url, String param) {
        try {

            URL add_url = new URL(url);

            HttpURLConnection connection = (HttpURLConnection) add_url.openConnection();

            connection.setDoInput(true);

            connection.setDoOutput(true);

            connection.setRequestMethod("PUT");

            connection.setUseCaches(false);

            connection.setInstanceFollowRedirects(true);
            connection.setRequestProperty("Accept", "application/json"); // 设置接收数据的格式
            connection.setRequestProperty("Content-Type", "application/json;charset=utf-8"); // 设置发送数据的格式
            // connection.addRequestProperty("access_token", access_token);
            JSONObject paramJb = new JSONObject(param);
            if (paramJb.has("channel")) {
                connection.setRequestProperty("channel", paramJb.getString("channel"));
            }
            if (paramJb.has("channelNo")) {
                connection.setRequestProperty("channel_no", paramJb.getString("channelNo"));
            }
            if (paramJb.has("token")) {
                connection.setRequestProperty("access_token", paramJb.getString("token"));
            }
            if (paramJb.has("access_token")) {
                connection.setRequestProperty("access_token", paramJb.getString("access_token"));
            }
            // if(paramJb.containsKey("token")){
            // connection.setRequestProperty("Authorization", "Bearer " +
            // paramJb.get("token").toString());
            // }
            connection.connect();

            DataOutputStream out = new DataOutputStream(connection.getOutputStream());

            out.write(param.getBytes("utf-8"));

            out.flush();

            out.close();

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            String lines;

            StringBuffer sbf = new StringBuffer();

            while ((lines = reader.readLine()) != null) {

                lines = new String(lines.getBytes(), "utf-8");

                sbf.append(lines);

            }

            System.out.println(sbf);

            reader.close();

            // 断开连接

            connection.disconnect();
            return sbf.toString();

        } catch (MalformedURLException e) {
            e.getMessage();
            System.out.println(e.getMessage());
            e.printStackTrace();

        } catch (IOException e) {
            e.getMessage();
            System.out.println(e.getMessage());
            e.printStackTrace();

        }
        return null;
    }

    /**
     * @Title param DESCRIPTION: 调用app后台(token验证)
     * @author yl
     */
    public static String sendPutAppToken(String url, String param) {
        try {

            URL add_url = new URL(url);

            HttpURLConnection connection = (HttpURLConnection) add_url.openConnection();

            connection.setDoInput(true);

            connection.setDoOutput(true);

            connection.setRequestMethod("PUT");

            connection.setUseCaches(false);

            connection.setInstanceFollowRedirects(true);
            connection.setRequestProperty("Accept", "application/json"); // 设置接收数据的格式
            connection.setRequestProperty("Content-Type", "application/json;charset=utf-8"); // 设置发送数据的格式
            // connection.addRequestProperty("access_token", access_token);
            JSONObject paramJb = new JSONObject(param);
            if (paramJb.has("channel")) {
                connection.setRequestProperty("channel", paramJb.getString("channel"));
            }
            if (paramJb.has("channelNo")) {
                connection.setRequestProperty("channel_no", paramJb.getString("channelNo"));
            }
            if (paramJb.has("token")) {
                connection.setRequestProperty("Authorization", "Bearer " + paramJb.get("token").toString());
            }
            if (paramJb.has("token")) {
                connection.setRequestProperty("access_token", paramJb.get("token").toString());
            }
            connection.connect();

            DataOutputStream out = new DataOutputStream(connection.getOutputStream());

            out.write(param.getBytes("utf-8"));

            out.flush();

            out.close();

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            String lines;

            StringBuffer sbf = new StringBuffer();

            while ((lines = reader.readLine()) != null) {

                lines = new String(lines.getBytes(), "utf-8");

                sbf.append(lines);

            }

            System.out.println(sbf);

            reader.close();

            // 断开连接

            connection.disconnect();
            return sbf.toString();

        } catch (MalformedURLException e) {
            e.getMessage();
            System.out.println(e.getMessage());
            e.printStackTrace();

        } catch (IOException e) {
            e.getMessage();
            System.out.println(e.getMessage());
            e.printStackTrace();

        }
        return null;
    }

    /**
     * @Title param DESCRIPTION: 调用app后台(token验证)
     * @author yl
     */
    public static String sendDeleteAppToken(String url, String param) {
        try {

            URL add_url = new URL(url);

            HttpURLConnection connection = (HttpURLConnection) add_url.openConnection();

            connection.setDoInput(true);

            connection.setDoOutput(true);

            connection.setRequestMethod("DELETE");

            connection.setUseCaches(false);

            connection.setInstanceFollowRedirects(true);
            connection.setRequestProperty("Accept", "application/json"); // 设置接收数据的格式
            connection.setRequestProperty("Content-Type", "application/json;charset=utf-8"); // 设置发送数据的格式
            // connection.addRequestProperty("access_token", access_token);
            JSONObject paramJb = new JSONObject(param);
            if (paramJb.has("channel")) {
                connection.setRequestProperty("channel", paramJb.getString("channel"));
            }
            if (paramJb.has("channelNo")) {
                connection.setRequestProperty("channel_no", paramJb.getString("channelNo"));
            }
            if (paramJb.has("token")) {
                connection.setRequestProperty("Authorization", "Bearer " + paramJb.get("token").toString());
            }
            if (paramJb.has("access_token")) {
                connection.setRequestProperty("access_token", paramJb.get("access_token").toString());
            }
            connection.connect();

            DataOutputStream out = new DataOutputStream(connection.getOutputStream());

            out.write(param.getBytes("utf-8"));

            out.flush();

            out.close();

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            String lines;

            StringBuffer sbf = new StringBuffer();

            while ((lines = reader.readLine()) != null) {

                lines = new String(lines.getBytes(), "utf-8");

                sbf.append(lines);

            }

            System.out.println(sbf);

            reader.close();

            // 断开连接

            connection.disconnect();
            return sbf.toString();

        } catch (MalformedURLException e) {
            e.getMessage();
            System.out.println(e.getMessage());
            e.printStackTrace();

        } catch (IOException e) {
            e.getMessage();
            System.out.println(e.getMessage());
            e.printStackTrace();

        }
        return null;
    }

    public static String sendPostDoCA(String url, String param, String charset, String signature) {
        BufferedReader in = null;
        DataOutputStream out = null;
        String result = "";
        try {
            URL realUrl = new URL(url);
            // 打开和URL之间的连接
            HttpURLConnection conn = (HttpURLConnection) realUrl.openConnection();
            // 设置通用的请求属性
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("connection", "Keep-Alive");
            conn.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            conn.setRequestProperty("Content-Type", "application/xml;charset=" + charset); // 设置发送数据的格式
            conn.setRequestProperty("Content-Signature", "HMAC-SHA1 " + signature); // 设置发送数据的格式

            // 发送POST请求必须设置如下两行
            conn.setDoOutput(true);
            conn.setDoInput(true);

            out = new DataOutputStream(conn.getOutputStream());

            out.write(param.getBytes("utf-8"));
            out.flush();

            out.close();
            // // 获取URLConnection对象对应的输出流
            // out = new PrintWriter(conn.getOutputStream());
            // // 发送请求参数
            // out.print(param);
            // // flush输出流的缓冲
            // out.flush();
            // 定义BufferedReader输入流来读取URL的响应
            in = new BufferedReader(new InputStreamReader(conn.getInputStream(), charset));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
        } catch (Exception e) {
            e.getMessage();
            System.out.println("发送 POST 请求出现异常！" + e);
            e.printStackTrace();
        }
        // 使用finally块来关闭输出流、输入流
        finally {
            try {
                if (out != null) {
                    out.close();
                }
                if (in != null) {
                    in.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return result;
    }

    public static String sendGet(String url, String param) {
        PrintWriter out = null;
        BufferedReader in = null;
        String result = "";
        try {
            if (!"".equals(param) && param != null) {
                int index = url.indexOf("?");
                if (index > 0) {
                    url = url + "&" + param;
                } else {
                    url = url + "?" + param;
                }
            }
            URL realUrl = new URL(url);
            // 打开和URL之间的连接
            HttpURLConnection conn = (HttpURLConnection) realUrl.openConnection();
            // 设置通用的请求属性
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("connection", "Keep-Alive");
            conn.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            conn.connect();
            // 发送POST请求必须设置如下两行
            // conn.setDoOutput(true);
            // conn.setDoInput(true);

            // OutputStreamWriter writer = new
            // OutputStreamWriter(conn.getOutputStream());
            // writer.write(param);
            // writer.flush();

            // // 获取URLConnection对象对应的输出流
            // out = new PrintWriter(conn.getOutputStream());
            // // 发送请求参数
            // out.print(param);
            // // flush输出流的缓冲
            // out.flush();
            // 定义BufferedReader输入流来读取URL的响应

            in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
        } catch (Exception e) {
            e.getMessage();
            System.out.println("发送 GET 请求出现异常！" + e);
            e.printStackTrace();
        }
        // 使用finally块来关闭输出流、输入流
        finally {
            try {
                if (out != null) {
                    out.close();
                }
                if (in != null) {
                    in.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        // 打印返回报文
        StringBuffer loggerBugffer = new StringBuffer();// 清空日志缓存
        loggerBugffer.append("*************客户登录返回报文************ /n");
        loggerBugffer.append(result + "/n");
        loggerBugffer.append("*************客户登录返回报文************ ");
        // logger.info(loggerBugffer);
        // 打印返回报文
        return result;
    }

    /**
     * DESCRIPTION:CA报文发送需要的加密方法
     *
     * @param key
     * @param data
     * @param @param  key
     * @param @param  data
     * @param @return
     * @param @throws Exception
     * @return String
     * @throws Exception
     * @author xuchao
     * @date 2016年1月6日 hmacSha1 方法
     */
    public static String hmacSha1(byte[] key, byte[] data) throws Exception {
        SecretKeySpec signingKey = new SecretKeySpec(key, HMAC_SHA1_ALGORITHM);
        Mac mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
        mac.init(signingKey);
        byte[] rawHmac = mac.doFinal(data);
        return new String(Base64Utils.encode(rawHmac));
    }

    /**
     * @param in
     * @return
     * @throws IOException
     * @Title:read
     * @Description:
     * @author 'zn'
     * @date 2016年8月5日 下午5:42:54
     */
    public static byte[] read(InputStream in) throws IOException {
        byte[] buf = new byte[1024];
        int length = 0;
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        while ((length = in.read(buf, 0, buf.length)) > 0) {
            bout.write(buf, 0, length);
        }
        bout.flush();
        return bout.toByteArray();
    }

    /**
     * 发送Get请求
     *
     * @param url
     * @return
     * @auther zhaohan
     */
    public static String sendGetUrl(String url) {
        DefaultHttpClient httpClient = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet(url);
//		JSONObject jsonObj = null;
        String result = null;
        try {
            HttpResponse response = httpClient.execute(httpGet);
            HttpEntity entity = response.getEntity();
            result = EntityUtils.toString(entity, "UTF-8");
//			if (result != null && !"".equals(result)) {
//				jsonObj = new JSONObject(result);
//			}
            httpGet.releaseConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * @Title sendGetUrlForApp DESCRIPTION: 字符串拼接调用app后台
     * @author yu jianwei
     */
    public static JSONObject sendGetUrlForApp(String url) {
        String header = "";
        DefaultHttpClient httpClient = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet(url);
        int a = url.indexOf("?");
        if (a != -1) {
            header = url.substring(a + 1, url.length());
        }
        String[] params = header.split("&");
        for (int i = 0; i < params.length; i++) {
            String param = params[i];
            String[] pa = param.split("=");
            if ("channel".equals(pa[0])) {
                httpGet.addHeader("channel", pa[1]);
            } else if ("channelNo".equals(pa[0])) {
                httpGet.addHeader("channel_no", pa[1]);
            } else if ("token".equals(pa[0])) {
                httpGet.addHeader("access_token", pa[1]);
            } else if ("access_token".equals(pa[0])) {
                httpGet.addHeader("access_token", pa[1]);
            }

        }
        JSONObject jsonObj = null;
        try {
            HttpResponse response = httpClient.execute(httpGet);
            HttpEntity entity = response.getEntity();
            String result = EntityUtils.toString(entity, "UTF-8");
            if (result != null && !"".equals(result)) {
                jsonObj = new JSONObject(result);
            }
            httpGet.releaseConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return jsonObj;
    }

    public static JSONObject sendGetToken(String url) {
        String header = "";
        DefaultHttpClient httpClient = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet(url);
        int a = url.indexOf("?");
        if (a != -1) {
            header = url.substring(a + 1, url.length());
        }
        String[] params = header.split("&");
        for (int i = 0; i < params.length; i++) {
            String param = params[i];
            String[] pa = param.split("=");
            if ("channel".equals(pa[0])) {
                httpGet.addHeader("channel", pa[1]);
            } else if ("channelNo".equals(pa[0])) {
                httpGet.addHeader("channel_no", pa[1]);
            } /*
                 * else if("token".equals(pa[0])){
				 * httpGet.addHeader("Authorization", "Bearer " + pa[1]); }
				 */

        }
        JSONObject jsonObj = null;
        try {
            HttpResponse response = httpClient.execute(httpGet);
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                String result = EntityUtils.toString(entity, "UTF-8");
                jsonObj = new JSONObject(result);
            }
            httpGet.releaseConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return jsonObj;
    }

    /**
     * @param url
     * @param map
     * @return
     * @Title:sendGetNew
     * @Description:发送GET请求 调用app后台
     * @author 'zn'
     * @date 2017年2月17日 下午9:31:26
     */
    public static String sendGetNew(String url, Map<String, String> map) {
        PrintWriter out = null;
        BufferedReader in = null;
        String result = "";
        try {
            StringBuffer param = new StringBuffer();
            int i = 0;
            for (String key : map.keySet()) {
                if (!url.contains("?")) {
                    if (i == 0)
                        param.append("?");
                    else
                        param.append("&");
                } else {
                    param.append("&");
                }
                param.append(key).append("=").append(map.get(key));
                i++;
            }
            url += param;
            URL realUrl = new URL(url);
            // 打开和URL之间的连接
            HttpURLConnection conn = (HttpURLConnection) realUrl.openConnection();
            // 设置通用的请求属性
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("connection", "Keep-Alive");
            conn.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            if (map.containsKey("channel")) {
                conn.setRequestProperty("channel", (String) map.get("channel"));
            }
            if (map.containsKey("channelNo")) {
                conn.setRequestProperty("channel_no", (String) map.get("channelNo"));
            }
            if (map.containsKey("token")) {
                conn.setRequestProperty("access_token", (String) map.get("token"));
            }
            if (map.containsKey("access_token")) {
                conn.setRequestProperty("access_token", (String) map.get("access_token"));
            }
            conn.connect();
            // 定义BufferedReader输入流来读取URL的响应

            in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
        } catch (Exception e) {
            // logger.error("HttpClient.sendGet 发送 GET
            // 请求出现异常："+e.getMessage(),e);
            System.out.println("发送 GET 请求出现异常！" + e);
            e.printStackTrace();
        }
        // 使用finally块来关闭输出流、输入流
        finally {
            try {
                if (out != null) {
                    out.close();
                }
                if (in != null) {
                    in.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        // 打印返回报文
        StringBuffer loggerBugffer = new StringBuffer();// 清空日志缓存
        loggerBugffer.append("*************客户登录返回报文************ /n");
        loggerBugffer.append(result + "/n");
        loggerBugffer.append("*************客户登录返回报文************ ");
        // 打印返回报文
        return result;
    }

    /**
     * @Title map DESCRIPTION: get 调用app后台
     * @author yu jianwei
     */
    public static String sendGetNewObj(String url, Map<String, Object> map) {
        PrintWriter out = null;
        BufferedReader in = null;
        String result = "";
        try {
            StringBuffer param = new StringBuffer();
            int i = 0;
            for (String key : map.keySet()) {
                if (i == 0)
                    param.append("?");
                else
                    param.append("&");
                param.append(key).append("=").append(map.get(key));
                i++;
            }
            url += param;
            URL realUrl = new URL(url);
            // 打开和URL之间的连接
            HttpURLConnection conn = (HttpURLConnection) realUrl.openConnection();
            // 设置通用的请求属性
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("connection", "Keep-Alive");
            conn.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            if (map.containsKey("channel")) {
                conn.setRequestProperty("channel", (String) map.get("channel"));
            }
            if (map.containsKey("channelNo")) {
                conn.setRequestProperty("channel_no", (String) map.get("channelNo"));
            }
            if (map.containsKey("token")) {
                conn.setRequestProperty("access_token", (String) map.get("token"));
            }
            if (map.containsKey("access_token")) {
                conn.setRequestProperty("access_token", (String) map.get("access_token"));
            }
            // if(map.containsKey("token")){
            // conn.setRequestProperty("Authorization", "Bearer " +
            // map.get("token").toString());
            // }
            conn.connect();
            // 定义BufferedReader输入流来读取URL的响应

            in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
        } catch (Exception e) {
            // logger.error("HttpClient.sendGet 发送 GET
            // 请求出现异常："+e.getMessage(),e);
            System.out.println("发送 GET 请求出现异常！" + e);
            e.printStackTrace();
        }
        // 使用finally块来关闭输出流、输入流
        finally {
            try {
                if (out != null) {
                    out.close();
                }
                if (in != null) {
                    in.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        // 打印返回报文
        StringBuffer loggerBugffer = new StringBuffer();// 清空日志缓存
        loggerBugffer.append("*************客户登录返回报文************ /n");
        loggerBugffer.append(result + "/n");
        loggerBugffer.append("*************客户登录返回报文************ ");
        // 打印返回报文
        return result;
    }

    /**
     * @Title map DESCRIPTION: get 调用app后台(token验证)
     * @author yl
     */
    public static String sendGetAppToken(String url, Map<String, Object> map) {
        PrintWriter out = null;
        BufferedReader in = null;
        String result = "";
        try {
            StringBuffer param = new StringBuffer();
            int i = 0;
            for (String key : map.keySet()) {
                if (i == 0)
                    param.append("?");
                else
                    param.append("&");
                param.append(key).append("=").append(map.get(key));
                i++;
            }
            url += param;
            URL realUrl = new URL(url);
            // 打开和URL之间的连接
            HttpURLConnection conn = (HttpURLConnection) realUrl.openConnection();
            // 设置通用的请求属性
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("connection", "Keep-Alive");
            conn.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            if (map.containsKey("channel")) {
                conn.setRequestProperty("channel", (String) map.get("channel"));
            }
            if (map.containsKey("channelNo")) {
                conn.setRequestProperty("channel_no", (String) map.get("channelNo"));
            }
            if (map.containsKey("token")) {
                conn.setRequestProperty("Authorization", "Bearer " + map.get("token").toString());
            }
            conn.connect();
            // 定义BufferedReader输入流来读取URL的响应

            in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
        } catch (Exception e) {
            // logger.error("HttpClient.sendGet 发送 GET
            // 请求出现异常："+e.getMessage(),e);
            System.out.println("发送 GET 请求出现异常！" + e);
            e.printStackTrace();
        }
        // 使用finally块来关闭输出流、输入流
        finally {
            try {
                if (out != null) {
                    out.close();
                }
                if (in != null) {
                    in.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        // 打印返回报文
        StringBuffer loggerBugffer = new StringBuffer();// 清空日志缓存
        loggerBugffer.append("*************客户登录返回报文************ /n");
        loggerBugffer.append(result + "/n");
        loggerBugffer.append("*************客户登录返回报文************ ");
        // logger.info(loggerBugffer);
        // 打印返回报文
        return result;
    }

    /**
     * @param url
     * @param outStr
     * @return
     * @Describtion 发送put请求（可包含header,无header传null）
     * @Auther zhaohan
     */
    public static JSONObject sendPutHeader(String url, String outStr, Map<String, Object> headerMap) {
        DefaultHttpClient httpClient = new DefaultHttpClient();
        HttpPut httpPut = new HttpPut(url);
        JSONObject jsonObj = null;
        try {
            if (headerMap != null && !headerMap.isEmpty()) {
                for (Map.Entry<String, Object> entry : headerMap.entrySet()) {
                    httpPut.setHeader(entry.getKey(), entry.getValue().toString());
                }
            }
            httpPut.setEntity(new StringEntity(outStr, "UTF-8"));
            httpPut.setHeader("Content-Type", "application/json");
            HttpResponse response = httpClient.execute(httpPut);
            HttpEntity httpEntity = response.getEntity();
            String result = EntityUtils.toString(httpEntity, "UTF-8");
            jsonObj = new JSONObject(result);
            httpPut.releaseConnection();// 释放连接
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jsonObj;
    }

    /**
     * @param url
     * @param outStr
     * @return
     * @Describtion 发送post请求（可包含header,无header传null）
     * @Auther zhaohan
     */
    public static JSONObject sendPostHeader(String url, String outStr, Map<String, Object> headerMap) {
        DefaultHttpClient httpClient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(url);
        JSONObject jsonObj = null;
        try {
            if (headerMap != null && !headerMap.isEmpty()) {
                for (Map.Entry<String, Object> entry : headerMap.entrySet()) {
                    if ("channelNo".equals(entry.getKey())) {
                        httpPost.setHeader("channel_no", (String) entry.getValue());
                    }
                    httpPost.setHeader(entry.getKey(), entry.getValue().toString());
                }
            }
            if (outStr != null && !"".equals(outStr)) {// 参数不为空
                httpPost.setEntity(new StringEntity(outStr, "UTF-8"));
            }
            httpPost.setHeader("Content-Type", "application/json");
            HttpResponse response = httpClient.execute(httpPost);
            HttpEntity httpEntity = response.getEntity();
            String result = EntityUtils.toString(httpEntity, "UTF-8");
            jsonObj = new JSONObject(result);
            httpPost.releaseConnection();// 释放连接
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jsonObj;
    }

    /**
     * @param url
     * @param paramMap  请求参数 没有传null
     * @param headerMap header中需要传入的参数 没有传null
     * @return
     * @Describtion 发送get请求（可包含header,无header传null），拼装url
     * @Auther zhaohan
     */
    public static JSONObject sendGetHeader(String url, Map<String, Object> paramMap, Map<String, Object> headerMap) {
        DefaultHttpClient httpClient = new DefaultHttpClient();
        JSONObject jsonObj = null;
        try {
            StringBuffer paramStr = new StringBuffer();
            if (paramMap != null && !paramMap.isEmpty()) {
                int i = 0;
                for (Map.Entry<String, Object> entry : paramMap.entrySet()) {
                    if (i == 0)
                        paramStr.append("?");
                    else
                        paramStr.append("&");
                    paramStr.append(entry.getKey()).append("=").append(entry.getValue());
                    i++;
                }
                url += paramStr;
            }
            HttpGet httpGet = new HttpGet(url);
            if (headerMap != null && !headerMap.isEmpty()) {
                for (Map.Entry<String, Object> entry : headerMap.entrySet()) {
                    if ("channelNo".equals(entry.getKey())) {
                        httpGet.setHeader("channel_no", (String) entry.getValue());
                    }
                    httpGet.setHeader(entry.getKey(), (String) entry.getValue());
                }
            }
            httpGet.setHeader("Content-Type", "application/json");
            HttpResponse response = httpClient.execute(httpGet);
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                String result = EntityUtils.toString(entity, "UTF-8");
                jsonObj = new JSONObject(result);
            }
            httpGet.releaseConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return jsonObj;
    }

    /**
     * @param url
     * @param outStr
     * @param headerMap
     * @return
     * @Description:
     * @author zyj
     * @date 2017年3月16日下午4:58:41
     */
    public static JSONObject sendPutHeaderToken(String url, String outStr, Map<String, Object> headerMap) {
        DefaultHttpClient httpClient = new DefaultHttpClient();
        HttpPut httpPut = new HttpPut(url);
        JSONObject jsonObj = null;
        try {
            if (headerMap != null && !headerMap.isEmpty()) {
                for (Map.Entry<String, Object> entry : headerMap.entrySet()) {
                    if ("channelNo".equals(entry.getKey())) {
                        httpPut.setHeader("channel_no", (String) entry.getValue());
                    }
                    httpPut.setHeader(entry.getKey(), entry.getValue().toString());
                }
            }
            httpPut.setEntity(new StringEntity(outStr, "UTF-8"));
            httpPut.setHeader("Content-Type", "application/json");
            JSONObject paramJb = new JSONObject(outStr);
            if (paramJb.has("token")) {
                httpPut.setHeader("Authorization", "Bearer " + paramJb.get("token").toString());
            }
            HttpResponse response = httpClient.execute(httpPut);
            HttpEntity httpEntity = response.getEntity();
            String result = EntityUtils.toString(httpEntity, "UTF-8");
            jsonObj = new JSONObject(result);
            httpPut.releaseConnection();// 释放连接
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jsonObj;
    }

    public static JSONObject sendDeleteForApp(String url) {
        String header = "";
        DefaultHttpClient httpClient = new DefaultHttpClient();
        HttpDelete httpGet = new HttpDelete(url);
        int a = url.indexOf("?");
        if (a != -1) {
            header = url.substring(a + 1, url.length());
        }
        String[] params = header.split("&");
        for (int i = 0; i < params.length; i++) {
            String param = params[i];
            String[] pa = param.split("=");
            if ("channel".equals(pa[0])) {
                httpGet.addHeader("channel", pa[1]);
            } else if ("channelNo".equals(pa[0])) {
                httpGet.addHeader("channel_no", pa[1]);
            } else if ("token".equals(pa[0])) {
                httpGet.addHeader("Authorization", "Bearer " + pa[1]);
            } else if ("access_token".equals(pa[0])) {
                httpGet.addHeader("access_token", pa[1]);
            }

        }
        JSONObject jsonObj = null;
        try {
            HttpResponse response = httpClient.execute(httpGet);
            HttpEntity entity = response.getEntity();
            String result = EntityUtils.toString(entity, "UTF-8");
            System.out.println("删除**********result:" + result);
            if (result != null && !"".equals(result)) {
                jsonObj = new JSONObject(result);
            }
            httpGet.releaseConnection();
        } catch (IOException e) {
            e.getMessage();
        }
        return jsonObj;
    }

    /**
     * @Title param DESCRIPTION: 调用app后台(token验证)
     * @author yl
     */
    public static String sendDelateApp(String url, String param) {
        try {

            URL add_url = new URL(url);

            HttpURLConnection connection = (HttpURLConnection) add_url.openConnection();

            connection.setDoInput(true);

            connection.setDoOutput(true);

            connection.setRequestMethod("DELETE");

            connection.setUseCaches(false);

            connection.setInstanceFollowRedirects(true);
            JSONObject paramJb = new JSONObject(param);
            if (paramJb.has("channel")) {
                connection.setRequestProperty("channel", paramJb.getString("channel"));
            }
            if (paramJb.has("channelNo")) {
                connection.setRequestProperty("channel_no", paramJb.getString("channelNo"));
            }
            if (paramJb.has("token")) {
                connection.setRequestProperty("Authorization", "Bearer " + paramJb.get("token").toString());
            }
            connection.setRequestProperty("Accept", "application/json"); // 设置接收数据的格式
            connection.setRequestProperty("Content-Type", "application/json;charset=utf-8"); // 设置发送数据的格式

            connection.connect();

            DataOutputStream out = new DataOutputStream(connection.getOutputStream());

            out.write(param.getBytes("utf-8"));

            out.flush();

            out.close();

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            String lines;

            StringBuffer sbf = new StringBuffer();

            while ((lines = reader.readLine()) != null) {

                lines = new String(lines.getBytes(), "utf-8");

                sbf.append(lines);

            }

            System.out.println(sbf);

            reader.close();

            // 断开连接

            connection.disconnect();
            return sbf.toString();

        } catch (MalformedURLException e) {
            e.getMessage();
            e.printStackTrace();

        } catch (IOException e) {
            e.getMessage();
            e.printStackTrace();

        }
        return null;
    }

    /**
     * @Title formUpload DESCRIPTION: 调用app后台人脸验证
     * @author yu jianwei
     */
    public static String formUpload(String urlStr, Map<String, String> textMap, Map<String, String> headerMap,
                                    String contentType) {
        String res = "";
        String ress = "";
        String rex = "";
        HttpURLConnection conn = null;
        String BOUNDARY = "---------------------------123821742118716";
        try {
            URL url = new URL(urlStr);
            conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(30000);
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setUseCaches(false);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows; U; Windows NT 6.1; zh-CN; rv:1.9.2.6)");
            conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + BOUNDARY);
            conn.setRequestProperty("Charset", "UTF-8");
            if (headerMap != null) {
                StringBuffer strBuf = new StringBuffer();
                Iterator iter = textMap.entrySet().iterator();
                while (iter.hasNext()) {
                    Map.Entry entry = (Map.Entry) iter.next();
                    String inputName = (String) entry.getKey();
                    String inputValue = (String) entry.getValue();
                    if (inputValue == null) {
                        continue;
                    }
                    conn.setRequestProperty(inputName, inputValue);
                }
            }
            OutputStream out = new DataOutputStream(conn.getOutputStream());
            if (textMap != null) {
                StringBuffer strBuf = new StringBuffer();
                Iterator iter = textMap.entrySet().iterator();
                while (iter.hasNext()) {
                    Map.Entry entry = (Map.Entry) iter.next();
                    String inputName = (String) entry.getKey();
                    String inputValue = (String) entry.getValue();
                    if (inputValue == null) {
                        continue;
                    }
                    strBuf.append("\r\n").append("--").append(BOUNDARY).append("\r\n");
                    strBuf.append("Content-Disposition: form-data; name=\"" + inputName + "\"\r\n\r\n");
                    strBuf.append(inputValue);
                }
                out.write(strBuf.toString().getBytes());
            }
            byte[] endData = ("\r\n--" + BOUNDARY + "--\r\n").getBytes();
            out.write(endData);
            out.flush();
            out.close();
            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                // 读取返回数据
                StringBuffer strBuf = new StringBuffer();
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));

                String line = null;
                while ((line = reader.readLine()) != null) {
                    strBuf.append(line).append("\n");
                }
                // res = strBuf.toString();
                res = new String(strBuf.toString().getBytes(), "UTF-8");
                ress = new JSONTokener(res).nextValue().toString();
                reader.close();
                reader = null;
            } else {
                StringBuffer error = new StringBuffer();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                String line1 = null;
                while ((line1 = bufferedReader.readLine()) != null) {
                    error.append(line1).append("\n");
                }
                ress = error.toString();
                bufferedReader.close();
                bufferedReader = null;
            }
        } catch (Exception e) {
            System.out.println("发送POST请求出错。" + e);
            e.printStackTrace();
        } finally {
            if (conn != null) {
                conn.disconnect();
                conn = null;
            }
        }
        return ress;
    }

    public static String sendPostParamnew(String url, String param) {
        PrintWriter out = null;
        BufferedReader in = null;
        String result = "";
        try {
            URL realUrl = new URL(url);
            // 打开和URL之间的连接
            HttpURLConnection conn = (HttpURLConnection) realUrl.openConnection();
            // 设置通用的请求属性
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("connection", "Keep-Alive");
            conn.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=utf-8"); // 设置发送数据的格式
            // 发送POST请求必须设置如下两行
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setRequestMethod("POST");

            OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream(), "UTF-8");
            writer.write(param);
            writer.flush();

            if (conn.getResponseCode() == 400) {
                return "error";
            }


//            // 获取URLConnection对象对应的输出流
//            out = new PrintWriter(conn.getOutputStream());
//            // 发送请求参数
//            out.print(param);
//            // flush输出流的缓冲
//            out.flush();
            // 定义BufferedReader输入流来读取URL的响应
            in = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), "utf-8"));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
        } catch (Exception e) {
            System.out.println("发送 POST 请求出现异常！" + e);
            e.printStackTrace();
        }
        //使用finally块来关闭输出流、输入流
        finally {
            try {
                if (out != null) {
                    out.close();
                }
                if (in != null) {
                    in.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return result;
    }

    /**
     * 获取通信结果
     *
     * @return
     */
    public String getResult() {
        return result;
    }

    /**
     * 设置通信结果
     *
     * @param result
     */
    public void setResult(String result) {
        this.result = result;
    }

    /**
     * @param connection
     * @param message
     * @param encoder
     * @throws Exception
     * @Title:requestServer
     * @Description:HTTP Post发送消息
     * @author 'zn'
     * @date 2016年8月5日 下午5:40:31
     */
    private void requestServer(final URLConnection connection, String message, String encoder) throws Exception {
        PrintStream out = null;
        try {
            connection.connect();
            out = new PrintStream(connection.getOutputStream(), false, encoder);
            out.print(message);
            out.flush();
        } catch (Exception e) {
            throw e;
        } finally {
            if (null != out) {
                out.close();
            }
        }
    }

    /**
     * @param requestParam
     * @param coder
     * @return
     * @Title:getRequestParamString
     * @Description:将Map存储的对象，转换为key=value&key=value的字符
     * @author 'zn'
     * @date 2016年8月5日 下午5:40:01
     */
    private String getRequestParamString(Map<String, String> requestParam, String coder) {
        if (null == coder || "".equals(coder)) {
            coder = "UTF-8";
        }
        StringBuffer sf = new StringBuffer("");
        String reqstr = "";
        if (null != requestParam && 0 != requestParam.size()) {
            for (Entry<String, String> en : requestParam.entrySet()) {
                try {
                    sf.append(en.getKey() + "=" + (null == en.getValue() || "".equals(en.getValue()) ? ""
                            : URLEncoder.encode(en.getValue(), coder)) + "&");
                } catch (UnsupportedEncodingException e) {
                    e.getMessage();
                    return "";
                }
            }
            reqstr = sf.substring(0, sf.length() - 1);
        }
        return reqstr;
    }

    /**
     * @param connection
     * @param encoding
     * @return
     * @throws URISyntaxException
     * @throws IOException
     * @throws Exception
     * @Title:response
     * @Description:显示Response消息
     * @author 'zn'
     * @date 2016年8月5日 下午5:42:38
     */
    private String response(final HttpURLConnection connection, String encoding)
            throws URISyntaxException, IOException, Exception {
        InputStream in = null;
        StringBuilder sb = new StringBuilder(1024);
        BufferedReader br = null;
        try {
            if (200 == connection.getResponseCode()) {
                in = connection.getInputStream();
                sb.append(new String(read(in), encoding));
            } else {
                in = connection.getErrorStream();
                sb.append(new String(read(in), encoding));
            }
            return sb.toString();
        } catch (Exception e) {
            throw e;
        } finally {
            if (null != br) {
                br.close();
            }
            if (null != in) {
                in.close();
            }
            if (null != connection) {
                connection.disconnect();
            }
        }
    }

}    
