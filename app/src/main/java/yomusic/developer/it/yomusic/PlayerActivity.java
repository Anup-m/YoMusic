package yomusic.developer.it.yomusic;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import yomusic.developer.it.yomusic.Infos.SongInfo;
import yomusic.developer.it.yomusic.Utils.PreferencesUtils;
import yomusic.developer.it.yomusic.Utils.Utility;

import static yomusic.developer.it.yomusic.Fragments.SongListFragment.currSongPosition;
import static yomusic.developer.it.yomusic.MusicService.isPlaying;
import static yomusic.developer.it.yomusic.MusicService.isPrepared;
import static yomusic.developer.it.yomusic.MusicService.isShuffleOn;
import static yomusic.developer.it.yomusic.MusicService.repeatCount;
import static yomusic.developer.it.yomusic.Utils.Utility._favourites;
import static yomusic.developer.it.yomusic.Utils.Utility._nowPlayingList;

public class PlayerActivity extends AppCompatActivity  implements View.OnClickListener{

    private static final String TAG = "PlayerActivity";
    Context context;
    public static boolean checkIfPlayerUiNeedsToUpdate = false;
    private PreferencesUtils preferencesUtils;

    LinearLayout playerLayout;
    ImageButton btnShuffle, btnPrev, btnPlayPause, btnNext, btnRepeat, btnDock, btnVol;
    ImageView btnFav, btnQueueMusic, btnMenu,btnBack;
    ImageView albumArt;

    SeekBar seekBar;
    TextView songName, artistName, currDur, totalDur;

    ImageLoader imageLoader; //class for lazy loading images into an imageView...
    private Handler myHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        //getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        setContentView(R.layout.activity_player);

        initializeFields();
        updatePlayerUI();

        btnShuffle.setOnClickListener(this);
        btnPrev.setOnClickListener(this);
        btnPlayPause.setOnClickListener(this);
        btnNext.setOnClickListener(this);
        btnRepeat.setOnClickListener(this);

        btnFav.setOnClickListener(this);
        btnQueueMusic.setOnClickListener(this);
        btnMenu.setOnClickListener(this);
        btnBack.setOnClickListener(this);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            boolean userTouch;
            @Override
            public void onProgressChanged(SeekBar seekBar, int length, boolean b) {
                if(MusicService.mediaPlayer.isPlaying() && userTouch) {
                    MusicService.mediaPlayer.seekTo(length);
                    MusicService.mediaPlayer.start();
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                userTouch = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                userTouch = false;
            }
        });

        albumArt.setOnTouchListener(new OnSwipeTouchListener(PlayerActivity.this) {
            public void onSwipeBottom() {
                //onBackPressed();
                finishAfterTransition();
            }
            public void onSwipeRight(){
                playNext();
                //updatePlayerUI();
            }
            public void onSwipeLeft(){
                playPrev();
                //updatePlayerUI();
            }
        });
    }

    private void updatePlayerUI() {
        //For PlayPause Button
        SongInfo songInfo;
        if(currSongPosition == -1) {
            if(!(isPlaying)) {
                btnPlayPause.setImageDrawable(getResources().getDrawable(R.drawable.ic_p_play));
                currDur.setText("00:00");
                totalDur.setText("00:00");
            }
            else {
                btnPlayPause.setImageDrawable(getResources().getDrawable(R.drawable.ic_p_pause));
            }

            if(!_nowPlayingList.isEmpty())
                songInfo = Utility._nowPlayingList.get(currSongPosition + 1);  //TODO changed...
            else
                songInfo = Utility._songs.get(currSongPosition + 1);
        }
        else {
            if(isPlaying)
                btnPlayPause.setImageDrawable(getResources().getDrawable(R.drawable.ic_p_pause));
            else
                btnPlayPause.setImageDrawable(getResources().getDrawable(R.drawable.ic_p_play));

            if(!_nowPlayingList.isEmpty())
                songInfo = Utility._nowPlayingList.get(currSongPosition);  //changed...
            else
                songInfo = Utility._songs.get(currSongPosition);
        }

        //For Shuffle Button
        if(isShuffleOn){
            btnShuffle.setColorFilter(Color.BLACK);
        }

        //For Repeat Button
        if(repeatCount == 0){
            btnRepeat.setColorFilter(Color.GRAY);
            btnRepeat.setImageDrawable(getResources().getDrawable(R.drawable.ic_p_repeat));
        }
        else if(repeatCount == 1){
            btnRepeat.setColorFilter(Color.BLACK);
            btnRepeat.setImageDrawable(getResources().getDrawable(R.drawable.ic_p_repeate_one));
        }
        else {
            btnRepeat.setColorFilter(Color.BLACK);
            btnRepeat.setImageDrawable(getResources().getDrawable(R.drawable.ic_p_repeat));
        }

        //For TextBoxes like songName & artistName..
        songName.setText(songInfo.getSongName());
        artistName.setText(songInfo.getArtistName());

        //For albumArt
       imageLoader.displayImage(songInfo.getContentUris().toString(),albumArt);

        //For FavouriteButton
        if(_favourites.contains(songInfo)){
            btnFav.setImageDrawable(getResources().getDrawable(R.drawable.ic_p_favourite_recognised));
        } else
            btnFav.setImageDrawable(getResources().getDrawable(R.drawable.ic_p_favourite));

        //For seekBar
        try {
            Thread.sleep(500);
            setSeekBar();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        checkIfPlayerUiNeedsToUpdate = false;
    }

    private void setSeekBar() {
        try {
            if (isPrepared) {
                seekBar.setMax(MusicService.mediaPlayer.getDuration());
                seekBar.setProgress(MusicService.mediaPlayer.getCurrentPosition());

                if (currSongPosition != -1) {
                    String endTime = findTotalDuration(false, MusicService.mediaPlayer.getDuration());
                    totalDur.setText(endTime);
                    myHandler.postDelayed(UpdateSongTime, 100);
                }
            }
        }catch(IllegalStateException e){
            Log.i(TAG,"PlayerStopped:"+e.getMessage());
        }
    }

    private Runnable UpdateSongTime = new Runnable() {
        public void run() {
            int currTime = MusicService.mediaPlayer.getCurrentPosition();
            seekBar.setProgress(currTime);

            if(checkIfPlayerUiNeedsToUpdate) {
                updatePlayerUI();
                checkIfPlayerUiNeedsToUpdate = false;
            }

            String progressBarTime = findTotalDuration(false,currTime);
            currDur.setText(progressBarTime);
            myHandler.postDelayed(this, 100);
        }
    };


    private void initializeFields(){
        context = getApplicationContext();

        imageLoader = new ImageLoader(context,3);
        playerLayout = findViewById(R.id.player_layout);

        btnRepeat = (ImageButton) findViewById(R.id.pRepeat);
        btnShuffle = (ImageButton) findViewById(R.id.pShuffle);
        btnPrev = (ImageButton) findViewById(R.id.pPrevious);
        btnPlayPause = (ImageButton) findViewById(R.id.pPause);
        btnNext = (ImageButton) findViewById(R.id.pNext);

        btnFav = findViewById(R.id.pFavourits);
        btnQueueMusic = findViewById(R.id.pPlayingQueue);
        btnMenu = findViewById(R.id.pOverflow);
        btnBack = findViewById(R.id.pBack);
        songName = (TextView) findViewById(R.id.pSongName);
        artistName = (TextView)findViewById(R.id.pArtistName);

        currDur = (TextView)findViewById(R.id.pCurrDur);
        totalDur = (TextView)findViewById(R.id.pTotalDur);

        albumArt = (ImageView) findViewById(R.id.pAlbumArt);
        seekBar = (SeekBar)findViewById(R.id.pSeekBar);
        changeStatusBarColor();

        MusicService.getSharedPreferencesForShuffleAndRepeat(this);
    }

    private void playNext() {
        MusicService.playNext();
        //SongsListActivity.checkIfUiNeedsToUpdate = true;
        updatePlayerUI();
    }

    private void playPrev() {
        MusicService.playPrevious();
        //SongsListActivity.checkIfUiNeedsToUpdate = true;
        updatePlayerUI();
    }

    private void pause() {
        MusicService.mediaPlayer.pause();
        btnPlayPause.setImageDrawable((getResources().getDrawable(R.drawable.ic_p_play)));
        MusicService.playedLength = MusicService.mediaPlayer.getCurrentPosition();
        //SongsListActivity.checkIfUiNeedsToUpdate = true;
        isPlaying = false;
    }

    private void resume() {
        isPlaying = true;
        if(MusicService.playedLength == -1) {
            SongInfo songInfo = Utility._songs.get(currSongPosition + 1);
            MusicService.playSong(songInfo);
        }
        else {
            MusicService.mediaPlayer.seekTo(MusicService.playedLength);
            MusicService.mediaPlayer.start();
        }
        //btnPlayPause.setImageDrawable((getResources().getDrawable(R.drawable.ic_p_pause)));
        updatePlayerUI();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.pShuffle:
                shuffle(isShuffleOn);
                break;
            case R.id.pPrevious:
                playPrev();
                break;
            case R.id.pPause:
                if(isPrepared) {
                    if (MusicService.mediaPlayer.isPlaying())
                        pause();
                    else
                        resume();
                }
                else{
                    SongInfo song;
                    if(_nowPlayingList.isEmpty())
                        song = Utility._songs.get(currSongPosition);
                    else
                        song = Utility._nowPlayingList.get(currSongPosition);  //changed...

                    Intent playIntent = new Intent(this,MusicService.class);
                    playIntent.putExtra("songId",song.getSongId());
                    startService(playIntent);
                    updatePlayerUI();
                }
                break;
            case R.id.pNext:
                playNext();
                break;
            case R.id.pRepeat:
                if(repeatCount != 2)
                    repeatCount++;
                else
                    repeatCount = 0;
                repeat(repeatCount);
                break;
            case R.id.pFavourits:
                favourites();
                break;
            case R.id.pPlayingQueue:
                startDisplayTracksActivity("upComingList");
                break;
            case R.id.pOverflow:
                showPopupMenu(btnMenu);
                break;

            case R.id.pBack:
                //onBackPressed();
                finishAfterTransition();
                break;
        }
        try {
            Utility.recallNotificationBuilder(context);
        }catch (Exception ignored) { }
    }

    private void shuffle(boolean isShuffleOn) {
        preferencesUtils = new PreferencesUtils(context);
        if(isShuffleOn){
            isShuffleOn = false;
            btnShuffle.setColorFilter(Color.GRAY);
            Toast.makeText(this,"Shuffle off",Toast.LENGTH_SHORT).show();
        }
        else {
            isShuffleOn = true;
            btnShuffle.setColorFilter(Color.BLACK);
            Toast.makeText(this,"Shuffle on",Toast.LENGTH_SHORT).show();

            if(repeatCount != 0) {
                repeatCount = 0;
                preferencesUtils.addRepeatPreference(repeatCount);
                btnRepeat.setImageDrawable(getResources().getDrawable(R.drawable.ic_p_repeat));
                btnRepeat.setColorFilter(Color.GRAY);
            }
        }
        preferencesUtils.addShufflePreference(isShuffleOn);
    }

    private void repeat(int repeatCount) {
        preferencesUtils = new PreferencesUtils(context);
        switch (repeatCount){
            case 0:
                btnRepeat.setImageDrawable(getResources().getDrawable(R.drawable.ic_p_repeat));
                btnRepeat.setColorFilter(Color.GRAY);
                Toast.makeText(this,"Repeat off",Toast.LENGTH_SHORT).show();
                break;
            case 1:
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
                isShuffleOn = sharedPreferences.getBoolean(getString(R.string.shuffle_music), false);

                if(isShuffleOn) {
                    isShuffleOn = false;
                    preferencesUtils.addShufflePreference(isShuffleOn);
                    btnShuffle.setColorFilter(Color.GRAY);
                }

                btnRepeat.setImageDrawable(getResources().getDrawable(R.drawable.ic_p_repeate_one));
                btnRepeat.setColorFilter(Color.BLACK);

                Toast.makeText(this,"Repeat this song",Toast.LENGTH_SHORT).show();
                break;
            case 2:
                if(isShuffleOn){
                    isShuffleOn = false;
                    preferencesUtils.addShufflePreference(isShuffleOn);
                    btnShuffle.setColorFilter(Color.GRAY);
                }

                btnRepeat.setImageDrawable(getResources().getDrawable(R.drawable.ic_p_repeat));
                btnRepeat.setColorFilter(Color.BLACK);

                Toast.makeText(this,"Repeat all",Toast.LENGTH_SHORT).show();
                break;
        }
        preferencesUtils.addRepeatPreference(repeatCount);
    }

    private void favourites(){
        preferencesUtils = new PreferencesUtils(context,"favPlaylist");
        SongInfo song = Utility._nowPlayingList.get(currSongPosition);
        if(_favourites.contains(song)){
            _favourites.remove(song);
            preferencesUtils.saveListPreference(_favourites);
            btnFav.setImageDrawable(getResources().getDrawable(R.drawable.ic_p_favourite));
        } else {
            try {

                _favourites.add(song);
                preferencesUtils.saveListPreference(_favourites);
                btnFav.setImageDrawable(getResources().getDrawable(R.drawable.ic_p_favourite_recognised));
            } catch (Exception e){
                Log.d("FAVOURITE ERROR :", e.getMessage());
            }
        }
    }

    private void showPopupMenu(View view) {
        PopupMenu popup = new PopupMenu(this,view);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.player_menu, popup.getMenu());
        popup.setOnMenuItemClickListener(new PlayerActivity.MyMenuItemClickListener());
        popup.show();
    }

    class MyMenuItemClickListener implements PopupMenu.OnMenuItemClickListener {

        public MyMenuItemClickListener() {}

        @Override
        public boolean onMenuItemClick(MenuItem menuItem) {
            switch (menuItem.getItemId()) {

                case R.id.action_share:
                    try {
                        Utility.shareSong(context);
                    }catch (Exception e){
                        Log.d("SHARE_DETAILS ERROR :",e.getMessage());
                    }
                    break;

                case R.id.action_add_favourite:
                    //favourites();
                    break;

                case R.id.action_set_ringtone:
                    setRingTone();
                    break;

                case R.id.action_rename:
                    Toast.makeText(getApplicationContext(),"Feature under devlopment!!..",Toast.LENGTH_SHORT).show();
                    break;

                case R.id.action_settings:
                    startActivity(new Intent(getApplicationContext(),SettingsActivity.class));
                    break;

                default:
            }
            return false;
        }
    }

    private void setRingTone(){
        try {
            SongInfo songObj = Utility._songs.get(currSongPosition);
            long id = songObj.getSongId();
            getPermission(id);
        }catch (Exception e){
            Log.d("SETTING RINGTONE ERROR:",e.getMessage());
        }

    }

    private void checkPermission(long id){
        if(Build.VERSION.SDK_INT >= 23) {
            if(ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_SETTINGS)
                    != PackageManager.PERMISSION_GRANTED){
                requestPermissions(new String[]{Manifest.permission.WRITE_SETTINGS},123);
            }
        }
        else{
            Utility.setRingtone(context,id);
        }
    }

    private void getPermission(long id){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (Settings.System.canWrite(context)){
                // Do stuff here
                Utility.setRingtone(context,id);
            }
            else {
                Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_WRITE_SETTINGS);
                String s = "package:"+context.getPackageName();
                intent.setData(Uri.parse(s));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        SongInfo songObj = Utility._songs.get(currSongPosition);
        long id = songObj.getSongId();

        switch (requestCode){
            case 123:
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    Utility.setRingtone(context,id);
                }
                else{
                    Toast.makeText(this,"Permission Denied",Toast.LENGTH_SHORT).show();
                    checkPermission(id);
                }
                break;

            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        updatePlayerUI();
    }

    private void startDisplayTracksActivity(String listName) {
        String name="", number="";
        switch (listName){
            case "upComingList":
                name = "Up Next";
                number = String.valueOf(Utility._nowPlayingList.size());  //changed...
                break;
        }
        Intent intent = new Intent(context, DisplayTracksActivity.class);
        intent.putExtra("list", listName);
        intent.putExtra("name", name);
        intent.putExtra("noOfSongs",number);
        startActivity(intent);
    }

    public static String findTotalDuration(Boolean setEndTime,long time){
        long millis;
        if(setEndTime)
            millis = MusicService.mediaPlayer.getCurrentPosition();
        else
            millis = time;//For converting ProgressTime to H:M:S

        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
        long totalSeconds = TimeUnit.MILLISECONDS.toSeconds(millis);

        long seconds = totalSeconds - (minutes*60);
        String min = String.valueOf(minutes);
        String sec = String.valueOf(seconds);

        if(min.length() == 1)
            min = "0" + min;
        if(sec.length() == 1)
            sec = "0" + sec;

        return min + ":" + sec;
    }

    public void changeStatusBarColor(){
        Window window = this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(this.getResources().getColor(R.color.fui_transparent));
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

}
