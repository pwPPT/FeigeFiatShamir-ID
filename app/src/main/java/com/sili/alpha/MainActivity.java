package com.sili.alpha;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class MainActivity extends AppCompatActivity {

    private static final String URL = "http://10.0.2.2:8085/api/ca/";
//    private static final long N = 131 * 239;
    private static final long N = 39769 * 50423;

    TextView statusTextView;
    Button registerButton;
    Button authButton;
    EditText usernameEditText;
    Button testAuthButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.statusTextView = findViewById(R.id.statuesTextView);
        this.statusTextView.setText("");

        this.registerButton = findViewById(R.id.registerButton);
        this.registerButton.setOnClickListener(v -> register());

        this.authButton = findViewById(R.id.authButton);
        this.authButton.setOnClickListener(v -> new AuthenticationTask(URL, N, this.getApplicationContext(), usernameEditText, statusTextView).execute());

        this.usernameEditText = findViewById(R.id.userNameEditText);

        // Button for testing AuthenticationTask
        this.testAuthButton = findViewById(R.id.testButton);
        this.testAuthButton.setOnClickListener(v -> statusTextView.setText(""));

    }

    private void register() {
        Store store = new Store(this.getApplicationContext(), N);
        if(!store.hasPrivateKeyStored()) {
            store.generatePrivateKey();
        }

        long[] pk = store.loadPrivateKey();

        long[] publicKey = new long[pk.length];
        for(int i = 0; i < publicKey.length; i++) {
            publicKey[i] = (pk[i] * pk[i]) % N;
        }

        String username = usernameEditText.getText().toString();

        statusTextView.setText("Trying to register user '" + username + "' with public key: " + Arrays.toString(publicKey) + "\n");
        // TODO - send POST request to URL+register with username and publicKey in body
        statusTextView.append("Success!");
    }
}