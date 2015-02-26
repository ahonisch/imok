package info.honisch.imok;

import android.content.SharedPreferences;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;


public class Settings extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Spinner spinnerWarningDelay = (Spinner) findViewById(R.id.spinner_warning_delay);
        ArrayAdapter<CharSequence> adapterWarningDelay = ArrayAdapter.createFromResource(this, R.array.select_alarm_delay, android.R.layout.simple_spinner_item);
        adapterWarningDelay.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerWarningDelay.setAdapter(adapterWarningDelay);

        Spinner spinnerAlarmDelay = (Spinner) findViewById(R.id.spinner_alarm_delay);
        ArrayAdapter<CharSequence> adapterAlarmDelay = ArrayAdapter.createFromResource(this, R.array.select_alarm_delay, android.R.layout.simple_spinner_item);
        adapterAlarmDelay.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerAlarmDelay.setAdapter(adapterAlarmDelay);

        Spinner spinnerWarningDuration = (Spinner) findViewById(R.id.spinner_warning_duration);
        ArrayAdapter<CharSequence> adapterWarningDuration = ArrayAdapter.createFromResource(this, R.array.select_alarm_duration, android.R.layout.simple_spinner_item);
        adapterWarningDuration.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerWarningDuration.setAdapter(adapterWarningDuration);

        Spinner spinnerAlarmDuration = (Spinner) findViewById(R.id.spinner_alarm_duration);
        ArrayAdapter<CharSequence> adapterAlarmDuration = ArrayAdapter.createFromResource(this, R.array.select_alarm_duration, android.R.layout.simple_spinner_item);
        adapterAlarmDuration.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerAlarmDuration.setAdapter(adapterAlarmDuration);

        initFromPrefs();
}


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }





    private void initFromPrefs() {
        //initSpinnerWarningDelayFromPref();
        initSpinnerFromPref(R.array.select_alarm_delay, MainActivity.SHARED_PREF_WARNING_DELAY, R.id.spinner_warning_delay, R.string.unit_minute);
        initSpinnerFromPref(R.array.select_alarm_duration, MainActivity.SHARED_PREF_WARNING_DURATION, R.id.spinner_warning_duration, R.string.unit_second);
        initSpinnerFromPref(R.array.select_alarm_delay, MainActivity.SHARED_PREF_ALARM_DELAY, R.id.spinner_alarm_delay, R.string.unit_minute);
        initSpinnerFromPref(R.array.select_alarm_duration, MainActivity.SHARED_PREF_ALARM_DURATION, R.id.spinner_alarm_duration, R.string.unit_second);
    }

    private void initEditTextFromPref(int arrayId, String prefName, int viewId, int unitId) {
    }

    private void initSpinnerFromPref(int arrayId, String prefName, int viewId, int unitId) {
        SharedPreferences sharedPreferences = getSharedPreferences(MainActivity.SHARED_PREF_NAME, MODE_PRIVATE);
        List<String> arrayList = Arrays.asList(getResources().getStringArray(arrayId));

        int index = arrayList.indexOf(Long.toString(Long.valueOf(sharedPreferences.getString(prefName, null)) / 60000) + " " + getString(unitId));

        Spinner spinnerWarningDelay = (Spinner) findViewById(viewId);
        spinnerWarningDelay.setSelection(index);
    }
}

