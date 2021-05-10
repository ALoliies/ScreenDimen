package com.signature;

public class SignatureClient {
    private static MobileSignatureService mobileSignatureService = new MobileSignatureServiceImpl();

    public SignatureClient() {
    }

    public static String getResultByUriAndAppkeyAndAppsecret(String url, String appKey, String appSecret) throws Exception {
        return mobileSignatureService.getResultByUriAndAppkeyAndAppsecret(url, appKey, appSecret);
    }
}
