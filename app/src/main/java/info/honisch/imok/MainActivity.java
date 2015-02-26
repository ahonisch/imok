package info.honisch.imok;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.PersistableBundle;
import android.os.Vibrator;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;


public class MainActivity extends ActionBarActivity {
    // Shared Preferences
    final public static String SHARED_PREF_NAME = "info.honisch.imok.prefs";
    final public static String SHARED_PREF_NEXT_ALARM = "info.honisch.imok.prefs.NEXT_ALARM";
    final public static String SHARED_PREF_ALARM_TYPE = "info.honisch.imok.prefs.ALARM_TYPE";

    final public static String SHARED_PREF_WARNING_DELAY = "info.honisch.imok.prefs.warningDelay";
    final public static String SHARED_PREF_WARNING_DURATION = "info.honisch.imok.prefs.warningSoundDuration";
    final public static String SHARED_PREF_WARNING_SMS_TEXT = "info.honisch.imok.prefs.warningSmsText";
    final public static String SHARED_PREF_WARNING_SMS_TELNO = "info.honisch.imok.prefs.n";
    final public static String SHARED_PREF_WARNING_VIBRATE_PATTERN = "info.honisch.imok.prefs.warningVibratePattern";

    final public static String SHARED_PREF_ALARM_DELAY = "info.honisch.imok.prefs.alarmDelay";
    final public static String SHARED_PREF_ALARM_DURATION = "info.honisch.imok.prefs.QUALIFIER_ALARM_SOUND_DURATION";
    final public static String SHARED_PREF_ALARM_SMS_TEXT = "info.honisch.imok.prefs.alarmSmsText";
    final public static String SHARED_PREF_ALARM_SMS_TELNO = "info.honisch.imok.prefs.alarmSmsTelno";
    final public static String SHARED_PREF_ALARM_VIBRATE_PATTERN = "info.honisch.imok.prefs.alarmVibratePattern";

    final public static String SHARED_PREF_MANUALLY_SMS_TEXT = "info.honisch.imok.prefs.manuallySmsText";
    final public static String SHARED_PREF_MANUALLY_SMS_TELNO = "info.honisch.imok.prefs.manuallySmsTelno";

    final public static String SHARED_PREF_EMERGENCY_TELNO = "info.honisch.imok.prefs.emergencyTelno";

    // Qualifiers
    final public static String QUALIFIER_ALARM_TYPE = "Alarm Type";
    final public static String QUALIFIER_ALARM_SOUND_DURATION = "Alarm Sound Duration";
    final public static String QUALIFIER_ALARM_VIBRATE_PATTERN = "Alarm Vibrate Pattern";
    final public static String QUALIFIER_SEQUENCE_ALARM_TYPE = "Sequence Alarm Type";
    final public static String QUALIFIER_SEQUENCE_ALARM_START_TIME = "Sequence Alarm Time";
    final public static String QUALIFIER_SEQUENCE_ALARM_SOUND_DURATION = "Sequence Alarm Sound Duration";
    final public static String QUALIFIER_SEQUENCE_VIBRATE_PATTERN = "Sequence Vibrate Pattern";
    final public static String QUALIFIER_ALARM_SMS_TELNO = "SMS TelNo";
    final public static String QUALIFIER_ALARM_SMS_TEXT = "SMS Text";
    final public static String QUALIFIER_SEQUENCE_SMS_TELNO = "Sequence SMS TelNo";
    final public static String QUALIFIER_SEQUENCE_SMS_TEXT = "Sequence SMS TelNo";

    // Alarm Types
    final public static int ALARM_TYPE_UNKNOWN = -1;
    final public static int ALARM_TYPE_SOUNDOFF = 0;
    final public static int ALARM_TYPE_WARNING = 1;
    final public static int ALARM_TYPE_ALARM = 2;
    final public static int ALARM_TYPE_SLEEPING = 3;
    final public static int ALARM_TYPE_MANUALLY = 4;

    // Defaults
    final public static long WARNING_SOUND_DURATION = 30 * 1000; // 30 sec
    final public static long ALARM_SOUND_DURATION = 30 * 1000; // 30 sec
    final public static String WARNING_VIBRATE_PATTERN = "EEEEEEEEEE";
    final public static String ALARM_VIBRATE_PATTERN = "EEEEEEEEEEEEEEEEEEEE";

    // Status
    final static int STATUS_INIT = 0;
    final static int STATUS_IMOK = 1;
    final static int STATUS_WARNING = 2;
    final static int STATUS_ALARM = 3;
    final static int STATUS_SLEEPING = -1;

    // Confirm Button
    final static long[] CONFIRM_VIBRATE_PATTERN = {0,500,0};

    // UI
    final static int TIMER_BACKGROUND_COLOR_WARNING = 0xffbcffbd;
    final static int TIMER_BACKGROUND_COLOR_ALARM = 0xffff9b93;
    final static int TIMER_BACKGROUND_COLOR_NOK = 0xfffffbd1;
    final static int TIMER_BACKGROUND_COLOR_SLEEPING = 0xfffffbd1;


    // class variables
    private long m_nextAlarm;
    private int m_Status = STATUS_INIT;
    private AlarmManagerBroadcastReceiver m_Alarm;

    // Warning
    long warningDelay = 30 * 60 * 1000; // 1 min to waiting
    long warningSoundDuration = WARNING_SOUND_DURATION;
    String warningSmsText = "I'm ok - Warning! ";
    String warningSmsTelno= "";
    String warningVibratePattern = WARNING_VIBRATE_PATTERN;

    // Alarm
    long alarmDelay = 10 * 60 * 1000; // 1 min to alarm
    long alarmSoundDuration = 30 * 1000; // 30 sec
    String alarmSmsText = "I'm not ok - Alarm! ";
    String alarmSmsTelno = "";
    String alarmVibratePattern = ALARM_VIBRATE_PATTERN; // vibrate stackato

    // Manually
    String manuallySmsText = "I'm ok - Notify! ";
    String manuallySmsTelno = "";

    // Emergency Call
    String emergencyTelno = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d("I'm ok", "onCreate");

        setContentView(R.layout.layout_reset);

        Chronometer chronometer = (Chronometer)findViewById(R.id.chronometer);
        chronometer.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
            @Override
            public void onChronometerTick(Chronometer chronometer) {
                handleChronometerTick();
            }
        });

        ImageButton btnSleeping = (ImageButton) findViewById(R.id.btn_Sleeping);
        btnSleeping.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                handleBtnSleepingLongClick();

                return true;
            }
        });

        ImageButton btnReset = (ImageButton) findViewById(R.id.btn_Reset);
        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleBtnResetClick();
            }
        });
        btnReset.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                handleBtnResetLongClick();

                return true;
            }
        });

        ImageButton btnEmergencyCall = (ImageButton) findViewById(R.id.btn_emergency_call);
        btnEmergencyCall.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                handleBtnEmergencyCallLongClick();

                return true;
            }
        });

        initActivity();
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onRestoreInstanceState(savedInstanceState);

        Log.d("I'm ok", "onRestoreInstanceState");
        initActivity();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            openSettings();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    // *** private methods ***
    private void initActivity() {
        Log.d("I'm ok", "initActivity");

        m_Alarm = new AlarmManagerBroadcastReceiver();

        Chronometer chronometer = (Chronometer)findViewById(R.id.chronometer);
        chronometer.setBase(System.currentTimeMillis());

        chronometer.stop();
        chronometer.start();

        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREF_NAME, MODE_PRIVATE);
        String restoredText = sharedPreferences.getString(SHARED_PREF_ALARM_TYPE, null);

        m_Status = STATUS_IMOK;

        if (restoredText == null) {
            createSharedPref();
            setAlarm();
        }

        initActivityFromPref();

        updateGui();
    }

    private void initActivityFromPref() {
        Log.d("I'm ok", "initActivityFromPref");

        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREF_NAME, MODE_PRIVATE);
        String prefAlarmType = sharedPreferences.getString(SHARED_PREF_ALARM_TYPE, null);
        String prefNextAlarm = sharedPreferences.getString(SHARED_PREF_NEXT_ALARM, null);

        Log.d("I'm ok", "initActivityFromPref (" + prefAlarmType + "/" + Long.toString((Long.valueOf(prefNextAlarm) - System.currentTimeMillis()) / 1000) + ")");

        int alarmType = Integer.valueOf(prefAlarmType);
        m_nextAlarm = Long.valueOf(prefNextAlarm);

        switch (alarmType) {
            case ALARM_TYPE_WARNING:
                if (m_nextAlarm < System.currentTimeMillis()) setAlarm();

                m_Status = STATUS_IMOK;
                Log.d("I'm ok", "STATUS_IMOK");
                break;
            case ALARM_TYPE_ALARM:
                if (m_nextAlarm < System.currentTimeMillis()) {
                    m_Status = STATUS_IMOK;
                    Log.d("I'm ok", "STATUS_IMOK");
                    setAlarm();
                }
                else {
                    m_Status = STATUS_WARNING;
                    Log.d("I'm ok", "STATUS_WARNING");
                }
                break;
            case ALARM_TYPE_UNKNOWN:
                m_Status = STATUS_ALARM;
                Log.d("I'm ok", "STATUS_ALARM");
                break;
            case ALARM_TYPE_SLEEPING:
                m_Status = STATUS_SLEEPING;
                Log.d("I'm ok", "STATUS_SLEEPING");
                break;
        }
    }

    private void openSettings() {
        Intent intent = new Intent(this, Settings.class);
        startActivity(intent);
    }

    private void createSharedPref() {
        Log.i("I'm ok", "createSharedPref");

        SharedPreferences.Editor editor = getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE).edit();
        editor.putString(SHARED_PREF_ALARM_TYPE, String.valueOf(ALARM_TYPE_UNKNOWN));
        editor.putString(SHARED_PREF_NEXT_ALARM, String.valueOf(System.currentTimeMillis() - 1));

        editor.putString(SHARED_PREF_WARNING_DELAY, String.valueOf(warningDelay));
        editor.putString(SHARED_PREF_WARNING_DURATION, String.valueOf(warningSoundDuration));
        editor.putString(SHARED_PREF_WARNING_SMS_TEXT, warningSmsText);
        editor.putString(SHARED_PREF_WARNING_SMS_TELNO, warningSmsTelno);
        editor.putString(SHARED_PREF_WARNING_VIBRATE_PATTERN, warningVibratePattern);

        editor.putString(SHARED_PREF_ALARM_DELAY, String.valueOf(alarmDelay));
        editor.putString(SHARED_PREF_ALARM_DURATION, String.valueOf(alarmSoundDuration));
        editor.putString(SHARED_PREF_ALARM_SMS_TEXT, alarmSmsText);
        editor.putString(SHARED_PREF_ALARM_SMS_TELNO, alarmSmsTelno);
        editor.putString(SHARED_PREF_ALARM_VIBRATE_PATTERN, alarmVibratePattern);

        editor.putString(SHARED_PREF_MANUALLY_SMS_TEXT, manuallySmsText);
        editor.putString(SHARED_PREF_MANUALLY_SMS_TELNO, manuallySmsTelno);

        editor.putString(SHARED_PREF_EMERGENCY_TELNO, emergencyTelno);
        editor.commit();
    }

    private void handleBtnEmergencyCallLongClick() {
        Log.d("I'm ok", "handleBtnEmergencyCallLongClick");

        if (m_Status == STATUS_INIT) return;

        confirmLongClick();

        m_Status = STATUS_IMOK;

        stopCounter(false);

        startCounter();

        updateGui();

        moveTaskToBack(true);

        Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse("tel:" + emergencyTelno));
        callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(callIntent);
    }

    private void handleBtnResetLongClick() {
        Log.d("I'm ok", "handleBtnResetLongClick");

        confirmLongClick();

        sendSms();

        updateGui();

        moveTaskToBack(true);
    }

    private void handleBtnResetClick() {
        Log.d("I'm ok", "handleBtnResetClick");

        if (m_Status == STATUS_INIT) return;

        m_Status = STATUS_IMOK;

        stopCounter();

        startCounter();

        updateGui();

        moveTaskToBack(true);
    }

    private void handleBtnSleepingLongClick() {
        Log.d("I'm ok", "handleBtnSleepingLongClick");

        if (m_Status == STATUS_INIT) return;

        m_Status = STATUS_SLEEPING;

        stopCounter();

        m_Alarm.writePref(this.getApplicationContext(), ALARM_TYPE_SLEEPING, System.currentTimeMillis() - 1);

        updateGui();

        moveTaskToBack(true);
    }

    private void handleChronometerTick() {
        if (m_Status == STATUS_INIT) return;

        updateGui();
    }

    private void startCounter() {
        Log.d("I'm ok", "startCounter");

        Chronometer chronometer = (Chronometer)findViewById(R.id.chronometer);
        chronometer.setBase(System.currentTimeMillis());

        setAlarm();
        chronometer.start();
    }

    private void stopCounter() {
        stopCounter(true);
    }

    private void stopCounter(Boolean turnOffVibrator) {
        Log.d("I'm ok", "stopCounter");

        cancelAlarm();
        Chronometer chronometer = (Chronometer)findViewById(R.id.chronometer);
        chronometer.stop();
        if (turnOffVibrator) cancelVibrator();
        cancelRingtone();
    }

    private void cancelVibrator() {
        Log.d("I'm ok", "cancelVibrator");

        Context context = this.getApplicationContext();
        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);

        if (vibrator.hasVibrator()) vibrator.cancel();
    }

    private void cancelRingtone() {
        Log.d("I'm ok", "cancelRingtone");

        Intent alarmRingtoneService = new Intent(this.getApplicationContext(), AlarmRingtoneService.class);
        stopService(alarmRingtoneService);
    }

    private void setAlarm() {
        Log.d("I'm ok", "setAlarm");

        Context context = this.getApplicationContext();

        long timeToWarning = warningDelay;
        long timeToAlarm = alarmDelay;
        long alarmSoundDuration = warningSoundDuration;
        long sequenceAlarmSoundDuration = this.alarmSoundDuration;

        m_nextAlarm = System.currentTimeMillis() + timeToWarning;
        long sequenceAlarmStartTime = System.currentTimeMillis() + timeToWarning + timeToAlarm;

        m_Alarm.setAlarm(context,
                warningSmsTelno, warningSmsText,
                m_nextAlarm, ALARM_TYPE_WARNING, alarmSoundDuration, warningVibratePattern,
                alarmSmsTelno, alarmSmsText,
                sequenceAlarmStartTime, ALARM_TYPE_ALARM, sequenceAlarmSoundDuration, alarmVibratePattern);
        m_Alarm.writePref(context, ALARM_TYPE_WARNING, m_nextAlarm);

    }

    private void confirmLongClick() {
        Log.d("I'm ok", "confirmLongClick");

        Context context = this.getApplicationContext();
        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);

        if (vibrator.hasVibrator()) vibrator.vibrate(CONFIRM_VIBRATE_PATTERN, -1);
    }

    private void cancelAlarm() {
        Log.d("I'm ok", "cancelAlarm");

        Context context = this.getApplicationContext();

        m_Alarm.cancelAlarm(context);
    }

    private void sendSms() {
        Log.d("I'm ok", "sendSmsFromLocationUpdate");

        m_Alarm.sendSmsFromLocationUpdate(getApplicationContext(), manuallySmsTelno, manuallySmsText, ALARM_TYPE_MANUALLY);
    }

    private void updateGui() {
        //Log.v("I'm ok", "updateGui");

        RelativeLayout layoutTimer = (RelativeLayout) findViewById(R.id.layout_Timer);
        TextView txtTimer = (TextView) findViewById(R.id.txt_Timer);
        ImageButton btnReset = (ImageButton) findViewById(R.id.btn_Reset);
        ImageButton btnSleeping = (ImageButton) findViewById(R.id.btn_Sleeping);

        SimpleDateFormat ft = new SimpleDateFormat("mm:ss");
        Date timeCounter = new Date();
        timeCounter.setTime(m_nextAlarm - System.currentTimeMillis());
        txtTimer.setText(ft.format(timeCounter));

        switch (m_Status) {
            case STATUS_IMOK:
                layoutTimer.setBackgroundColor(TIMER_BACKGROUND_COLOR_WARNING);
                txtTimer.setVisibility(View.VISIBLE);
                btnReset.setImageResource(R.drawable.imok);
                btnSleeping.setVisibility(View.VISIBLE);
                break;
            case STATUS_WARNING:
                layoutTimer.setBackgroundColor(TIMER_BACKGROUND_COLOR_ALARM);
                txtTimer.setVisibility(View.VISIBLE);
                btnReset.setImageResource(R.drawable.warning);
                btnSleeping.setVisibility(View.INVISIBLE);
                break;
            case STATUS_ALARM:
                layoutTimer.setBackgroundColor(TIMER_BACKGROUND_COLOR_NOK);
                txtTimer.setVisibility(View.INVISIBLE);
                btnReset.setImageResource(R.drawable.alarm);
                btnSleeping.setVisibility(View.INVISIBLE);
                break;
            case STATUS_SLEEPING:
                //Log.v("I'm ok", "updateGui: Sleeping");
                layoutTimer.setBackgroundColor(TIMER_BACKGROUND_COLOR_SLEEPING);
                txtTimer.setVisibility(View.INVISIBLE);
                btnReset.setImageResource(R.drawable.sleeping);
                btnSleeping.setVisibility(View.INVISIBLE);
                break;
        }
    }
}

