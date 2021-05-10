package com.signature;


import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

public class SummallURLEncodedUtils {
    public static final String CONTENT_TYPE = "application/x-www-form-urlencoded";
    private static final String PARAMETER_SEPARATOR = "&";
    private static final String NAME_VALUE_SEPARATOR = "=";

    public SummallURLEncodedUtils() {
    }

    public static List<NameValuePair> parse(URI uri, String encoding) {
        String query = uri.getRawQuery();
        if (query != null && query.length() > 0) {
            List<NameValuePair> result = new ArrayList();
            Scanner scanner = new Scanner(query);
            parse(result, scanner, encoding);
            return result;
        } else {
            return Collections.emptyList();
        }
    }

    public static void parse(List<NameValuePair> parameters, Scanner scanner, String charset) {
        scanner.useDelimiter("&");

        String name;
        String value;
        for(; scanner.hasNext(); parameters.add(new BasicNameValuePair(name, value))) {
            name = null;
            value = null;
            String token = scanner.next();
            int i = token.indexOf("=");
            if (i != -1) {
                name = decode(token.substring(0, i).trim(), charset);
                value = decode(token.substring(i + 1).trim(), charset);
            } else {
                name = decode(token.trim(), charset);
            }
        }

    }

    private static String decode(String content, String charset) {
        if (content == null) {
            return null;
        } else {
            try {
                return URLDecoder.decode(content, charset != null ? charset : HTTP.DEF_CONTENT_CHARSET.name());
            } catch (UnsupportedEncodingException var3) {
                throw new IllegalArgumentException(var3);
            }
        }
    }
}
