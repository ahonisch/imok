package info.honisch.imok;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

/**
 * Created by Andi on 26.02.2015.
 */
public class EditTextActionListener implements TextView.OnEditorActionListener {
    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        boolean isHandled = false;

        if(actionId == EditorInfo.IME_ACTION_NEXT) {
            switch (v.getId()) {
                case R.id.txt_warning_sms_telno:
                    writePref(v.getContext(), MainActivity.SHARED_PREF_WARNING_SMS_TELNO, v.getText().toString());
                    isHandled = true;
                    break;
                case R.id.txt_warning_sms_text:
                    writePref(v.getContext(), MainActivity.SHARED_PREF_WARNING_SMS_TEXT, v.getText().toString());
                    isHandled = true;
                    break;
                case R.id.txt_alarm_sms_telno:
                    writePref(v.getContext(), MainActivity.SHARED_PREF_ALARM_SMS_TELNO, v.getText().toString());
                    isHandled = true;
                    break;
                case R.id.txt_alarm_sms_text:
                    writePref(v.getContext(), MainActivity.SHARED_PREF_ALARM_SMS_TELNO, v.getText().toString());
                    isHandled = true;
                    break;
                case R.id.txt_manually_sms_telno:
                    writePref(v.getContext(), MainActivity.SHARED_PREF_MANUALLY_SMS_TELNO, v.getText().toString());
                    isHandled = true;
                    break;
                case R.id.txt_manually_sms_text:
                    writePref(v.getContext(), MainActivity.SHARED_PREF_MANUALLY_SMS_TEXT, v.getText().toString());
                    isHandled = true;
                    break;
            }
        }

        return false;
    }

    private void writePref(Context context, String prefKey, String prefValue) {
        Log.i("I'm ok", "writePref: " + prefKey + "=" + prefValue);

        SharedPreferences.Editor editor = context.getSharedPreferences(MainActivity.SHARED_PREF_NAME, Context.MODE_PRIVATE).edit();
        editor.putString(prefKey, prefValue);
        editor.commit();
    }

}
