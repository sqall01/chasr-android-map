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
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import static de.alertr.chasrmap.Alert.showAlert;

public abstract class JsInterfaceBase {

    protected String lastErrorMsg = "";
    protected Context context;

    public JsInterfaceBase(Context ctx) {
        context = ctx;
    }

    /**
     * Display toast message
     * @param text Message
     * @param duration Duration
     */
    private void showToast(CharSequence text, int duration) {
        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
    }

    /**
     * Handles log message of javascript code.
     * @param text Message
     */
    @JavascriptInterface
    public void console_log(String text) {
        if(text.equals("")) {
            return;
        }
        showToast(text, Toast.LENGTH_LONG);
    }

    /**
     * Handles error message of javascript code.
     * @param text Message
     */
    @JavascriptInterface
    public void console_error(String text) {

        if(text.equals("")) {
            return;
        }

        // Do not show repeatedly the same error message.
        if(text.equals(lastErrorMsg)) {
            return;
        }
        lastErrorMsg = text;

        final AlertDialog dialog = showAlert(context,
                context.getString(R.string.error),
                R.layout.error);

        final TextView errorView = dialog.findViewById(R.id.error_msg);
        errorView.setText(text);

        Button okBtn = dialog.findViewById(R.id.button_ok);
        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lastErrorMsg = "";
                dialog.dismiss();
            }
        });
    }

    @JavascriptInterface
    public void addGpsPosition(double lat, double lon, double alt, double speed, long utctime) {
    }

    @JavascriptInterface
    public void startGpsPosition(double lat, double lon, double alt, double speed, long utctime) {
    }

    @JavascriptInterface
    public void endGpsPosition(double lat, double lon, double alt, double speed, long utctime) {
    }

    @JavascriptInterface
    public void startDecryptAllGpsPositions(int numPositions) {
    }

    @JavascriptInterface
    public void endDecryptAllGpsPositions(int numPositions) {
    }

    @JavascriptInterface
    public void startDecryptGpsPosition(int numPosition) {
    }

    @JavascriptInterface
    public void endDecryptGpsPosition(int numPosition) {
    }

}
