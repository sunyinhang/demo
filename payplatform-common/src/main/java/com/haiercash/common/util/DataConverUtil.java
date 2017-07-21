package com.haiercash.common.util;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.haiercash.commons.util.ResultHead;
import de.odysseus.staxon.json.JsonXMLConfig;
import de.odysseus.staxon.json.JsonXMLConfigBuilder;
import de.odysseus.staxon.json.JsonXMLInputFactory;
import de.odysseus.staxon.json.JsonXMLOutputFactory;
import de.odysseus.staxon.xml.util.PrettyXMLEventWriter;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.json.JSONObject;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.*;

/**
 * @author 'zsc'
 * @description 1、json-lib在xml转json时会将<key></key> 会转为{key:[]} 显示不符合要求，其次该jar已经停止更新,转化效率较低
 * 2、jackson在xml转json时弥补了json-lib的错误，但在<good></good><good></good>中不会按照json数组的结构，也不理想
 * 最后提供staxon的转换方式 详情可参考 http://blog.csdn.net/sdyy321/article/details/7024236/
 */
public class DataConverUtil {

    /**
     * @param jsonstr
     * @return
     * @Title:jsonToMap
     * @Description:
     * @author 'zsc'
     * @date 2016年5月26日 下午3:24:11
     */
    public static Map<String, Object> jsonToMap(String jsonstr) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
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
     * @param map
     * @return
     * @Title:mapToJson
     * @Description:
     * @author 'zsc'
     * @date 2016年5月26日 下午3:22:43
     */
    public static String mapToJson(Map<String, Object> map) {
        JSONObject jsonObject = new JSONObject(map);
        //JSONObject jsonObject = JSONObject.fromObject(map);
        String jsonStr = jsonObject.toString();
        return jsonStr;
    }

    /**
     * @param json
     * @return
     * @Title:jsonToXml
     * @Description:json转xml
     * @author 'zsc'
     * @date 2016年6月15日 下午3:07:51
     */
    public static String jsonToXml(String json) {
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

    public static String jsonToResponseXml(String json) {
        StringBuilder sb = new StringBuilder();
        sb.append("{response:").append(json).append("}");
        return jsonToXml(sb.toString());
    }

    /**
     * @param xml
     * @return
     * @Title:xmlToJson
     * @Description: xml转json
     * @author 'zsc'
     * @date 2016年6月15日 下午3:09:46
     */
    public static String xmlToJson(String xml) {
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
     * @param xml
     * @return
     * @Title:xmlToJsonObj
     * @Description:xml转jsonObject
     * @author 'zsc'
     * @date 2016年6月15日 下午3:11:55
     */
    public static JSONObject xmlToJsonObj(String xml) {
        //JSONObject jsonObject = JSONObject.fromObject(xmlToJson(xml));
        JSONObject jsonObject = new JSONObject(xmlToJson(xml));
        return jsonObject;
    }

    /**
     * @param xml
     * @return
     * @Title:xmlToMap
     * @Description:
     * @author 'zsc'
     * @date 2016年5月26日 下午3:25:47
     */
    public static Map<String, Object> xmlToMap(String xml) {
        String jsonstr = xmlToJson(xml);
        Map<String, Object> map = jsonToMap(jsonstr);
        return map;
    }

    /**
     * @param map
     * @param sb
     * @return
     * @Title:mapToXml
     * @Description:
     * @author 'zsc'
     * @date 2016年5月26日 下午3:25:23
     */
    public static String mapToXml(Map map, StringBuffer sb) {
        if (sb == null) {
            sb = new StringBuffer();
        }
        Set set = map.keySet();
        for (Iterator it = set.iterator(); it.hasNext(); ) {
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

    public static String mapToResponseXml(Map map, StringBuffer sb) {
        if (sb == null) {
            sb = new StringBuffer();
        }
        // 添加头
        sb.append("<?xml version=\\\"1.0\\\" encoding=\\\"utf-8\\\"?>");
        sb.append("<response>");
        sb.append("<head>");
        Set set = map.keySet();
        for (Iterator it = set.iterator(); it.hasNext(); ) {
            String key = (String) it.next();
            Object value = map.get(key);
            if (null == value) {
                value = "";
            }
            if (value instanceof HashMap) {
                sb.append("<" + key + ">");
                mapToXml((HashMap) value, sb);
                sb.append("</" + key + ">");
            } else if (value instanceof ResultHead) {
                sb.append("<retFlag>");
                sb.append(((ResultHead) value).getRetFlag());
                sb.append("</retFlag>");
                sb.append("<retMsg>");
                sb.append(((ResultHead) value).getRetMsg());
                sb.append("</retMsg>");
            } else {
                sb.append("<" + key + ">" + value + "</" + key + ">");
            }
        }
        sb.append("</head>");
        sb.append("</response>");
        return sb.toString();
    }

    /**
     * @param xml
     * @return
     * @Title:xmlToList
     * @Description:
     * @author 'zsc'
     * @date 2016年5月26日 下午3:25:16
     */
    public static List xmlToList(String xml) {
        try {
            List<Map> list = new ArrayList<Map>();
            Document document = DocumentHelper.parseText(xml);
            Element nodesElement = document.getRootElement();
            List nodes = nodesElement.elements();
            for (Iterator its = nodes.iterator(); its.hasNext(); ) {
                Element nodeElement = (Element) its.next();
                Map map = xmlToMap(nodeElement.asXML());
                list.add(map);
                map = null;
            }
            nodes = null;
            nodesElement = null;
            document = null;
            return list;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     *@Title:bulidCorerequestXml
     *@Description:
     *@author  'zsc'
     *@date 2016年6月13日 下午5:41:10
     *@param body
     *@param head
     *@return
     */
//	public String bulidCorerequestXml(Object body, Object head) {
//		CoreBusinessRequest request = new CoreBusinessRequest();
//		request.setBody(body);
//		request.setHead(head);
//		XStream stream = new XStream();
//		stream.autodetectAnnotations(true);// 打开注解
//		stream.aliasSystemAttribute(null, "class");// 去掉class属性
//		StringBuffer sbf = new StringBuffer();
//		sbf.append("<?xml version=\"1.0\" encoding=\"utf-8\" ?>");
//		String xml = stream.toXML(request).replaceAll("__","_");//xstream转换双下划线BUG解决
//		sbf.append(xml);
//		return sbf.toString();
//	}
    /**
     * @Title requestToString
     * DESCRIPTION: request 转 String
     * @author yu jianwei
     */
    /*public static String requestToString(HttpServletRequest request) throws Exception{
		StringBuffer json = new StringBuffer();
		String line = null;
		BufferedReader jsonreader = request.getReader();
		while ((line = jsonreader.readLine()) != null) {
			json.append(line);
		}
		String json_ = URLDecoder.decode(json.toString(), "UTF-8");
		//JSONObject jsons = JSONObject.fromObject(json_);
		JSONObject jsons = new JSONObject(json_);
		jsons.put("ip", SysUtil.getRealIp(request));
		return jsons.toString();
	}*/
//	public static void main(String args[]) throws Exception{
////		String str = "<?xml version=\"1.0\" encoding=\"utf-8\"?>    "
////				+"<request>                                 "
////				+"  <head>                                  "
////				+"    <tradeCode>100022</tradeCode>         "
////				+"    <serno>20160615091410975</serno>      "
////				+"    <sysFlag>11</sysFlag>                 "
////				+"    <tradeType/>                          "
////				+"    <tradeDate>2016-06-15</tradeDate>     "
////				+"    <tradeTime>09:14:10</tradeTime>       "
////				+"    <channelNo/>                          "
////				+"    <cooprCode/>                          "
////				+"  </head>                                 "
////				+"  <body>                                  "
////				+"    <msgTyp>01</msgTyp>                   "
////				+"    <applSeq>956309</applSeq>             "
////				+"    <idNo>371522199102140532</idNo>       "
////				+"    <outSts>01</outSts>                   "
////                + "<good>"
////                + "<brand>海尔</brand>"
////                + "<price>9999</price>"
////                + "</good>"
////                + "<good>"
////                + "<brand>华夏</brand>"
////                + "<price>8888</price>"
////                + "</good>"
////				+"  </body>                                 "
////				+"</request>                                ";
//
//		String str = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>                    "
//		+"<request>                                                                 "
//		+"<head>                                                                    "
//		+"<tradeCode>100001</tradeCode>                                             "
//		+"<serno>14633927730006000000</serno>                                       "
//		+"<sysFlag>15</sysFlag>                                                     "
//		+"<tradeType>1</tradeType>                                                  "
//		+"<tradeDate>2016-05-16</tradeDate>                                         "
//		+"<tradeTime>17:59:33</tradeTime>                                           "
//		+"<channelNo>15</channelNo>                                                 "
//		+"<cooprCode/>                                                              "
//		+"<autoFlag>N</autoFlag>                                                    "
//		+"</head>                                                                   "
//		+"<body>                                                                    "
//		+"<id_typ>20</id_typ>                                                       "
//		+"<id_no>320305198809110028</id_no>                                         "
//		+"<cust_name>殷梦茹</cust_name>                                                "
//		+"<coopr_name>南京华美美容医院</coopr_name>                                         "
//		+"<coopr_cde>902016000145</coopr_cde>                                       "
//		+"<typ_seq>4105512</typ_seq>                                                "
//		+"<typ_cde>16028a</typ_cde>                                                 "
//		+"<typ_grp>02</typ_grp>                                                     "
//		+"<typ_desc>6期0.94</typ_desc>                                               "
//		+"<apply_dt>2016-05-16</apply_dt>                                           "
//		+"<crt_dt>2016-05-16</crt_dt>                                               "
//		+"<crt_usr>YF0160</crt_usr>                                                 "
//		+"<indiv_mobile>13945710056</indiv_mobile>                                  "
//		+"<goodsList> </goodsList>                                                  "
//		+"<pro_pur_amt>1000.00</pro_pur_amt>                                        "
//		+"<fst_pct>0.00</fst_pct>                                                   "
//		+"<fst_pay>0.00</fst_pay>                                                   "
//		+"<apply_amt>1000.00</apply_amt>                                            "
//		+"<apply_tnr>6</apply_tnr>                                                  "
//		+"<apply_tnr_typ>6</apply_tnr_typ>                                          "
//		+"<purpose>OTH</purpose>                                                    "
//		+"<other_purpose>医疗</other_purpose>                                         "
//		+"<month_repay/>                                                            "
//		+"<oper_goods_typ>Y</oper_goods_typ>                                        "
//		+"<mtd_cde>LT001</mtd_cde>                                                  "
//		+"<loan_freq>1M</loan_freq>                                                 "
//		+"<mtd_mode/>                                                               "
//		+"<repc_opt/>                                                               "
//		+"<price_int_rat/>                                                          "
//		+"<due_day_opt/>                                                            "
//		+"<due_day/>                                                                "
//		+"<doc_channel>DOCLIST001</doc_channel>                                     "
//		+"<appl_ac_typ>02</appl_ac_typ>                                             "
//		+"<appl_ac_nam/>                                                            "
//		+"<appl_card_no/>                                                           "
//		+"<acc_bank_cde/>                                                           "
//		+"<acc_bank_name/>                                                          "
//		+"<ac_province/>                                                            "
//		+"<acCity/>                                                                 "
//		+"<appl_ac_bch/>                                                            "
//		+"<repay_appl_ac_nam>殷梦茹</repay_appl_ac_nam>                                "
//		+"<repay_appl_card_no>6212261001048382441</repay_appl_card_no>              "
//		+"<repay_acc_bank_cde>102</repay_acc_bank_cde>                              "
//		+"<repay_acc_bank_name>中国工商银行上海市石门一路支行</repay_acc_bank_name>                "
//		+"<repay_ac_province>上海</repay_ac_province>                                 "
//		+"<repay_ac_city>上海市</repay_ac_city>                                        "
//		+"<saler_cde>YF0160</saler_cde>                                             "
//		+"<saler_name>无</saler_name>                                                "
//		+"<saler_mobile>13945710056</saler_mobile>                                  "
//		+"<crd_flag>Y</crd_flag>                                                    "
//		+"<apptList>                                                                "
//		+"<appt>                                                                    "
//		+"<appt_typ>01</appt_typ>                                                   "
//		+"<appt_relation/>                                                          "
//		+"<appt_id_typ>20</appt_id_typ>                                             "
//		+"<appt_id_no>320305198809110028</appt_id_no>                               "
//		+"<appt_cust_name>殷梦茹</appt_cust_name>                                      "
//		+"<appt_indiv_sex>20</appt_indiv_sex>                                       "
//		+"<appt_start_date>1988-9-11</appt_start_date>                              "
//		+"<indiv_marital>20</indiv_marital>                                         "
//		+"<indiv_edu>10</indiv_edu>                                                 "
//		+"<appt_reg_province>320000</appt_reg_province>                             "
//		+"<appt_reg_city>320300</appt_reg_city>                                     "
//		+"<live_info>50</live_info>                                                 "
//		+"<live_province>310000</live_province>                                     "
//		+"<live_city>310000</live_city>                                             "
//		+"<live_area>无</live_area>                                                  "
//		+"<live_addr>罗阳路568弄21号404</live_addr>                                      "
//		+"<ppty_province/>                                                          "
//		+"<ppty_city/>                                                              "
//		+"<ppty_area>无</ppty_area>                                                  "
//		+"<ppty_addr/>                                                              "
//		+"<ppty_mj/>                                                                "
//		+"<indiv_mobile>18217333556</indiv_mobile>                                  "
//		+"<local_resid>10</local_resid>                                             "
//		+"<live_year>0</live_year>                                                  "
//		+"<indiv_dep_no>1</indiv_dep_no>                                            "
//		+"<annual_earn>8000.00</annual_earn>                                        "
//		+"<position_opt>10</position_opt>                                           "
//		+"<indiv_mth_inc>8000.00</indiv_mth_inc>                                    "
//		+"<study_mth/>                                                              "
//		+"<studying_deg/>                                                           "
//		+"<school_name/>                                                            "
//		+"<study_major/>                                                            "
//		+"<school_kind>01</school_kind>                                             "
//		+"<school_leng>OTH</school_leng>                                            "
//		+"<geade/>                                                                  "
//		+"<emp_reg_name/>                                                           "
//		+"<emp_reg_dt/>                                                             "
//		+"<manage_addr/>                                                            "
//		+"<manage_province/>                                                        "
//		+"<manage_city/>                                                            "
//		+"<emp_reg_rel_tel/>                                                        "
//		+"<emp_reg_num/>                                                            "
//		+"<indiv_opt/>                                                              "
//		+"<manage_typ>04</manage_typ>                                               "
//		+"<manage_main_biz/>                                                        "
//		+"<mth_turnover/>                                                           "
//		+"<manage_no/>                                                              "
//		+"<pur_sale_cont_no/>                                                       "
//		+"<indiv_emp_name>上海豆子金融信息服务有限公司</indiv_emp_name>                           "
//		+"<indiv_branch>市场部</indiv_branch>                                          "
//		+"<indiv_emp_typ>D</indiv_emp_typ>                                          "
//		+"<indiv_emp_yrs>5</indiv_emp_yrs>                                          "
//		+"<indiv_emp_province>310000</indiv_emp_province>                           "
//		+"<indiv_emp_city>310000</indiv_emp_city>                                   "
//		+"<empaddr>居里路99号</empaddr>                                                 "
//		+"<indiv_emp_zone/>                                                         "
//		+"<indiv_emp_tel>021-68913080</indiv_emp_tel>                               "
//		+"<indiv_emp_tel_sub/>                                                      "
//		+"<indiv_position>01</indiv_position>                                       "
//		+"<spouse_name>马志龙</spouse_name>                                            "
//		+"<spouse_id_no/>                                                           "
//		+"<spouse_emp>上海乾创信息服务有限公司</spouse_emp>                                     "
//		+"<spouse_mobile>13482252690</spouse_mobile>                                "
//		+"<relList>                                                                 "
//		+"<rel>                                                                     "
//		+"<rel_name>鲁凤华</rel_name>                                                  "
//		+"<rel_relation>01</rel_relation>                                           "
//		+"<rel_addr>无</rel_addr>                                                    "
//		+"<rel_mobile>13952153220</rel_mobile>                                      "
//		+"</rel>                                                                    "
//		+"<rel>                                                                     "
//		+"<rel_name>鲁凤华111</rel_name>                                                  "
//		+"<rel_relation>0111</rel_relation>                                           "
//		+"<rel_addr>无1111</rel_addr>                                                    "
//		+"<rel_mobile>13952153220</rel_mobile>                                      "
//		+"</rel>                                                                    "
//		+"</relList>                                                                "
//		+"<ppty_live_province>无</ppty_live_province>                                "
//		+"<ppty_live_city>无</ppty_live_city>                                        "
//		+"<ppty_live_area>无</ppty_live_area>                                        "
//		+"<ppty_live_addr/>                                                         "
//		+"<ppty_righ_name>殷梦茹</ppty_righ_name>                                      "
//		+"<ppty_amt/>                                                               "
//		+"<ppty_loan_ind/>                                                          "
//		+"<ppty_loan_amt>2500.00</ppty_loan_amt>                                    "
//		+"</appt>                                                                   "
//		+"</apptList>                                                               "
//		+"</body>                                                                   "
//		+"</request>                                                                ";
//
//		System.out.println(str);
//		JSONObject obj = xmlToJsonObj(str);
////		JSONObject objre = obj.getJSONObject("request");
////		JSONObject objhead = objre.getJSONObject("head");
////		String chanenlNo = objhead.getString("channelNo");
////		System.out.println("chanenlNo="+chanenlNo);
////		System.out.println("chanenlNo="+chanenlNo==null);
////		System.out.println(SysUtil.stringIsEmpty(chanenlNo));
//		//System.out.println("历史："+xmlToJson1(str));
//
//
//		System.out.println("现在："+obj.toString());
//		Map<String, Object> map = xmlToMap(str);
//		System.out.println("现在xmlToMap："+map);
////		System.out.println(xmlToJson(str));
//		//System.out.println(obj.toString());
//	}

	/*public static String xmlToJson1(String xml) {
		XMLSerializer xmlSerializer = new XMLSerializer();
		return xmlSerializer.read(xml).toString();
	}*/
}
