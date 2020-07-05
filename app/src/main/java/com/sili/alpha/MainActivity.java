package com.sili.alpha;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

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
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;

import java.io.InputStream;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;

public class MainActivity extends AppCompatActivity {

//    private static final String URL = "http://192.168.1.106:8000/api/ca/";   // FLASK APP
    private static final String URL = "https://10.0.2.2:8443/api/ca/";   // QUARKUS - docker
//    private static final String URL = "http://10.0.2.2:8085/api/ca/";   // QUARKUS - docker
    private static final long N = 39769 * 50423;
//    private static final long N = 1009 * 1019;

    Session session;
    HttpClient httpclient;

    TextView statusTextView;
    Button registerButton;
    Button authButton;
    EditText usernameEditText;
    Button testAuthButton;
    Button postButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.httpclient = Utils.initHttpClient(this.getApplicationContext());

        this.session = new Session();

        this.statusTextView = findViewById(R.id.statuesTextView);
        this.registerButton = findViewById(R.id.registerButton);
        this.authButton = findViewById(R.id.authButton);
        this.usernameEditText = findViewById(R.id.userNameEditText);
        this.testAuthButton = findViewById(R.id.testButton);
        this.postButton = findViewById(R.id.postButton);

        List<Button> buttons = new ArrayList<>();
        buttons.add(registerButton);
        buttons.add(authButton);
        buttons.add(testAuthButton);
        buttons.add(postButton);

        this.statusTextView.setText("");

        this.registerButton.setOnClickListener(v -> new RegisterTask(URL, N, this.getApplicationContext(), usernameEditText, statusTextView, httpclient, buttons).execute());

        this.authButton.setOnClickListener(v -> new AuthenticationTask(URL, N, this.getApplicationContext(), session, usernameEditText, statusTextView, httpclient, buttons).execute());

        this.testAuthButton.setOnClickListener(v -> statusTextView.setText(""));

        this.postButton.setOnClickListener(v -> new GetSecretTask(URL, session, this.getApplicationContext(), statusTextView, httpclient, buttons));
    }
}