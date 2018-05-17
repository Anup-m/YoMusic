package yomusic.developer.it.yomusic;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.IBinder;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import java.util.Random;

import yomusic.developer.it.yomusic.Infos.SongInfo;
import yomusic.developer.it.yomusic.Utils.Utility;

import static yomusic.developer.it.yomusic.Fragments.SongListFragment.currSongPosition;

/**
 * Created by anupam on 08-02-2018.
 */
public class Testing extends Service implements MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener {

    public static long songId;
    public static MediaPlayer mediaPlayer;
    public static boolean isPrepared = false;
    public static boolean isPlaying = false;
    private static boolean isShuffleOn = false;
    private static int repeatCount = 0;
    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.reset();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        songId = intent.getExtras().getLong("songId", 0);
        SongInfo song = SongInfo.getSongInfoObject(songId);
        playSong(song);
        return START_STICKY;
    }

    private static void playSong(SongInfo obj){
        try {
            Utility.addSongsToLastPlayedList(context, obj);
            Uri uri = SongInfo.getSongUri(songId);
            mediaPlayer.reset();

            if (!mediaPlayer.isPlaying()) {
                if (uri != null) {
                    try {
                        mediaPlayer.setDataSource(context, uri);
                        mediaPlayer.prepareAsync();
                    } catch (Exception e) {
                        Log.e("Playing Error:", e.getMessage());
                        Toast.makeText(context, "Couldn't play this song", Toast.LENGTH_SHORT).show();
                        playNext();
                    }
                }
            }
        }catch (IllegalArgumentException | IllegalStateException e) {
            e.printStackTrace();
        }catch (Exception e){
            e.getMessage();
        }
    }

    public static void playNext() {
        getSharedPreferencesForShuffleAndRepeat(context);
        if(isShuffleOn){
            generateRandomSongPosition();
        }
        else if(repeatCount == 1){

        }
        else {
            currSongPosition++;
            if (currSongPosition >= Utility._songs.size())
                currSongPosition = 0;
        }
        SongInfo songInfo = Utility._songs.get(currSongPosition);
        playSong(songInfo);
        //Utility.recallNotificationBuilder(context);
    }

    public static void playPrevious() {
        getSharedPreferencesForShuffleAndRepeat(context);
        if(isShuffleOn){
            generateRandomSongPosition();
        }
        else {
            currSongPosition--;
            if (currSongPosition < 0)
                currSongPosition = Utility._songs.size() - 1;
        }
        SongInfo songInfo = Utility._songs.get(currSongPosition);
        playSong(songInfo);
        //Utility.recallNotificationBuilder(context);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
            stopSelf();
        }
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        isPrepared = true;
        play();
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if(mediaPlayer.isPlaying()){
            isPlaying = false;
            //mediaPlayer.release();
            //stopSelf();
        }
    }

    private static void generateRandomSongPosition() {
        Random randomGenerate = new Random();
        currSongPosition = randomGenerate.nextInt( Utility._songs.size() );
        if(Utility.random_list.contains(currSongPosition))
            generateRandomSongPosition();
        else
            Utility.random_list.add(currSongPosition);
    }

    public static void play() {
        if(!mediaPlayer.isPlaying()) {
            mediaPlayer.start();
            isPlaying = true;
        }
    }

    private static void getSharedPreferencesForShuffleAndRepeat(Context context) {
        //sharedPreference data for shuffle and repeat
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        isShuffleOn = sharedPreferences.getBoolean(context.getString(R.string.shuffle_music), false);
        repeatCount = sharedPreferences.getInt(context.getString(R.string.repeat_music),0);
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        switch (what) {
            case MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK :
                Log.d("MediaPlayer Error", "MEDIA ERROR NOT VALID FOR PROGRESSIVE PLAYBACK " + extra);
                mp.reset();
                break;
            case MediaPlayer.MEDIA_ERROR_SERVER_DIED :
                Log.d("MediaPlayer Error", "MEDIA ERROR SERVER DIED " + extra);
                mp.reset();
                break;
            case MediaPlayer.MEDIA_ERROR_UNKNOWN :
                Log.d("MediaPlayer Error", "MEDIA ERROR UNKNOWN " + extra);
                mp.reset();
                break;
        }
        return false;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}


/*

    public static long songId;
    public static MediaPlayer mediaPlayer;
    public static boolean isPrepared = false;
    public static boolean isPlaying = false;
    private Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.reset();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            songId = intent.getExtras().getLong("songId", 0);
            SongInfo obj = SongInfo.getSongInfoObject(songId);
            Utility.addSongsToLastPlayedList(context, obj);
            Uri uri = SongInfo.getSongUri(songId);
            mediaPlayer.reset();

            if (!mediaPlayer.isPlaying()) {
                if (uri != null) {
                    try {
                        mediaPlayer.setDataSource(this, uri);
                        mediaPlayer.prepareAsync();
                    } catch (Exception e) {
                        Log.e("Playing Error:", e.getMessage());
                        Toast.makeText(context, "Couldn't play this song", Toast.LENGTH_SHORT).show();
                        //playNext();
                        //SongInfo songInfo = _songs.get(currSongPosition);
                        //playSong(songInfo,currSongPosition,context);
                    }
                }
            }
        }catch (IllegalArgumentException | IllegalStateException e) {
            e.printStackTrace();
        }catch (Exception e){
            e.getMessage();
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
            stopSelf();
        }
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        isPrepared = true;
        playSong();
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if(mediaPlayer.isPlaying()){
            isPlaying = false;
            //mediaPlayer.release();
            //stopSelf();
        }
    }

    public static void playSong() {
        if(!mediaPlayer.isPlaying()) {
            mediaPlayer.start();
            isPlaying = true;
        }

    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        switch (what) {
            case MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK :
                Log.d("MediaPlayer Error", "MEDIA ERROR NOT VALID FOR PROGRESSIVE PLAYBACK " + extra);
                mp.reset();
                break;
            case MediaPlayer.MEDIA_ERROR_SERVER_DIED :
                Log.d("MediaPlayer Error", "MEDIA ERROR SERVER DIED " + extra);
                mp.reset();
                break;
            case MediaPlayer.MEDIA_ERROR_UNKNOWN :
                Log.d("MediaPlayer Error", "MEDIA ERROR UNKNOWN " + extra);
                mp.reset();
                break;
        }
        return false;
    }

    @Nullable @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


 */