
package yomusic.developer.it.yomusic;

import android.app.ActivityOptions;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import yomusic.developer.it.yomusic.Infos.SongInfo;
import yomusic.developer.it.yomusic.Utils.PreferencesUtils;
import yomusic.developer.it.yomusic.Utils.Utility;

import static yomusic.developer.it.yomusic.Fragments.SongListFragment.currSongPosition;
import static yomusic.developer.it.yomusic.MusicService.isPlaying;
import static yomusic.developer.it.yomusic.MusicService.mediaPlayer;
import static yomusic.developer.it.yomusic.MusicService.playedLength;
import static yomusic.developer.it.yomusic.Utils.Utility._artists;
import static yomusic.developer.it.yomusic.Utils.Utility._lastPlayed;
import static yomusic.developer.it.yomusic.Utils.Utility._nowPlayingList;
import static yomusic.developer.it.yomusic.Utils.Utility._songs;
import static yomusic.developer.it.yomusic.Utils.Utility.auth;

public class MainActivity extends AppCompatActivity implements View.OnClickListener,
        SearchView.OnQueryTextListener,NavigationView.OnNavigationItemSelectedListener,
        PrepareRetrieverTask.MusicRetrieverPreparedListener{

    private static Context context;
    private static PreferencesUtils preferencesUtils;


    Toolbar toolbar;
    DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mToggle;
    NavigationView navigationView;

    //Login Stuff
    Button btnLogin;
    TextView txUserName;
    ImageView avatar;
    private static final int RC_SIGN_IN = 101;
    //for profile image...
    private String imgUrl;


    //MusicController
    ImageLoader imageLoader;
    ImageButton mcBtn_up,mcBtn_playPause,btnPlayAll;
    ImageView mcAlbumArt;
    SeekBar mcSeekBar;
    TextView mcSongName,mcArtistName,totalNoOfSongs;
    View mediaController,allSongs,favourites,lastPlayed,recentlyAdded;
    public static boolean checkIfUiNeedsToUpdate = false;
    private Handler myHandler = new Handler();
    //Animations
    Animation play_in, pause_in, rotate_clockwise, rotate_anticlockwise;

    //Static variables
    private final static String TAG = "MainActivity";
    public static SongInfo lastPlayedSongItem;
    private View mRootView;

    //Broadcast Receiver
    private BroadcastReceiver broadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initializeComponents();
        updateProfile();
        totalNoOfSongs.setText(String.format("%s songs", String.valueOf(Utility._songs.size())));

        allSongs.setOnClickListener(this);
        btnPlayAll.setOnClickListener(this);
        lastPlayed.setOnClickListener(this);
        mcBtn_playPause.setOnClickListener(this);
        favourites.setOnClickListener(this);
        recentlyAdded.setOnClickListener(this);
        btnLogin.setOnClickListener(this);

        getLastPlayedSongDetails(context);
        updateMediaController();

        mcSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            boolean userTouch;
            @Override
            public void onProgressChanged(SeekBar seekBar, int length, boolean b) {
                if(mediaPlayer != null) {
                    if (mediaPlayer.isPlaying() && userTouch) {
                        mediaPlayer.seekTo(length);
                        mediaPlayer.start();
                    }
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
//                PlayerActivity.checkIfPlayerUiNeedsToUpdate = true;
//                Intent playerIntent = new Intent(getApplicationContext(),PlayerActivity.class);
//                startActivity(playerIntent);
//                overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
                startPlayerActivity();
            }
        });

        mediaController.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startPlayerActivity();
            }
        });

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                updateMediaController();
            }
        };
        registerReceiver(broadcastReceiver,new IntentFilter(MusicService.PLAYING_BROADCAST));

    }

    private void startPlayerActivity(){
        PlayerActivity.checkIfPlayerUiNeedsToUpdate = true;
        Pair pair = new Pair<View,String>(mcAlbumArt,"album_art_shared");
        ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(this,pair);

        Intent playerIntent = new Intent(getApplicationContext(),PlayerActivity.class);
        startActivity(playerIntent,options.toBundle());
    }

    private void  updateMediaController() {
        SongInfo songInfo;
        if(currSongPosition == -1){
            try {
                Thread.sleep(400);
                songInfo = Utility._songs.get(currSongPosition + 1);  //changed...
                mcSongName.setText(songInfo.getSongName());
                mcArtistName.setText(songInfo.getArtistName());
                imageLoader.displayImage(songInfo.getContentUris().toString(), mcAlbumArt);
                //random_list.add(0);
            }catch (Exception ignored){ }
        }
        else {
            try {
                if(!Utility._nowPlayingList.isEmpty()){
                    songInfo = Utility._nowPlayingList.get(currSongPosition);
                }else {
                    songInfo = Utility._songs.get(currSongPosition);
                }
                mcSongName.setText(songInfo.getSongName());
                mcArtistName.setText(songInfo.getArtistName());
                imageLoader.displayImage(songInfo.getContentUris().toString(),mcAlbumArt);

                if (isPlaying){
                    /*mediaPlayer.isPlaying() ||  PlayerActivity.isPlaying */
                    mcBtn_playPause.setImageDrawable(getResources().getDrawable(R.drawable.ic_mc_pause));
                    mcBtn_playPause.startAnimation(rotate_clockwise);
                } else {
                    mcBtn_playPause.setImageDrawable(getResources().getDrawable(R.drawable.ic_mc_play));
                    mcBtn_playPause.startAnimation(rotate_anticlockwise);
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
            mcSeekBar.setMax(mediaPlayer.getDuration());

            if(playedLength != -1){
                mcSeekBar.setProgress(playedLength);
            } else {
                mcSeekBar.setProgress(mediaPlayer.getCurrentPosition());
            }
            myHandler.postDelayed(UpdateSongTime, 100);
        } catch (Exception e){ }
    }

    private Runnable UpdateSongTime = new Runnable() {
        public void run() {
            try {
                mcSeekBar.setProgress(mediaPlayer.getCurrentPosition());
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

    private static void getLastPlayedSongDetails(Context context) {
        //sharedPreference data for last played song...
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String lastPlayedSongName = sharedPreferences.getString(context.getString(R.string.last_played_song),null);
        try {
            lastPlayedSongItem = SongInfo.getSongInfoObject(lastPlayedSongName);
            currSongPosition = Utility._songs.indexOf(lastPlayedSongItem);
        }catch (Exception e){
            //First Time opening the app....
        }
    }

    private void initializeComponents() {
        context = getApplicationContext();
        preferencesUtils = new PreferencesUtils(context);
        mRootView = findViewById(R.id.root);

        mediaController = findViewById(R.id.mediaController);
        mcBtn_up = findViewById(R.id.mCUp);
        mcBtn_playPause = findViewById(R.id.mCPlayPause);
        mcSeekBar = findViewById(R.id.mCSeekBar);
        mcSongName = findViewById(R.id.mCSongName);
        mcArtistName = findViewById(R.id.mCArtistName);
        mcAlbumArt = findViewById(R.id.mcAlbumArt);
        mcSongName.setSelected(true);

        allSongs = findViewById(R.id.all_songs);
        btnPlayAll = findViewById(R.id.play_all_songs);
        totalNoOfSongs = findViewById(R.id.no_of_songs);
        favourites = findViewById(R.id.favourites);
        lastPlayed = findViewById(R.id.last_played);
        recentlyAdded = findViewById(R.id.recently_added);

        imageLoader = new ImageLoader(getApplicationContext(),1);

        btnLogin = findViewById(R.id.btnLogin);
        avatar = findViewById(R.id.avatar);
        txUserName = findViewById(R.id.username);

        //Animations
        play_in = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.play_in);
        pause_in = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.pause_in);
        rotate_clockwise = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.rotate_clockwise);
        rotate_anticlockwise = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.rotate_anticlockwise);

        //Toolbar
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        changeStatusBarColor();

        //NavigationDrawer
        mDrawerLayout = findViewById(R.id.drawer_layout);
        mToggle = new ActionBarDrawerToggle(this,mDrawerLayout,R.string.navigation_drawer_open,R.string.navigation_drawer_close);
        mDrawerLayout.addDrawerListener(mToggle);
        mToggle.syncState();
        try {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }catch (Exception ignored){  }
        navigationView = findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(mToggle.onOptionsItemSelected(item)){
            return true;
        }
        switch (item.getItemId()) {
            case R.id.action_search:
                SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);
                if(searchView != null)
                    searchView.setOnQueryTextListener(this);
                break;
            case R.id.action_shuffle_all:
                break;
            case R.id.action_new_playlist:
                break;
            case R.id.action_settings:
                startActivity(new Intent(this,SettingsActivity.class));
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return false;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.id_albums) {
            Intent intent = new Intent(this,AllSongsActivity.class);
            intent.putExtra("No",1);
            startActivity(intent);

        } else if (id == R.id.id_artists) {
            Intent intent = new Intent(this,AllSongsActivity.class);
            intent.putExtra("No",2);
            startActivity(intent);

        }else if(id == R.id.id_folders){
            Intent intent = new Intent(this,AllSongsActivity.class);
            intent.putExtra("No",3);
            startActivity(intent);
        }
        else if (id == R.id.id_songs) {
            Intent intent = new Intent(this,AllSongsActivity.class);
            intent.putExtra("No",0);
            startActivity(intent);

        }else if(id == R.id.id_playing_queue){
            startDisplayTracksActivity("upComingList");
        }else if(id == R.id.id_playlist) {
            startActivity(new Intent(this, PlayListActivity.class));
        }else if (id == R.id.id_settings) {
            startActivity(new Intent(this,SettingsActivity.class));
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return false;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id){
            case R.id.all_songs:
                Intent intent = new Intent(this,AllSongsActivity.class);
                intent.putExtra("No",0);
                startActivity(intent);
                break;
            case R.id.mediaController:
                break;
            case R.id.play_all_songs:
                playAll();
                break;
            case R.id.favourites:
                startDisplayTracksActivity("favouriteList");
                break;
            case R.id.last_played:
                startDisplayTracksActivity("lastPlayedList");
                break;
            case R.id.recently_added:
                startDisplayTracksActivity("recentlyAddedList");
                break;
            case R.id.mCPlayPause:
                mcPlayPause();
                break;
            case R.id.btnLogin:
                login();
                break;
        }
    }

    private void mcPlayPause() {
        try{
            if(mediaPlayer != null) {
                if (mediaPlayer.isPlaying()) {
                    playedLength = mediaPlayer.getCurrentPosition();
                    mediaPlayer.pause();
                    mcBtn_playPause.setImageResource(R.drawable.ic_mc_play);
                    mcBtn_playPause.startAnimation(rotate_anticlockwise);
                    isPlaying = false;
//                    if(Objects.equals(btnPlayAll.getDrawable(), getDrawable(R.drawable.pause_all))){
//                        btnPlayAll.setImageResource(R.drawable.play_all);  //todo play_all button fix...
//                    }
                } else {
                    mcBtn_playPause.setImageResource(R.drawable.ic_mc_pause);
                    mcBtn_playPause.startAnimation(rotate_clockwise);
                    mediaPlayer.seekTo(playedLength);
                    mediaPlayer.start();
                    isPlaying = true;

//                    if(Objects.equals(btnPlayAll.getDrawable(), getDrawable(R.drawable.play_all))){
//                        btnPlayAll.setImageResource(R.drawable.pause_all);
//                    }
                }
            } else{
                _nowPlayingList = new ArrayList<>();
                _nowPlayingList.addAll(_songs);   /// changed ... TODO jana na ki....
                Intent serviceIntent = new Intent(this, MusicService.class);
                serviceIntent.putExtra("songId",lastPlayedSongItem.getSongId());
                startService(serviceIntent);
                updateMediaController();
            }
            Utility.recallNotificationBuilder(context);
        }catch(Exception e){
            Log.i(TAG,e.getMessage());
        }
    }

    private void playAll(){
        if(currSongPosition == -1)
            currSongPosition = currSongPosition + 1;

        if(mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                playedLength = mediaPlayer.getCurrentPosition();
                mediaPlayer.pause();
                btnPlayAll.setImageResource(R.drawable.play_all);
                isPlaying = false;
            } else {
                btnPlayAll.setImageResource(R.drawable.pause_all);
                mediaPlayer.seekTo(playedLength);
                mediaPlayer.start();
                isPlaying = true;
            }
        }else {
            SongInfo obj;
            if(!Utility._nowPlayingList.isEmpty()){
                obj = Utility._nowPlayingList.get(currSongPosition);
            }else {
                obj = Utility._songs.get(currSongPosition);
            }
            _nowPlayingList = new ArrayList<>();
            _nowPlayingList.addAll(_songs);

            Intent serviceIntent = new Intent(getApplicationContext(), MusicService.class);
            serviceIntent.putExtra("songId", obj.getSongId());
            startService(serviceIntent);

            btnPlayAll.setImageDrawable(getResources().getDrawable(R.drawable.pause_all));
        }
        updateMediaController();
        Utility.recallNotificationBuilder(context);
    }

    private void startDisplayTracksActivity(String listName) {
        String name="", number="";
        switch (listName){
            case "lastPlayedList":
                name = "Last Played Songs";
                number = String.valueOf(Utility.getLastPlayedListSize());
                break;
            case "favouriteList":
                name = "Favourites Songs";
                number = String.valueOf(Utility.getFavouriteListSize());
                break;
            case "recentlyAddedList":
                name = "Recently Added Songs";
                number = String.valueOf(Utility.getRecentlyAddedListSize());
                break;
            case "upComingList":
                name = "Now Playing";
                if(!_nowPlayingList.isEmpty()) {  //changed...
                    number = String.valueOf(Utility._nowPlayingList.size());
                }else {
                    number = String.valueOf(Utility._songs.size());
                }
                break;
        }
        Intent intent = new Intent(context, DisplayTracksActivity.class);
        intent.putExtra("list", listName);
        intent.putExtra("name", name);
        intent.putExtra("noOfSongs",number);
        startActivity(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        if(SettingsActivity.hasDataChanged){
            //load();
            SettingsActivity.hasDataChanged = false;
        }
        updateMediaController();
        //updateProfile();
    }

    private void load(){
        Retriever mRetriever;
        if(!Utility._songs.isEmpty()) {
            Utility._songs = new ArrayList<>();
            mRetriever = new Retriever(getApplicationContext(),getContentResolver());
            (new PrepareRetrieverTask(mRetriever, this,"prepareMusicRetriever")).execute();
        }

        if(!Utility._albums.isEmpty()) {
            Utility._albums = new ArrayList<>();
            mRetriever = new Retriever(getApplicationContext(),getContentResolver());
            (new PrepareRetrieverTask(mRetriever, this,"prepareAlbumsRetriever")).execute();
        }

        if(!Utility._artists.isEmpty()) {
            Utility._artists = new ArrayList<>();
            mRetriever = new Retriever(getApplicationContext(),getContentResolver());
            (new PrepareRetrieverTask(mRetriever, this,"prepareArtistsRetriever")).execute();
        }

        if(!Utility.folderList.isEmpty()) {
            Utility.folderList = new ArrayList<>();
            mRetriever = new Retriever(getApplicationContext(),getContentResolver());
            (new PrepareRetrieverTask(mRetriever, this,"prepareFolderRetriever")).execute();
        }

        if(!Utility._lastPlayed.isEmpty()){
            Utility._lastPlayed = new ArrayList<>();
            mRetriever = new Retriever(getApplicationContext(),getContentResolver());
            (new PrepareRetrieverTask(mRetriever, this,"prepareLastPlayedRetriever")).execute();
        }

        if(!Utility._favourites.isEmpty()){
            Utility._favourites = new ArrayList<>();
            mRetriever = new Retriever(getApplicationContext(),getContentResolver());
            (new PrepareRetrieverTask(mRetriever, this,"prepareFavouritesRetriever")).execute();
        }

        if(!Utility._recentlyAdded.isEmpty()){
            Utility._recentlyAdded = new ArrayList<>();
            mRetriever = new Retriever(getApplicationContext(),getContentResolver());
            (new PrepareRetrieverTask(mRetriever, this,"prepareRecentlyAddedRetriever")).execute();
        }
    }

    private void login() {
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(getSelectedProviders())
                        .setTheme(R.style.MyTheme)
                        .setLogo(R.drawable.last_played_main)
                        .setIsSmartLockEnabled(true)
                        .build(), RC_SIGN_IN);
    }

    private List<AuthUI.IdpConfig> getSelectedProviders() {
        List<AuthUI.IdpConfig> selectedProviders = new ArrayList<>();

        selectedProviders.add(new AuthUI.IdpConfig.GoogleBuilder().build());

        selectedProviders.add(new AuthUI.IdpConfig.EmailBuilder().build());

        selectedProviders.add(new AuthUI.IdpConfig.PhoneBuilder().build());

        return selectedProviders;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == RC_SIGN_IN) {
            handleSignInResponse(resultCode, data);
            return;
        }
        showSnackbar(R.string.unknown_response);
    }

    @MainThread
    private void handleSignInResponse(int resultCode, Intent data) {
        IdpResponse response = IdpResponse.fromResultIntent(data);

        // Successfully signed in
        if (resultCode == RESULT_OK) {
            getUserDetails();
            return;
        } else {
            // Sign in failed
            if (response == null) {
                // User pressed back button
                showSnackbar(R.string.sign_in_cancelled);
                return;
            }

            if (response.getErrorCode() == ErrorCodes.NO_NETWORK) {
                showSnackbar(R.string.no_internet_connection);
                return;
            }

            if (response.getErrorCode() == ErrorCodes.UNKNOWN_ERROR) {
                showSnackbar(R.string.unknown_error);
                return;
            }
        }

        showSnackbar(R.string.unknown_sign_in_response);
    }

    private void getUserDetails() {
        try {
            txUserName.setText(auth.getCurrentUser().getDisplayName());
            imgUrl = auth.getCurrentUser().getPhotoUrl().toString();
            putProfileImage();
        } catch (NullPointerException ignored) {  }
    }

    private void updateProfile(){
        auth = FirebaseAuth.getInstance();
        if(auth.getCurrentUser() == null){              /*!preferencesUtils.getSignInPreference(context)*/
            txUserName.setVisibility(View.GONE);
            avatar.setVisibility(View.GONE);
            btnLogin.setVisibility(View.VISIBLE);
        }else {
            btnLogin.setVisibility(View.GONE);
            txUserName.setVisibility(View.VISIBLE);
            avatar.setVisibility(View.VISIBLE);

            if(!Objects.equals(getIntent().getExtras().getString("NAME"), null)) {
                txUserName.setText(getIntent().getExtras().getString("NAME"));
                imgUrl = getIntent().getExtras().getString("IMG_URL");
                putProfileImage();
            }
        }
    }

    private void putProfileImage() {
        Glide.with(this).asBitmap().load(imgUrl).into(new BitmapImageViewTarget(avatar){
            @Override
            protected void setResource(Bitmap resource){

                RoundedBitmapDrawable circularBitmapDrawable =
                        RoundedBitmapDrawableFactory.create(getApplicationContext().getResources(),resource);

                circularBitmapDrawable.setCircular(true);
                avatar.setImageDrawable(circularBitmapDrawable);
            }
        });
    }

    @MainThread
    private void showSnackbar(@StringRes int errorMessageRes) {
        Snackbar.make(mRootView, errorMessageRes, Snackbar.LENGTH_LONG).show();
    }

    public void changeStatusBarColor(){
        Window window = this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(this.getResources().getColor(R.color.colorGrey));
    }

    @Override
    public void onMusicRetrieverPrepared() {

    }

    private int countTotalSongs() {
        ContentResolver cr = context.getContentResolver();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0";
        Cursor cur = cr.query(uri, null, selection, null, null);
        int count = 0;
        if (cur != null) {
            count = cur.getCount();
            cur.close();
            return count;
        }
        return count;
    }

}


/*

private Runnable UpdateSongTime = new Runnable() {
        public void run() {
            try {
                if (playedLength != -1) {
                    int currTime = mediaPlayer.getCurrentPosition();
                    mcSeekBar.setProgress(currTime);
                } else {
                    mcSeekBar.setProgress(playedLength);
                    playedLength = -1;
                }

                mcSeekBar.setProgress(mediaPlayer.getCurrentPosition());

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

 */