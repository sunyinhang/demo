package com.haiercash.core.http;

import com.haiercash.core.collection.iterator.CharSequenceIterable;
import com.haiercash.core.lang.Environment;

/**
 * Created by 许崇雷 on 2017-11-21.
 */
public final class HtmlUtils extends org.springframework.web.util.HtmlUtils {
    private static String htmlEscapeChar(char ch) {
        switch (ch) {
            case '<':
                return "&lt;";
            case '>':
                return "&gt;";
            case '"':
                return "&quot;";
            case '&':
                return "&amp;";
            case '\'':
                return "&#39;";
            case ' ':
            case '　':
                return "&nbsp;";
            default:
                return null;
        }
    }

    public static String htmlEscape(String input) {
        if (input == null)
            return null;
        StringBuilder builder = new StringBuilder(input.length() * 2);
        for (char ch : new CharSequenceIterable(input)) {
            String value = htmlEscapeChar(ch);
            builder.append(value == null ? ch : value);
        }
        String html = builder.toString();
        return html.replace(Environment.NewLine, "<br/>");
    }
}
