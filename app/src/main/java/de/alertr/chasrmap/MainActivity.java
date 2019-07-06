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
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import static de.alertr.chasrmap.Alert.showAlert;


public class MainActivity extends AppCompatActivity {

    private final String TAG = MainActivity.class.getSimpleName();

    private final static int RESULT_PREFS_UPDATED = 1;

    private Button refreshButton;
    private Button mapButton;
    private Spinner deviceSpinner;
    private Spinner modeSpinner;
    private TextView errorLabel;
    private TextView startDateLabel;
    private TextView endDateLabel;
    private TextView deviceSlotLabel;
    private LinearLayout selectorElements;
    private LinearLayout dateSelector;
    private LinearLayout loader;
    private TableLayout dateViewer;
    private ArrayAdapter<String> deviceDataAdapter;
    private boolean pref_username_set;
    private boolean pref_password_set;
    private boolean pref_secret_set;

    private int viewMode = 0; // Live, View, Delete

    int startYear = 0;
    int startMonth = 0;
    int startDay = 0;
    int endYear = 0;
    int endMonth = 0;
    int endDay = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        updatePreferences();
        setContentView(R.layout.activity_main);
        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        refreshButton = findViewById(R.id.button_refresh);
        mapButton = findViewById(R.id.button_map);

        // Set up spinner for device selection.
        deviceSpinner = findViewById(R.id.device_spinner);
        deviceSpinner.setOnItemSelectedListener(new SpinnerDeviceListener());
        ArrayList<String> deviceList = new ArrayList<String>();
        deviceDataAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_dropdown_item, deviceList);
        deviceSpinner.setAdapter(deviceDataAdapter);

        deviceSlotLabel = findViewById(R.id.slots_label);

        // Set up spinner for mode selection.
        modeSpinner = findViewById(R.id.mode_spinner);
        modeSpinner.setOnItemSelectedListener(new SpinnerModeListener());

        errorLabel = findViewById(R.id.error);
        selectorElements = findViewById(R.id.selector_elements);
        dateSelector = findViewById(R.id.date_selector);
        startDateLabel = findViewById(R.id.start_date_label);
        endDateLabel = findViewById(R.id.end_date_label);
        dateViewer = findViewById(R.id.date_viewer);
        loader = findViewById(R.id.loader);
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

        // If needed chasr settings are not configured, open settings.
        if(!pref_username_set
                || !pref_password_set
                || !pref_secret_set) {
            showGoSettings();
            showToast(getString(R.string.provide_chasr_settings), Toast.LENGTH_LONG);
        }

        // Fetch devices from server.
        refreshDevices();
    }

    /**
     * On destroy
     */
    @Override
    protected void onDestroy() {
        if (Logger.DEBUG) { Log.d(TAG, "[onDestroy]"); }
        super.onDestroy();
    }

    /**
     * Create main menu
     * @param menu Menu
     * @return Always true
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    /**
     * Main menu options
     * @param item Selected option
     * @return True if handled
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.menu_settings:
                Intent i = new Intent(MainActivity.this, SettingsActivity.class);
                startActivityForResult(i, RESULT_PREFS_UPDATED);
                return true;
            case R.id.menu_about:
                showAbout();
                return true;

            default:
                return super.onOptionsItemSelected(item);

        }
    }

    /**
     * Callback on activity result.
     * Called after user updated preferences
     *
     * @param requestCode Activity code
     * @param resultCode Result
     * @param data Data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case RESULT_PREFS_UPDATED:
                // Preferences updated
                updatePreferences();
                break;
        }
    }

    /**
     * Reread user preferences
     */
    private void updatePreferences() {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String pref_username = prefs.getString("prefUsername", "");
        pref_username_set = !pref_username.equals("");
        String pref_pass = prefs.getString("prefPass", "");
        pref_password_set = !pref_pass.equals("");
        String pref_secret = prefs.getString("prefSecret", "");
        pref_secret_set = !pref_secret.equals("");
    }

    /**
     * Display Go Settings dialog
     */
    private void showGoSettings() {
        final AlertDialog dialog = showAlert(MainActivity.this,
                getString(R.string.app_name),
                R.layout.go_settings,
                R.drawable.ic_chasr_logo);
        final TextView descriptionLabel = dialog.findViewById(R.id.go_settings_description);
        descriptionLabel.setText(getString(R.string.go_settings_description));
        final Button goButton = dialog.findViewById(R.id.go_settings_button);

        goButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                Intent i = new Intent(MainActivity.this, SettingsActivity.class);
                startActivityForResult(i, RESULT_PREFS_UPDATED);
            }
        });
    }

    /**
     * Display About dialog
     */
    private void showAbout() {
        final AlertDialog dialog = showAlert(MainActivity.this,
                getString(R.string.app_name),
                R.layout.about,
                R.drawable.ic_chasr_logo);
        final TextView versionLabel = dialog.findViewById(R.id.about_version);
        versionLabel.setText(getString(R.string.about_version, BuildConfig.VERSION_NAME));
        final TextView descriptionLabel = dialog.findViewById(R.id.about_description);
        final TextView description2Label = dialog.findViewById(R.id.about_description2);
        descriptionLabel.setText(getString(R.string.about_description));
        description2Label.setText(getString(R.string.about_description2));
        final Button okButton = dialog.findViewById(R.id.about_button_ok);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }

    /**
     * Display delete confirmation dialog
     */
    private void showDeleteConfirmation(final String device) {

        final AlertDialog dialog = showAlert(MainActivity.this,
                getString(R.string.app_name),
                R.layout.delete_confirmation,
                R.drawable.ic_chasr_logo);
        final TextView confirm_label = dialog.findViewById(R.id.confirm_label);
        confirm_label.setText(getString(R.string.delete_confirmation, device));

        final Button okButton = dialog.findViewById(R.id.confirm_button_ok);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Logger.DEBUG) { Log.d(TAG, "[delete confirmation ok]"); }
                deleteDevice(device);
                dialog.dismiss();
            }
        });

        final Button cancelButton = dialog.findViewById(R.id.confirm_button_cancel);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }

    /**
     * Display toast message
     * @param text Message
     * @param duration Duration
     */
    private void showToast(CharSequence text, int duration) {
        Context context = getApplicationContext();
        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
    }

    /**
     * Display error message
     * @param text Message
     */
    public void setErrorLabel(String text) {
        errorLabel.setText(text);
    }

    /**
     * Show selector Gui elements
     */
    private void showLoader() {
        selectorElements.setVisibility(View.GONE);
        errorLabel.setVisibility(View.GONE);
        mapButton.setVisibility(View.GONE);
        if(viewMode == 1) { // View
            hideDateSelection();
        }
        loader.setVisibility(View.VISIBLE);
    }

    /**
     * Show date selector Gui elements
     */
    private void showDateSelection() {
        dateSelector.setVisibility(View.VISIBLE);
        dateViewer.setVisibility(View.VISIBLE);
    }

    /**
     * Hide selector Gui elements
     */
    private void hideLoader() {
        loader.setVisibility(View.GONE);
        selectorElements.setVisibility(View.VISIBLE);
        errorLabel.setVisibility(View.VISIBLE);
        mapButton.setVisibility(View.VISIBLE);
        if(viewMode == 1) { // View
            showDateSelection();
        }
    }

    /**
     * Hide date selector Gui elements
     */
    private void hideDateSelection() {
        dateSelector.setVisibility(View.GONE);
        dateViewer.setVisibility(View.GONE);
    }

    /**
     * Called when the user clicks the Refresh button
     * @param view View
     */
    public void refreshBtn(@SuppressWarnings("UnusedParameters") View view) {
        refreshDevices();
    }

    /**
     * Gets the devices from the server and refreshs them locally.
     */
    public void refreshDevices() {
        showLoader();

        // Get devices from server.
        AsyncWebGetDevices async = new AsyncWebGetDevices(this);
        async.execute();
    }

    /**
     * Called by AsyncWebGetDevices to update device spinner and device slots
     * @param devices List of all devices available
     * @param availDeviceSlots Number of device slots available for the user
     */
    public void updateDeviceSpinner(ArrayList<String> devices, int availDeviceSlots) {
        deviceDataAdapter.clear();
        for(String device: devices) {
            deviceDataAdapter.add(device);
        }

        int remainingDeviceSlots = availDeviceSlots - devices.size();
        String message = "";
        if(devices.size() == 1) {
            message += getString(R.string.devices_stored_single, devices.size());
        }
        else {
            message += getString(R.string.devices_stored_multi, devices.size());
        }
        message += " (";
        if(remainingDeviceSlots == 1) {
            message += getString(R.string.slots_left_single, remainingDeviceSlots);
        }
        else {
            message += getString(R.string.slots_left_multi, remainingDeviceSlots);
        }
        message += ")";
        deviceSlotLabel.setText(message);

        hideLoader();
    }

    /**
     * Deletes given device from the server.
     * @param device device to delete
     */
    private void deleteDevice(String device) {

        showLoader();

        // Delete device from server.
        AsyncWebDeleteDevice async = new AsyncWebDeleteDevice(this);
        async.execute(device);
    }

    /**
     * Called when the user clicks the Map button
     * @param view View
     */
    public void mapBtn(@SuppressWarnings("UnusedParameters") View view) {

        final String device;
        try {
            device = deviceSpinner.getSelectedItem().toString();
        }
        catch (NullPointerException e) {
            if (Logger.DEBUG) { Log.e(TAG, "[mapBtn] No device available"); }
            return;
        }

        switch(viewMode) {
            case 0: { // Live
                Intent i = new Intent(MainActivity.this, MapLive.class);
                i.putExtra("device", device);
                startActivity(i);
                break;
            }

            case 1: { // View
                if (startYear == 0 && startMonth == 0 && startDay == 0) {
                    showToast(getString(R.string.e_start_date_missing), Toast.LENGTH_LONG);
                } else if (endYear == 0 && endMonth == 0 && endDay == 0) {
                    showToast(getString(R.string.e_end_date_missing), Toast.LENGTH_LONG);
                } else {
                    long startTime = convertDateToTimestamp(startYear, startMonth, startDay) / 1000;
                    // End time + 23h 59min 59sec
                    long endTime = (convertDateToTimestamp(endYear, endMonth, endDay) / 1000) + 86399;
                    if (endTime < startTime) {
                        showToast(getString(R.string.e_start_end_date_wrong), Toast.LENGTH_LONG);
                    } else {
                        Intent i = new Intent(MainActivity.this, MapView.class);
                        i.putExtra("device", device);
                        i.putExtra("start_time", startTime);
                        i.putExtra("end_time", endTime);
                        startActivity(i);
                    }
                }
                break;
            }

            case 2: // Delete
                showDeleteConfirmation(device);
                break;

            default:
                break;
        }
    }

    /**
     * Switch view mode.
     * @param spinnerPos position in the mode spinner (Live, View, Delete).
     */
    public void switchMapBtnLabel(int spinnerPos) {
        switch(spinnerPos) {
            case 0:
                viewMode = spinnerPos;
                mapButton.setText(getString(R.string.button_map_live));
                hideDateSelection();
                break;
            case 1:
                viewMode = spinnerPos;
                mapButton.setText(getString(R.string.button_map_view));
                showDateSelection();
                break;
            case 2:
                viewMode = spinnerPos;
                mapButton.setText(getString(R.string.button_map_delete));
                hideDateSelection();
                break;
            default: // We should never reach this state.
                mapButton.setText(getString(R.string.error));
                break;
        }
    }

    /**
     * Converts given date to a formatted string date.
     * @param year year
     * @param month month of year
     * @param day day of month
     * @return formatted string of the date
     */
    static private String convertDateToString(int year, int month, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day, 0, 0 ,0);
        DateFormat df = DateFormat.getDateInstance();
        return df.format(calendar.getTime());
    }

    /**
     * Converts given date to a timestamp
     * @param year year
     * @param month month of year
     * @param day day of month
     * @return timestamp of date
     */
    static private long convertDateToTimestamp(int year, int month, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day, 0, 0 ,0);
        return calendar.getTime().getTime();
    }

    /**
     * Called when the user clicks the set start date button
     * @param view View
     */
    public void startDatePickerBtn(@SuppressWarnings("UnusedParameters") View view) {
        final AlertDialog dialog = showAlert(MainActivity.this,
                getString(R.string.start_date),
                R.layout.date_picker);

        final DatePicker startDatePicker = dialog.findViewById(R.id.date_picker);

        if(startYear != 0 && startMonth != 0 && startDay != 0) {
            startDatePicker.updateDate(startYear, startMonth, startDay);
        }

        // Disable start dates after the set end date.
        if(endYear != 0 && endMonth != 0 && endDay != 0) {
            startDatePicker.setMaxDate(convertDateToTimestamp(endYear, endMonth, endDay));
        }

        Button okBtn = dialog.findViewById(R.id.button_ok);
        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startYear = startDatePicker.getYear();
                startMonth = startDatePicker.getMonth();
                startDay = startDatePicker.getDayOfMonth();
                startDateLabel.setText(convertDateToString(startYear, startMonth, startDay));
                dialog.dismiss();
            }
        });
    }

    /**
     * Called when the user clicks the set end date button
     * @param view View
     */
    public void endDatePickerBtn(@SuppressWarnings("UnusedParameters") View view) {
        final AlertDialog dialog = showAlert(MainActivity.this,
                getString(R.string.end_date),
                R.layout.date_picker);

        final DatePicker endDatePicker = dialog.findViewById(R.id.date_picker);

        if(endYear != 0 && endMonth != 0 && endDay != 0) {
            endDatePicker.updateDate(endYear, endMonth, endDay);
        }

        // Disable end dates before the set start date.
        if(startYear != 0 && startMonth != 0 && startDay != 0) {
            endDatePicker.setMinDate(convertDateToTimestamp(startYear, startMonth, startDay));
        }

        Button okBtn = dialog.findViewById(R.id.button_ok);
        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                endYear = endDatePicker.getYear();
                endMonth = endDatePicker.getMonth();
                endDay = endDatePicker.getDayOfMonth();
                endDateLabel.setText(convertDateToString(endYear, endMonth, endDay));
                dialog.dismiss();
            }
        });
    }

}
