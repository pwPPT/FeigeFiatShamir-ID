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
import org.json.JSONException;
import org.json.JSONObject;

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

//    private static final String URL = "http://192.168.1.106:8000/api/ca/";   // FLASK APP
//    private static final String URL = "https://10.0.2.2:8443/api/ca/";   // QUARKUS - docker
    private static final String URL = "http://10.0.2.2:8085/api/ca/";   // QUARKUS - docker
    private static final long N = 39769 * 50423;
//    private static final long N = 1009 * 1019;

    Session session;

    TextView statusTextView;
    Button registerButton;
    Button authButton;
    EditText usernameEditText;
    Button testAuthButton;
    Button getButton;
    Button postButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.session = new Session();

        this.statusTextView = findViewById(R.id.statuesTextView);
        this.statusTextView.setText("");

        this.registerButton = findViewById(R.id.registerButton);
        this.registerButton.setOnClickListener(v -> new RegisterTask(URL, N, this.getApplicationContext(), usernameEditText, statusTextView).execute());

        this.authButton = findViewById(R.id.authButton);
        this.authButton.setOnClickListener(v -> new AuthenticationTask(URL, N, this.getApplicationContext(), session, usernameEditText, statusTextView).execute());

        this.usernameEditText = findViewById(R.id.userNameEditText);

        // Button for testing AuthenticationTask
        this.testAuthButton = findViewById(R.id.testButton);
        this.testAuthButton.setOnClickListener(v -> statusTextView.setText(""));

        this.getButton = findViewById(R.id.getButton);
        this.getButton.setOnClickListener(v -> new GetNotesTask(URL, session, statusTextView));

        this.postButton = findViewById(R.id.postButton);
        this.postButton.setOnClickListener(v -> new CreateNoteTask(URL, session, usernameEditText, statusTextView));


    }
}