package info.honisch.imok;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.telephony.SmsManager;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

/**
 * Created by Andi on 18.02.2015.
 */

public class AlarmManagerBroadcastReceiver extends BroadcastReceiver {

    final public static String ALARM_TYPE = "Alarm Type";
    final public static String ALARM_SOUND_DURATION = "Alarm Sound Duration";
    final public static String SEQUENCE_ALARM_TYPE = "Sequence Alarm Type";
    final public static String SEQUENCE_ALARM_START_TIME = "Sequence Alarm Time";
    final public static String SEQUENCE_ALARM_SOUND_DURATION = "Sequence Alarm Sound Duration";

    final public static int ALARM_TYPE_UNKNOWN = -1;
    final public static int ALARM_TYPE_SOUNDOFF = 0;
    final public static int ALARM_TYPE_WARNING = 1;
    final public static int ALARM_TYPE_ALARM = 2;
    final public static int ALARM_TYPE_MANUALLY = 3;

    final public static long DEFAULT_ALARM_SOUND_DURATION = 30 * 1000; // 30 sec

    final public static String VIBRATE_PATTERN_WARNING = "EEEEEEEEEE";
    final public static String VIBRATE_PATTERN_ALARM = "EEEEEEEEEEEEEEEEEEEE"; // vibrate stackato

    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("I'm ok", "AlarmManager.onReceive");

        long startTime;

        int alarmType = intent.getIntExtra(ALARM_TYPE, ALARM_TYPE_UNKNOWN);
        long alarmSoundDuration = intent.getLongExtra(ALARM_SOUND_DURATION, DEFAULT_ALARM_SOUND_DURATION);
        long sequenceAlarmStartTime = intent.getLongExtra(SEQUENCE_ALARM_START_TIME, 0);
        int sequenceAlarmType = intent.getIntExtra(SEQUENCE_ALARM_TYPE, ALARM_TYPE_UNKNOWN);
        long sequenceAlarmSoundDuration = intent.getLongExtra(SEQUENCE_ALARM_SOUND_DURATION, DEFAULT_ALARM_SOUND_DURATION);
        Log.d("I'm ok", "AlarmType: " + Integer.toString(alarmType));

        // Start MainActivity or bring it to front
        Intent intentMainActivity = new Intent(context, MainActivity.class);
        Bundle bundle = new Bundle();
        bundle.putInt(ALARM_TYPE, alarmType);
        bundle.putInt(SEQUENCE_ALARM_TYPE, sequenceAlarmType);
        //intentMainActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        intentMainActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intentMainActivity, bundle);


        // Stop Vibrator when new Alarm is received
        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator.hasVibrator()) vibrator.cancel();

        // Stop AlarmSound when new Alarm received
        Intent alarmRingtoneService = new Intent(context, AlarmRingtoneService.class);
        context.stopService(alarmRingtoneService);

        switch (alarmType) {
            case ALARM_TYPE_WARNING:
                Log.d("I'm ok", "AlarmManager.onReceive ALARM_TYPE_WARNING");

                if (vibrator.hasVibrator()) {
                    long[] pattern = MorseCodeConverter.pattern(VIBRATE_PATTERN_WARNING);
                    vibrator.vibrate(pattern, -1);
                }

                context.startService(alarmRingtoneService);

                startTime = System.currentTimeMillis() + alarmSoundDuration;
                setAlarm(context, startTime, ALARM_TYPE_SOUNDOFF, alarmSoundDuration, sequenceAlarmStartTime, sequenceAlarmType, sequenceAlarmSoundDuration);
                break;
            case ALARM_TYPE_ALARM:
                Log.d("I'm ok", "AlarmManager.onReceive ALARM_TYPE_ALARM");

                if (vibrator.hasVibrator()) {
                    long[] pattern = MorseCodeConverter.pattern(VIBRATE_PATTERN_ALARM);
                    vibrator.vibrate(pattern, -1);
                }

                context.startService(alarmRingtoneService);

                startTime = System.currentTimeMillis() + alarmSoundDuration;
                setAlarm(context, startTime, ALARM_TYPE_SOUNDOFF, 0, 0, ALARM_TYPE_UNKNOWN, 0);
                break;
            case ALARM_TYPE_SOUNDOFF:
                Log.d("I'm ok", "AlarmManager.onReceive ALARM_TYPE_SOUNDOFF");

                if (sequenceAlarmType != ALARM_TYPE_UNKNOWN) {
                    setAlarm(context, sequenceAlarmStartTime, sequenceAlarmType, sequenceAlarmSoundDuration, 0, ALARM_TYPE_UNKNOWN, 0);
                    sendSms(context, ALARM_TYPE_WARNING);
                } else {
                    sendSms(context, ALARM_TYPE_ALARM);
                }

                break;
            default:
                Log.d("I'm ok", "AlarmManager.onReceive unknown!!");
                break;
        }
    }


    public void setAlarm(Context context, long startTime, int alarmType, long alarmSoundDuration,
                         long sequenceAlarmStartTime, int sequenceAlarmType, long sequenceAlarmSoundDuration) {
        Log.d("I'm ok", "AlarmManager.SetAlarm (" + Integer.toString(alarmType) + ")");

        Intent intent = new Intent(context, AlarmManagerBroadcastReceiver.class);

        intent.putExtra(ALARM_TYPE, alarmType);
        intent.putExtra(ALARM_SOUND_DURATION, alarmSoundDuration);
        intent.putExtra(SEQUENCE_ALARM_START_TIME, sequenceAlarmStartTime);
        intent.putExtra(SEQUENCE_ALARM_TYPE, sequenceAlarmType);
        intent.putExtra(SEQUENCE_ALARM_SOUND_DURATION, sequenceAlarmSoundDuration);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, startTime, pendingIntent);
    }

    public void cancelAlarm(Context context) {
        Log.d("I'm ok", "AlarmManager.CancelAlarm");

        Intent intent = new Intent(context, AlarmManagerBroadcastReceiver.class);
        PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, 0);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(sender);
    }

    public void sendSms(Context context, int alarmType) {
        Log.d("I'm ok", "AlarmManager.sendSms");

        //get Location from Location Service
        LocationManager locationManager = (LocationManager) context.getSystemService(context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        String bestProvider = locationManager.getBestProvider(criteria, true);
        Location location = locationManager.getLastKnownLocation(bestProvider);

        String smsText = "Alarm! Location: "
                + "https://maps.google.com/maps?s="
                + Double.toString(location.getLatitude()) + ","
                + Double.toString(location.getLongitude());

        SmsManager sms = SmsManager.getDefault();
        //sms.sendTextMessage("01603147344", null, smsText, null, null);
    }

}
