package yomusic.developer.it.yomusic.Utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.ArrayList;
import yomusic.developer.it.yomusic.Infos.SongInfo;
import yomusic.developer.it.yomusic.R;

/**
 * Created by anupam on 11-02-2018.
 */

public class PreferencesUtils {
    private Context mContext;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor fileEditor;
    private SharedPreferences.Editor defaultEditor;

    @SuppressLint("CommitPrefEdits")
    public PreferencesUtils(Context ctx){
        mContext = ctx;
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(ctx);
        defaultEditor = sharedPreferences.edit();
    }

    @SuppressLint("CommitPrefEdits")
    public PreferencesUtils(Context context, String fileName) {
        mContext = context;
        sharedPreferences = context.getSharedPreferences(fileName, Context.MODE_PRIVATE);
        fileEditor = sharedPreferences.edit();
    }

    //saves list preference like favourites and last played
    public void saveListPreference(ArrayList<SongInfo> _list){
        fileEditor.putInt("listSize", _list.size());
        for(int i = 0; i < _list.size(); i++){
            fileEditor.putString(String.valueOf(i),_list.get(i).getSongName());
        }
        fileEditor.apply();
    }

    //returns list preference like favourites and last played
    public ArrayList<String> loadListPreference(){
        ArrayList<String> savedSongs = new ArrayList<String>();
        int numOfSavedSongs = sharedPreferences.getInt("listSize", 0);
        for (int i = 0; i < numOfSavedSongs; i++) {
            savedSongs.add(sharedPreferences.getString(String.valueOf(i),null));
        }
        return savedSongs;
    }

    // sets preference for shuffle
    public void addShufflePreference(boolean isShuffleOn) {
        defaultEditor.putBoolean(mContext.getString(R.string.shuffle_music),isShuffleOn);
        defaultEditor.apply();
    }

    // set preference for repeat
    public void addRepeatPreference(int repeatCount) {
        defaultEditor.putInt(mContext.getString(R.string.repeat_music),repeatCount);
        defaultEditor.apply();
    }




    //+--------------------------------------------------------------------------------------------------+
    //.................................. Functions not in use ...........................................
    //+--------------------------------------------------------------------------------------------------+


    // Set preference for signIn
    public void addSignInPreference(boolean value){
        defaultEditor.putBoolean(mContext.getString(R.string.log_out),value);
        defaultEditor.apply();
    }

    // Gets preference for signIn
    public boolean getSignInPreference(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getBoolean(context.getString(R.string.log_out),false);
    }

}
