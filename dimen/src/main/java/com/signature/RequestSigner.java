package com.signature;


import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.SignatureException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.crypto.Mac;
import org.apache.http.Header;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URLEncodedUtils;

public class RequestSigner {
    private static final String ENCODING = "UTF-8";
    private static final String HMAC_SHA256_ALGORITHM = "HmacSHA256";
    private static final String HEADER_PREFIX = "sm-";
    private static final String HEADER_NAME_NONCE = "sm-nonce";
    private static final String HEADER_NAME_TIMESTAMP = "sm-timestamp";
    private static final String HEADER_NAME_SIGNATURE = "sm-signature";
    private static final String HEADER_NAME_APPKET = "sm-appkey";

    public RequestSigner() {
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

    private static long generateTimestamp() {
        return System.currentTimeMillis() / 1000L;
    }

    private static String generateNonce() {
        return RandomStringUtils.randomAlphanumeric(32);
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

    private static String getHttpRequestMethod(HttpUriRequest httpUriRequest) {
        return httpUriRequest.getMethod().toUpperCase();
    }

    private static String getCanonicalizedRequestPath(HttpUriRequest httpUriRequest) {
        String path = httpUriRequest.getURI().getPath();
        if (StringUtils.isEmpty(path)) {
            path = "/";
        }

        return path;
    }

    private static String getCanonicalizedHeaderString(HttpUriRequest httpUriRequest) {
        List<String> headerStrings = new ArrayList();
        Header[] headers = httpUriRequest.getAllHeaders();
        Header[] arr$ = headers;
        int len$ = headers.length;

        for(int i$ = 0; i$ < len$; ++i$) {
            Header header = arr$[i$];
            if (header.getName().startsWith("sm-")) {
                headerStrings.add(new String(header.getName().toLowerCase() + ":" + header.getValue().trim()));
            }
        }

        Collections.sort(headerStrings);
        return StringUtils.join(headerStrings, "\n");
    }

    private static String getCanonicalizedQueryString(HttpUriRequest httpUriRequest) {
        try {
            List<String> parameterStrings = new ArrayList();
            List<NameValuePair> queryParameters = SummallURLEncodedUtils.parse(httpUriRequest.getURI(), "UTF-8");
            Iterator i$ = queryParameters.iterator();

            while(i$.hasNext()) {
                NameValuePair nameValuePair = (NameValuePair)i$.next();
                parameterStrings.add(getPercentEncodedParameterString(nameValuePair));
            }

            if (httpUriRequest instanceof HttpEntityEnclosingRequestBase) {
                HttpEntityEnclosingRequestBase httpEntityEnclosingRequest = (HttpEntityEnclosingRequestBase)httpUriRequest;
                List<NameValuePair> entityParameters = URLEncodedUtils.parse(httpEntityEnclosingRequest.getEntity());
                if (entityParameters.size() > 0) {
                    Iterator i$1 = entityParameters.iterator();

                    while(i$1.hasNext()) {
                        NameValuePair nameValuePair = (NameValuePair)i$1.next();
                        parameterStrings.add(getPercentEncodedParameterString(nameValuePair));
                    }
                }
            }

            Collections.sort(parameterStrings);
            return StringUtils.join(parameterStrings, "&");
        } catch (Exception var7) {
            throw new RuntimeException("获取规格化查询参数字符串出现异常", var7);
        }
    }

    private static String getPercentEncodedParameterString(NameValuePair nameValuePair) {
        return percentEncode(nameValuePair.getName()) + "=" + percentEncode(nameValuePair.getValue());
    }

    private static void preSign(HttpUriRequest httpUriRequest, String appKey) {
        httpUriRequest.setHeader("sm-appkey", appKey);
        httpUriRequest.setHeader("sm-nonce", generateNonce());
        httpUriRequest.setHeader("sm-timestamp", String.valueOf(generateTimestamp()));
    }

    public static void sign(HttpUriRequest httpUriRequest, String appKey, String appSecret) {
        try {
            preSign(httpUriRequest, appKey);
            String baseString = getHttpRequestMethod(httpUriRequest) + "\n" + getCanonicalizedRequestPath(httpUriRequest) + "\n" + getCanonicalizedQueryString(httpUriRequest) + "\n" + getCanonicalizedHeaderString(httpUriRequest);
            String signature = calculateRFC2104HMAC(baseString, appSecret);
            httpUriRequest.addHeader("sm-signature", signature);
        } catch (Exception var5) {
            throw new RuntimeException("签名GET请求出现异常", var5);
        }
    }
}
