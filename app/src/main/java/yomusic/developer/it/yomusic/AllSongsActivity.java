package yomusic.developer.it.yomusic;

import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import java.util.ArrayList;

import yomusic.developer.it.yomusic.Adapters.SongRecyclerAdapter;
import yomusic.developer.it.yomusic.Adapters.ViewPagerAdapter;
import yomusic.developer.it.yomusic.Fragments.AlbumFragment;
import yomusic.developer.it.yomusic.Fragments.ArtistFragment;
import yomusic.developer.it.yomusic.Fragments.FolderFragment;
import yomusic.developer.it.yomusic.Fragments.SongListFragment;
import yomusic.developer.it.yomusic.Infos.SongInfo;
import yomusic.developer.it.yomusic.Utils.Utility;

public class AllSongsActivity extends AppCompatActivity implements
        PrepareRetrieverTask.MusicRetrieverPreparedListener ,SearchView.OnQueryTextListener  {

    Toolbar toolbar;
    TabLayout tabLayout;
    ViewPager viewPager;
    ViewPagerAdapter viewPagerAdapter;

    Retriever mRetriever;
    SongRecyclerAdapter songRecyclerAdapter;
    RecyclerView recyclerView;
    int number;

    // indicates the state our service:
    public enum State {
        Retrieving, // the MediaRetriever is retrieving music
        Stopped,    // media player is stopped and not prepared to play
        Preparing,  // media player is preparing...
        Playing,    // playback active (media player ready!). (but the media player may actually be
                    // paused in this state if we don't have audio focus. But we stay in this state
                    // so that we know we have to resume playback once we get focus back)
        Paused      // playback paused (media player ready!)
    };

    static State mState = State.Retrieving;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_songs);
        initializeFields();

        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        viewPagerAdapter.addFragments(new SongListFragment(),"All");
        viewPagerAdapter.addFragments(new AlbumFragment(),"Album");
        viewPagerAdapter.addFragments(new ArtistFragment(),"Artist");
        viewPagerAdapter.addFragments(new FolderFragment(),"Folder");
        viewPager.setAdapter(viewPagerAdapter);
        tabLayout.setupWithViewPager(viewPager);

        final TabLayout.Tab currentTab = tabLayout.getTabAt(number);
        if(currentTab != null){
            tabLayout.post(new Runnable() {
                @Override
                public void run() {
                    currentTab.select();
                }
            });
        }

    }

    @Override
    public void onMusicRetrieverPrepared() {
        // Done retrieving!
        mState = State.Stopped;
    }

    private void initializeFields() {
        toolbar = findViewById(R.id.toolbar);
        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        changeStatusBarColor();

        number =  getIntent().getIntExtra("No",0);
//        recyclerView = findViewById(R.id.recyclerView);
//        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
//        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
//                linearLayoutManager.getOrientation());
//
//        songRecyclerAdapter = new SongRecyclerAdapter(Utility._songs, this);
//        recyclerView.addItemDecoration(dividerItemDecoration);
//        recyclerView.setLayoutManager(linearLayoutManager);
//        recyclerView.setAdapter(songRecyclerAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.all_songs_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        switch (id){
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.action_search:
                SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);
                if(searchView != null)
                    searchView.setOnQueryTextListener(this);
                break;
            case R.id.action_settings:
                startActivity(new Intent(this,SettingsActivity.class));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        newText = newText.toLowerCase();
        ArrayList<SongInfo> searchList = new ArrayList<>();
        for(SongInfo song : Utility._songs){
            String name = song.getSongName().toLowerCase();
            if(name.contains(newText))
                searchList.add(song);
        }
        songRecyclerAdapter = new SongRecyclerAdapter(searchList,this);
        //recyclerView.setAdapter(songRecyclerAdapter);
        //songRecyclerAdapter.setFilter(searchList);

        songRecyclerAdapter.setOnItemClickListener(new SongRecyclerAdapter.OnItemClickListener() {
            @Override
            public void OnItemClick(View v, SongInfo obj, int position) {
                //mediaPlayer.reset();
                position = getRealSongPosition2(obj);
                //playSong(obj,position,context);
            }
        });
        return true;
    }

    public static int getRealSongPosition(SongInfo obj){
        int position = 0;
        for(SongInfo song : Utility._songs){
            if(obj.equals(song)){
                position = Utility._songs.indexOf(obj);
            }
        }
        return position;
    }

    public static int getRealSongPosition2(SongInfo obj){
        int position = 0;
        for(SongInfo song : Utility._nowPlayingList){
            if(obj.equals(song)){
                position = Utility._nowPlayingList.indexOf(obj);
            }
        }
        return position;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    public void changeStatusBarColor(){
        Window window = this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(this.getResources().getColor(R.color.colorGrey));
    }
}