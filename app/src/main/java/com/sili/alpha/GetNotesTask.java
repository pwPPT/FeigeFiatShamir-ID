package com.sili.alpha;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Button;
import android.widget.TextView;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

public class GetNotesTask extends AsyncTask<Void, Void, Boolean> {

    private String sessionId;
    private String URL;
    private HttpClient httpclient;
    private Context context;
    private List<Button> buttons;

    private TextView statusTextView;

    public GetNotesTask(String URL, Session session, Context context, TextView statusTextView, HttpClient httpclient, List<Button> buttons) {
        super();
        this.sessionId = session.getSessionId();
        this.URL = URL;
        this.statusTextView = statusTextView;
        this.httpclient = httpclient;
        this.context = context;
        this.buttons = buttons;
    }

    @Override
    protected void onPreExecute() {
        Utils.SetButtonsEnabled(buttons, false);
        // Do before authentication task
        statusTextView.setText("Fetching notes...\n");

        if(this.httpclient == null) {
            this.httpclient = Utils.initHttpClient(context);
        }
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        if(sessionId == null) {
            statusTextView.append("There is no sessionId.");
            return false;
        }

        String url = URL + "notes";

        try {
            HttpGet request = new HttpGet(url);

            request.setHeader("Accept", "application/json");

            HttpResponse response = this.httpclient.execute(request);

            if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                statusTextView.append("Success.\n");

                ByteArrayOutputStream out = new ByteArrayOutputStream();
                response.getEntity().writeTo(out);
                String responseString = out.toString();
                out.close();

                // parse response
                statusTextView.setText("Notes:\n");
                JSONObject resp = new JSONObject(responseString);
                JSONArray notes = resp.getJSONArray("notes");
                for(int i = 0; i < notes.length(); i++) {
                    String note = notes.getString(i);
                    statusTextView.append(note + "\n");
                }
                return true;
            } else {
                statusTextView.append("Error occurred while fetching notes.");
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
