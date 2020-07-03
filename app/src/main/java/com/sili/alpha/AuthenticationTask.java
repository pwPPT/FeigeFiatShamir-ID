package com.sili.alpha;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.widget.EditText;
import android.widget.TextView;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.ThreadLocalRandom;

public class AuthenticationTask extends AsyncTask<Void, Void, String> {

    private static final int N = 131 * 239;

    private String username;
    private HttpClient httpclient;
    private int[] secret;
    private int r;  // random value from getX()

    @SuppressLint("StaticFieldLeak")
    EditText usernameEditText;
    @SuppressLint("StaticFieldLeak")
    TextView statusTextView;
    String URL;

    public AuthenticationTask(String URL, EditText usernameEditText, TextView statusTextView) {
        super();
        this.usernameEditText = usernameEditText;
        this.statusTextView = statusTextView;
        this.URL = URL;
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
        // Do before authentication task
        this.username = usernameEditText.getText().toString();
        this.httpclient = new DefaultHttpClient();
        // TODO - load secret
        this.secret = new int[]{10,10,10,10};
    }

    @Override
    protected String doInBackground(Void... voids) {
        // Try to authenticate
        try {
            String token = this.getToken();
            AuthResult res = new AuthResult(true, false, null);
            while(res.repeat) {
                int[] A = sendX(token);
                res = sendY(token, A, this.secret);
            }
            return res.sessionId;
        } catch(AuthFailedException e) {
            statusTextView.append(e.toString());
            return null;
        }
    }

    @Override
    protected void onPostExecute(String token) {
        // Do sth depending on the result
        if(token == null) { // AuthFailedException occurred
            statusTextView.append("Authentication failed.");
        }
        statusTextView.append("Authentication succeed - token: " + token);
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
            throw new AuthFailedException(-1, "Connection problem - IOException in getToken.");
        }

        try {
            StatusLine statusLine = response.getStatusLine();

            if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
                // TODO - return value of "token" key from response
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                response.getEntity().writeTo(out);
                String responseString = out.toString();
                out.close();
                return responseString;
            } else {
                int statusCode = statusLine.getStatusCode();
                //Closes the connection.
                response.getEntity().getContent().close();
                throw new AuthFailedException(statusCode, URL + "token");
            }
        } catch(IOException e) {
            e.printStackTrace();
            throw new AuthFailedException(-2, URL + e.getMessage());
        }
    }

    private int[] sendX(String token) throws AuthFailedException {
        // generate X value
        this.r = ThreadLocalRandom.current().nextInt(1,  N + 1);
        while(N % this.r == 0) {
            this.r = ThreadLocalRandom.current().nextInt(1,  N + 1);
        }
        int X = (this.r * this.r) % N;

        // send X
        String url = URL + "X";
        String payload = "{\"token\":\"" + token + "\", \"X\":" + X + "}";

        HttpResponse response = post(url, payload);
        if(response == null) {  // IOException occurred
            throw new AuthFailedException(-1, "Connection problem - IOException in sendX.");
        }

        try {
            StatusLine statusLine = response.getStatusLine();

            if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
                // TODO - parse JSON response from format
                // { "A": [... ints ...] }
                // into int[]
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                response.getEntity().writeTo(out);
                String responseString = out.toString();
                out.close();
                return new int[]{1, 1, 1, 1};
            } else {
                int statusCode = statusLine.getStatusCode();
                //Closes the connection.
                response.getEntity().getContent().close();
                throw new AuthFailedException(statusCode, URL + "X");
            }
        } catch(IOException e) {
            e.printStackTrace();
            throw new AuthFailedException(-2, URL + e.getMessage());
        }
    }

    private AuthResult sendY(String token, int[] A, int[] secret) throws AuthFailedException {
        // compute Y
        int[] results = new int[A.length];
        int Y = 1;
        for(int i = 0; i < A.length; i++) {
            if(A[i] == 1) {
                Y *= secret[i];
            }
        }
        Y =  (Y * this.r) % N;

        // send Y
        String url = URL + "Y";
        String payload = "{\"token\":\"" + token + "\", \"Y\":" + Y + "}";

        HttpResponse response = post(url, payload);
        if(response == null) {
            throw new AuthFailedException(-1, "Connection problem - IOException in sendY.");
        }

        try {
            StatusLine statusLine = response.getStatusLine();

            if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
                // TODO - parse JSON response from format
                // { "repeat": boolean, is_authenticated: boolean, session_id: str }
                // into AuthResult object
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                response.getEntity().writeTo(out);
                String responseString = out.toString();
                out.close();
                return new AuthResult(false, true, token);
            } else {
                int statusCode = statusLine.getStatusCode();
                //Closes the connection.
                response.getEntity().getContent().close();
                throw new AuthFailedException(statusCode, URL + "Y");
            }
        } catch(IOException e) {
            e.printStackTrace();
            throw new AuthFailedException(-2, URL + e.getMessage());
        }
    }
}
