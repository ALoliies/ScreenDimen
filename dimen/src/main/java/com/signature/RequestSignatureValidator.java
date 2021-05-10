package com.signature;


import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.security.SignatureException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServletRequest;

public class RequestSignatureValidator {
    private static final String ENCODING = "UTF-8";
    private static final Charset CHARSET = Charset.forName("UTF-8");
    private static final String FORM_CONTENT_TYPE = "application/x-www-form-urlencoded";
    private static final String HMAC_SHA256_ALGORITHM = "HmacSHA256";
    private static final String HEADER_PREFIX = "sm-";
    private static final String HEADER_NAME_NONCE = "sm-nonce";
    private static final String HEADER_NAME_TIMESTAMP = "sm-timestamp";
    private static final String HEADER_NAME_SIGNATURE = "sm-signature";
    private static final String HEADER_NAME_APPKET = "sm-appkey";

    public RequestSignatureValidator() {
    }

    private static String calculateRFC2104HMAC(String data, String key) throws SignatureException {
        String result = null;

        try {
            SecretKeySpec signingKey = new SecretKeySpec(key.getBytes("UTF-8"), "HmacSHA256");
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(signingKey);
            byte[] rawHmac = mac.doFinal(data.getBytes("UTF-8"));
            result = Base64.encodeBase64String(rawHmac);
            return result;
        } catch (Exception var6) {
            throw new SignatureException("Failed to generate HMAC : " + var6.getMessage());
        }
    }

    private static String percentEncode(String s) {
        if (s == null) {
            return "";
        } else {
            try {
                return URLEncoder.encode(s, "UTF-8").replace("+", "%20").replace("*", "%2A").replace("%7E", "~");
            } catch (UnsupportedEncodingException var2) {
                throw new RuntimeException(var2.getMessage(), var2);
            }
        }
    }

    private static String getHttpRequestMethod(HttpServletRequest httpServletRequest) {
        return httpServletRequest.getMethod().toUpperCase();
    }

    private static String getCanonicalizedRequestPath(HttpServletRequest httpServletRequest) {
        String path = httpServletRequest.getRequestURI();
        if (StringUtils.isEmpty(path)) {
            path = "/";
        }

        return path;
    }

    private static String getCanonicalizedHeaderString(HttpServletRequest httpServletRequest) {
        List<String> headerStrings = new ArrayList();
        headerStrings.add(getHeaderString(httpServletRequest, "sm-appkey"));
        headerStrings.add(getHeaderString(httpServletRequest, "sm-timestamp"));
        headerStrings.add(getHeaderString(httpServletRequest, "sm-nonce"));
        Collections.sort(headerStrings);
        return StringUtils.join(headerStrings, "\n");
    }

    private static String getHeaderString(HttpServletRequest httpServletRequest, String headerName) {
        return headerName.toLowerCase() + ":" + retrieveHeaderValue(httpServletRequest, headerName);
    }

    private static String retrieveHeaderValue(HttpServletRequest httpServletRequest, String headerName) {
        String headerValue = httpServletRequest.getHeader(headerName);
        if (StringUtils.isEmpty(headerValue)) {
            throw new IllegalArgumentException(headerName + "缺失");
        } else {
            return headerValue.trim();
        }
    }

    private static String getCanonicalizedQueryString(HttpServletRequest httpServletRequest) {
        try {
            List<String> parameterStrings = new ArrayList();
            String contentType = httpServletRequest.getContentType();
            Iterator i$;
            if (contentType != null && httpServletRequest.getContentType().startsWith("application/x-www-form-urlencoded")) {
                Map<String, String[]> parameters = httpServletRequest.getParameterMap();
                i$ = parameters.entrySet().iterator();

                while(i$.hasNext()) {
                    Entry<String, String[]> entry = (Entry)i$.next();
                    String[] arr$ = (String[])entry.getValue();
                    int len$ = arr$.length;

                    for(int i$1 = 0; i$1 < len$; ++i$1) {
                        String value = arr$[i$1];
                        parameterStrings.add(getPercentEncodedParameterString((String)entry.getKey(), value));
                    }
                }
            } else {
                List<NameValuePair> queryParameters = URLEncodedUtils.parse(httpServletRequest.getQueryString(), CHARSET);
                i$ = queryParameters.iterator();

                while(i$.hasNext()) {
                    NameValuePair nameValuePair = (NameValuePair)i$.next();
                    parameterStrings.add(getPercentEncodedParameterString(nameValuePair.getName(), nameValuePair.getValue()));
                }
            }

            Collections.sort(parameterStrings);
            return StringUtils.join(parameterStrings, "&");
        } catch (Exception var10) {
            throw new RuntimeException("获取规格化查询参数字符串出现异常", var10);
        }
    }

    private static String getPercentEncodedParameterString(String name, String value) {
        return percentEncode(name) + "=" + percentEncode(value);
    }

    public static boolean validate(HttpServletRequest httpServletRequest, String appSecret) {
        try {
            String signature = retrieveHeaderValue(httpServletRequest, "sm-signature");
            String baseString = getHttpRequestMethod(httpServletRequest) + "\n" + getCanonicalizedRequestPath(httpServletRequest) + "\n" + getCanonicalizedQueryString(httpServletRequest) + "\n" + getCanonicalizedHeaderString(httpServletRequest);
            String signatureComputed = calculateRFC2104HMAC(baseString, appSecret);
            return StringUtils.equals(signature, signatureComputed);
        } catch (SignatureException var5) {
            throw new RuntimeException("签名出现异常", var5);
        }
    }
}
