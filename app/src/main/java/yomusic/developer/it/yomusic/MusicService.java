package yomusic.developer.it.yomusic;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.IBinder;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import java.util.Random;

import yomusic.developer.it.yomusic.Fragments.SongListFragment;
import yomusic.developer.it.yomusic.Infos.SongInfo;
import yomusic.developer.it.yomusic.Utils.Utility;

import static yomusic.developer.it.yomusic.Fragments.SongListFragment.currSongPosition;

/**
 * Created by anupam on 1-02-2018.
 */

public class MusicService extends Service implements MediaPlayer.OnCompletionListener,
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener {

    private static final String TAG = "MusicService" ;
    @SuppressLint("StaticFieldLeak")
    private static Context context;

    public static long songId;
    public static MediaPlayer mediaPlayer;
    public static boolean isPrepared = false;
    public static boolean isPlaying = false;
    public static boolean isShuffleOn = false;
    public static int repeatCount = 0;
    public static int playedLength = -1;

    // Declare variables & references for listening to incoming calls...
    private boolean isPausedInCall = false;
    private PhoneStateListener phoneStateListener;
    private TelephonyManager telephonyManager;

    // Declare headsetSwitch variable
    private int headsetSwitch = 1;

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

        getSharedPreferencesForShuffleAndRepeat(this);
        // Register headset receiver
        registerReceiver(headsetReceiver, new IntentFilter(
                Intent.ACTION_HEADSET_PLUG));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Manage incoming phone calls during playback. Pause mp on incoming,
        // resume on hangup.
        // -----------------------------------------------------------------------------------
        // Get the telephony manager
        Log.v(TAG, "Starting telephony");
        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        Log.v(TAG, "Starting listener");
        phoneStateListener = new PhoneStateListener() {
            @Override
            public void onCallStateChanged(int state, String incomingNumber) {
                // String stateString = "N/A";
                Log.v(TAG, "Starting CallStateChange");
                switch (state) {
                    case TelephonyManager.CALL_STATE_OFFHOOK:
                    case TelephonyManager.CALL_STATE_RINGING:
                        if (mediaPlayer != null) {
                            pauseMedia();
                            isPausedInCall = true;
                        }
                        break;
                    case TelephonyManager.CALL_STATE_IDLE:
                        // Phone idle. Start playing.
                        if (mediaPlayer != null) {
                            if (isPausedInCall) {
                                isPausedInCall = false;
                                //resumeMedia();
                                play();
                            }
                        }
                        break;
                }
            }
        };

        // Register the listener with the telephony manager
        telephonyManager.listen(phoneStateListener,
                PhoneStateListener.LISTEN_CALL_STATE);

        try {
            songId = intent.getExtras().getLong("songId", 0);
            if(songId != -1) {
                SongInfo song = SongInfo.getSongInfoObject(songId);
                playSong(song);
            }
        }catch (Exception ignored){ }
        return START_STICKY;
    }

    public static void playSong(SongInfo song){
        try {
            Utility.addSongsToLastPlayedList(context, song);
            Uri uri = SongInfo.getSongUri(song.getSongId());
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
            Log.e(TAG, e.getMessage());
        }catch (Exception e){
            e.printStackTrace();
            Log.e(TAG, e.getMessage());
        }
    }

    public static void playNext() {
        SongInfo songInfo;
        try {
            getSharedPreferencesForShuffleAndRepeat(context);
        }catch (Exception ignored){ }

        if(isShuffleOn){
            generateRandomSongPosition();
        }
        else if(repeatCount == 1){

        }
        else {
            currSongPosition++;
            if(Utility._nowPlayingList.isEmpty()){
                if (currSongPosition >= Utility._songs.size())
                    currSongPosition = 0;
            }
            else {
                if(currSongPosition >= Utility._nowPlayingList.size())  //changed...
                currSongPosition = 0;
            }
        }
        if(Utility._nowPlayingList.isEmpty())
            songInfo = Utility._songs.get(currSongPosition);
        else
            songInfo = Utility._nowPlayingList.get(currSongPosition);  //changed...
        playSong(songInfo);
        Utility.recallNotificationBuilder(context);
    }

    public static void playPrevious() {
        SongInfo songInfo;
        try {
            getSharedPreferencesForShuffleAndRepeat(context);
        }catch (Exception ignored){ }

        if(isShuffleOn){
            generateRandomSongPosition();
        }
        else {
            currSongPosition--;
            if(Utility._nowPlayingList.isEmpty()){
                if (currSongPosition < 0)
                    currSongPosition = Utility._songs.size() - 1;
            }
            else {
                if (currSongPosition < 0)
                    currSongPosition = Utility._nowPlayingList.size() - 1;  //changed...
            }
        }
        if(Utility._nowPlayingList.isEmpty())
            songInfo = Utility._songs.get(currSongPosition);
        else
            songInfo = Utility._nowPlayingList.get(currSongPosition);  //changed...
        playSong(songInfo);
        Utility.recallNotificationBuilder(context);
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        isPrepared = true;
        play();
        PlayerActivity.checkIfPlayerUiNeedsToUpdate = true;
        MainActivity.checkIfUiNeedsToUpdate = true;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        //stopSelf();
        if(mediaPlayer.getCurrentPosition()> 0){
            mediaPlayer.reset();
            playNext();
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

    public static final String PLAYING_BROADCAST = "playing";
    public void play() {
        if(!mediaPlayer.isPlaying()) {
            mediaPlayer.start();
            alertUIs(true);
            //Sending a broadcast so that music controllers can be updated now.....
            getApplicationContext().sendBroadcast(new Intent(PLAYING_BROADCAST));
        }
    }

    public void resumeMedia() {
        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.seekTo(playedLength);
            mediaPlayer.start();
            alertUIs(true);
        }
    }

    // Added for Telephony Manager
    public void pauseMedia() {
        // Log.v(TAG, "Pause Media");
        if (mediaPlayer.isPlaying()) {
            playedLength = mediaPlayer.getCurrentPosition();
            mediaPlayer.pause();
            alertUIs(false);
        }
    }

    public void stopMedia() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            alertUIs(false);
        }
    }

    // If headset gets unplugged, stop music and service.
    private BroadcastReceiver headsetReceiver = new BroadcastReceiver() {
        private boolean headsetConnected = false;

        @Override
        public void onReceive(Context context, Intent intent) {
            // Log.v(TAG, "ACTION_HEADSET_PLUG Intent received");
            if (intent.hasExtra("state")) {
                if (headsetConnected && intent.getIntExtra("state", 0) == 0) {
                    headsetConnected = false;
                    headsetSwitch = 0;
                    // Log.v(TAG, "State =  Headset disconnected");
                    // headsetDisconnected();
                } else if (!headsetConnected
                        && intent.getIntExtra("state", 0) == 1) {
                    headsetConnected = true;
                    headsetSwitch = 1;
                    // Log.v(TAG, "State =  Headset connected");
                }

            }

            switch (headsetSwitch) {
                case (0):
                    headsetDisconnected();
                    break;
                case (1):
                    break;
            }
        }

    };

    private void headsetDisconnected() {
        isPlaying = false;
        stopMedia();
        stopSelf();  // changed...
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                isPlaying = false;
                mediaPlayer.stop();
            }
            mediaPlayer.release();
            stopSelf();
        }

        //Stop the TelephonyManager to listen to calls anymore..
        if (phoneStateListener != null) {
            telephonyManager.listen(phoneStateListener,
                    PhoneStateListener.LISTEN_NONE);
        }

        // Cancel Notification
        NotificationGenerator.exitNotification(context);

        // Unregister headsetReceiver
        unregisterReceiver(headsetReceiver);

        alertUIs(false);
    }

    private void alertUIs(boolean playing){
        isPlaying = playing;
        PlayerActivity.checkIfPlayerUiNeedsToUpdate = true;
        SongListFragment.checkIfUiNeedsToUpdate = true;
        Utility.recallNotificationBuilder(getApplicationContext());
    }

    public static void getSharedPreferencesForShuffleAndRepeat(Context context) {
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
