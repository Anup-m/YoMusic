package yomusic.developer.it.yomusic;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.MultiSelectListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import yomusic.developer.it.yomusic.Utils.Utility;

import static yomusic.developer.it.yomusic.Utils.Utility.auth;
import static yomusic.developer.it.yomusic.Utils.Utility.getAlbumThumbnails;

public class SettingsActivity extends AppCompatPreferenceActivity {

    public static boolean hasDataChanged = false;

    private static Context context;
    public static ArrayList<String> folderContainingMusic = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getFragmentManager().beginTransaction().replace(android.R.id.content, new MainPreferenceFragment()).commit();
        context = getApplicationContext();
    }

    public static class MainPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_main);

            //Filter SwitchPreferences
            bindPreferenceSummaryToValue(findPreference(getString(R.string.filter_by_name)));
            bindPreferenceSummaryToValue(findPreference(getString(R.string.filter_by_extension)));

            //music folders MultiSelectListPreference
            final MultiSelectListPreference musicFoldersPreference = (MultiSelectListPreference) findPreference(getString(R.string.music_folders));
            final ArrayList<String> musicFolders = Utility.getMusicFoldersList();
            folderContainingMusic.addAll(musicFolders);
            bindPreferenceSummaryToValue(musicFoldersPreference,musicFolders);

            //sleepTimer ListPreference
            final ListPreference sleepDataPref = (ListPreference) findPreference("sleep_timer");
            String val = sleepDataPref.getValue();
            int index = getIndexForValue(val);
            sleepDataPref.setValueIndex(index);
            bindPreferenceSummaryToValue(sleepDataPref);

            //logout Preference
            final Preference logOutPreference = findPreference(getString(R.string.log_out));
            logOutPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    //logoutAlert();
                    auth = FirebaseAuth.getInstance();
                    auth.signOut();
                    Toast.makeText(context,"SIGNED OUT ",Toast.LENGTH_SHORT).show();
                    getActivity().onBackPressed();
                    return true;
                }
            });
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    public static void bindPreferenceSummaryToValue(Preference preference) {
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }

    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            String stringValue = newValue.toString();

            if (preference instanceof ListPreference) {
                // For list preferences, look up the correct display value in
                // the preference's 'entries' list.
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);
                // Set the summary to reflect the new value.
                preference.setSummary(index >= 0 ? listPreference.getEntries()[index] : null);
                // Turn on/off the sleep-timer according to the value in listPreference
                setSleepTimeForListPreferenceValue(listPreference,stringValue);
                hasDataChanged = true;

            } else if (preference instanceof EditTextPreference) {
                if (preference.getKey().equals("filter_by_name") ||preference.getKey().equals("filter_by_extension")) {
                    preference.setSummary(stringValue);
                    hasDataChanged = true;
                }
            } else if(preference instanceof MultiSelectListPreference){
                MultiSelectListPreference multiListPreference = (MultiSelectListPreference) preference;

                int folderCount = multiListPreference.getEntries().length;
                int selectedCount = ((Set<String>) newValue).size();

                preference.setSummary(preference.getContext().getString(
                        R.string.summary_music_folders_new, selectedCount, folderCount));
                hasDataChanged = true;
            } else {
                preference.setSummary(stringValue);
                hasDataChanged = true;
            }
            return true;
        }
    };

    private static void bindPreferenceSummaryToValue(MultiSelectListPreference preference, ArrayList<String> folders) {
        int len = folders.size();
        CharSequence[] entries = new CharSequence[len];
        CharSequence[] entryValues = new CharSequence[len];
        Set<String> defaultValue = new HashSet<String>();

        Set<String> savedFolders = PreferenceManager.getDefaultSharedPreferences(
                preference.getContext()).getStringSet(preference.getKey(), null);

        // Populate MultiSelectListPreference with entries for all available folders
        entries = folders.toArray(new CharSequence[folders.size()]);
        entryValues = new CharSequence[entries.length];
        for(int i = 0; i < entries.length; i++) {
            String s = String.valueOf(i);
            entryValues[i] = s;
            defaultValue.add(s);
            Log.i("entryValues", entryValues[i].toString());
        }

//        for(int i = 0; i < entries.length; i++) {
//            String s = String.valueOf(i);
//            entryValues[i] = entries[i];
//            defaultValue.add(String.valueOf(entryValues[i]));
//            Log.i("entryValues", entryValues[i].toString());
//        }

        preference.setEntries(entries);
        preference.setEntryValues(entryValues);

        // Check currently selected folders
        Set<String> selectedFolders;
        if (savedFolders == null) {
            // Select all folders if there was no saved configuration
            selectedFolders = defaultValue;
        } else {
            // Clear out folders that no longer exist
            selectedFolders = new HashSet<String>(savedFolders);
            for (Iterator<String> iter = selectedFolders.iterator(); iter.hasNext(); ) {
                String folderName = iter.next();
                if (!defaultValue.contains(folderName)) {
                    iter.remove();
                }
            }
        }
        preference.setValues(selectedFolders);

        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference, selectedFolders);

        preference.setEnabled(true);
    }

    private static void setSleepTimeForListPreferenceValue(ListPreference listPreference, String stringValue){
        if(listPreference.getKey().equals("sleep_timer")){
            String value = stringValue;
            //Toast.makeText(this,value,Toast.LENGTH_LONG).show();
            Log.i("SleepTime :",value);
            if(!value.equals("") && !value.equals(" ")) {
                if (!value.equals("0") && !value.equals("00")) {
                    if(!Utility.isTimerOn)
                        setSleepTime(value);
                    else
                        Utility.sleep_timer.cancel();
                }
                else if(value.equals("00")){
                    Utility.isTimerOn = false;
                    if(Utility.isTimerOn) {
                        Utility.sleep_timer.cancel();
                        Toast.makeText(context, "Timer off", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }

    private static void setSleepTime(String sleepTime){
        long sleepTimeInMillis = Long.valueOf(sleepTime);
        sleepTimeInMillis *= 60000;

        if(!Utility.isTimerOn) {
            Utility.setTimer(context, sleepTimeInMillis);
            Toast.makeText(context, "Sleep timer set for " + sleepTime + " minutes", Toast.LENGTH_SHORT).show();
        }
        else {
            Toast.makeText(context, "Sleep timer running", Toast.LENGTH_SHORT).show();
        }
    }

    private static int getIndexForValue(String val) {
        int index;
        switch (val){
            case "00":
                index = 0;
                return index;
            case "2":
                index = 1;
                return index;
            case "10":
                index = 2;
                return index;
            case "20":
                index = 3;
                return index;
            case "30":
                index = 4;
                return index;
            case "60":
                index = 5;
                return index;
            case "0":
                index = 6;
                return index;
        }
        return -1;
    }

    public static   void logoutAlert() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder.setTitle("Logout!!");
        alertDialogBuilder
                .setMessage("Really want to Logout??")
                .setCancelable(false)
                .setPositiveButton("Yes",
                        new DialogInterface.OnClickListener()
                        {
                            public void onClick(DialogInterface dialog, int id)
                            {
                                auth.signOut();
                                Toast.makeText(context,"You are loggout ",Toast.LENGTH_SHORT).show();
                                context.startActivity(new Intent(context,MainActivity.class));
                            }
                        })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id)
                    {
                        dialog.cancel();
                    }
                });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }
}

/*


                if(listPreference.getKey().equals("sleep_timer")){
                    String value = stringValue;
                    //Toast.makeText(this,value,Toast.LENGTH_LONG).show();
                    Log.i("SleepTime :",value);
                    if(!value.equals("") && !value.equals(" ")) {
                        if (!value.equals("0") && !value.equals("00")) {
                           if(!Utility.isTimerOn)
                               setSleepTime(value);
                           else
                               Utility.sleep_timer.cancel();
                        }
                        else if(value.equals("00")){
                            Utility.isTimerOn = false;
                            if(Utility.isTimerOn) {
                                Utility.sleep_timer.cancel();
                                Toast.makeText(context, "Timer off", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }



                if(preference.getKey().equals("log_out")){
                    MainActivity.logOut();
                }
 */