package com.sili.alpha;

import android.content.Context;
import android.widget.Button;

import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.params.ConnPerRoute;
import org.apache.http.conn.params.ConnPerRouteBean;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.security.KeyStore;
import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;

public class Utils {

    public static byte[] longsToBytes(long[] l) {
        ByteBuffer buf = ByteBuffer.allocate(l.length * Long.BYTES);

        for(long val : l) {
            buf = buf.putLong(val);
        }

        return buf.array();
    }

    public static long[] bytesToLongs(byte[] b) {
        ByteBuffer buf = ByteBuffer.wrap(b);
        long[] l = new long[b.length/Long.BYTES];

        for(int i = 0; i < l.length; i++) {
            l[i] = buf.getLong();
        }

        return l;
    }

    public static HttpClient initHttpClient(Context context) {
        try {

            // Load keystore and truststore
            InputStream clientTruststore = context.getResources().openRawResource(R.raw.truststore);
            KeyStore trustStore = null;
            trustStore = KeyStore.getInstance("BKS");
            trustStore.load(clientTruststore, "password".toCharArray());

            System.out.println("Loaded server certificates: " + trustStore.size());

            // initialize trust manager factory with the read truststore
            TrustManagerFactory trustManagerFactory = null;
            trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(trustStore);

            // load client certificate
            InputStream keyStoreStream = context.getResources().openRawResource(R.raw.keystore);
            KeyStore keyStore = null;
            keyStore = KeyStore.getInstance("BKS");
            keyStore.load(keyStoreStream, "password".toCharArray());

            System.out.println("Loaded client certificates: " + keyStore.size());

            // initialize key manager factory with the read client certificate
            KeyManagerFactory keyManagerFactory = null;
            keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(keyStore, "password".toCharArray());


            // initialize SSLSocketFactory to use the certificates
            SSLSocketFactory socketFactory = null;
            socketFactory = new SSLSocketFactory(SSLSocketFactory.TLS, keyStore, "password",
                    trustStore, null, null);
            HostnameVerifier hostnameVerifier = org.apache.http.conn.ssl.SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER;
            socketFactory.setHostnameVerifier((X509HostnameVerifier) hostnameVerifier);

            // Set basic data
            HttpParams params = new BasicHttpParams();
            HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
            HttpProtocolParams.setContentCharset(params, "UTF-8");
            HttpProtocolParams.setUseExpectContinue(params, true);
            HttpProtocolParams.setUserAgent(params, "Android app/1.0.0");

            // Make pool
            ConnPerRoute connPerRoute = new ConnPerRouteBean(12);
            ConnManagerParams.setMaxConnectionsPerRoute(params, connPerRoute);
            ConnManagerParams.setMaxTotalConnections(params, 20);

            // Set timeout
            HttpConnectionParams.setStaleCheckingEnabled(params, false);
            HttpConnectionParams.setConnectionTimeout(params, 20 * 1000);
            HttpConnectionParams.setSoTimeout(params, 20 * 1000);
            HttpConnectionParams.setSocketBufferSize(params, 8192);

            // Some client params
            HttpClientParams.setRedirecting(params, false);

            // Register http/s shemas!
            SchemeRegistry schReg = new SchemeRegistry();
            schReg.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
            schReg.register(new Scheme("https", socketFactory, 443));
            ClientConnectionManager conMgr = new ThreadSafeClientConnManager(params, schReg);
            HttpClient sClient = new DefaultHttpClient(conMgr, params);

            return sClient;
        } catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void SetButtonsEnabled(List<Button> buttons, boolean isEnabled) {
        for(Button b : buttons) {
            b.setEnabled(isEnabled);
        }
    }
}
