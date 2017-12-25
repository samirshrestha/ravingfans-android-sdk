package vc.followtheseed;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Date;
import java.util.TimeZone;
import java.util.UUID;

public class RFAnalytics {

    private static RFAnalytics _instance = null;

    private static final String prefsKey = "FTSAnalyticsPrefs";
    private static final String prefsRandomIdKey = "FTSRandomId";

    private static final String ftsApiHost = "https://ravingfans.bouqt.com";

    // 1 minute from last end session, before starting a new one.
    private static final long millisBeforeNewSession = 60 * 1000;

    private String appKey = null;
    private String sessionId = null;
    private String timezone = null;
    private String randomId = null;

    private RequestTask requestTask = null;
    private long lastSessionEnd = 0;

    private RFAnalytics(Context context, String appKey) {
        // private singleton constructor
        this.appKey = appKey;
        this.timezone = TimeZone.getDefault().getDisplayName();

        SharedPreferences prefs = context.getSharedPreferences(prefsKey, Context.MODE_PRIVATE);
        this.randomId = prefs.getString(prefsRandomIdKey, null);

        if (this.randomId == null) {
            this.randomId = UUID.randomUUID().toString();

            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(prefsRandomIdKey, this.randomId);
            editor.apply();
        }
    }

    public static synchronized void init(Context context, String appKey) {
        if (_instance == null)
            _instance = new RFAnalytics(context, appKey);
    }

    public static synchronized void startSession() {

        long now = new Date().getTime();
        long delta = now - _instance.lastSessionEnd;

        if (delta > millisBeforeNewSession) {
            _instance.sessionId = UUID.randomUUID().toString();
            _instance.sendRequest("session/started");
            _instance.lastSessionEnd = now;
        }
    }

    public static synchronized void endSession() {
        _instance.sendRequest("session/ended");
        _instance.lastSessionEnd = new Date().getTime();
    }

    private void sendRequest(String command) {
        try {
            String address = ftsApiHost + "/" + command +
                    "?key=" + URLEncoder.encode(this.appKey, "utf-8") +
                    "&did=" + URLEncoder.encode(this.randomId, "utf-8") +
                    "&sid=" + URLEncoder.encode(this.sessionId, "utf-8") +
                    "&tz=" + URLEncoder.encode(this.timezone, "utf-8");

            this.requestTask = new RequestTask();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
                this.requestTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, address);
            else
                this.requestTask.execute(address);

        } catch (UnsupportedEncodingException e) {
            Log.d("RFAnalytics", "Error while encoding parameters for request.", e);
        }
    }

    class RequestTask extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... uri) {
            try {
                URL obj = new URL(uri[0]);

                HttpURLConnection con = (HttpURLConnection) obj.openConnection();
                con.setRequestMethod("GET");

                if (con.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                    String inputLine;
                    StringBuilder response = new StringBuilder();

                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();

                    return response.toString();
                }
            } catch (Exception e) {
                Log.d("RFAnalytics", "Error while sending HTTP request to address " + uri[0], e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            if (result == null || !result.equals("OK"))
                Log.d("RFAnalytics", "Bad response from server: " + result);
        }
    }
}
