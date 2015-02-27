package info.honisch.imok;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;

public class SpinnerSelectedListener implements AdapterView.OnItemSelectedListener {
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        Log.i("I'm ok", "ItemSelected: " + parent.getItemAtPosition(position).toString() + " view:" + parent.getId() + "/" + R.id.spinner_warning_delay);

        Context context = view.getContext();
        String selection = parent.getItemAtPosition(position).toString();
        String prefValueMinute = Long.toString(extractIntFromSpinnerSelection(context, selection, R.string.unit_minute) * 60000);
        String prefValueSecond = Long.toString(extractIntFromSpinnerSelection(context, selection, R.string.unit_minute) * 1000);

        switch (parent.getId()) {
            case R.id.spinner_warning_delay:
                writePref(view.getContext(), MainActivity.SHARED_PREF_WARNING_DELAY, prefValueMinute);
                break;
            case R.id.spinner_warning_duration:
                writePref(view.getContext(), MainActivity.SHARED_PREF_WARNING_DURATION, prefValueSecond);
                break;
            case R.id.spinner_alarm_delay:
                writePref(view.getContext(), MainActivity.SHARED_PREF_ALARM_DELAY, prefValueMinute);
                break;
            case R.id.spinner_alarm_duration:
                writePref(view.getContext(), MainActivity.SHARED_PREF_ALARM_DURATION, prefValueSecond);
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    private long extractIntFromSpinnerSelection(Context context, String selection, int unitId){
        Log.d("I'm ok", "extractIntFromSpinnerSelection: " + selection + "=" + context.getString(unitId));
        Log.d("I'm ok", "extractIntFromSpinnerSelection: " + selection.substring(0, 2) + "/" + selection.length() + "/" + context.getString(unitId).length());
        return Long.valueOf(selection.substring(0, selection.length() - (context.getString(unitId).length()+1)));
    }

    private void writePref(Context context, String prefKey, String prefValue) {
        Log.i("I'm ok", "writePref: " + prefKey + "=" + prefValue);

        SharedPreferences.Editor editor = context.getSharedPreferences(MainActivity.SHARED_PREF_NAME, Context.MODE_PRIVATE).edit();
        editor.putString(prefKey, prefValue);
        editor.commit();
    }

}
