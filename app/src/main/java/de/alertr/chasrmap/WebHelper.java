/*
 * written by sqall
 * Twitter: https://twitter.com/sqall01
 * Blog: https://h4des.org
 * Github: https://github.com/sqall01
 * Github Repository: https://github.com/sqall01/chasr-android-map
 *
 * original from https://github.com/bfabiszewski/ulogger-android by Bartek Fabiszewski
 *
 * This file is part of Chasr Android Logger.
 * Licensed under GPL, either version 3, or any later.
 * See <http://www.gnu.org/licenses/>
 */

package de.alertr.chasrmap;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Map;
import javax.net.ssl.HttpsURLConnection;

/**
 * Web server communication
 *
 */

class WebHelper {
    private static final String TAG = WebHelper.class.getSimpleName();

    private static String host;
    private static String user;
    private static String pass;

    // auth
    static final String PARAM_USER = "user";
    static final String PARAM_PASS = "password";

    private final String userAgent;
    private final Context context;

    // Socket timeout in milliseconds
    static final int SOCKET_TIMEOUT = 30 * 1000;

    private ArrayList<String> devices = new ArrayList<String>();
    private int availDeviceSlots = 0;

    /**
     * Constructor
     * @param ctx Context
     */
    WebHelper(Context ctx) {
        context = ctx;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        user = prefs.getString("prefUsername", "");
        pass = prefs.getString("prefPass", "");
        host = prefs.getString("prefHost", "").replaceAll("/+$", "");
        userAgent = context.getString(R.string.app_name) + "/" + BuildConfig.VERSION_NAME + "; " + System.getProperty("http.agent");
    }

    /**
     * Deletes given device from server.
     * @param device device to delete
     * @throws IOException Connection error
     */
    void deleteDevice(String device) throws IOException  {
        String urlStr = host + "/delete.php?mode=device&device=";
        try {
            urlStr += URLEncoder.encode(device, "UTF-8");
        }
        catch (UnsupportedEncodingException e) {
            throw new IOException(e.getMessage());
        }
        Map<String, String> params = new HashMap<>();
        params.put(PARAM_USER, user);
        params.put(PARAM_PASS, pass);

        String response = postWithParams(params, urlStr);
        int code = -1;
        JSONObject json = null;
        try {
            json = new JSONObject(response);
            code = json.getInt("code");
        } catch (JSONException e) {
            if (Logger.DEBUG) { Log.d(TAG, "[deleteDevice json failed: " + e + "]"); }
        }
        if (code == WebErrorCodes.NO_ERROR) {
            return;
        }
        else if (code == WebErrorCodes.DATABASE_ERROR) {
            throw new IOException(context.getString(R.string.e_database_error));
        }
        else if (code == WebErrorCodes.AUTH_ERROR) {
            throw new IOException(context.getString(R.string.e_auth_error));
        }
        else if (code == WebErrorCodes.ILLEGAL_MSG_ERROR) {
            throw new IOException(context.getString(R.string.e_illegal_msg_error));
        }
        else if (code == WebErrorCodes.SESSION_EXPIRED) {
            throw new IOException(context.getString(R.string.e_session_expired));
        }
        else if(code == -1) {
            throw new IOException(context.getString(R.string.e_unknown_server));
        }
        else {
            throw new IOException(context.getString(R.string.e_unknown));
        }
    }

    /**
     * Fetches all devices from the server.
     * @throws IOException Connection error
     */
    void fetchDevices() throws IOException {

        String urlStr = host + "/get.php?mode=devices";
        Map<String, String> params = new HashMap<>();
        params.put(PARAM_USER, user);
        params.put(PARAM_PASS, pass);

        String response = postWithParams(params, urlStr);
        int code = -1;
        JSONObject json = null;
        try {
            json = new JSONObject(response);
            code = json.getInt("code");
        } catch (JSONException e) {
            if (Logger.DEBUG) { Log.d(TAG, "[fetchDevices json failed: " + e + "]"); }
        }
        if (code == WebErrorCodes.NO_ERROR) {

            // Extract devices from server response.
            try {
                JSONObject jsonData = json.getJSONObject("data");
                JSONArray jsonDevices = jsonData.getJSONArray("devices");
                for (int i = 0; i < jsonDevices.length(); i++) {
                    devices.add(jsonDevices.getJSONObject(i).getString("device_name"));
                }
                availDeviceSlots = jsonData.getInt("avail_slots");
            }
            catch (JSONException e) {
                throw new IOException(context.getString(R.string.e_unknown_server));
            }

        }
        else if (code == WebErrorCodes.DATABASE_ERROR) {
            throw new IOException(context.getString(R.string.e_database_error));
        }
        else if (code == WebErrorCodes.AUTH_ERROR) {
            throw new IOException(context.getString(R.string.e_auth_error));
        }
        else if (code == WebErrorCodes.ILLEGAL_MSG_ERROR) {
            throw new IOException(context.getString(R.string.e_illegal_msg_error));
        }
        else if (code == WebErrorCodes.SESSION_EXPIRED) {
            throw new IOException(context.getString(R.string.e_session_expired));
        }
        else if (code == WebErrorCodes.ACL_ERROR) {
            throw new IOException(context.getString(R.string.e_acl_error));
        }
        else if(code == -1) {
            throw new IOException(context.getString(R.string.e_unknown_server));
        }
        else {
            throw new IOException(context.getString(R.string.e_unknown));
        }
    }

    /**
     * Get the fetched devices.
     * @return fetched devices.
     */
    ArrayList<String> getDevices() {
        return devices;
    }

    /**
     * Get available device slots.
     * @return device slots.
     */
    public int getAvailDeviceSlots() {
        return availDeviceSlots;
    }

    /**
     * Send post request
     * @param params Request parameters
     * @param urlStr URL to request
     * @return Server response
     * @throws IOException Connection error
     */
    @SuppressWarnings("StringConcatenationInLoop")
    private String postWithParams(Map<String, String> params, String urlStr) throws IOException {

        URL url = new URL(urlStr);
        if (Logger.DEBUG) { Log.d(TAG, "[postWithParams: " + url + " : " + params + "]"); }
        String response;

        // Encode data for POST request (key1=value1&key2=value2&...).
        String dataString = "";
        for (Map.Entry<String, String> p : params.entrySet()) {
            String key = p.getKey();
            String value = p.getValue();
            if (dataString.length() > 0) {
                dataString += "&";
            }
            dataString += URLEncoder.encode(key, "UTF-8") + "=";
            dataString += URLEncoder.encode(value, "UTF-8");
        }
        byte[] data = dataString.getBytes();

        // Perform https request.
        HttpsURLConnection connection = null;
        InputStream in = null;
        OutputStream out = null;
        try {
            boolean redirect;
            int redirectTries = 5;
            do {
                redirect = false;
                connection = (HttpsURLConnection) url.openConnection();
                connection.setDoOutput(true);
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                connection.setRequestProperty("Content-Length", Integer.toString(data.length));
                connection.setRequestProperty("User-Agent", userAgent);
                connection.setInstanceFollowRedirects(false);
                connection.setConnectTimeout(SOCKET_TIMEOUT);
                connection.setReadTimeout(SOCKET_TIMEOUT);
                connection.setUseCaches(true);

                out = new BufferedOutputStream(connection.getOutputStream());
                out.write(data);
                out.flush();

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpsURLConnection.HTTP_MOVED_PERM
                        || responseCode == HttpsURLConnection.HTTP_MOVED_TEMP
                        || responseCode == HttpsURLConnection.HTTP_SEE_OTHER
                        || responseCode == 307) {
                    URL base = connection.getURL();
                    String location = connection.getHeaderField("Location");
                    if (Logger.DEBUG) { Log.d(TAG, "[postWithParams redirect: " + location + "]"); }
                    if (location == null || redirectTries == 0) {
                        throw new IOException(context.getString(R.string.e_illegal_redirect, responseCode));
                    }
                    redirect = true;
                    redirectTries--;
                    url = new URL(base, location);
                    String h1 = base.getHost();
                    String h2 = url.getHost();
                    if (h1 != null && !h1.equalsIgnoreCase(h2)) {
                        throw new IOException(context.getString(R.string.e_illegal_redirect, responseCode));
                    }
                    try {
                        out.close();
                        connection.getInputStream().close();
                        connection.disconnect();
                    } catch (final IOException e) {
                        if (Logger.DEBUG) { Log.d(TAG, "[connection cleanup failed (ignored)]"); }
                    }
                }
                else if (responseCode != HttpsURLConnection.HTTP_OK) {
                    throw new IOException(context.getString(R.string.e_http_code, responseCode));
                }
            } while (redirect);

            in = new BufferedInputStream(connection.getInputStream());

            StringBuilder sb = new StringBuilder();
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String inputLine;
            while ((inputLine = br.readLine()) != null) {
                sb.append(inputLine);
            }
            response = sb.toString();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
                if (in != null) {
                    in.close();
                }
                if (connection != null) {
                    connection.disconnect();
                }
            } catch (final IOException e) {
                if (Logger.DEBUG) { Log.d(TAG, "[connection cleanup failed (ignored)]"); }
            }
        }
        if (Logger.DEBUG) { Log.d(TAG, "[postWithParams response: " + response + "]"); }
        return response;

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
     * Check whether given url is valid.
     * Uses relaxed pattern (@see WebPatterns#WEB_URL_RELAXED)
     * @param url URL
     * @return True if valid, false otherwise
     */
    static boolean isValidURL(String url) {
        return WebPatterns.WEB_URL_RELAXED.matcher(url).matches();
    }

}
