package info.honisch.imok;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.SystemClock;
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
    final static String CURRENT_TimeCounter = "CurrentTimeCounter";

    private Date m_TimeCounter;
    private int m_Status = STATUS_INIT;
    private AlarmManagerBroadcastReceiver m_Alarm;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d("I'm ok", "onCreate");

        setContentView(R.layout.layout_reset);

        Chronometer chronometer = (Chronometer)findViewById(R.id.chronometer);
        chronometer.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
            @Override
            public void onChronometerTick(Chronometer chronometer) {
                handleChronometerTick(chronometer);
            }
        });

        ToggleButton toggleActive = (ToggleButton) findViewById(R.id.toggle_Active);
        toggleActive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleToggleActiveClick(v);
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

        ImageButton btnEmergencyCall = (ImageButton) findViewById(R.id.btnEmergencyCall);
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

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        Log.d("I'm ok", "onSaveInstanceState");

        outState.putInt(CURRENT_STATUS, m_Status);
        outState.putLong(CURRENT_TimeCounter, m_TimeCounter.getTime());
    }



/*    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        Log.d("I'm ok", "onRestoreInstanceState");

        m_Status = savedInstanceState.getInt(CURRENT_STATUS);

        long tc = savedInstanceState.getLong(CURRENT_TimeCounter);
        m_TimeCounter.setTime(tc);
    }
*/


    // *** private methods ***
    private void initActivity(Bundle savedInstanceState) {
        Log.d("I'm ok", "initActivity");

        Chronometer chronometer = (Chronometer)findViewById(R.id.chronometer);
        m_Alarm = new AlarmManagerBroadcastReceiver();
        m_TimeCounter = new Date();

        if (savedInstanceState != null) initActivityFromBundle(savedInstanceState);
        else {
            Bundle bundle = getIntent().getExtras();

//          if (intent.hasExtra(ALARM_TYPE)) initActivityFromIntent();
            if (bundle != null) initActivityFromIntent();
            else initActivityFromStartup();
        }

        updateGui();
    }

    private void initActivityFromIntent() {
        Log.d("I'm ok", "initActivityFromIntent");

        long tc = 0;

        Intent intent = getIntent();

        int alarmType = intent.getIntExtra(ALARM_TYPE, ALARM_TYPE_UNKNOWN);
        int sequenceAlarmType = intent.getIntExtra(SEQUENCE_ALARM_TYPE, ALARM_TYPE_UNKNOWN);

        if (alarmType == ALARM_TYPE_WARNING) {
            tc = 0;
            m_Status = STATUS_IMOK;
        }

        if (alarmType == ALARM_TYPE_SOUNDOFF && sequenceAlarmType == ALARM_TYPE_ALARM) {
            m_Status = STATUS_WARNING;
        }

        if (alarmType == ALARM_TYPE_ALARM) {
            tc = 0;
            m_Status = STATUS_WARNING;
        }

        if (alarmType == ALARM_TYPE_SOUNDOFF && sequenceAlarmType == ALARM_TYPE_UNKNOWN) {
            m_Status = STATUS_ALARM;
        }

        Chronometer chronometer = (Chronometer)findViewById(R.id.chronometer);
        chronometer.setBase(SystemClock.elapsedRealtime() + tc);

        m_TimeCounter.setTime(tc);
        chronometer.start();
    }

    private void initActivityFromStartup() {
        Log.d("I'm ok", "initActivityFromStartup");

        long tc = TIME_TO_WARNING;
        setAlarm();

        Chronometer chronometer = (Chronometer)findViewById(R.id.chronometer);
        chronometer.setBase(SystemClock.elapsedRealtime());

        m_TimeCounter.setTime(tc);
        chronometer.start();

        m_Status = STATUS_IMOK;
    }

    private void initActivityFromBundle(Bundle savedInstanceState) {
        Log.d("I'm ok", "initActivityFromBundle");

        m_Status = savedInstanceState.getInt(CURRENT_STATUS);
        long tc = savedInstanceState.getLong(CURRENT_TimeCounter);
        m_TimeCounter.setTime(tc);

        Chronometer chronometer = (Chronometer)findViewById(R.id.chronometer);

        switch (m_Status) {
            case STATUS_IMOK:
                chronometer.setBase(SystemClock.elapsedRealtime() - TIME_TO_WARNING + tc);
                chronometer.start();
                break;
            case STATUS_WARNING:
                chronometer.setBase(SystemClock.elapsedRealtime() - TIME_TO_ALARM + tc);
                chronometer.start();
                break;
            default:
                break;
        }
    }

    private void handleBtnEmergencyCallLongClick(View v) {
        Log.d("I'm ok", "handleBtnEmergencyCallLongClick");

        if (m_Status == STATUS_INIT) return;

        confirmLongClick();

        m_Status = STATUS_IMOK;

        stopCounter(false);

        ToggleButton toggleActive = (ToggleButton) findViewById(R.id.toggle_Active);
        if (!toggleActive.isChecked()) toggleActive.setChecked(true);

        startCounter();

        updateGui();

        moveTaskToBack(false);

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

        moveTaskToBack(false);
    }

    private void handleBtnResetClick(View v) {
        Log.d("I'm ok", "handleBtnResetClick");

        if (m_Status == STATUS_INIT) return;

        m_Status = STATUS_IMOK;

        stopCounter();

        ToggleButton toggleActive = (ToggleButton) findViewById(R.id.toggle_Active);
        if (!toggleActive.isChecked()) toggleActive.setChecked(true);

        startCounter();

        updateGui();

        moveTaskToBack(false);
    }

    private void handleToggleActiveClick(View v) {
        Log.d("I'm ok", "handleToggleActiveClick");

        if (m_Status == STATUS_INIT) return;

        m_Status = STATUS_SLEEPING;

        stopCounter();

        updateGui();

        moveTaskToBack(false);
    }

    private void handleChronometerTick(Chronometer chronometer) {
        long timeOut;

        if (m_Status == STATUS_INIT) return;

        switch (m_Status) {
            case STATUS_IMOK:
                timeOut = TIME_TO_WARNING;
                break;
            case STATUS_WARNING:
                timeOut = TIME_TO_ALARM;
                break;
            default:
                timeOut = TIME_TO_WARNING;
                break;
        }

        long timeDiff = SystemClock.elapsedRealtime() - chronometer.getBase();
        if (timeOut - timeDiff > 0) {
            m_TimeCounter.setTime(timeOut - timeDiff);
        }

        if ((timeOut - timeDiff) < 0) {
            chronometer.stop();
            switch (m_Status) {
                case STATUS_IMOK:
                    m_Status = STATUS_WARNING;
                    chronometer.setBase(SystemClock.elapsedRealtime());
                    chronometer.start();
                    break;
                default:
                    m_Status = STATUS_ALARM;
                    break;
            }
        }

        updateGui();
    }

    private void startCounter() {
        Log.d("I'm ok", "startCounter");

        Chronometer chronometer = (Chronometer)findViewById(R.id.chronometer);
        chronometer.setBase(SystemClock.elapsedRealtime());

        m_TimeCounter.setTime(TIME_TO_WARNING);

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

        long startTime = System.currentTimeMillis() + timeToWarning;
        long sequenceAlarmStartTime = System.currentTimeMillis() + timeToWarning + timeToAlarm;
        int sequenceAlarmType = AlarmManagerBroadcastReceiver.ALARM_TYPE_ALARM;

        m_Alarm.setAlarm(context, startTime, AlarmManagerBroadcastReceiver.ALARM_TYPE_WARNING, alarmSoundDuration, sequenceAlarmStartTime, sequenceAlarmType, sequenceAlarmSoundDuration);
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
        // Log.d("I'm ok", "updateGui");

        RelativeLayout layoutTimer = (RelativeLayout) findViewById(R.id.layout_Timer);
        TextView txtTimer = (TextView) findViewById(R.id.txt_Timer);
        ImageButton btnReset = (ImageButton) findViewById(R.id.btn_Reset);
        ToggleButton toggleActive = (ToggleButton) findViewById(R.id.toggle_Active);

        SimpleDateFormat ft = new SimpleDateFormat("mm:ss");
        txtTimer.setText(ft.format(m_TimeCounter));

        switch (m_Status) {
            case STATUS_IMOK:
                layoutTimer.setBackgroundColor(TIMER_BACKGROUND_COLOR_WARNING);
                txtTimer.setVisibility(View.VISIBLE);
                btnReset.setImageResource(R.drawable.imok);
                toggleActive.setVisibility(View.VISIBLE);
                break;
            case STATUS_WARNING:
                layoutTimer.setBackgroundColor(TIMER_BACKGROUND_COLOR_ALARM);
                txtTimer.setVisibility(View.VISIBLE);
                btnReset.setImageResource(R.drawable.warning);
                toggleActive.setVisibility(View.INVISIBLE);
                break;
            case STATUS_ALARM:
                layoutTimer.setBackgroundColor(TIMER_BACKGROUND_COLOR_NOK);
                txtTimer.setVisibility(View.INVISIBLE);
                btnReset.setImageResource(R.drawable.alarm);
                toggleActive.setVisibility(View.INVISIBLE);
                break;
            case STATUS_SLEEPING:
                layoutTimer.setBackgroundColor(TIMER_BACKGROUND_COLOR_SLEEPING);
                txtTimer.setVisibility(View.INVISIBLE);
                btnReset.setImageResource(R.drawable.sleeping);
                toggleActive.setVisibility(View.INVISIBLE);
                break;
        }
    }
}

