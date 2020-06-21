package com.sili.alpha;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class MainActivity extends AppCompatActivity {

    private static final String URL = "http://10.0.2.2:8085/api/ca/auth";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView statusTextView = findViewById(R.id.statuesTextView);
        statusTextView.setText(LocalDateTime.now().format(DateTimeFormatter.ofPattern("hh:mm:ss")) + ": Pending\n");

        CompletableFuture<String> async_print = CompletableFuture.supplyAsync(
                () -> {
                    statusTextView.append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("hh:mm:ss")) + ": Authentication attempt\n");
                    return executeHttpRequest();
                });

        async_print
                .thenAccept(status -> statusTextView.append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("hh:mm:ss")) + ": " + status));

        try {
            async_print.get(1000, TimeUnit.MILLISECONDS);
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            statusTextView.append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("hh:mm:ss")) + ": TIMEOUT");
        }
    }

    private String executeHttpRequest() {
        try {
            HttpClient httpclient = new DefaultHttpClient();
            HttpGet request = new HttpGet(URL);
            HttpResponse response = httpclient.execute(request);
            StatusLine statusLine = response.getStatusLine();

            if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                response.getEntity().writeTo(out);
                String responseString = out.toString();
                out.close();
                return responseString;
            } else {
                //Closes the connection.
                response.getEntity().getContent().close();
                throw new IOException(statusLine.getReasonPhrase());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "FAILED";
    }
}