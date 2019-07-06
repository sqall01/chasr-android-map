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
import android.os.AsyncTask;
import android.util.Log;


public class AsyncWebDeleteDevice extends AsyncTask<String, Void, Void> {
    private static final String TAG = AsyncWebDeleteDevice.class.getSimpleName();

    private Context context;
    private String error = "";

    public AsyncWebDeleteDevice(Context ctx) {
        super();
        context = ctx;
    }

    protected Void doInBackground(@SuppressWarnings("UnusedParameters") String... params) {

        String device = params[0];
        WebHelper web = new WebHelper(context);
        try {
            web.deleteDevice(device);
        } catch (Exception e) {
            if (Logger.DEBUG) { Log.d(TAG, "[doInBackground failed: " + e + "]"); }
            error = e.getMessage();
        }
        return null;
    }

    @Override
    protected void onPostExecute (@SuppressWarnings("UnusedParameters") Void param) {
        MainActivity activity = (MainActivity)context;
        activity.setErrorLabel(error);
        activity.refreshDevices();
    }
}
