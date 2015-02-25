package info.honisch.imok;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.Vibrator;
import android.telephony.SmsManager;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Andi on 18.02.2015.
 */

public class AlarmManagerBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("I'm ok", "AlarmManager.onReceive");

        long startTime;
        boolean isStartActivity;

        int alarmType = intent.getIntExtra(MainActivity.QUALIFIER_ALARM_TYPE, MainActivity.ALARM_TYPE_UNKNOWN);
        long alarmSoundDuration = intent.getLongExtra(MainActivity.QUALIFIER_ALARM_SOUND_DURATION, MainActivity.WARNING_SOUND_DURATION);
        String alarmVibratePattern = intent.getStringExtra(MainActivity.QUALIFIER_ALARM_VIBRATE_PATTERN);
        long sequenceAlarmStartTime = intent.getLongExtra(MainActivity.QUALIFIER_SEQUENCE_ALARM_START_TIME, 0);
        int sequenceAlarmType = intent.getIntExtra(MainActivity.QUALIFIER_SEQUENCE_ALARM_TYPE, MainActivity.ALARM_TYPE_UNKNOWN);
        long sequenceAlarmSoundDuration = intent.getLongExtra(MainActivity.QUALIFIER_SEQUENCE_ALARM_SOUND_DURATION, MainActivity.ALARM_SOUND_DURATION);
        String sequenceVibratePattern = intent.getStringExtra(MainActivity.QUALIFIER_SEQUENCE_VIBRATE_PATTERN);
        String smsTelno = intent.getStringExtra(MainActivity.QUALIFIER_ALARM_SMS_TELNO);
        String smsText = intent.getStringExtra(MainActivity.QUALIFIER_ALARM_SMS_TEXT);
        String sequenceSmsTelno = intent.getStringExtra(MainActivity.QUALIFIER_SEQUENCE_SMS_TELNO);
        String sequenceSmsText = intent.getStringExtra(MainActivity.QUALIFIER_SEQUENCE_SMS_TEXT);

        Log.d("I'm ok", "AlarmType: " + Integer.toString(alarmType));

        // Stop Vibrator when new Alarm is received
        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator.hasVibrator()) vibrator.cancel();

        // Stop AlarmSound when new Alarm received
        Intent alarmRingtoneService = new Intent(context, AlarmRingtoneService.class);
        context.stopService(alarmRingtoneService);

        switch (alarmType) {
            case MainActivity.ALARM_TYPE_WARNING:
                Log.d("I'm ok", "AlarmManager.onReceive ALARM_TYPE_WARNING");

                if (vibrator.hasVibrator()) {
                    long[] pattern = MorseCodeConverter.pattern(alarmVibratePattern);
                    vibrator.vibrate(pattern, -1);
                }

                context.startService(alarmRingtoneService);

                startTime = System.currentTimeMillis() + alarmSoundDuration;
                setAlarm(context,
                        smsTelno, smsText,
                        startTime, MainActivity.ALARM_TYPE_SOUNDOFF, alarmSoundDuration, alarmVibratePattern,
                        sequenceSmsTelno, sequenceSmsText,
                        sequenceAlarmStartTime, sequenceAlarmType, sequenceAlarmSoundDuration, sequenceVibratePattern);
                writePref(context, MainActivity.ALARM_TYPE_ALARM, sequenceAlarmStartTime);

                isStartActivity = true;

                break;
            case MainActivity.ALARM_TYPE_ALARM:
                Log.d("I'm ok", "AlarmManager.onReceive ALARM_TYPE_ALARM");


                if (vibrator.hasVibrator()) {
                    long[] pattern = MorseCodeConverter.pattern(alarmVibratePattern);
                    vibrator.vibrate(pattern, -1);
                }

                context.startService(alarmRingtoneService);

                startTime = System.currentTimeMillis() + alarmSoundDuration;
                setAlarm(context,
                        smsTelno, smsText,
                        startTime, MainActivity.ALARM_TYPE_SOUNDOFF, 0, alarmVibratePattern,
                        sequenceSmsTelno, sequenceSmsText,
                        0, MainActivity.ALARM_TYPE_UNKNOWN, 0, sequenceVibratePattern);
                writePref(context, MainActivity.ALARM_TYPE_UNKNOWN, System.currentTimeMillis() - 1);

                isStartActivity = true;

                break;
            case MainActivity.ALARM_TYPE_SOUNDOFF:
                Log.d("I'm ok", "AlarmManager.onReceive ALARM_TYPE_SOUNDOFF");

                if (sequenceAlarmType != MainActivity.ALARM_TYPE_UNKNOWN) {
                    setAlarm(context,
                            smsTelno, smsText,
                            sequenceAlarmStartTime, sequenceAlarmType, sequenceAlarmSoundDuration, sequenceVibratePattern,
                            sequenceSmsTelno, sequenceSmsText,
                            0, MainActivity.ALARM_TYPE_UNKNOWN, 0, sequenceVibratePattern);
                    sendSmsFromLocationUpdate(context, smsTelno, smsText, MainActivity.ALARM_TYPE_WARNING);
                } else {
                    sendSmsFromLocationUpdate(context, sequenceSmsTelno, sequenceSmsText, MainActivity.ALARM_TYPE_ALARM);
                }

                isStartActivity = false;

                break;
            default:
                Log.d("I'm ok", "AlarmManager.onReceive unknown!!");
                isStartActivity = false;
                break;
        }

        if (isStartActivity) {
            Intent intentMainActivity = new Intent(context, MainActivity.class);
            intentMainActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            context.startActivity(intentMainActivity);
        }
    }

    public void setAlarm(Context context,
                         String smsTelno, String smsText,
                         long startTime, int alarmType, long alarmSoundDuration, String alarmVibratePattern,
                         String sequenceSmsTelno, String sequenceSmsText,
                         long sequenceAlarmStartTime, int sequenceAlarmType, long sequenceAlarmSoundDuration, String sequenceVibratePattern) {
        Log.d("I'm ok", "AlarmManager.SetAlarm (" + Integer.toString(alarmType) + ")");

        Intent intent = new Intent(context, AlarmManagerBroadcastReceiver.class);

        intent.putExtra(MainActivity.QUALIFIER_ALARM_TYPE, alarmType);
        intent.putExtra(MainActivity.QUALIFIER_ALARM_SOUND_DURATION, alarmSoundDuration);
        intent.putExtra(MainActivity.QUALIFIER_ALARM_VIBRATE_PATTERN, alarmVibratePattern);
        intent.putExtra(MainActivity.QUALIFIER_SEQUENCE_ALARM_START_TIME, sequenceAlarmStartTime);
        intent.putExtra(MainActivity.QUALIFIER_SEQUENCE_ALARM_TYPE, sequenceAlarmType);
        intent.putExtra(MainActivity.QUALIFIER_SEQUENCE_ALARM_SOUND_DURATION, sequenceAlarmSoundDuration);
        intent.putExtra(MainActivity.QUALIFIER_SEQUENCE_VIBRATE_PATTERN, sequenceVibratePattern);
        intent.putExtra(MainActivity.QUALIFIER_ALARM_SMS_TELNO, smsTelno);
        intent.putExtra(MainActivity.QUALIFIER_ALARM_SMS_TEXT, smsText);
        intent.putExtra(MainActivity.QUALIFIER_SEQUENCE_SMS_TELNO, sequenceSmsTelno);
        intent.putExtra(MainActivity.QUALIFIER_SEQUENCE_SMS_TEXT, sequenceSmsText);

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

    public void sendSmsFromLocationUpdate(Context context, final String smsTelno, final String smsText, final int alarmType) {
        Log.d("I'm ok", "AlarmManager.sendSmsFromLocationUpdate");

        //get Location from Location Service
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        LocationListener locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                String locationText = "http://maps.google.com/maps?q="
                        + Double.toString(location.getLatitude()) + ","
                        + Double.toString(location.getLongitude());
                sendSms(smsTelno, smsText, locationText, alarmType);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
                if (status == LocationProvider.OUT_OF_SERVICE) {
                    String locationText = "unknown location";
                    sendSms(smsTelno, smsText, locationText, alarmType);
                }
            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {
                 String locationText = "unknown location";
                 sendSms(smsTelno, smsText, locationText, alarmType);
            }
        };

        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        String bestProvider = locationManager.getBestProvider(criteria, true);
        locationManager.requestSingleUpdate(bestProvider, locationListener, null);
    }

    public void writePref(Context context, int alarmType, long nextAlarmTime) {
        Log.d("I'm ok", "AlarmManager.writePref (" + Integer.toString(alarmType) + "/" + Long.toString((nextAlarmTime - System.currentTimeMillis()) / 1000) + ")");

        SharedPreferences.Editor editor = context.getSharedPreferences(MainActivity.SHARED_PREF_NAME, Context.MODE_PRIVATE).edit();
        editor.putString(MainActivity.SHARED_PREF_ALARM_TYPE,Integer.toString(alarmType));
        editor.putString(MainActivity.SHARED_PREF_NEXT_ALARM,Long.toString(nextAlarmTime));
        editor.commit();
    }

    private void sendSms(String smsTelno, String smsText, String locationText, int alarmType) {
        Log.d("I'm ok", "AlarmManager.sendSmsFromLocationUpdate");

        Date currentDateTime = new Date();
        currentDateTime.setTime(System.currentTimeMillis());

        SimpleDateFormat ft = new SimpleDateFormat("dd.MM.yy HH:mm");
        smsText = smsText
                + ft.format(currentDateTime) + " "
                + locationText;

        Log.i("I'm ok", smsText);
        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(smsTelno, null, smsText, null, null);
    }
}
