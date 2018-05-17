package yomusic.developer.it.yomusic.Fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import yomusic.developer.it.yomusic.Adapters.ArtistRecyclerAdapter;
import yomusic.developer.it.yomusic.DisplayAlbumAndTracksActivity;
import yomusic.developer.it.yomusic.ImageLoader;
import yomusic.developer.it.yomusic.Infos.ArtistInfo;
import yomusic.developer.it.yomusic.Infos.SongInfo;
import yomusic.developer.it.yomusic.MusicService;
import yomusic.developer.it.yomusic.OnSwipeTouchListener;
import yomusic.developer.it.yomusic.PlayerActivity;
import yomusic.developer.it.yomusic.R;
import yomusic.developer.it.yomusic.Utils.Utility;

import static yomusic.developer.it.yomusic.Fragments.SongListFragment.currSongPosition;
import static yomusic.developer.it.yomusic.MainActivity.lastPlayedSongItem;
import static yomusic.developer.it.yomusic.MusicService.isPlaying;
import static yomusic.developer.it.yomusic.MusicService.mediaPlayer;
import static yomusic.developer.it.yomusic.MusicService.playedLength;
import static yomusic.developer.it.yomusic.Utils.Utility._artists;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ArtistFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class ArtistFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "ArtistFragment";
    private OnFragmentInteractionListener mListener;

    public ArtistFragment() {
        // Required empty public constructor
    }

    public boolean checkIfUiNeedsToUpdate = false;
    Animation play_in, pause_in, rotate_clockwise, rotate_anticlockwise;

    RecyclerView recyclerView;
    ArtistRecyclerAdapter artistRecyclerAdapter;

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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_artist, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());

        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                layoutManager.getOrientation());


        artistRecyclerAdapter = new ArtistRecyclerAdapter(getActivity(),_artists);

        recyclerView.addItemDecoration(dividerItemDecoration);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(artistRecyclerAdapter);

        artistRecyclerAdapter.setOnItemClickListener(new ArtistRecyclerAdapter.OnItemClickListener() {
            @Override
            public void OnItemClick(View v, ArtistInfo obj, int position) {
                //String folderPath = obj.getFolderPath();
                //Utility._songs = Utility.getSongsInTheFolder(getContext(),folderPath);
                startNextActivity(obj);
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

        return view;
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
                if(!Utility._nowPlayingList.isEmpty()){
                    songInfo = Utility._nowPlayingList.get(currSongPosition);
                }else {
                    songInfo = Utility._songs.get(currSongPosition);
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
            }catch (Exception ignored){ }

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

    private void startNextActivity(ArtistInfo obj) {
        Intent intent = new Intent(getContext(), DisplayAlbumAndTracksActivity.class);
        intent.putExtra("list", "artistSongList");
        intent.putExtra("artistName", obj.getaName());
        intent.putExtra("noOfTracks", String.valueOf(obj.getaNoOfTracks()));
        intent.putExtra("albumNum", String.valueOf(obj.getaNoOfAlbums()));
        startActivity(intent);
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
        }
//        else {
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
