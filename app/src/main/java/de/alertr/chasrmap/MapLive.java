/*
 * written by sqall
 * Twitter: https://twitter.com/sqall01
 * Blog: https://h4des.org
 * Github: https://github.com/sqall01
 * Github Repository: https://github.com/sqall01/chasr-android-map
 *
 * This file is part of Chasr Android Logger.
 * Licensed under GPL, either version 3, or any later.
 * See <http://www.gnu.org/licenses/>
 */

package de.alertr.chasrmap;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.TextView;
import android.widget.Toast;

import java.security.MessageDigest;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Formatter;
import java.util.Locale;
import java.util.TimeZone;

public class MapLive extends AppCompatActivity {

    private final String TAG = MapLive.class.getSimpleName();

    private String pref_username;
    private String pref_password;
    private String pref_secret_hash;
    private String pref_host;
    private String device;

    private WebView map;
    private TextView timeLabel;
    private TextView locationLabel;

    private long last_gps_time = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        updatePreferences();
        setContentView(R.layout.map_live);
        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        // Enable back button on tool bar.
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        try {
            device = getIntent().getExtras().getString("device");
        }
        catch(NullPointerException e) {
            if (Logger.DEBUG) {
                Log.d(TAG, "[onCreate] " + e);
            }
            device = "";
        }

        timeLabel = findViewById(R.id.time);
        locationLabel = findViewById(R.id.location);

        map = findViewById(R.id.map);



        //map.clearCache(true); // TODO for debugging




        map.requestFocus(View.FOCUS_DOWN);
        map.addJavascriptInterface(new JsInterfaceLive(this), "Android");

        WebSettings webSettings = map.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setDefaultTextEncodingName("utf-8");

        String userAgent = this.getString(R.string.app_name) + "/" + BuildConfig.VERSION_NAME +
                "; " + System.getProperty("http.agent");
        webSettings.setUserAgentString(userAgent);

        // Request map (do it onCreate since otherwise we reload the page each time
        // the app comes up to the foreground).
        String url = pref_host + "/map.php#mode=live" +
                "&device_name=" + device +
                "&secret_hash=" + pref_secret_hash;
        String auth = WebHelper.PARAM_USER + "=" + pref_username;
        auth += "&" + WebHelper.PARAM_PASS + "=" + pref_password;
        map.postUrl(url, auth.getBytes());
    }

    /**
     * On resume
     */
    @Override
    protected void onResume() {
        super.onResume();
        if (Logger.DEBUG) {
            Log.d(TAG, "[onResume]");
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        // Go back if it was clicked.
        if(id == android.R.id.home) {
            finish();
        }
        return true;
    }

    /**
     * Converts bytes array to hex string.
     * @param bytes bytes to convert
     * @return String of bytes as hex value
     */
    private static String toHexString(byte[] bytes) {
        Formatter formatter = new Formatter();
        for(byte b : bytes) {
            formatter.format("%02x", b);
        }
        return formatter.toString();
    }

    /**
     * Reread user preferences
     */
    private void updatePreferences() {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        pref_username = prefs.getString("prefUsername", "");
        pref_password = prefs.getString("prefPass", "");
        pref_host = prefs.getString("prefHost", "");

        String pref_secret = prefs.getString("prefSecret", "");
        byte[] encryption_key = new byte[32];
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.reset();
            encryption_key = digest.digest(pref_secret.getBytes());
        }
        catch (Throwable e) {
            if (Logger.DEBUG) { Log.d(TAG, "[updatePreferences key failed: " + e + "]"); }
        }
        pref_secret_hash = toHexString(encryption_key);
    }


    public void setLatestGps(double lat, double lon, long timestamp) {

        if(timestamp > last_gps_time) {

            last_gps_time = timestamp;

            // Set GPS time.
            String timeString;
            if (timestamp > 0) {
                final Date updateDate = new Date(timestamp * 1000);
                final Calendar calendar = Calendar.getInstance();
                calendar.setTime(updateDate);
                final Calendar today = Calendar.getInstance();

                DateFormat df;
                if (calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR)
                        && calendar.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)) {
                    df = DateFormat.getTimeInstance();
                } else {
                    df = DateFormat.getDateTimeInstance();
                }
                df.setTimeZone(TimeZone.getDefault());

                timeString = df.format(updateDate);
            } else {
                timeString = "-";
            }
            timeLabel.setText(timeString);

            // Set coordinates of last GPS update.
            String coordString;
            coordString = String.format(Locale.US, "%.6f", lat);
            coordString += ", ";
            coordString += String.format(Locale.US, "%.6f", lon);
            locationLabel.setText(coordString);
        }
    }
}
