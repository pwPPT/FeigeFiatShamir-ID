package com.sili.alpha;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class RegisterTask extends AsyncTask<Void, Void, Void> {

    Context context;
    long N;
    String username;
    long[] publicKey;
    HttpClient httpclient;
    List<Button> buttons;

    @SuppressLint("StaticFieldLeak")
    EditText usernameEditText;
    @SuppressLint("StaticFieldLeak")
    TextView statusTextView;
    String URL;

    public RegisterTask(String URL, long N, Context context, EditText usernameEditText, TextView statusTextView, HttpClient httpclient, List<Button> buttons) {
        super();
        this.usernameEditText = usernameEditText;
        this.statusTextView = statusTextView;
        this.URL = URL;
        this.N = N;
        this.context = context;
        this.httpclient = httpclient;
        this.buttons = buttons;
    }

    @Override
    protected void onPreExecute() {
        Utils.SetButtonsEnabled(buttons, false);
        // Do before authentication task
        Store store = new Store(context, N);
        if(!store.hasPrivateKeyStored()) {
            store.generatePrivateKey();
        }

        long[] pk = store.loadPrivateKey();

        this.publicKey = new long[pk.length];
        for(int i = 0; i < publicKey.length; i++) {
            this.publicKey[i] = (pk[i] * pk[i]) % N;
        }

        this.username = usernameEditText.getText().toString();

        if(httpclient == null) {
            this.httpclient = Utils.initHttpClient(context);
        }

        statusTextView.setText("Trying to register user '" + username + "' with public key: " + Arrays.toString(publicKey) + "\n");
    }

    @Override
    protected Void doInBackground(Void... voids) {
        register();
        return null;
    }

    @Override
    protected void onPostExecute(Void v) {
        // Do sth depending on the result
        Utils.SetButtonsEnabled(buttons, true);
        // TODO - popup?
    }


    private void register() {
        try {
//            HttpClient httpclient = new DefaultHttpClient();
            HttpPost request = new HttpPost(URL + "register");

            StringBuilder payload = new StringBuilder("{\"username\": \"" + this.username + "\", \"public_key\": [" + this.publicKey[0]);
            for(int i = 1; i < this.publicKey.length; i++) {
                payload = payload.append("," + this.publicKey[i]);
            }
            payload = payload.append("]}");

            String payloadStr = payload.toString();
            System.out.println(payloadStr);
            StringEntity entity = new StringEntity(payloadStr);
            request.setEntity(entity);
            request.setHeader("Accept", "application/json");
            request.setHeader("Content-type", "application/json");

            HttpResponse response = httpclient.execute(request);

            StatusLine statusLine = response.getStatusLine();

            if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
                // success
                statusTextView.append(("User " + this.username + " created.\n"));
            } else {
                // fail
                int statusCode = statusLine.getStatusCode();
                //Closes the connection.
                response.getEntity().getContent().close();
                statusTextView.append(("Cannot register user " + this.username + "  [code: " + statusCode + "]\n"));
            }
        } catch(IOException e) {
            e.printStackTrace();
        }
    }
}
