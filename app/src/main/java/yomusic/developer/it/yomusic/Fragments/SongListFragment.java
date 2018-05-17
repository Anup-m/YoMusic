package yomusic.developer.it.yomusic.Fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.ArrayList;

import yomusic.developer.it.yomusic.Adapters.SongRecyclerAdapter;
import yomusic.developer.it.yomusic.AllSongsActivity;
import yomusic.developer.it.yomusic.ImageLoader;
import yomusic.developer.it.yomusic.MusicService;
import yomusic.developer.it.yomusic.OnSwipeTouchListener;
import yomusic.developer.it.yomusic.PlayerActivity;
import yomusic.developer.it.yomusic.R;
import yomusic.developer.it.yomusic.Infos.SongInfo;
import yomusic.developer.it.yomusic.Utils.Utility;

import static yomusic.developer.it.yomusic.MainActivity.lastPlayedSongItem;
import static yomusic.developer.it.yomusic.MusicService.isPlaying;
import static yomusic.developer.it.yomusic.MusicService.mediaPlayer;
import static yomusic.developer.it.yomusic.MusicService.playedLength;
import static yomusic.developer.it.yomusic.Utils.Utility._nowPlayingList;
import static yomusic.developer.it.yomusic.Utils.Utility._songs;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SongListFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class SongListFragment extends Fragment implements SearchView.OnQueryTextListener, View.OnClickListener {

    private static final String TAG = "SongListFragment";
    private OnFragmentInteractionListener mListener;

    /*-------Variables--------*/
    public static boolean checkIfUiNeedsToUpdate = false;
    public static int currSongPosition = -1;
    Animation play_in, pause_in, rotate_clockwise, rotate_anticlockwise;


    /*-------Views--------*/
    //RecyclerView
    RecyclerView recyclerView;
    SongRecyclerAdapter songRecyclerAdapter;

    //MusicController
    ImageButton btn_playPause;
    SeekBar seekBar;
    ImageView mcAlbumArt;
    TextView songTitle,artistName;
    View mediaController;
    ImageLoader imageLoader;
    private Handler myHandler = new Handler();

    //Broadcast Receiver
    private BroadcastReceiver broadcastReceiver;

    public SongListFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_song_list, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                                                                                     linearLayoutManager.getOrientation());

        songRecyclerAdapter = new SongRecyclerAdapter(_songs, getActivity());
        recyclerView.addItemDecoration(dividerItemDecoration);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(songRecyclerAdapter);

        songRecyclerAdapter.setOnItemClickListener(new SongRecyclerAdapter.OnItemClickListener() {
            @Override
            public void OnItemClick(View v, SongInfo obj, int position) {
                _nowPlayingList = new ArrayList<>();
                _nowPlayingList.addAll(_songs);
                currSongPosition = AllSongsActivity.getRealSongPosition2(obj); //changed..
                //currSongPosition = position;
                Intent serviceIntent = new Intent(getActivity(), MusicService.class);
                serviceIntent.putExtra("songId",obj.getSongId());
                getActivity().startService(serviceIntent);

                //updateMediaController();  //TODO chnged...
            }
        });


        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                updateMediaController();
            }
        };
        getActivity().registerReceiver(broadcastReceiver,new IntentFilter(MusicService.PLAYING_BROADCAST));

        btn_playPause = view.findViewById(R.id.mCPlayPause);
        mcAlbumArt = view.findViewById(R.id.mcAlbumArt);
        songTitle = view.findViewById(R.id.mCSongName);
        artistName = view.findViewById(R.id.mCArtistName);
        seekBar = view.findViewById(R.id.mCSeekBar);
        imageLoader = new ImageLoader(getContext(),1);
        mediaController = view.findViewById(R.id.mediaController);


        //Animations
        rotate_anticlockwise = AnimationUtils.loadAnimation(getActivity(),R.anim.rotate_anticlockwise);
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
        mediaController.setOnTouchListener(new OnSwipeTouchListener(getContext()){
            public void onSwipeTop() {
                PlayerActivity.checkIfPlayerUiNeedsToUpdate = true;
                Intent playerIntent = new Intent(getContext(),PlayerActivity.class);
                startActivity(playerIntent);
                getActivity().overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
            }
        });

        updateMediaController();

        // Inflate the layout for this fragment
        return view;
    }

    public void  updateMediaController() {
        SongInfo songInfo;
        if(currSongPosition == -1){
            songInfo = _songs.get(currSongPosition + 1);  //changed...
            songTitle.setText(songInfo.getSongName());
            artistName.setText(songInfo.getArtistName());
            imageLoader.displayImage(songInfo.getContentUris().toString(),mcAlbumArt);
            //random_list.add(0);
        }
        else {
            try {
                if (MusicService.isPlaying /*mediaPlayer.isPlaying() ||  PlayerActivity.isPlaying */) {
                    btn_playPause.setImageDrawable(getResources().getDrawable(R.drawable.ic_mc_pause));
                    btn_playPause.startAnimation(rotate_anticlockwise);
                } else {
                    btn_playPause.setImageDrawable(getResources().getDrawable(R.drawable.ic_mc_play));
                    btn_playPause.startAnimation(rotate_anticlockwise);
                }

                if(!Utility._nowPlayingList.isEmpty()){
                    songInfo = Utility._nowPlayingList.get(currSongPosition);
                }else {
                    songInfo = Utility._songs.get(currSongPosition);
                }
                songTitle.setText(songInfo.getSongName());
                artistName.setText(songInfo.getArtistName());
                imageLoader.displayImage(songInfo.getContentUris().toString(),mcAlbumArt);
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
        //checkIfUiNeedsToUpdate = false;
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

    @Override
    public void onClick(View v) {
        switch (v.getId()){
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
                _nowPlayingList.addAll(_songs);
                Intent serviceIntent = new Intent(getActivity(), MusicService.class);
                serviceIntent.putExtra("songId", lastPlayedSongItem.getSongId());
                getActivity().startService(serviceIntent);
                updateMediaController();
            }
            Utility.recallNotificationBuilder(getActivity());
        }catch(Exception e){
            Log.i(TAG,e.getMessage());
        }
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.all_songs_menu,menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        switch (id){
            case R.id.action_search:
                SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);
                if(searchView != null)
                    searchView.setOnQueryTextListener(this);
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
        for(SongInfo song : _songs){
            String name = song.getSongName().toLowerCase();
            if(name.contains(newText))
                searchList.add(song);
        }
        songRecyclerAdapter = new SongRecyclerAdapter(searchList,getActivity());
        recyclerView.setAdapter(songRecyclerAdapter);
       // songRecyclerAdapter.setFilter(searchList);

        songRecyclerAdapter.setOnItemClickListener(new SongRecyclerAdapter.OnItemClickListener() {
            @Override
            public void OnItemClick(View v, SongInfo obj, int position) {
                //mediaPlayer.reset();
                position = getRealSongPosition(obj);
                //playSong(obj,position,context);
            }
        });
        return true;
    }

    public static int getRealSongPosition(SongInfo obj){
        int position = 0;
        for(SongInfo song : _songs){
            if(obj.equals(song)){
                position = _songs.indexOf(obj);
            }
        }
        return position;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateMediaController();
    }





    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } //else {
//            throw new RuntimeException(context.toString()
//                    + " must implement OnFragmentInteractionListener");
//        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

}


/*

private Runnable UpdateSongTime = new Runnable() {
        public void run() {
            try {
                if (playedLength != -1) {
                    int currTime = mediaPlayer.getCurrentPosition();
                    seekBar.setProgress(currTime);
                } else {
                    seekBar.setProgress(playedLength);
                    playedLength = -1;
                }

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