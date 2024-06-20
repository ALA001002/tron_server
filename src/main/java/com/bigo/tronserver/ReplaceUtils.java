package com.bigo.tronserver;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReplaceUtils {
    private final static String REGREX = "[\\u200b-\\u200f]|[\\u200e-\\u200f]|[\\u202a-\\u202e]|[\\u2066-\\u2069]|\ufeff|\u06ec";

    private final static String BLANK = "";

    public static String replaceAll(String source) {
        return replaceAll(source, REGREX, BLANK);
    }

    public static String replaceAll(String source, String regex, String replacement) {
        if (regex == null) {
            regex = REGREX;
        }
        if (replacement == null) {
            replacement = BLANK;
        }
        Pattern compile = Pattern.compile(regex);
        Matcher matcher = compile.matcher(source);
        if (matcher.find()) {
            return matcher.replaceAll(replacement);
        }
        return source;
    }
}
