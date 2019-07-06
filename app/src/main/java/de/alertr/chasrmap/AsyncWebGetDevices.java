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

import java.util.ArrayList;

public class AsyncWebGetDevices extends AsyncTask<Void, Void, ArrayList<String>> {
    private static final String TAG = AsyncWebGetDevices.class.getSimpleName();

    private Context context;
    private String error = "";
    private int availDeviceSlots = 0;

    public AsyncWebGetDevices(Context ctx) {
        super();
        context = ctx;
    }

    protected ArrayList<String> doInBackground(@SuppressWarnings("UnusedParameters") Void... params) {

        WebHelper web = new WebHelper(context);
        try {
            web.fetchDevices();
        } catch (Exception e) {
            if (Logger.DEBUG) { Log.d(TAG, "[doInBackground failed: " + e + "]"); }
            error = e.getMessage();
        }
        availDeviceSlots = web.getAvailDeviceSlots();
        return web.getDevices();
    }

    @Override
    protected void onPostExecute (ArrayList<String> devices) {
        MainActivity activity = (MainActivity)context;
        activity.setErrorLabel(error);
        activity.updateDeviceSpinner(devices, availDeviceSlots);
    }
}