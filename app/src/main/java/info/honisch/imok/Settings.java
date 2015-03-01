package info.honisch.imok;

import android.content.SharedPreferences;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.EditText;
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
        ArrayAdapter<CharSequence> adapterWarningDelay = ArrayAdapter.createFromResource(this, R.array.select_warning_delay, android.R.layout.simple_spinner_item);
        adapterWarningDelay.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerWarningDelay.setAdapter(adapterWarningDelay);
        spinnerWarningDelay.setOnItemSelectedListener(new SpinnerSelectedListener());

        Spinner spinnerAlarmDelay = (Spinner) findViewById(R.id.spinner_alarm_delay);
        ArrayAdapter<CharSequence> adapterAlarmDelay = ArrayAdapter.createFromResource(this, R.array.select_alarm_delay, android.R.layout.simple_spinner_item);
        adapterAlarmDelay.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerAlarmDelay.setAdapter(adapterAlarmDelay);
        spinnerAlarmDelay.setOnItemSelectedListener(new SpinnerSelectedListener());

        Spinner spinnerWarningDuration = (Spinner) findViewById(R.id.spinner_warning_duration);
        ArrayAdapter<CharSequence> adapterWarningDuration = ArrayAdapter.createFromResource(this, R.array.select_alarm_duration, android.R.layout.simple_spinner_item);
        adapterWarningDuration.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerWarningDuration.setAdapter(adapterWarningDuration);
        spinnerWarningDuration.setOnItemSelectedListener(new SpinnerSelectedListener());

        Spinner spinnerAlarmDuration = (Spinner) findViewById(R.id.spinner_alarm_duration);
        ArrayAdapter<CharSequence> adapterAlarmDuration = ArrayAdapter.createFromResource(this, R.array.select_alarm_duration, android.R.layout.simple_spinner_item);
        adapterAlarmDuration.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerAlarmDuration.setAdapter(adapterAlarmDuration);
        spinnerAlarmDuration.setOnItemSelectedListener(new SpinnerSelectedListener());

        EditText txtWarningSmsTelNo = (EditText) findViewById(R.id.txt_warning_sms_telno);
        txtWarningSmsTelNo.setOnEditorActionListener(new EditTextActionListener());

        EditText txtWarningSmsText = (EditText) findViewById(R.id.txt_warning_sms_text);
        txtWarningSmsText.setOnEditorActionListener(new EditTextActionListener());

        EditText txtAlarmSmsTelno = (EditText) findViewById(R.id.txt_alarm_sms_telno);
        txtAlarmSmsTelno.setOnEditorActionListener(new EditTextActionListener());

        EditText txtAlarmSmsText = (EditText) findViewById(R.id.txt_alarm_sms_text);
        txtAlarmSmsText.setOnEditorActionListener(new EditTextActionListener());

        EditText txtManuallySmsTelno = (EditText) findViewById(R.id.txt_manually_sms_telno);
        txtManuallySmsTelno.setOnEditorActionListener(new EditTextActionListener());

        EditText txtManuallySmsText = (EditText) findViewById(R.id.txt_manually_sms_text);
        txtManuallySmsText.setOnEditorActionListener(new EditTextActionListener());

        EditText txtEmergencyTelNo = (EditText) findViewById(R.id.txt_emergency_telno);
        txtEmergencyTelNo.setOnEditorActionListener(new EditTextActionListener());

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

        initEditTextFromPref(MainActivity.SHARED_PREF_WARNING_SMS_TELNO, R.id.txt_warning_sms_telno);
        initEditTextFromPref(MainActivity.SHARED_PREF_WARNING_SMS_TEXT, R.id.txt_warning_sms_text);
        initEditTextFromPref(MainActivity.SHARED_PREF_ALARM_SMS_TELNO, R.id.txt_alarm_sms_telno);
        initEditTextFromPref(MainActivity.SHARED_PREF_ALARM_SMS_TEXT, R.id.txt_alarm_sms_text);
        initEditTextFromPref(MainActivity.SHARED_PREF_MANUALLY_SMS_TELNO, R.id.txt_manually_sms_telno);
        initEditTextFromPref(MainActivity.SHARED_PREF_MANUALLY_SMS_TEXT, R.id.txt_manually_sms_text);

        initEditTextFromPref(MainActivity.SHARED_PREF_EMERGENCY_TELNO, R.id.txt_emergency_telno);
    }

    private void initEditTextFromPref(String prefName, int viewId) {
        SharedPreferences sharedPreferences = getSharedPreferences(MainActivity.SHARED_PREF_NAME, MODE_PRIVATE);

        EditText editText = (EditText) findViewById(viewId);
        editText.setText(sharedPreferences.getString(prefName, null));
    }

    private void initSpinnerFromPref(int arrayId, String prefName, int viewId, int unitId) {
        SharedPreferences sharedPreferences = getSharedPreferences(MainActivity.SHARED_PREF_NAME, MODE_PRIVATE);
        List<String> arrayList = Arrays.asList(getResources().getStringArray(arrayId));

        long divisor = 60000;
        if (unitId == R.string.unit_second) divisor = 1000;

        int index = arrayList.indexOf(Long.toString(Long.valueOf(sharedPreferences.getString(prefName, null)) / divisor) + " " + getString(unitId));

        Spinner spinnerWarningDelay = (Spinner) findViewById(viewId);
        spinnerWarningDelay.setSelection(index);
    }
}

