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

import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;

public class SpinnerModeListener implements OnItemSelectedListener {

    /**
     * Is called when a new item in spinner is selected.
     */
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        // Change map button label.
        MainActivity activity = (MainActivity) parent.getContext();
        activity.switchMapBtnLabel(pos); // Live, View, Delete
    }

    public void onNothingSelected(AdapterView<?> arg0) {
        // TODO Auto-generated method stub
    }
}
