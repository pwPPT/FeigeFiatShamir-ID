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

import java.io.IOException;
import java.util.List;

public class CreateNoteTask extends AsyncTask<Void, Void, Boolean> {

    private String sessionId;
    private String URL;
    private HttpClient httpclient;
    private Session session;
    private Context context;
    List<Button> buttons;

    private EditText noteEditText;
    private TextView statusTextView;

    public CreateNoteTask(String URL, Session session, Context context, EditText noteEditText, TextView statusTextView, HttpClient httpclient, List<Button> buttons) {
        super();
        this.URL = URL;
        this.sessionId = session.getSessionId();
        this.session = session;
        this.noteEditText = noteEditText;
        this.statusTextView = statusTextView;
        this.httpclient = httpclient;
        this.context = context;
        this.buttons = buttons;
    }

    @Override
    protected void onPreExecute() {
        Utils.SetButtonsEnabled(buttons, false);
        // Do before task
        statusTextView.setText("Creating new note...\n");

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

        String note = noteEditText.getText().toString();
        String url = URL + "notes";
        String payload = "{\"session_id\": \"" + sessionId + "\", \"note\": \"" + note + "\"}";

        try {
            HttpPost request = new HttpPost(url);

            StringEntity entity = new StringEntity(payload);
            request.setEntity(entity);
            request.setHeader("Accept", "application/json");
            request.setHeader("Content-type", "application/json");

            HttpResponse response = this.httpclient.execute(request);

            if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                statusTextView.append("Note created.\n");
                return true;
            } else {
                statusTextView.append("Error occurred while creating note.");
                return false;
            }
        } catch(IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    protected void onPostExecute(Boolean result) {
        Utils.SetButtonsEnabled(buttons, true);
        if(result) {
            new GetNotesTask(URL, session, context, statusTextView, httpclient, buttons).execute();
        }
    }
}
