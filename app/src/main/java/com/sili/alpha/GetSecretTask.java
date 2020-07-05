package com.sili.alpha;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
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

public class GetSecretTask extends AsyncTask<Void, Void, Boolean> {

    private String sessionId;
    private String URL;
    private HttpClient httpclient;
    private Session session;
    private Context context;
    List<Button> buttons;

    private TextView statusTextView;

    public GetSecretTask(String URL, Session session, Context context, TextView statusTextView, HttpClient httpclient, List<Button> buttons) {
        super();
        this.URL = URL;
        this.sessionId = session.getSessionId();
        this.session = session;
        this.statusTextView = statusTextView;
        this.httpclient = httpclient;
        this.context = context;
        this.buttons = buttons;
    }

    @Override
    protected void onPreExecute() {
        Utils.SetButtonsEnabled(buttons, false);
        // Do before task
        statusTextView.setText("Fetching secret...\n");

        if(this.httpclient == null) {
            this.httpclient = Utils.initHttpClient(context);
        }
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        if(sessionId == null) {
            statusTextView.append("Unauthorized!\n");
            return false;
        }

        String url = URL + "secret";
        String payload = "{\"session_id\": \"" + sessionId + "\"}";

        try {
            HttpPost request = new HttpPost(url);

            StringEntity entity = new StringEntity(payload);
            request.setEntity(entity);
            request.setHeader("Accept", "application/json");
            request.setHeader("Content-type", "application/json");

            HttpResponse response = this.httpclient.execute(request);

            if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                response.getEntity().writeTo(out);
                String responseString = out.toString();
                out.close();

                // parse response
                JSONObject resp = new JSONObject(responseString);
                String secret = resp.getString("secret");
                statusTextView.append("Secret: " + secret + "\n");
                return true;
            } else {
                statusTextView.append("Unauthorized! Cannot fetch secret!\n");
                return false;
            }
        } catch(IOException e) {
            e.printStackTrace();
            return false;
        } catch(JSONException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    protected void onPostExecute(Boolean result) {
        Utils.SetButtonsEnabled(buttons, true);
    }
}
