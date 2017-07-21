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
package com.haiercash.appserver.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONObject;
import org.springframework.util.Base64Utils;

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
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;

/**
 * SignUtil.java
 *
 * @author suyang
 * @serial
 * @since 2014年5月23日 上午10:17:56
 */
public class HttpClient {
	private static Log logger = LogFactory.getLog(HttpClient.class);
	public static String HMAC_SHA1_ALGORITHM = "HmacSHA1";
	private static final String CHARSET = "UTF-8";
	/**
	 * 向指定 URL 发送POST方法的请求
	 *
	 * @param url
	 *            发送请求的 URL
	 * @param param
	 *            请求参数，请求参数应该是 name1=value1&name2=value2 的形式。
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

			OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());
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
			in.close();
			out.close();
		} catch (Exception e) {
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
		logger.info(loggerBugffer);
		// 打印返回报文
		return result;
	}

	/**
	 * DESCRIPTION:发送http请求
	 *
	 * @param url
	 * @param param
	 * @param charset
	 * @return String
	 * @throws @author
	 *             xuchao
	 * @date 2015年7月27日 sendPost 方法
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
			in.close();
			writer.close();
		} catch (Exception e) {
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
	 * DESCRIPTION:发送post参数请求
	 *
	 * @param url
	 * @param param
	 * @return String
	 * @throws @author
	 *             xuchao
	 * @date 2015年6月12日 sendPost 方法
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
			in.close();
			out.close();
		} catch (Exception e) {
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
	 * DESCRIPTION:发送JSON
	 *
	 * @param url
	 * @param param
	 * @return String
	 * @throws @author
	 *             xuchao
	 * @date 2015年6月12日 sendPost 方法
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

//			System.out.println(sbf);

			reader.close();

			// 断开连接

			connection.disconnect();
			return sbf.toString();

		} catch (MalformedURLException e) {

			// TODO Auto-generated catch block

			e.printStackTrace();

		} catch (IOException e) {

			// TODO Auto-generated catch block

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
			// logger.debug("conn="+conn);
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
			in.close();
			out.close();
		} catch (Exception e) {
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
		// logger.debug("RESULT="+result);
		return result;
	}

	public  static String postSignedJson(String requestUri, JSONObject reqData, String appSecret)
			throws IOException, NoSuchAlgorithmException, InvalidKeyException {
		System.setProperty("jsse.enableSNIExtension", "false");

		CloseableHttpClient httpClient = HttpClients.createDefault();
		Key signingKey = new SecretKeySpec(appSecret.getBytes(), HMAC_SHA1_ALGORITHM);
		Mac mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
		mac.init(signingKey);
		byte[] rawData = reqData.toString().getBytes(CHARSET);
		byte[] rawHmac = mac.doFinal(rawData);
		HttpPost httpPost = new HttpPost(requestUri);
		httpPost.addHeader("Content-Type", "application/json; charset=utf-8");
		httpPost.addHeader("Accept", "application/json; charset=utf-8");
		httpPost.addHeader("Content-Signature", "HMAC-SHA1 " + new String(Base64Utils.encode(rawHmac)));
		ByteArrayEntity reqEntity = new ByteArrayEntity(rawData);
		httpPost.setEntity(reqEntity);
		CloseableHttpResponse httpResponse = httpClient.execute(httpPost);
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		copy(httpResponse.getEntity().getContent(), outputStream);
		String respStr = new String(outputStream.toByteArray(), CHARSET);
		outputStream.close();
		if (httpResponse.getStatusLine().getStatusCode() == HttpURLConnection.HTTP_OK) {
			return respStr;
		} else {
			throw new IOException(httpResponse.getStatusLine().toString() + " ERROR\nResponseText=" + respStr);
		}
	}
	public static long copy(InputStream input, OutputStream output) throws IOException {
		byte[] buffer = new byte[1024 * 4];
		long count = 0;
		int n = 0;
		while (-1 != (n = input.read(buffer))) {
//			org.json.JSONObject jsonResp = new org.json.JSONObject(new String(buffer, CHARSET));
//			if (!(Boolean) jsonResp.get("isOk") && jsonResp.get("message").toString().equals("设置pdf模板出错")) {
//				continue;
//			}
			output.write(buffer, 0, n);
			count += n;
		}
		return count;
	}
	/**
	 * DESCRIPTION:CA报文发送需要的加密方法
	 *
	 * @param key
	 * @param data
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

	/*
	 * public static void main(String[] args) { HttpClient client = new
	 * HttpClient(); Object obj = client.sendPost(
	 * "http://10.135.104.154:8888/Portal/Creditforecast.asmx?op=GetAllInfo",
	 * "01393386"); System.out.println(obj); }
	 */
}