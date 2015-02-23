package info.honisch.imok;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
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
import android.widget.ToggleButton;

import java.text.SimpleDateFormat;
import java.util.Date;

import static info.honisch.imok.AlarmManagerBroadcastReceiver.*;


public class MainActivity extends ActionBarActivity {

    final static int TIMER_BACKGROUND_COLOR_WARNING = 0xffbcffbd;
    final static int TIMER_BACKGROUND_COLOR_ALARM = 0xffff9b93;
    final static int TIMER_BACKGROUND_COLOR_NOK = 0xfffffbd1;
    final static int TIMER_BACKGROUND_COLOR_SLEEPING = 0xfffffbd1;

    final static long TIME_TO_WARNING = 1 * 60 * 1000; // 1 min to waiting
    final static long TIME_TO_ALARM = 1 * 60 * 1000; // 1 min to alarm

    final static long[] CONFIRM_BUTTON_PATTERN = {0,500,0};

    final static int STATUS_INIT = 0;
    final static int STATUS_IMOK = 1;
    final static int STATUS_WARNING = 2;
    final static int STATUS_ALARM = 3;
    final static int STATUS_SLEEPING = -1;

    final static String CURRENT_STATUS = "CurrentStatus";
    final static String NEXT_ALARM = "NextAlarm";

    private long m_nextAlarm;
    private int m_Status = STATUS_INIT;
    private AlarmManagerBroadcastReceiver m_Alarm;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.i("I'm ok", "onCreate");

        setContentView(R.layout.layout_reset);
        //this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Chronometer chronometer = (Chronometer)findViewById(R.id.chronometer);
        chronometer.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
            @Override
            public void onChronometerTick(Chronometer chronometer) {
                handleChronometerTick(chronometer);
            }
        });

        ImageButton btnSleeping = (ImageButton) findViewById(R.id.btn_Sleeping);
        btnSleeping.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                handleBtnSleepingLongClick(v);

                return true;
            }
        });

        ImageButton btnReset = (ImageButton) findViewById(R.id.btn_Reset);
        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleBtnResetClick(v);
            }
        });
        btnReset.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                handleBtnResetLongClick(v);

                return true;
            }
        });

        ImageButton btnEmergencyCall = (ImageButton) findViewById(R.id.btn_emergency_call);
        btnEmergencyCall.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                handleBtnEmergencyCallLongClick(v);

                return true;
            }
        });

        initActivity(savedInstanceState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        Log.d("I'm ok", "onRestoreInstanceState");
        initActivity(savedInstanceState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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


    // *** private methods ***
    private void initActivity(Bundle savedInstanceState) {
        Log.i("I'm ok", "initActivity");

        m_Alarm = new AlarmManagerBroadcastReceiver();

        Chronometer chronometer = (Chronometer)findViewById(R.id.chronometer);
        chronometer.setBase(System.currentTimeMillis());

        chronometer.stop();
        chronometer.start();

        SharedPreferences sharedPreferences = getSharedPreferences(AlarmManagerBroadcastReceiver.SHARED_PREF_NAME, MODE_PRIVATE);
        String restoredText = sharedPreferences.getString(AlarmManagerBroadcastReceiver.SHARED_PREF_ALARM_TYPE, null);

        m_Status = STATUS_IMOK;

        if (restoredText == null) setAlarm();
        else initActivityFromPref();

        updateGui();
    }

    private void initActivityFromPref() {
        Log.i("I'm ok", "initActivityFromPref");

        SharedPreferences sharedPreferences = getSharedPreferences(AlarmManagerBroadcastReceiver.SHARED_PREF_NAME, MODE_PRIVATE);
        String prefAlarmType = sharedPreferences.getString(AlarmManagerBroadcastReceiver.SHARED_PREF_ALARM_TYPE, null);
        String prefNextAlarm = sharedPreferences.getString(AlarmManagerBroadcastReceiver.SHARED_PREF_NEXT_ALARM, null);

        Log.i("I'm ok", "initActivityFromPref (" + prefAlarmType + "/" + Long.toString((Long.valueOf(prefNextAlarm) - System.currentTimeMillis()) / 1000) + ")");

        int alarmType = Integer.valueOf(prefAlarmType);
        m_nextAlarm = Long.valueOf(prefNextAlarm);

        switch (alarmType) {
            case ALARM_TYPE_WARNING:
                if (m_nextAlarm < System.currentTimeMillis()) setAlarm();

                m_Status = STATUS_IMOK;
                Log.i("I'm ok", "STATUS_IMOK");
                break;
            case ALARM_TYPE_ALARM:
                if (m_nextAlarm < System.currentTimeMillis()) {
                    m_Status = STATUS_IMOK;
                    Log.i("I'm ok", "STATUS_IMOK");
                    setAlarm();
                }
                else {
                    m_Status = STATUS_WARNING;
                    Log.i("I'm ok", "STATUS_WARNING");
                }
                break;
            case ALARM_TYPE_UNKNOWN:
                m_Status = STATUS_ALARM;
                Log.i("I'm ok", "STATUS_ALARM");
                break;
            case ALARM_TYPE_SLEEPING:
                m_Status = STATUS_SLEEPING;
                Log.i("I'm ok", "STATUS_SLEEPING");
                break;
        }
    }

    private void initActivityFromStartup() {
        Log.i("I'm ok", "initActivityFromStartup");

        setAlarm();

        m_Status = STATUS_IMOK;
    }

    private void handleBtnEmergencyCallLongClick(View v) {
        Log.d("I'm ok", "handleBtnEmergencyCallLongClick");

        if (m_Status == STATUS_INIT) return;

        confirmLongClick();

        m_Status = STATUS_IMOK;

        stopCounter(false);

        startCounter();

        updateGui();

        moveTaskToBack(true);

        Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse("tel:075244092256"));
        callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(callIntent);
    }

    private void handleBtnResetLongClick(View v) {
        Log.d("I'm ok", "handleBtnResetLongClick");

        confirmLongClick();

        sendSms();

        updateGui();

        moveTaskToBack(true);
    }

    private void handleBtnResetClick(View v) {
        Log.d("I'm ok", "handleBtnResetClick");

        if (m_Status == STATUS_INIT) return;

        m_Status = STATUS_IMOK;

        stopCounter();

        startCounter();

        updateGui();

        moveTaskToBack(true);
    }

    private void handleBtnSleepingLongClick(View v) {
        Log.d("I'm ok", "handleBtnSleepingLongClick");

        if (m_Status == STATUS_INIT) return;

        m_Status = STATUS_SLEEPING;

        stopCounter();

        m_Alarm.writePref(this.getApplicationContext(), ALARM_TYPE_SLEEPING, System.currentTimeMillis() - 1);

        updateGui();

        moveTaskToBack(true);
    }

    private void handleChronometerTick(Chronometer chronometer) {
        long timeOut;

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

        // TODO: Params have to be initialized from DB
        long timeToWarning = TIME_TO_WARNING;
        long timeToAlarm = TIME_TO_ALARM;
        long alarmSoundDuration = AlarmManagerBroadcastReceiver.DEFAULT_ALARM_SOUND_DURATION;
        long sequenceAlarmSoundDuration = AlarmManagerBroadcastReceiver.DEFAULT_ALARM_SOUND_DURATION;
        // String smsTextAlarmTypeWarning
        // String smsTextAlarmTypeAlarm
        // String smsTextAlarmTypeManually
        // String smsTelNoAlarmTypeWarning
        // String smsTelNoAlarmTypeAlarm
        // String smsTelNoAlarmTypeManually
        // String telNoEmergencyCall
        // String vibratePatternWarning
        // String vibratePatternAlarm
        // long ConfirmButtonVibrateDuration
        // long AlarmSoundDuration
        // ENDTODO

        m_nextAlarm = System.currentTimeMillis() + timeToWarning;
        long sequenceAlarmStartTime = System.currentTimeMillis() + timeToWarning + timeToAlarm;
        int sequenceAlarmType = AlarmManagerBroadcastReceiver.ALARM_TYPE_ALARM;

        m_Alarm.setAlarm(context, m_nextAlarm, AlarmManagerBroadcastReceiver.ALARM_TYPE_WARNING, alarmSoundDuration, sequenceAlarmStartTime, sequenceAlarmType, sequenceAlarmSoundDuration);
        m_Alarm.writePref(context, ALARM_TYPE_WARNING, m_nextAlarm);

    }

    private void confirmLongClick() {
        Log.d("I'm ok", "confirmLongClick");

        Context context = this.getApplicationContext();
        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);

        if (vibrator.hasVibrator()) vibrator.vibrate(CONFIRM_BUTTON_PATTERN, -1);
    }

    private void cancelAlarm() {
        Log.d("I'm ok", "cancelAlarm");

        Context context = this.getApplicationContext();

        m_Alarm.cancelAlarm(context);
    }

    private void sendSms() {
        Log.d("I'm ok", "sendSms");

        m_Alarm.sendSms(this, AlarmManagerBroadcastReceiver.ALARM_TYPE_MANUALLY);
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

