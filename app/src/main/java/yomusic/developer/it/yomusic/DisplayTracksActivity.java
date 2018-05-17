package yomusic.developer.it.yomusic;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.ArrayList;

import yomusic.developer.it.yomusic.Adapters.SongRecyclerAdapter;
import yomusic.developer.it.yomusic.Infos.SongInfo;
import yomusic.developer.it.yomusic.Utils.Utility;

import static yomusic.developer.it.yomusic.Fragments.SongListFragment.currSongPosition;
import static yomusic.developer.it.yomusic.MainActivity.lastPlayedSongItem;
import static yomusic.developer.it.yomusic.MusicService.isPlaying;
import static yomusic.developer.it.yomusic.MusicService.mediaPlayer;
import static yomusic.developer.it.yomusic.MusicService.playedLength;
import static yomusic.developer.it.yomusic.Utils.Utility._nowPlayingList;

public class DisplayTracksActivity extends AppCompatActivity implements View.OnClickListener{

    private static final String TAG = "DisplayTracksActivity";
    Context context;
    View mRootView;

    ImageView btnSearch, btnMenu, btnBack,contentImage;
    TextView contentName, contentPath,contentNumber;
    String name,path,number;

    RecyclerView recyclerView;
    SongRecyclerAdapter songRecyclerAdapter;

    //MusicController
    public boolean checkIfUiNeedsToUpdate = false;
    ImageButton btn_playPause;
    SeekBar seekBar;
    ImageView mcAlbumArt;
    TextView songTitle,artistName;
    View mediaController;
    ImageLoader imageLoader;
    private Handler myHandler = new Handler();
    Animation play_in, pause_in, rotate_clockwise, rotate_anticlockwise;

    ArrayList<SongInfo> _songList = new ArrayList<>();

    //Broadcast Receiver
    private BroadcastReceiver broadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_tracks);

        initializeFields();
        final String extraIntent  = getIntent().getStringExtra("list");
        setSongList(extraIntent);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),linearLayoutManager.getOrientation());

        songRecyclerAdapter = new SongRecyclerAdapter(_songList, this);
        recyclerView.addItemDecoration(dividerItemDecoration);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(songRecyclerAdapter);

        songRecyclerAdapter.setOnItemClickListener(new SongRecyclerAdapter.OnItemClickListener() {
            @Override
            public void OnItemClick(View v, SongInfo obj, int position) {
                _nowPlayingList = new ArrayList<>();
                _nowPlayingList.addAll(_songList);
                currSongPosition = AllSongsActivity.getRealSongPosition2(obj);  //changed....
                Intent serviceIntent = new Intent(getApplicationContext(), MusicService.class);
                serviceIntent.putExtra("songId",obj.getSongId());
                startService(serviceIntent);

                //updateMediaController();
            }
        });

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                updateMediaController();
            }
        };
        registerReceiver(broadcastReceiver,new IntentFilter(MusicService.PLAYING_BROADCAST));

        btnBack.setOnClickListener(this);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            boolean userTouch;
            @Override
            public void onProgressChanged(SeekBar seekBar, int length, boolean b) {
                try {
                    if(mediaPlayer.isPlaying() && userTouch) {
                        mediaPlayer.seekTo(length);
                        mediaPlayer.start();
                    }
                } catch (Exception e) { }

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
        mediaController.setOnTouchListener(new OnSwipeTouchListener(this){
            public void onSwipeTop() {
                PlayerActivity.checkIfPlayerUiNeedsToUpdate = true;
                Intent playerIntent = new Intent(getApplicationContext(),PlayerActivity.class);
                startActivity(playerIntent);
                overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
            }
        });
        btn_playPause.setOnClickListener(this);
        updateMediaController();
    }


    private void  updateMediaController() {
        SongInfo songInfo;
        if(currSongPosition == -1){
            songInfo = Utility._songs.get(currSongPosition + 1); //changed...
            songTitle.setText(songInfo.getSongName());
            artistName.setText(songInfo.getArtistName());
            imageLoader.displayImage(songInfo.getContentUris().toString(),mcAlbumArt);
            //random_list.add(0);
        }
        else {
            try {
                if(_nowPlayingList.isEmpty()){
                    songInfo = Utility._songs.get(currSongPosition);
                } else {
                    songInfo = Utility._nowPlayingList.get(currSongPosition);
                }
                songTitle.setText(songInfo.getSongName());
                artistName.setText(songInfo.getArtistName());
                imageLoader.displayImage(songInfo.getContentUris().toString(),mcAlbumArt);

                if (isPlaying) {
                    btn_playPause.setImageDrawable(getResources().getDrawable(R.drawable.ic_mc_pause));
                    btn_playPause.startAnimation(rotate_anticlockwise);
                } else {
                    btn_playPause.setImageDrawable(getResources().getDrawable(R.drawable.ic_mc_play));
                    btn_playPause.startAnimation(rotate_anticlockwise);
                }
            }catch (Exception ignored){
                //updateMediaController();
            }

            try {
                Thread.sleep(100);
                setSeekBar();
            }catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        checkIfUiNeedsToUpdate = false;
    }

    private void setSeekBar() {
        try{
            seekBar.setMax(mediaPlayer.getDuration());

            if(playedLength != -1){
                seekBar.setProgress(playedLength);
            } else {
                seekBar.setProgress(mediaPlayer.getCurrentPosition());
            }
            myHandler.postDelayed(UpdateSongTime, 100);
        } catch (Exception e){

        }
    }

    private Runnable UpdateSongTime = new Runnable() {
        public void run() {
            try {
                seekBar.setProgress(mediaPlayer.getCurrentPosition());

                if (checkIfUiNeedsToUpdate) {
                    updateMediaController();
                    checkIfUiNeedsToUpdate = false;
                }
                myHandler.postDelayed(this, 100);
            }catch (IllegalStateException e){
                Log.i(TAG,"HeadSet Unplugged MediaPlayerStopped :"+e.getMessage());
            }
        }
    };



    private void setSongList( String extraIntent) {
        switch (extraIntent){
            case "folderList":
                name = getIntent().getStringExtra("folderName");
                path = getIntent().getStringExtra("folderPath");
                number = getIntent().getStringExtra("folderNum");

                //_songList = Utility.getSongsInTheFolder(context,path);
                _songList = Utility.getSongsInTheFolder(name);

                contentName.setText(name);
                contentPath.setText(path);
                contentNumber.setText(String.format("%s Songs", number));
                break;
            case "albumSongList":
                name = getIntent().getStringExtra("albumName");
                path = getIntent().getStringExtra("artistName");
                number = getIntent().getStringExtra("albumNum");

                _songList = Utility.getSongsOfTheAlbum(name);

                contentName.setText(name);
                contentPath.setText(path);
                contentNumber.setText(String.format("%s Songs", number));
                contentImage.setImageDrawable(getResources().getDrawable(R.drawable.album_cd));
                break;
            case "lastPlayedList":
                name = getIntent().getStringExtra("name");
                number = getIntent().getStringExtra("noOfSongs");

                _songList = Utility.getLastPlayedSongs();

                contentName.setText(name);
                contentPath.setVisibility(View.GONE);
                contentNumber.setText(String.format("%s Songs", number));
                contentImage.setImageDrawable(getResources().getDrawable(R.drawable.last_played_main));
                break;
            case "upComingList":
                name = getIntent().getStringExtra("name");
                number = getIntent().getStringExtra("noOfSongs");

                if(!_nowPlayingList.isEmpty()) {  //changed..
                    _songList = Utility._nowPlayingList;
                } else {
                    _songList = Utility._songs;
                }
                contentName.setText(name);
                contentPath.setVisibility(View.GONE);
                contentNumber.setText(String.format("%s Songs", number));
                contentImage.setImageDrawable(getResources().getDrawable(R.drawable.up_coming_colored));

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        SongInfo song = SongInfo.getSongInfoObject();
                        int pos = AllSongsActivity.getRealSongPosition2(song);
                        recyclerView.scrollToPosition(pos);
                    }
                }, 200);

                break;
            case "favouriteList":
                name = getIntent().getStringExtra("name");
                number = getIntent().getStringExtra("noOfSongs");

                _songList = Utility.getFavouritesSongs();

                contentName.setText(name);
                contentPath.setVisibility(View.GONE);
                contentNumber.setText(String.format("%s Songs", number));
                contentImage.setImageDrawable(getResources().getDrawable(R.drawable.favourites_main_content));
                break;
            case "recentlyAddedList":
                name = getIntent().getStringExtra("name");
                number = getIntent().getStringExtra("noOfSongs");

                _songList = Utility.getRecentlyAddedSongs();

                contentName.setText(name);
                contentPath.setVisibility(View.GONE);
                contentNumber.setText(String.format("%s Songs", number));
                contentImage.setImageDrawable(getResources().getDrawable(R.drawable.recently_added_main));

                Snackbar.make(mRootView, " Sorry, Module currently under development....", Snackbar.LENGTH_LONG).show();
                break;


        }
    }

    private void initializeFields() {
        context = getApplicationContext();
        mRootView = findViewById(R.id.root);
        contentName = findViewById(R.id.contentName);
        contentPath = findViewById(R.id.contentPath);
        contentNumber = findViewById(R.id.contentNoOfSongs);
        contentImage = findViewById(R.id.contentImage);

        btnSearch = findViewById(R.id.search);
        btnMenu = findViewById(R.id.pOverflow);
        btnBack = findViewById(R.id.pBack);

        //MusicController
        mediaController = findViewById(R.id.mediaController);
        btn_playPause = findViewById(R.id.mCPlayPause);
        mcAlbumArt = findViewById(R.id.mcAlbumArt);
        songTitle = findViewById(R.id.mCSongName);
        songTitle.setSelected(true);
        artistName = findViewById(R.id.mCArtistName);
        seekBar = findViewById(R.id.mCSeekBar);
        imageLoader = new ImageLoader(this,1);

        //RecyclerView
        recyclerView = findViewById(R.id.recyclerView);

        //Animations
        play_in = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.play_in);
        pause_in = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.pause_in);
        rotate_clockwise = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.rotate_clockwise);
        rotate_anticlockwise = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.rotate_anticlockwise);

        changeStatusBarColor();
    }

    @Override
    public void onResume() {
        super.onResume();
        updateMediaController();
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.pBack:
                onBackPressed();
                break;
            case R.id.pOverflow:
                break;
            case R.id.mCPlayPause:
                mcPlayPause();
                break;
        }
    }

    private void mcPlayPause() {
        try{
            if(mediaPlayer != null) {
                if (mediaPlayer.isPlaying()) {
                    playedLength = mediaPlayer.getCurrentPosition();
                    mediaPlayer.pause();
                    btn_playPause.setImageResource(R.drawable.ic_mc_play);
                    btn_playPause.startAnimation(rotate_anticlockwise);
                    isPlaying = false;
                } else {
                    btn_playPause.setImageResource(R.drawable.ic_mc_pause);
                    btn_playPause.startAnimation(rotate_clockwise);
                    mediaPlayer.seekTo(playedLength);
                    mediaPlayer.start();
                    isPlaying = true;
                }
            } else{
                _nowPlayingList = new ArrayList<>();
                _nowPlayingList.addAll(_songList);
                Intent serviceIntent = new Intent(this, MusicService.class);
                serviceIntent.putExtra("songId", lastPlayedSongItem.getSongId());
                startService(serviceIntent);
                updateMediaController();
            }
            Utility.recallNotificationBuilder(this);
        }catch(Exception e){
            Log.i(TAG,e.getMessage());
        }
    }

    public void changeStatusBarColor(){
        Window window = this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(this.getResources().getColor(R.color.colorGrey));
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

}
