/*
 * 功  能：简单说明该类的功能
 *
 * 文件名：XmlUtilTool.java
 *
 * 描  述：
 *
 * [变更履历]
 * Version   变更日         部署              作者           变更内容
 * -----------------------------------------------------------------------------
 * V1.00     2015年5月29日   haiercash    xuchao      CREATE
 * -----------------------------------------------------------------------------
 *
 *
 * Copyright (c) 2015 haiercash All Rights Reserved.
 *┌─────────────────────────────────────────────────—────┐
 *│ 版权声明                               haiercash       │
 *└──────────────────────────────—————————————————————───┘
 */

package com.haiercash.payplatform.utils;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class XmlUtilTool {
    /**
     * DESCRIPTION:将xml转换为map
     *
     * @param xmlStr
     * @return Map<String,Object>
     * @throws Exception
     * @throws
     * @author xuchao
     * @date 2015年6月1日
     * xmlStrToMap 方法
     */
    public static Map<String, Object> xmlStrToMap(String xmlStr) throws Exception {
        if (StringUtils.isEmpty(xmlStr)) {
            return null;
        }
        Map<String, Object> map = new HashMap<>();
        //将xml格式的字符串转换成Document对象
        Document doc = DocumentHelper.parseText(xmlStr);
        //获取根节点
        Element root = doc.getRootElement();
        //获取根节点下的所有元素
        List children = root.elements();
        //循环所有子元素
        if (children != null && children.size() > 0) {
            for (Object aChildren : children) {
                Element child = (Element) aChildren;
                map.put(child.getName(), child.getTextTrim());
            }
        }
        return map;
    }

    /**
     * 一个迭代方法
     *
     * @param element : org.jdom.Element
     * @return java.util.Map 实例
     */
    @SuppressWarnings("unchecked")
    public static Map iterateElement(Element element) {
        List jiedian = element.elements();
        Element et;
        Map obj = new HashMap();
        List list;
        for (Object aJiedian : jiedian) {
            list = new ArrayList();
            et = (Element) aJiedian;
            if (et.getTextTrim().equals("")) {
                if (et.elements().size() == 0)
                    continue;
                if (obj.containsKey(et.getName())) {
                    list = (List) obj.get(et.getName());
                }
                list.add(iterateElement(et));
                obj.put(et.getName(), list);
            } else {
                if (obj.containsKey(et.getName())) {
                    list = (List) obj.get(et.getName());
                }
                list.add(et.getTextTrim());
                obj.put(et.getName(), list);
            }
        }
        return obj;
    }


    /**
     * DESCRIPTION:初始化
     *
     * @param element
     * @return Map
     * @throws
     * @author xuchao
     * @date 2015年6月11日
     * iterateElement 方法
     */
    public static void initElement(Element element) {
        List jiedian = element.elements();
        Element et;
        for (Object aJiedian : jiedian) {
            et = (Element) aJiedian;
            et.setText("");
            initElement(et);
        }
    }

    /**
     * DESCRIPTION:去掉首行字符串
     *
     * @param fileName
     * @return String
     * @throws
     * @author xuchao
     * @date 2015年6月1日
     * readFileByLines 方法
     */
    public static String readFileByLines(String fileName) {
        File file = new File(fileName);
        BufferedReader reader = null;
        try {
            System.out.println("以行为单位读取文件内容，一次读一整行：");
            reader = new BufferedReader(new FileReader(file));
            String tempString;
            StringBuilder sb = new StringBuilder();
            int line = 1;
            // 一次读入一行，直到读入null为文件结束
            while ((tempString = reader.readLine()) != null) {
                // 显示行号
                if (line > 1) {
                    sb.append(tempString);
                }
                line++;
            }
            reader.close();
            return sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ignored) {
                }
            }
        }
        return null;
    }

}
