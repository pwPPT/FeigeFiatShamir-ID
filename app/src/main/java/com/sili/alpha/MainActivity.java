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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class MainActivity extends AppCompatActivity {

    private static final String URL = "http://10.0.2.2:8085/api/ca/";

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
        this.statusTextView.setText(LocalDateTime.now().format(DateTimeFormatter.ofPattern("hh:mm:ss")) + ": Pending\n");

        this.registerButton = findViewById(R.id.registerButton);
        this.registerButton.setOnClickListener(v -> register(statusTextView));

        this.authButton = findViewById(R.id.authButton);
        this.authButton.setOnClickListener(v -> authenticate(statusTextView));

        this.usernameEditText = findViewById(R.id.userNameEditText);

        // Button for testing AuthenticationTask
        this.testAuthButton = findViewById(R.id.testButton);
        this.testAuthButton.setOnClickListener(v -> new AuthenticationTask(URL, usernameEditText, statusTextView).execute());

    }

    private void authenticate(TextView statusTextView) {
        CompletableFuture<String> async_print = CompletableFuture.supplyAsync(
                () -> {
                    String username = this.usernameEditText.getText().toString();
                    this.statusTextView.append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("hh:mm:ss")) + ": Authentication attempt\n");
                    return executeHttpGetRequest(username);
                });

        async_print
                .thenAccept(status -> this.statusTextView.append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("hh:mm:ss")) + ": " + status));

        try {
            async_print.get(1000, TimeUnit.MILLISECONDS);
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            this.statusTextView.append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("hh:mm:ss")) + ": TIMEOUT");
        }
    }

    private void register(TextView statusTextView) {
        CompletableFuture<String> async_print = CompletableFuture.supplyAsync(
                () -> {
                    String username = this.usernameEditText.getText().toString();
                    this.statusTextView.append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("hh:mm:ss")) + ": Registration attempt\n");
                    return executeHttpPostRequest(username);
                });

        async_print
                .thenAccept(status -> this.statusTextView.append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("hh:mm:ss")) + ": " + status));

        try {
            async_print.get(1000, TimeUnit.MILLISECONDS);
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            this.statusTextView.append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("hh:mm:ss")) + ": TIMEOUT");
        }
    }


    private String executeHttpPostRequest(String username) {
        try {
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost request = new HttpPost(URL + "register");

            String json = "{\"username\":\"" + username + "\"}";
            StringEntity entity = new StringEntity(json);
            request.setEntity(entity);
            request.setHeader("Accept", "application/json");
            request.setHeader("Content-type", "application/json");

            HttpResponse response = httpclient.execute(request);
            StatusLine statusLine = response.getStatusLine();

            if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                response.getEntity().writeTo(out);
                String responseString = out.toString();
                out.close();
                return "Registered user '" + username + "'\n";
            } else {
                System.out.println(statusLine.getStatusCode());
                //Closes the connection.
                response.getEntity().getContent().close();
                throw new IOException(statusLine.getReasonPhrase());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Registration user '" + username + "' FAILED\n";
    }

    private String executeHttpGetRequest(String username) {
        try {
            HttpClient httpclient = new DefaultHttpClient();
            HttpGet request = new HttpGet(URL + "auth/" + username);
            HttpResponse response = httpclient.execute(request);
            StatusLine statusLine = response.getStatusLine();

            if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                response.getEntity().writeTo(out);
                String responseString = out.toString();
                out.close();
                return responseString + "\n";
            } else {
                System.out.println(statusLine.getStatusCode());
                //Closes the connection.
                response.getEntity().getContent().close();
                throw new IOException(statusLine.getReasonPhrase());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Auth for " + username + " FAILED";
    }
}