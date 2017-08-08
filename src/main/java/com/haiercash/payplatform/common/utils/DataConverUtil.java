package com.haiercash.payplatform.common.utils;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;

import com.fasterxml.jackson.databind.DeserializationFeature;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONObject;

import de.odysseus.staxon.json.JsonXMLConfig;
import de.odysseus.staxon.json.JsonXMLConfigBuilder;
import de.odysseus.staxon.json.JsonXMLInputFactory;
import de.odysseus.staxon.json.JsonXMLOutputFactory;
import de.odysseus.staxon.xml.util.PrettyXMLEventWriter;
//import org.dom4j.Document;
//import org.dom4j.DocumentHelper;
//import org.dom4j.Element;


/**
 *  java转换xml、list、map和json 
 * common xml conver utility
 * 
 * @author viruscodecn@gmail.com
 * @version Framework 2010.10.26
 * @url http://blog.csdn.net/arjick/article/details/6251777
 */
public class DataConverUtil {
	
   /**
    *@Title:jsonToMap
    *@Description:
    *@author  'zsc'
    *@date 2016年5月26日 下午3:24:11
    *@param jsonstr
    *@return
    */
	public static Map<String, Object> jsonToMap(String jsonstr) {
		ObjectMapper objectMapper = new ObjectMapper();
		try {
			Map<String, Object> map = objectMapper.readValue(jsonstr, Map.class);
			return map;
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
		
	}
	
    /**
     *@Title:mapToJson
     *@Description:
     *@author  'zsc'
     *@date 2016年5月26日 下午3:22:43
     *@param map
     *@return
     */
	public static String mapToJson(Map<String, Object> map) {
		//JSONObject jsonObject = JSONObject.fromObject(map);
		JSONObject jsonObject = new JSONObject(map);
		String jsonStr = jsonObject.toString();
		return jsonStr;
	}
	
	/**
	 * 
	 *@Title:mapToJsonStr
	 *@Description:
	 *@author  'zn'
	 *@date 2016年5月27日 下午7:56:13
	 *@param map
	 *@return
	 */
	public static String mapToJsonStr(Map<String, String> map) {
		//JSONObject jsonObject = JSONObject.fromObject(map);
		JSONObject jsonObject = new JSONObject(map);
		String jsonStr = jsonObject.toString();
		return jsonStr;
	}
	
	
    /**
     *@Title:jsonToXml
     *@Description:
     *@author  'zsc'
     *@date 2016年5月26日 下午3:26:25
     *@param json
     *@return
     */
	public static String jsonToXml(String json) {
		try {
//			XMLSerializer serializer = new XMLSerializer();
//			serializer.setTypeHintsEnabled(false);
//			//serializer.setTypeHintsCompatibility(false);
//			serializer.setRootName("oooooooo");
			//serializer.removeNamespace("<o>");
			//serializer.isForceTopLevelObject()
			//JSON jsonObject = JSONSerializer.toJSON(json);
//			String xmlstr = serializer.write(jsonObject);
//			xmlstr = xmlstr.replaceAll("<[/]?oooooooo>", "");
			StringBuffer bf  = new StringBuffer();
			mapToXml(jsonToMap(json), bf);
			return bf.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	    *@Title:jsonToXml
	    *@Description:json转xml
	    *@author  'zsc'
	    *@date 2016年6月15日 下午3:07:51
	    *@param json
	    *@return
	    */
	public static String jsonToXmlNew(String json){  
        StringReader input = new StringReader(json);  
        StringWriter output = new StringWriter();  
        JsonXMLConfig config = new JsonXMLConfigBuilder().multiplePI(false).repairingNamespaces(false).build();  
        try {  
            XMLEventReader reader = new JsonXMLInputFactory(config).createXMLEventReader(input);  
            XMLEventWriter writer = XMLOutputFactory.newInstance().createXMLEventWriter(output);  
            writer = new PrettyXMLEventWriter(writer);  
            writer.add(reader);  
            reader.close();  
            writer.close();  
            return output.toString();  
        } catch( Exception e){  
            e.printStackTrace();  
        } finally {  
            try {  
                output.close();  
                input.close();  
            } catch (IOException e) {  
                e.printStackTrace();  
            }  
        }  
        return null;
    }    

	/**
	 *@Title:xmlToJson
	 *@Description:
	 *@author  'zsc'
	 *@date 2016年5月26日 下午3:26:11
	 *@param xml
	 *@return
	 */
	public static String xmlToJson(String xml) {
//		XMLSerializer xmlSerializer = new XMLSerializer();
//		return xmlSerializer.read(xml).toString();
		StringReader input = new StringReader(xml);
		StringWriter output = new StringWriter();
		JsonXMLConfig config = new JsonXMLConfigBuilder().autoArray(true).autoPrimitive(true).prettyPrint(true).build();
		try {
			XMLEventReader reader = XMLInputFactory.newInstance().createXMLEventReader(input);
			XMLEventWriter writer = new JsonXMLOutputFactory(config).createXMLEventWriter(output);
			writer.add(reader);
			reader.close();
			writer.close();
			return output.toString();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				output.close();
				input.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	/**
	 *@Title:xmlToJsonObject
	 *@Description:Json对象
	 *@author  'zsc'
	 *@date 2016年5月26日 下午1:56:39
	 *@param xml
	 *@return
	 */
	public static JSONObject xmlToJsonObj(String xml) {
//		xmlXMLSerializer xmlSerializer = new XMLSerializer();
//		return (JSONObject) xmlSerializer.read(xml);
		JSONObject jsonObject = new JSONObject(xmlToJson(xml));
		return jsonObject;
	}
	
	/**
	    *@Title:xmlToJson 
	    *@Description: xml转json
	    *@author  'zsc'
	    *@date 2016年6月15日 下午3:09:46
	    *@param xml
	    *@return
	    */
	    public static String xmlToJsonNew(String xml){  
	        StringReader input = new StringReader(xml);  
	        StringWriter output = new StringWriter();  
	        JsonXMLConfig config = new JsonXMLConfigBuilder().autoArray(true).autoPrimitive(true).prettyPrint(true).build();  
	        try {  
	            XMLEventReader reader = XMLInputFactory.newInstance().createXMLEventReader(input);  
	            XMLEventWriter writer = new JsonXMLOutputFactory(config).createXMLEventWriter(output);  
	            writer.add(reader);  
	            reader.close();  
	            writer.close();  
	            return output.toString();  
	        } catch( Exception e){  
	            e.printStackTrace();  
	        } finally {  
	            try {  
	                output.close();  
	                input.close();  
	            } catch (IOException e) {  
	                e.printStackTrace();  
	            }  
	        }  
	        return null;
	    }
	
	/**
     *@Title:xmlToJsonObj
     *@Description:xml转jsonObject
     *@author  'zsc'
     *@date 2016年6月15日 下午3:11:55
     *@param xml
     *@return
     */
	public static JSONObject xmlToJsonObjNew(String xml) {
		JSONObject jsonObject = new JSONObject(xmlToJsonNew(xml));
		return jsonObject;
	}
	
    /**
     *@Title:xmlToMap
     *@Description:
     *@author  'zsc'
     *@date 2016年5月26日 下午3:25:47
     *@param xml
     *@return
     */
	public static Map<String,Object> xmlToMap(String xml){
		String jsonstr = xmlToJson(xml);
		Map<String,Object> map = jsonToMap(jsonstr);
		return map;
	}

	/**
	 *@Title:mapToXml
	 *@Description:
	 *@author  'zsc'
	 *@date 2016年5月26日 下午3:25:23
	 *@param map
	 *@param sb
	 *@return
	 */
	 public static String  mapToXml(Map map, StringBuffer sb) {  
		        if(sb == null){
		        	sb = new StringBuffer();
		        }
		        Set set = map.keySet();  
		         for (Iterator it = set.iterator(); it.hasNext();) {  
		            String key = (String) it.next();  
		            Object value = map.get(key);  
		            if (null == value) {
		                 value = "";  
		            }
	               if (value instanceof HashMap) {  
	                   sb.append("<" + key + ">");  
	                   mapToXml((HashMap) value, sb);  
	                   sb.append("</" + key + ">");  
	               } else {  
	                     sb.append("<" + key + ">" + value + "</" + key + ">");  
	               }  
		       }  
		  return sb.toString();
	 }

    /**
     *@Title:xmlToList
     *@Description:
     *@author  'zsc'
     *@date 2016年5月26日 下午3:25:16
     *@param xml
     *@return
     */
//	public static List xmlToList(String xml) {
//		try {
//			List<Map> list = new ArrayList<Map>();
//			Document document = DocumentHelper.parseText(xml);
//			Element nodesElement = document.getRootElement();
//			List nodes = nodesElement.elements();
//			for (Iterator its = nodes.iterator(); its.hasNext();) {
//				Element nodeElement = (Element) its.next();
//				Map map = xmlToMap(nodeElement.asXML());
//				list.add(map);
//				map = null;
//			}
//			nodes = null;
//			nodesElement = null;
//			document = null;
//			return list;
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		return null;
//	}
	/**
	 * 将json转化为实体POJO
	 * @param jsonStr
	 * @param obj
	 * @return
	 * @auth zhaohan
	 */
	public static<T> Object jsonToObj(String jsonStr,Class<T> obj) {
		T t = null;
		try {
			ObjectMapper objectMapper = new ObjectMapper();
			objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);//只转化实体类里面有的
			t = objectMapper.readValue(jsonStr,
					obj);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return t;
	}
}
