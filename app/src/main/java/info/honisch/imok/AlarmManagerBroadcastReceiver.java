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
import android.net.MailTo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.telephony.SmsManager;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;


public class AlarmManagerBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("I'm ok", "AlarmManager.onReceive");

        boolean isStartActivity;

        int alarmType = intent.getIntExtra(MainActivity.QUALIFIER_ALARM_TYPE, MainActivity.ALARM_TYPE_UNKNOWN);

        Log.d("I'm ok", "AlarmType: " + Integer.toString(alarmType));

        // Stop Vibrator when new Alarm is received
        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator.hasVibrator()) vibrator.cancel();

        // Stop AlarmSound when new Alarm received
        Intent alarmRingtoneService = new Intent(context, AlarmRingtoneService.class);
        context.stopService(alarmRingtoneService);

        SharedPreferences sharedPreferences = context.getSharedPreferences(MainActivity.SHARED_PREF_NAME, Context.MODE_PRIVATE);

        switch (alarmType) {
            case MainActivity.ALARM_TYPE_WARNING:
                Log.i("I'm ok", "AlarmManager.onReceive ALARM_TYPE_WARNING");

                if (vibrator.hasVibrator()) {
                    long[] pattern = MorseCodeConverter.pattern(sharedPreferences.getString(MainActivity.SHARED_PREF_WARNING_VIBRATE_PATTERN, null));
                    vibrator.vibrate(pattern, -1);
                }

                context.startService(alarmRingtoneService);

                setNextAlarm(context, MainActivity.ALARM_TYPE_WARNING_SOUNDOFF);

                isStartActivity = true;
                break;
            case MainActivity.ALARM_TYPE_WARNING_SOUNDOFF:
                Log.i("I'm ok", "AlarmManager.onReceive ALARM_TYPE_WARNING_SOUNDOFF");

                sendMessageFromLocationUpdate(context, MainActivity.ALARM_TYPE_WARNING);

                setNextAlarm(context, MainActivity.ALARM_TYPE_ALARM);

                isStartActivity = false;
                break;
            case MainActivity.ALARM_TYPE_ALARM:
                Log.i("I'm ok", "AlarmManager.onReceive ALARM_TYPE_ALARM");

                if (vibrator.hasVibrator()) {
                    long[] pattern = MorseCodeConverter.pattern(sharedPreferences.getString(MainActivity.SHARED_PREF_ALARM_VIBRATE_PATTERN, null));
                    vibrator.vibrate(pattern, -1);
                }

                context.startService(alarmRingtoneService);

                setNextAlarm(context, MainActivity.ALARM_TYPE_ALARM_SOUNDOFF);

                isStartActivity = true;
                break;
            case MainActivity.ALARM_TYPE_ALARM_SOUNDOFF:
                Log.i("I'm ok", "AlarmManager.onReceive ALARM_TYPE_ALARM_SOUNDOFF");

                sendMessageFromLocationUpdate(context, MainActivity.ALARM_TYPE_ALARM);

                isStartActivity = false;
                break;
            default:
                Log.i("I'm ok", "AlarmManager.onReceive unknown!!");
                isStartActivity = false;
                break;
        }

        if (isStartActivity) {
            Intent intentMainActivity = new Intent(context, MainActivity.class);
            intentMainActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            context.startActivity(intentMainActivity);
        }
    }

    public void setNextAlarm(Context context, int nextAlarmType) {
        Log.d("I'm ok", "AlarmManager.setNextAlarm");
        SharedPreferences sharedPreferences = context.getSharedPreferences(MainActivity.SHARED_PREF_NAME, Context.MODE_PRIVATE);

        long startTime = 0;
        long sequenceAlarmStartTime;

        switch (nextAlarmType) {
            case MainActivity.ALARM_TYPE_WARNING:
                Log.i("I'm ok", "AlarmManager.setNextAlarm ALARM_TYPE_WARNING");
                startTime = System.currentTimeMillis() + Long.valueOf(sharedPreferences.getString(MainActivity.SHARED_PREF_WARNING_DELAY, null));
                writePref(context, MainActivity.ALARM_TYPE_WARNING, startTime);
                break;
            case MainActivity.ALARM_TYPE_WARNING_SOUNDOFF:
                Log.i("I'm ok", "AlarmManager.setNextAlarm ALARM_TYPE_WARNING_SOUNDOFF");
                startTime = System.currentTimeMillis() + Long.valueOf(sharedPreferences.getString(MainActivity.SHARED_PREF_WARNING_DURATION, null));
                sequenceAlarmStartTime = System.currentTimeMillis() + Long.valueOf(sharedPreferences.getString(MainActivity.SHARED_PREF_ALARM_DELAY, null));
                writePref(context, MainActivity.ALARM_TYPE_ALARM, sequenceAlarmStartTime);
                break;
            case MainActivity.ALARM_TYPE_ALARM:
                Log.i("I'm ok", "AlarmManager.setNextAlarm ALARM_TYPE_ALARM");
                startTime = System.currentTimeMillis()
                        - Long.valueOf(sharedPreferences.getString(MainActivity.SHARED_PREF_WARNING_DURATION, null))
                        + Long.valueOf(sharedPreferences.getString(MainActivity.SHARED_PREF_ALARM_DELAY, null));
                break;
            case MainActivity.ALARM_TYPE_ALARM_SOUNDOFF:
                Log.i("I'm ok", "AlarmManager.setNextAlarm ALARM_TYPE_ALARM_SOUNDOFF: " + sharedPreferences.getString(MainActivity.SHARED_PREF_ALARM_DURATION, null));
                startTime = System.currentTimeMillis() + Long.valueOf(sharedPreferences.getString(MainActivity.SHARED_PREF_ALARM_DURATION, null));
                writePref(context, MainActivity.ALARM_TYPE_UNKNOWN, System.currentTimeMillis() -1);
                break;
        }

        Intent intent = new Intent(context, AlarmManagerBroadcastReceiver.class);
        intent.putExtra(MainActivity.QUALIFIER_ALARM_TYPE, nextAlarmType);
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

    public void sendMessageFromLocationUpdate(final Context context, final int alarmType) {
        Log.d("I'm ok", "AlarmManager.sendMessageFromLocationUpdate");

        //get Location from Location Service
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        LocationListener locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                String locationText = "http://maps.google.com/maps?q="
                        + Double.toString(location.getLatitude()) + ","
                        + Double.toString(location.getLongitude());
                sendMessage(context, locationText, alarmType);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
                if (status == LocationProvider.OUT_OF_SERVICE) {
                    String locationText = "unknown location";
                    sendMessage(context, locationText, alarmType);
                }
            }

            @Override
            public void onProviderEnabled(String provider) {
            }

            @Override
            public void onProviderDisabled(String provider) {
                 String locationText = "unknown location";
                 sendMessage(context, locationText, alarmType);
            }
        };

        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        String bestProvider = locationManager.getBestProvider(criteria, true);
        locationManager.requestSingleUpdate(bestProvider, locationListener, null);
    }

    public void writePref(Context context, int alarmType, long nextAlarmTime) {
        Log.i("I'm ok", "AlarmManager.writePref (" + Integer.toString(alarmType) + "/" + Long.toString((nextAlarmTime - System.currentTimeMillis()) / 1000) + ")");

        SharedPreferences.Editor editor = context.getSharedPreferences(MainActivity.SHARED_PREF_NAME, Context.MODE_PRIVATE).edit();
        editor.putString(MainActivity.SHARED_PREF_ALARM_TYPE,Integer.toString(alarmType));
        editor.putString(MainActivity.SHARED_PREF_NEXT_ALARM,Long.toString(nextAlarmTime));
        editor.commit();
    }

    private void sendMessage(Context context, String locationText, int alarmType){
        Log.d("I'm ok", "AlarmManager.sendMessage");

        sendSms(context, locationText, alarmType);
        sendMail(context, locationText, alarmType);
    }

    private void sendMail(Context context, String locationText, int alarmType) {
        Log.d("I'm ok", "AlarmManager.sendSms");

        SharedPreferences sharedPreferences = context.getSharedPreferences(MainActivity.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        String warningMailTo = sharedPreferences.getString(MainActivity.SHARED_PREF_WARNING_MAIL_TO, null);
        String warningSmsText = sharedPreferences.getString(MainActivity.SHARED_PREF_WARNING_SMS_TEXT, null);
        String alarmMailTo = sharedPreferences.getString(MainActivity.SHARED_PREF_ALARM_MAIL_TO, null);
        String alarmSmsText = sharedPreferences.getString(MainActivity.SHARED_PREF_ALARM_SMS_TEXT, null);
        String manuallyMailTo = sharedPreferences.getString(MainActivity.SHARED_PREF_MANUALLY_MAIL_TO, null);
        String manuallySmsTelno = sharedPreferences.getString(MainActivity.SHARED_PREF_MANUALLY_SMS_TELNO, null);

        Date currentDateTime = new Date();
        currentDateTime.setTime(System.currentTimeMillis());

        String mailTo = "";
        String mailSubject = "";
        switch (alarmType) {
            case MainActivity.ALARM_TYPE_WARNING:
                mailTo = warningMailTo;
                mailSubject = warningSmsText;
                break;
            case MainActivity.ALARM_TYPE_ALARM:
                mailTo = alarmMailTo;
                mailSubject = alarmSmsText;
                break;
            case MainActivity.ALARM_TYPE_MANUALLY:
                mailTo = manuallyMailTo;
                mailSubject = manuallySmsTelno;
                break;
        }

        SimpleDateFormat ft = new SimpleDateFormat("dd.MM.yy HH:mm");
        String mailBody = mailSubject + " "
                + ft.format(currentDateTime) + " "
                + locationText;

        Log.i("I'm ok", "AlarmManager.sendMail: " + mailTo + ":" + mailBody);

        if (mailTo != ""){
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_SUBJECT, mailSubject);
            intent.putExtra(Intent.EXTRA_TEXT, mailBody);
            intent.setData(Uri.parse("mailto:" + mailTo));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }
    }

    private void sendSms(Context context, String locationText, int alarmType) {
        Log.d("I'm ok", "AlarmManager.sendSms");

        SharedPreferences sharedPreferences = context.getSharedPreferences(MainActivity.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        String warningSmsTelno = sharedPreferences.getString(MainActivity.SHARED_PREF_WARNING_SMS_TELNO, null);
        String warningSmsText = sharedPreferences.getString(MainActivity.SHARED_PREF_WARNING_SMS_TEXT, null);
        String alarmSmsTelno = sharedPreferences.getString(MainActivity.SHARED_PREF_ALARM_SMS_TELNO, null);
        String alarmSmsText = sharedPreferences.getString(MainActivity.SHARED_PREF_ALARM_SMS_TEXT, null);
        String manuallySmsText = sharedPreferences.getString(MainActivity.SHARED_PREF_MANUALLY_SMS_TEXT, null);
        String manuallySmsTelno = sharedPreferences.getString(MainActivity.SHARED_PREF_MANUALLY_SMS_TELNO, null);

        Date currentDateTime = new Date();
        currentDateTime.setTime(System.currentTimeMillis());

        String smsTelno = "";
        String smsText = "";
        switch (alarmType) {
            case MainActivity.ALARM_TYPE_WARNING:
                smsTelno = warningSmsTelno;
                smsText = warningSmsText;
                break;
            case MainActivity.ALARM_TYPE_ALARM:
                smsTelno = alarmSmsTelno;
                smsText = alarmSmsText;
                break;
            case MainActivity.ALARM_TYPE_MANUALLY:
                smsTelno = manuallySmsTelno;
                smsText = manuallySmsText;
                break;
        }

        SimpleDateFormat ft = new SimpleDateFormat("dd.MM.yy HH:mm");
        smsText = smsText + " "
                + ft.format(currentDateTime) + " "
                + locationText;

        Log.i("I'm ok", "AlarmManager.sendSms: " + smsTelno + ":" + smsText);
        if (smsTelno != ""){
            SmsManager sms = SmsManager.getDefault();
            sms.sendTextMessage(smsTelno, null, smsText, null, null);
        }
    }
}
