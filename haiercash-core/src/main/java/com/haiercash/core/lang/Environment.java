package com.haiercash.core.lang;

import java.io.File;

/**
 * Created by 许崇雷 on 2016/7/9.
 */
public final class Environment {
    /**
     * 获取"."字符串
     */
    public static final String Dot = ".";
    /**
     * 获取"."字符
     */
    public static final char DotChar = '.';
    /**
     * 获取"/"字符串
     */
    public static final String Slash = "/";
    /**
     * 获取'/'字符
     */
    public static final char SlashChar = '/';
    /**
     * 获取"\"字符串
     */
    public static final String BackSlash = "\\";
    /**
     * 获取"\"字符
     */
    public static final char BackSlashChar = '\\';
    /**
     * 获取 "\r" 字符串
     */
    public static final String CR = "\r";
    /**
     * 获取 "\r" 字符
     */
    public static final char CRChar = '\r';
    /**
     * 获取 "\n" 字符串
     */
    public static final String LF = "\n";
    /**
     * 获取 "\n" 字符
     */
    public static final char LFChar = '\n';
    /**
     * 获取 "\r\n" 字符串
     */
    public static final String CRLF = "\r\n";
    /**
     * 获取当前系统路径分隔字符串
     */
    public static final String FileSeparator = File.separator;
    /**
     * 获取当前系统路径分割字符
     */
    public static final char FileSeparatorChar = File.separatorChar;
    /**
     * 获取当前系统换行字符串
     */
    public static final String NewLine = System.getProperty("line.separator");
}
