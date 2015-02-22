package info.honisch.imok;

import android.app.IntentService;
import android.app.Service;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.IBinder;
import android.util.Log;

/**
 * Created by Andi on 19.02.2015.
 */
public class AlarmRingtoneService extends Service {
    private static Ringtone m_ringtone;

        @Override
    public IBinder onBind(Intent intent) {
            return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("I'm ok", "Start AlarmRingtoneService");

        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        m_ringtone = RingtoneManager.getRingtone(this, notification);

        m_ringtone.play();

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.d("I'm ok", "Stop AlarmRingtoneService");

        m_ringtone.stop();
    }
}
