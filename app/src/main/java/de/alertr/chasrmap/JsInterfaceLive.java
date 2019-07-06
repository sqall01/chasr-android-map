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
import android.widget.Toast;

public class JsInterfaceLive extends JsInterfaceBase {

    public JsInterfaceLive(Context ctx) {
        super(ctx);
    }

    @Override
    @JavascriptInterface
    public void addGpsPosition(double lat, double lon, double alt, double speed, long utctime) {
        MapLive activity = (MapLive)context;
        activity.setLatestGps(lat, lon, utctime);
    }
}
