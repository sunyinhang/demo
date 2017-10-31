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

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * SignUtil.java
 *
 * @author suyang
 * @serial
 * @since 2014年5月23日 上午10:17:56
 */
@Deprecated
public class HttpClient {
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
     * 发送Get请求
     *
     * @param url
     * @return
     * @auther zhaohan
     */
    public static String sendGetUrl(String url) {
        DefaultHttpClient httpClient = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet(url);
        String result = null;
        try {
            HttpResponse response = httpClient.execute(httpGet);
            HttpEntity entity = response.getEntity();
            result = EntityUtils.toString(entity, "UTF-8");

            httpGet.releaseConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }
}    
