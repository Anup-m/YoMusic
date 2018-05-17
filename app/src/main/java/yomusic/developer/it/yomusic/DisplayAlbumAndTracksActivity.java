package yomusic.developer.it.yomusic;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
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

import yomusic.developer.it.yomusic.Adapters.AlbumRecyclerAdapter;
import yomusic.developer.it.yomusic.Adapters.SongRecyclerAdapter;
import yomusic.developer.it.yomusic.Infos.AlbumInfo;
import yomusic.developer.it.yomusic.Infos.SongInfo;
import yomusic.developer.it.yomusic.Utils.Utility;

import static yomusic.developer.it.yomusic.Fragments.SongListFragment.currSongPosition;
import static yomusic.developer.it.yomusic.MainActivity.lastPlayedSongItem;
import static yomusic.developer.it.yomusic.MusicService.isPlaying;
import static yomusic.developer.it.yomusic.MusicService.mediaPlayer;
import static yomusic.developer.it.yomusic.MusicService.playedLength;
import static yomusic.developer.it.yomusic.Utils.Utility._nowPlayingList;

public class DisplayAlbumAndTracksActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "DisplayAlbumsAndTracks";

    Context context;
    ImageView btnSearch, btnMenu, btnBack,contentImage;
    TextView contentName, contentPath,contentNumber;
    String name,path,number;

    RecyclerView recyclerViewAlbums,recyclerViewTracks;
    SongRecyclerAdapter songRecyclerAdapter;
    AlbumRecyclerAdapter albumRecyclerAdapter;

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
    ArrayList<AlbumInfo> _albumList = new ArrayList<>();

    //Broadcast Receiver
    private BroadcastReceiver broadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_album_and_tracks);
        initializeFields();
        String extraIntent  = getIntent().getStringExtra("list");
        setSongList(extraIntent);

        /*~~~~~~~~~~~~~~~~~~~~~~~~~~~ ALBUMS ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~*/
        recyclerViewAlbums = findViewById(R.id.recyclerViewAlbums);
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL);

        DividerItemDecoration dividerItemDecorationTracks = new DividerItemDecoration(recyclerViewAlbums.getContext(),
                layoutManager.getOrientation());

        albumRecyclerAdapter = new AlbumRecyclerAdapter(context,_albumList);

        recyclerViewAlbums.addItemDecoration(dividerItemDecorationTracks);
        recyclerViewAlbums.setLayoutManager(layoutManager);
        recyclerViewAlbums.setAdapter(albumRecyclerAdapter);

        albumRecyclerAdapter.setOnItemClickListener(new AlbumRecyclerAdapter.OnItemClickListener() {
            @Override
            public void OnItemClick(View v, AlbumInfo obj, int position) {
                startNextActivity(obj);
            }
        });

        /*~~~~~~~~~~~~~~~~~~~~~~~~~~ TRACKS ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~*/
        recyclerViewTracks = findViewById(R.id.recyclerViewTracks);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerViewTracks.getContext(),
                linearLayoutManager.getOrientation());

        songRecyclerAdapter = new SongRecyclerAdapter(_songList, this);
        recyclerViewTracks.addItemDecoration(dividerItemDecoration);
        recyclerViewTracks.setLayoutManager(linearLayoutManager);
        recyclerViewTracks.setAdapter(songRecyclerAdapter);

        songRecyclerAdapter.setOnItemClickListener(new SongRecyclerAdapter.OnItemClickListener() {
            @Override
            public void OnItemClick(View v, SongInfo obj, int position) {
                _nowPlayingList = new ArrayList<>();
                _nowPlayingList.addAll(_songList);
                currSongPosition = AllSongsActivity.getRealSongPosition2(obj);
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
        btn_playPause.setOnClickListener(this);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            boolean userTouch;
            @Override
            public void onProgressChanged(SeekBar seekBar, int length, boolean b) {
                if(mediaPlayer.isPlaying() && userTouch) {
                    mediaPlayer.seekTo(length);
                    mediaPlayer.start();
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
        mediaController.setOnTouchListener(new OnSwipeTouchListener(this){
            public void onSwipeTop() {
                PlayerActivity.checkIfPlayerUiNeedsToUpdate = true;
                Intent playerIntent = new Intent(getApplicationContext(),PlayerActivity.class);
                startActivity(playerIntent);
                overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
            }
        });

        updateMediaController();
    }

    private void  updateMediaController() {
        SongInfo songInfo;
        if(currSongPosition == -1){
            songInfo = Utility._songs.get(currSongPosition + 1);
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
            case "artistSongList":
                name = getIntent().getStringExtra("artistName");
                path = getIntent().getStringExtra("noOfTracks");
                number = getIntent().getStringExtra("albumNum");

                _songList = Utility.getSongsOfTheArtist(name);
                _albumList = Utility.getAlbumsOfArtists(name);
                contentName.setText(name);
                contentPath.setText(String.format("%s Albums", number));
                contentNumber.setText(String.format("%s Songs", path));
                contentImage.setImageDrawable(getResources().getDrawable(R.drawable.dj_dancing_big));
                break;
        }
    }

    private void startNextActivity(AlbumInfo obj) {
        Intent intent = new Intent(context, DisplayTracksActivity.class);
        intent.putExtra("list", "albumSongList");
        intent.putExtra("albumName", obj.getaName());
        intent.putExtra("artistName", obj.getArtistName());
        intent.putExtra("albumNum", String.valueOf(obj.getaNoOfTracks()));
        startActivity(intent);
    }


    private void initializeFields() {

        context = getApplicationContext();

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


        //Animations
        play_in = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.play_in);
        pause_in = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.pause_in);
        rotate_clockwise = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.rotate_clockwise);
        rotate_anticlockwise = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.rotate_anticlockwise);

        changeStatusBarColor();
    }

    public void onResume() {
        super.onResume();
        updateMediaController();
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


}
