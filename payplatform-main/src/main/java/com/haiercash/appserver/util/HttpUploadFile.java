package com.haiercash.appserver.util;

import com.haiercash.appserver.service.MerchFaceService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
/*
* <p>Title: java通过模拟post方式提交表单实现图片上传功能实例</p>
* <p>Description: </p>
* <p>Company: </p> 
* @author yuanli
 */
public class HttpUploadFile {

	private static Log logger = LogFactory.getLog(MerchFaceService.class);

	@Value("${common.face.face_gettoken_url}")
	protected static String face_gettoken_url;

//	private static String face_gettoken_url = "https://api.megvii.com/faceid/lite/get_token";//face++获取token


	/**
	 * 上传文件图片 
	 * @return 
	 */
	public static String testUploadImage(Map<String, String> paramMap){
		String api_key = paramMap.get("api_key");//调用此API的api_key
		String api_secret = paramMap.get("api_secret");//调用此API的api_key的secret
		String comparison_type = paramMap.get("comparison_type");//设定本次核身服务器类型； 1：有源对比； 2：无源对比
		String face_image_type = paramMap.get("face_image_type");
		String idcard_name = paramMap.get("idcard_name");//姓名
		String idcard_number = paramMap.get("idcard_number");//身份证
		String return_url = paramMap.get("return_url");//用户完成或取消验证后网页跳转的目标URL（post）
		String biz_no = paramMap.get("biz_no");//客户业务流水号，该号必须唯一。并会在notify和return时原封不动的返回给你的服务器
		String notify_url = paramMap.get("notify_url");//用户完成验证、取消验证、或验证超时后，有FaceID服务器请求客户的服务器的URL。校验数据可信性，回调方式POST
		//0：不拍身份证，通过idcard_name、idcard_number传入；1：拍摄身份证人像面，可获取人像面所有信息；2：拍摄身份证人像面和身份证国徽面，获取身份证所有信息；3：不拍摄身份证，但会要求用户在界面上输入身份证和姓名
		String idcard_mode = paramMap.get("idcard_mode");
		String image = paramMap.get("image");
		String contentType = paramMap.get("contentType");
		String url = paramMap.get("face_token");
		logger.info("face++******获取token的URL:"+url);
		Map<String, String> textMap = new HashMap<String, String>();
		Map<String, String> fileMap = new HashMap<String, String>();
		textMap.put("api_key",api_key);
		textMap.put("api_secret", api_secret);
		textMap.put("comparison_type",comparison_type);
		textMap.put("face_image_type", face_image_type);
		textMap.put("idcard_name", idcard_name);
		textMap.put("idcard_number", idcard_number);
		textMap.put("return_url", return_url);
		textMap.put("biz_no", biz_no);
		textMap.put("notify_url", notify_url);
		textMap.put("idcard_mode", idcard_mode);
		fileMap.put("image", image);
		String ret = formUpload(url, textMap, fileMap,contentType);
		System.out.println(ret);
		return ret;
//		String image = "/home/jboss/channelFile/3208/down/20161130/1111.png";
//		textMap.put("api_key", "D9svZuIdQ6uCqqBk4UMB2xiTLgKtVFLS");
//		textMap.put("api_secret", "PYDHtPUnDzBcXvQsyDSyfFIBjuWI5w8K");
//		textMap.put("comparison_type", "1");
//		textMap.put("face_image_type", "raw_image");
//		textMap.put("idcard_name", idcard_name);
//		textMap.put("idcard_number", idcard_number);
//		textMap.put("return_url", "https://api.megvii.com/faceid/lite/get_token");
//		textMap.put("biz_no", "https://api.megvii.com/faceid/lite/get_token");
//		textMap.put("notify_url", "https://api.megvii.com/faceid/lite/get_token");
//		textMap.put("idcard_mode", "0");
//		fileMap.put("image", path);
//		String contentType = "application/octet-stream";
	}

	/**
	 * 上传图片
	 */
	@SuppressWarnings("rawtypes")
	public static String formUpload(String urlStr, Map<String, String> textMap,
			Map<String, String> fileMap,String contentType) {
		String res = "";
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
			conn.setRequestProperty("User-Agent",
					"Mozilla/5.0 (Windows; U; Windows NT 6.1; zh-CN; rv:1.9.2.6)");
			conn.setRequestProperty("Content-Type",
					"multipart/form-data; boundary=" + BOUNDARY);
			conn.setRequestProperty("Charset", "UTF-8"); 
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
					strBuf.append("\r\n").append("--").append(BOUNDARY)
							.append("\r\n");
					strBuf.append("Content-Disposition: form-data; name=\""
							+ inputName + "\"\r\n\r\n");
					strBuf.append(inputValue);
				}
				out.write(strBuf.toString().getBytes());
			}
			if (fileMap != null) {
				Iterator iter = fileMap.entrySet().iterator();
				while (iter.hasNext()) {
					Map.Entry entry = (Map.Entry) iter.next();
					String inputName = (String) entry.getKey();
					String inputValue = (String) entry.getValue();
					if (inputValue == null) {
						continue;
					}
					File file = new File(inputValue);
					String filename = file.getName();
					StringBuffer strBuf = new StringBuffer();
					strBuf.append("\r\n").append("--").append(BOUNDARY)
							.append("\r\n");
					strBuf.append("Content-Disposition: form-data; name=\""
							+ inputName + "\"; filename=\"" + filename
							+ "\"\r\n");
					strBuf.append("Content-Type:" + contentType + "\r\n\r\n");
					out.write(strBuf.toString().getBytes());
					DataInputStream in = new DataInputStream(new FileInputStream(file));
					int bytes = 0;
					byte[] bufferOut = new byte[1024];
					while ((bytes = in.read(bufferOut)) != -1) {
						out.write(bufferOut, 0, bytes);
					}
					in.close();
				}
			}
			byte[] endData = ("\r\n--" + BOUNDARY + "--\r\n").getBytes();
			out.write(endData);
			out.flush();
			out.close();
			int responseCode = conn.getResponseCode();
		    if (responseCode==200) {
		    	// 读取返回数据
				StringBuffer strBuf = new StringBuffer();
				BufferedReader reader = new BufferedReader(new InputStreamReader(
						conn.getInputStream()));
				   
				String line = null;
				while ((line = reader.readLine()) != null) {
					strBuf.append(line).append("\n");
				}
				res = strBuf.toString();
				reader.close();
				reader = null;
			}else{
				StringBuffer error = new StringBuffer();
				BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(
						conn.getErrorStream()));
				String line1 = null;
				while ((line1=bufferedReader.readLine())!=null) {
				    error.append(line1).append("\n");
				}
				res=error.toString();
				bufferedReader.close();
				bufferedReader=null;
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
		return res;
	}

	public static String sendGet(String url, String param) {
		PrintWriter out = null;
		BufferedReader in = null;
		String result = "";
		try {
			if(!"".equals(param) && param!=null){
				int index = url.indexOf("?");
				if(index > 0){
					url =  url+"&"+param;
				}else{
					url =  url+"?"+param;
				}
			}
			URL realUrl = new URL(url);
			// 打开和URL之间的连接
			HttpURLConnection conn = (HttpURLConnection) realUrl.openConnection();
			// 设置通用的请求属性
			conn.setRequestProperty("accept", "*/*");
			conn.setRequestProperty("connection", "Keep-Alive");
			conn.setRequestProperty("user-agent",
					"Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
			conn.connect();

			in = new BufferedReader(
					new InputStreamReader(conn.getInputStream(), "UTF-8"));
			String line;
			while ((line = in.readLine()) != null) {
				result += line;
			}
		} catch (Exception e) {
			logger.error("HttpClient.sendGet 发送 GET 请求出现异常："+e.getMessage(),e);
			System.out.println("发送 GET 请求出现异常！"+e);
			e.printStackTrace();
		}
		//使用finally块来关闭输出流、输入流
		finally{
			try{
				if(out!=null){
					out.close();
				}
				if(in!=null){
					in.close();
				}
			}
			catch(IOException ex){
				ex.printStackTrace();
			}
		}
		// 打印返回报文
		StringBuffer loggerBugffer = new StringBuffer();//清空日志缓存
		loggerBugffer.append("*************客户登录返回报文************ /n");
		loggerBugffer.append(result+"/n");
		loggerBugffer.append("*************客户登录返回报文************ ");
		//logger.info(loggerBugffer);
		// 打印返回报文
		return result;
	}

	public static String sendJson(String url, String param) {
		try {

			URL add_url = new URL(url);

			HttpURLConnection connection = (HttpURLConnection)add_url.openConnection();

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

		} catch (IOException e) {
			logger.error("HttpClient.sendJson 发送 POST 请求出现异常："+e.getMessage(),e);
			e.printStackTrace();

		}
		return null;
	}

}
