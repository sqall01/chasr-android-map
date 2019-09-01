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
import android.webkit.JavascriptInterface;

public class JsInterfaceView extends JsInterfaceBase {

    public JsInterfaceView(Context ctx) {
        super(ctx);
    }

    @Override
    @JavascriptInterface
    public void addGpsPosition(double lat, double lon, double alt, double speed, long utctime) {
        MapView activity = (MapView) context;
        activity.addGpsPosition(lat, lon, alt, speed, utctime);

        // Update labels to visualize that app is working on the map.
        // NOTE: this is not as efficient as just doing it at the start and end gps position,
        // but it gives the user visual feedback that the app is doing something.
        activity.updateViewLabels();
    }
}
