package com.haiercash.appserver.util.sign.ca;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.POIXMLDocument;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.usermodel.Range;
import org.apache.poi.xwpf.usermodel.*;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author wei.jia
 * @Created on 2014-10-28
 */
public class ReplaceVarInWordImpl {
	private static Log logger = LogFactory.getLog(ReplaceVarInWordImpl.class);

	// 返回Docx中需要替换的特殊字符，没有重复项
	// 推荐传入正则表达式参数"\\$\\{[^{}]+\\}"
	public ArrayList<String> getReplaceElementsInWord(String filePath, String regex) {
		String[] p = filePath.split("\\.");
		if (p.length > 0) { // 判断文件有无扩展名
			// 比较文件扩展名
			if (p[p.length - 1].equalsIgnoreCase("doc")) {
				ArrayList<String> al = new ArrayList<String>();
				File file = new File(filePath);
				HWPFDocument document = null;
				try {
					InputStream is = new FileInputStream(file);
					document = new HWPFDocument(is);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				Range range = document.getRange();
				String rangeText = range.text();
				CharSequence cs = rangeText.subSequence(0, rangeText.length());
				Pattern pattern = Pattern.compile(regex);
				Matcher matcher = pattern.matcher(cs);
				int startPosition = 0;
				while (matcher.find(startPosition)) {
					if (!al.contains(matcher.group())) {
						al.add(matcher.group());
					}
					startPosition = matcher.end();
				}
				return al;
			} else if (p[p.length - 1].equalsIgnoreCase("docx")) {
				ArrayList<String> al = new ArrayList<String>();
				XWPFDocument document = null;
				try {
					document = new XWPFDocument(POIXMLDocument.openPackage(filePath));
				} catch (IOException e) {
					e.printStackTrace();
				}
				// 遍历段落
				Iterator<XWPFParagraph> itPara = document.getParagraphsIterator();
				while (itPara.hasNext()) {
					XWPFParagraph paragraph = (XWPFParagraph) itPara.next();
					String paragraphString = paragraph.getText();
					CharSequence cs = paragraphString.subSequence(0, paragraphString.length());
					Pattern pattern = Pattern.compile(regex);
					Matcher matcher = pattern.matcher(cs);
					int startPosition = 0;
					while (matcher.find(startPosition)) {
						if (!al.contains(matcher.group())) {
							al.add(matcher.group());
						}
						startPosition = matcher.end();
					}
				}
				// 遍历表
				Iterator<XWPFTable> itTable = document.getTablesIterator();
				while (itTable.hasNext()) {
					XWPFTable table = (XWPFTable) itTable.next();
					int rcount = table.getNumberOfRows();
					for (int i = 0; i < rcount; i++) {
						XWPFTableRow row = table.getRow(i);
						List<XWPFTableCell> cells = row.getTableCells();
						for (XWPFTableCell cell : cells) {
							String cellText = "";
							cellText = cell.getText();
							CharSequence cs = cellText.subSequence(0, cellText.length());
							Pattern pattern = Pattern.compile(regex);
							Matcher matcher = pattern.matcher(cs);
							int startPosition = 0;
							while (matcher.find(startPosition)) {
								if (!al.contains(matcher.group())) {
									al.add(matcher.group());
								}
								startPosition = matcher.end();
							}
						}
					}
				}
				return al;
			} else {
				return null;
			}
		} else {
			return null;
		}
	}


    /**
     * 替换word中需要替换的特殊字符
     * 由于poi包的变化，针对docx与doc分别处理
     * @param srcPath 源文件地址
     * @param destPath 目的生成文件地址
     * @param map 要替换的键值对
     * @return boolean
     */
	public static boolean replaceAndGenerateWord(String srcPath, String destPath, Map<String, String> map) {
		String[] sp = srcPath.split("\\.");
		String[] dp = destPath.split("\\.");

		// logger.debug("---------------------srcPath="+srcPath+",destPath="+destPath+",sp="+sp+",dp="+dp);
		if ((sp.length > 0) && (dp.length > 0)) {// 判断文件有无扩展名
			// 比较文件扩展名
			if (sp[sp.length - 1].equalsIgnoreCase("docx")) {
				try {
					logger.debug("参数转换:srcPath=" + srcPath);
					XWPFDocument document = new XWPFDocument(POIXMLDocument.openPackage(srcPath));
					// 替换段落中的指定文字
					Iterator<XWPFParagraph> itPara = document.getParagraphsIterator();
					while (itPara.hasNext()) {
						XWPFParagraph paragraph = itPara.next();
						List<XWPFRun> runs = paragraph.getRuns();
						for (int i = 0; i < runs.size(); i++) {
							String oneparaString = runs.get(i).getText(runs.get(i).getTextPosition());
							for (Map.Entry<String, String> entry : map.entrySet()) {
								if (oneparaString != null) {
									oneparaString = oneparaString.replace(entry.getKey(), entry.getValue());
								}

							}
							runs.get(i).setText(oneparaString, 0);
						}
					}

					// 替换表格中的指定文字
					Iterator<XWPFTable> itTable = document.getTablesIterator();
					while (itTable.hasNext()) {
						XWPFTable table = itTable.next();
						int rcount = table.getNumberOfRows();
						for (int i = 0; i < rcount; i++) {
							XWPFTableRow row = table.getRow(i);
							List<XWPFTableCell> cells = row.getTableCells();
							for (XWPFTableCell cell : cells) {
								String cellTextString = cell.getText();
								for (Entry<String, String> e : map.entrySet()) {
									if (cellTextString.contains(e.getKey()))
										cellTextString = cellTextString.replace(e.getKey(), e.getValue());
								}
								cell.removeParagraph(0);
								cell.setText(cellTextString);
							}
						}
					}
					FileOutputStream outStream = new FileOutputStream(destPath);
					document.write(outStream);
					outStream.close();
					return true;
				} catch (Exception e) {
					e.printStackTrace();
					return false;
				}

			} else {
				// doc只能生成doc，如果生成docx会出错
				if ((sp[sp.length - 1].equalsIgnoreCase("doc")) && (dp[dp.length - 1].equalsIgnoreCase("doc"))) {
					HWPFDocument document = null;
					try {
						document = new HWPFDocument(new FileInputStream(srcPath));
						Range range = document.getRange();
						for (Map.Entry<String, String> entry : map.entrySet()) {
							range.replaceText(entry.getKey(), entry.getValue());
						}
						FileOutputStream outStream = new FileOutputStream(destPath);
						document.write(outStream);
						outStream.close();
						return true;
					} catch (FileNotFoundException e) {
						e.printStackTrace();
						return false;
					} catch (IOException e) {
						e.printStackTrace();
						return false;
					}
				} else {
					return false;
				}

			}

		} else {
			return false;
		}
	}

}