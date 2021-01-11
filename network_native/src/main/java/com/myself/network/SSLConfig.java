package com.myself.network;

import android.app.Activity;
import android.content.Context;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

enum SSLWhichOne {
    TrustAll,//任信所有证书
    JustTrustMe//只信任自己服务器的证书
}

public class SSLConfig {

    //服务器绑定的域名
    private static String host = "www.anant.club";

    public static void set(SSLWhichOne type, Activity activity) {
        SSLSocketFactory sslSocketFactory = null;
        switch (type) {
            case TrustAll:
                sslSocketFactory = getDefaultSSLSocketFactory();
                break;
            case JustTrustMe:
                sslSocketFactory = getSSLContextFactory(activity);
                break;
        }

        //将获取的 ssl 证书 配置到 HttpsURLConnection
        HttpsURLConnection.setDefaultSSLSocketFactory(sslSocketFactory);
        //验证域名
        HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession sslsession) {
                if (host.equals(hostname)) {//判断域名是否和证书域名相等(不需要验证可以关掉)
                    return true;
                } else {
                    return false;
                }
            }
        });
    }

    // 信任所有证书，不建议这么操作
    private static synchronized SSLSocketFactory getDefaultSSLSocketFactory() {
        try {
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, new TrustManager[]{
                    new X509TrustManager() {
                        public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {

                        }

                        public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
                        }

                        public X509Certificate[] getAcceptedIssuers() {
                            return new X509Certificate[0];
                        }
                    }
            }, null);
            return sslContext.getSocketFactory();
        } catch (GeneralSecurityException e) {
            throw new AssertionError();
        }
    }

    // 信任自己放在本地的证书
    private static synchronized SSLSocketFactory getSSLContextFactory(Context context) {
        SSLContext s_sSLContext = null;
        CertificateFactory certificateFactory = null;
        InputStream inputStream = null;
        KeyStore keystore = null;
        String tmfAlgorithm = null;
        TrustManagerFactory trustManagerFactory = null;
        try {
            certificateFactory = CertificateFactory.getInstance("X.509");

            inputStream = context.getAssets().open("cert.pem");//这里导入SSL证书文件

            Certificate ca = certificateFactory.generateCertificate(inputStream);

            keystore = KeyStore.getInstance(KeyStore.getDefaultType());
            keystore.load(null, null);
            keystore.setCertificateEntry("ca", ca);

            tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
            trustManagerFactory = TrustManagerFactory.getInstance(tmfAlgorithm);
            trustManagerFactory.init(keystore);

            // Create an SSLContext that uses our TrustManager
            s_sSLContext = SSLContext.getInstance("TLS");
            s_sSLContext.init(null, trustManagerFactory.getTrustManagers(), null);
            return s_sSLContext.getSocketFactory();
        } catch (CertificateException | KeyManagementException | NoSuchAlgorithmException | KeyStoreException | IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}