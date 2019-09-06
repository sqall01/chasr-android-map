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

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.TextView;

import java.security.MessageDigest;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Formatter;
import java.util.Locale;
import java.util.SortedMap;
import java.util.TimeZone;
import java.util.TreeMap;

public class MapView extends AppCompatActivity {
    private final String TAG = MapView.class.getSimpleName();

    private String pref_username;
    private String pref_password;
    private String pref_secret_hash;
    private String pref_host;
    private String pref_units;
    private String device;
    private long startTime;
    private long endTime;

    private WebView map;
    private TextView startTimeLabel;
    private TextView endTimeLabel;
    private TextView distanceLabel;
    private TextView loadingLabel;

    private String unitName;
    private SortedMap<Long, GpsLocation> track = new TreeMap<Long, GpsLocation>();
    float distance = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        updatePreferences();
        setContentView(R.layout.map_view);
        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        // Enable back button on tool bar.
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        try {
            device = getIntent().getExtras().getString("device");
            startTime = getIntent().getExtras().getLong("start_time");
            endTime = getIntent().getExtras().getLong("end_time");
        }
        catch(NullPointerException e) {
            if (Logger.DEBUG) {
                Log.d(TAG, "[onCreate] " + e);
            }
            device = "";
            startTime = 0;
            endTime = 0;
        }

        loadingLabel = findViewById(R.id.view_loader_text);
        startTimeLabel = findViewById(R.id.start_time);
        endTimeLabel = findViewById(R.id.end_time);
        distanceLabel = findViewById(R.id.distance);
        map = findViewById(R.id.map);

        // Set preferred unit.
        unitName = getString(R.string.unit_kilometer);
        if(pref_units.equals(getString(R.string.pref_units_imperial))) {
            distance *= GpsLocation.KM_MILE;
            unitName = getString(R.string.unit_mile);
        }
        else if (pref_units.equals(getString(R.string.pref_units_nautical))) {
            distance *= GpsLocation.KM_NMILE;
            unitName = getString(R.string.unit_nmile);
        }



        //map.clearCache(true); // TODO for debugging




        map.requestFocus(View.FOCUS_DOWN);
        map.addJavascriptInterface(new JsInterfaceView(this), "Android");

        WebSettings webSettings = map.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setDefaultTextEncodingName("utf-8");

        String userAgent = this.getString(R.string.app_name) + "/" + BuildConfig.VERSION_NAME +
                "; " + System.getProperty("http.agent");

        // TODO Android dev user agent
        userAgent = this.getString(R.string.app_name) + " Test/" + BuildConfig.VERSION_NAME +
                "; " + System.getProperty("http.agent");


        webSettings.setUserAgentString(userAgent);
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

        String url = pref_host + "/map.php#mode=view" +
                "&device_name=" + device +
                "&secret_hash=" + pref_secret_hash +
                "&start=" + startTime +
                "&end=" + endTime;
        String auth = WebHelper.PARAM_USER + "=" + pref_username;
        auth += "&" + WebHelper.PARAM_PASS + "=" + pref_password;
        map.postUrl(url, auth.getBytes());
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
        pref_units = prefs.getString("prefUnits", "");

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

    /**
     * Adds GPS position to the track.
     * @param lat latitude
     * @param lon longitude
     * @param alt altitude
     * @param speed speed
     * @param utctime timestamp
     */
    public void addGpsPosition(double lat, double lon, double alt, double speed, long utctime) {

        GpsLocation curr = new GpsLocation(lat, lon, alt, speed, utctime);

        // Calculate track distance by adding the distance to the current position.
        if(!track.isEmpty()) {
            GpsLocation prev = track.get(track.lastKey());
            distance += prev.distanceTo(curr);
        }

        // Add new GPS position to track.
        track.put(utctime, curr);
    }

    /**
     * Updates map view labels with data of track.
     */
    public void updateViewLabels() {

        if(track.isEmpty()) {
            startTimeLabel.setText("-");
            endTimeLabel.setText("-");
            return;
        }

        // Set start time.
        GpsLocation start = track.get(track.firstKey());
        String timeString;
        Date updateDate = new Date(start.getUtctime() * 1000);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(updateDate);
        Calendar today = Calendar.getInstance();
        DateFormat df;
        if (calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR)
                && calendar.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)) {
            df = DateFormat.getTimeInstance();
        } else {
            df = DateFormat.getDateTimeInstance();
        }
        df.setTimeZone(TimeZone.getDefault());
        timeString = df.format(updateDate);
        startTimeLabel.setText(timeString);

        // Set end time.
        GpsLocation end = track.get(track.lastKey());
        updateDate = new Date(end.getUtctime() * 1000);
        calendar = Calendar.getInstance();
        calendar.setTime(updateDate);
        if (calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR)
                && calendar.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)) {
            df = DateFormat.getTimeInstance();
        } else {
            df = DateFormat.getDateTimeInstance();
        }
        df.setTimeZone(TimeZone.getDefault());
        timeString = df.format(updateDate);
        endTimeLabel.setText(timeString);

        String distanceText = String.format(Locale.getDefault(), "%.2f", distance / 1000);
        distanceText += " " + unitName;
        distanceLabel.setText(distanceText);
    }

    /**
     * Show loader
     */
    public void showLoader() {
        loadingLabel.setVisibility(View.VISIBLE);
    }

    /**
     * Hide loader
     */
    public void hideLoader() {
        loadingLabel.setVisibility(View.GONE);
    }

    /**
     * Sets loader text
     */
    public void setLoaderText(String text) {
        loadingLabel.setText(text);
    }
}
