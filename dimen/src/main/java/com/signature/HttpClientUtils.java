package com.signature;


import java.io.IOException;

import org.apache.http.Consts;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.protocol.RequestAcceptEncoding;
import org.apache.http.client.protocol.ResponseContentEncoding;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

public class HttpClientUtils {
    private static final HttpClient HTTP_CLIENT;
    private static final RequestConfig DEFAULT_REQUEST_CONFIG;

    public HttpClientUtils() {
    }

    public static String execute(HttpUriRequest request) throws ClientProtocolException, IOException {
        return (String) HTTP_CLIENT.execute(request, new BasicResponseHandler());
    }

    public static <T> T execute(HttpUriRequest request, ResponseHandler<T> responseHandler) throws ClientProtocolException, IOException {
        return HTTP_CLIENT.execute(request, responseHandler);
    }

    static {
        HttpClientBuilder httpClientBuilder = HttpClients.custom();
        DEFAULT_REQUEST_CONFIG = RequestConfig.custom().setExpectContinueEnabled(false).setConnectTimeout(60000).setSocketTimeout(60000).setCookieSpec("compatibility").build();
        httpClientBuilder.setDefaultRequestConfig(DEFAULT_REQUEST_CONFIG);
        httpClientBuilder.addInterceptorFirst(new RequestAcceptEncoding());
        httpClientBuilder.addInterceptorFirst(new ResponseContentEncoding());
        HttpRequestRetryHandler retryHandler = new DefaultHttpRequestRetryHandler(1, true);
        httpClientBuilder.setRetryHandler(retryHandler);
        Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create().register("http", PlainConnectionSocketFactory.INSTANCE).register("https", SSLConnectionSocketFactory.getSocketFactory()).build();
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager(registry);
        connectionManager.setMaxTotal(100);
        connectionManager.setDefaultMaxPerRoute(100);
        SocketConfig defaultSocketConfig = SocketConfig.custom().setTcpNoDelay(true).build();
        connectionManager.setDefaultSocketConfig(defaultSocketConfig);
        ConnectionConfig defaultConnectionConfig = ConnectionConfig.custom().setCharset(Consts.UTF_8).build();
        connectionManager.setDefaultConnectionConfig(defaultConnectionConfig);
        httpClientBuilder.setConnectionManager(connectionManager);
        httpClientBuilder.setRedirectStrategy(DefaultRedirectStrategy.INSTANCE);
        HTTP_CLIENT = httpClientBuilder.build();
    }
}
