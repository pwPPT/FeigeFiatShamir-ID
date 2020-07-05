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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class AuthenticationTask extends AsyncTask<Void, Void, String> {

    private long N;
    Context context;

    private String username;
    private HttpClient httpclient;
    private long[] secret;
    private long r;  // random value from getX()
    private List<Button> buttons;

    @SuppressLint("StaticFieldLeak")
    EditText usernameEditText;
    @SuppressLint("StaticFieldLeak")
    TextView statusTextView;
    String URL;
    Session session;

    public AuthenticationTask(String URL, long N, Context context, Session session, EditText usernameEditText, TextView statusTextView, HttpClient httpclient, List<Button> buttons) {
        super();
        this.usernameEditText = usernameEditText;
        this.statusTextView = statusTextView;
        this.URL = URL;
        this.N = N;
        this.context = context;
        this.session = session;
        this.httpclient = httpclient;
        this.buttons = buttons;
    }

    private static class AuthFailedException extends Exception {
        int statusCode;
        String endpoint;

        public AuthFailedException(int statusCode, String endpoint) {
            this.statusCode = statusCode;
            this.endpoint = endpoint;
        }

        public String toString() {
            return "AuthFailedException - code: " + this.statusCode + "   endpoint: " + this.endpoint;
        }
    }


    private static class AuthResult {
        boolean repeat;
        boolean isAuthorized;
        String sessionId;

        public AuthResult(boolean repeat, boolean isAuthorized, String sessionId) {
            this.repeat = repeat;
            this.isAuthorized = isAuthorized;
            this.sessionId = sessionId;
        }
    }

    @Override
    protected void onPreExecute() {
        Utils.SetButtonsEnabled(buttons, false);
        // Do before authentication task
        this.username = usernameEditText.getText().toString();
        if(this.httpclient == null) {
            this.httpclient = Utils.initHttpClient(context);
        }

        Store store = new Store(this.context, N);
        this.secret = store.loadPrivateKey();

        if(this.httpclient == null) {
            this.httpclient = Utils.initHttpClient(context);
        }

        statusTextView.setText("Authenticating user '" + username + "'...\n");
    }

    @Override
    protected String doInBackground(Void... voids) {
        // Try to authenticate
        if(this.secret == null) {
            return null;
        }

        try {
            String token = this.getToken();
            AuthResult res = new AuthResult(true, false, null);
            while(res.repeat) {
                int[] A = sendX(token);
                res = sendY(token, A, this.secret);
            }
            if(!res.isAuthorized) {
                throw new AuthFailedException(-100, "Wrong private key!\n");
            }
            session.setSessionId(res.sessionId);
            return res.sessionId;
        } catch(AuthFailedException e) {
            statusTextView.append(e.toString());
            return null;
        }
    }

    @Override
    protected void onPostExecute(String token) {
        Utils.SetButtonsEnabled(buttons, true);
        // Do sth depending on the result
        if(token == null) { // AuthFailedException occurred
            statusTextView.append("Authentication failed.\n");
            return;
        }
        statusTextView.append("Authentication succeed - token: " + token + "\n");
        session.setSessionId(token);
        // TODO - add new activity and move to it
    }

    private HttpResponse post(String url, String payload) {
        try {
            HttpPost request = new HttpPost(url);

            StringEntity entity = new StringEntity(payload);
            request.setEntity(entity);
            request.setHeader("Accept", "application/json");
            request.setHeader("Content-type", "application/json");

            return this.httpclient.execute(request);
        } catch(IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private String getToken() throws AuthFailedException {
        String url = URL + "token";
        String payload = "{\"username\":\"" + username + "\"}";

        HttpResponse response = post(url, payload);
        if(response == null) { // IOException occurred
            throw new AuthFailedException(-1, "Connection problem - IOException in getToken.\n");
        }

        try {
            StatusLine statusLine = response.getStatusLine();

            if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
                // TODO - return value of "token" key from response
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                response.getEntity().writeTo(out);
                String responseString = out.toString();
                out.close();

                // parse response
                JSONObject resp = new JSONObject(responseString);
                String token = resp.getString("token");

                statusTextView.append("token: " + token + "\n");
                return token;
            } else {
                int statusCode = statusLine.getStatusCode();
                //Closes the connection.
                response.getEntity().getContent().close();
                throw new AuthFailedException(statusCode, URL + "token\n");
            }
        } catch(IOException e) {
            e.printStackTrace();
            throw new AuthFailedException(-2, URL + e.getMessage());
        } catch(JSONException e) {
            e.printStackTrace();
            throw new AuthFailedException(-3, URL + e.getMessage());
        }
    }

    private int[] sendX(String token) throws AuthFailedException {
        // generate X value
        this.r = ThreadLocalRandom.current().nextLong(1,  N + 1);
        while(N % this.r == 0) {
            this.r = ThreadLocalRandom.current().nextLong(1,  N + 1);
        }
        long X = (this.r * this.r) % N;
        statusTextView.append("X: " + X + "\n");

        // send X
        String url = URL + "X";
        String payload = "{\"token\":\"" + token + "\", \"x\":" + X + "}";

        HttpResponse response = post(url, payload);
        if(response == null) {  // IOException occurred
            throw new AuthFailedException(-1, "Connection problem - IOException in sendX.\n");
        }

        try {
            StatusLine statusLine = response.getStatusLine();

            if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                response.getEntity().writeTo(out);
                String responseString = out.toString();
                out.close();

                // parse response
                JSONObject resp = new JSONObject(responseString);
                JSONArray vec = resp.getJSONArray("a");

                int[] A = new int[vec.length()];
                for(int i = 0; i < vec.length(); i++) {
                    A[i] = vec.getInt(i);
                }
                return A;
            } else {
                int statusCode = statusLine.getStatusCode();
                //Closes the connection.
                response.getEntity().getContent().close();
                throw new AuthFailedException(statusCode, URL + "X\n");
            }
        } catch(IOException e) {
            e.printStackTrace();
            throw new AuthFailedException(-2, URL + "X\n");
        } catch(JSONException e) {
            e.printStackTrace();
            throw new AuthFailedException(-3, URL + "X\n");
        }
    }

    private AuthResult sendY(String token, int[] A, long[] secret) throws AuthFailedException {
        // compute Y
        int[] results = new int[A.length];
        long Y = 1;
        for(int i = 0; i < A.length; i++) {
            if(A[i] == 1) {
                Y = (Y * secret[i]) % N;
            }
        }
        Y =  (Y * this.r) % N;
        statusTextView.append("Y: " + Y + "\n");

        // send Y
        String url = URL + "Y";
        String payload = "{\"token\":\"" + token + "\", \"y\":" + Y + "}";

        HttpResponse response = post(url, payload);
        if(response == null) {
            throw new AuthFailedException(-1, "Connection problem - IOException in sendY.");
        }

        try {
            StatusLine statusLine = response.getStatusLine();

            if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                response.getEntity().writeTo(out);
                String responseString = out.toString();
                out.close();

                // parse response
                System.out.println(responseString);
                JSONObject resp = new JSONObject(responseString);
                boolean repeat = resp.getBoolean("repeat");
                boolean isAuthenticated = resp.getBoolean("is_authenticated");
                String sessionId = resp.getString("session_id");
                return new AuthResult(repeat, isAuthenticated, sessionId);
            } else {
                int statusCode = statusLine.getStatusCode();
                //Closes the connection.
                response.getEntity().getContent().close();
                throw new AuthFailedException(statusCode, URL + "Y\n");
            }
        } catch(IOException e) {
            e.printStackTrace();
            throw new AuthFailedException(-2, URL + "Y\n");
        } catch(JSONException e) {
            e.printStackTrace();
            throw new AuthFailedException(-3, URL + "Y\n");
        }
    }
}
