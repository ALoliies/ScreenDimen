package com.signature;


import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;

import java.net.URI;

public class MobileSignatureServiceImpl implements MobileSignatureService {
    private static final String HEADER_PREFIX = "sm-";
    private static final String HEADER_NAME_NONCE = "sm-nonce";
    private static final String HEADER_NAME_TIMESTAMP = "sm-timestamp";
    private static final String HEADER_NAME_SIGNATURE = "sm-signature";
    private static final String HEADER_NAME_APPKET = "sm-appkey";

    public MobileSignatureServiceImpl() {
    }

    public String getResultByUriAndAppkeyAndAppsecret(String url, String appKey, String appSecret) throws Exception {
        HttpUriRequest request = new HttpGet(url);
        RequestSigner.sign(request, appKey, appSecret);
        URI encodedUri = request.getURI();

        try {
            String lines = HttpClientUtils.execute(request);
            return lines;
        } catch (Exception var7) {
            throw var7;
        }
    }
}
