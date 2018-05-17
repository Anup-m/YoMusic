package yomusic.developer.it.yomusic.Adapters;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.CardView;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.ArrayList;

import yomusic.developer.it.yomusic.AllSongsActivity;
import yomusic.developer.it.yomusic.Fragments.SongListFragment;
import yomusic.developer.it.yomusic.ImageLoader;
import yomusic.developer.it.yomusic.R;
import yomusic.developer.it.yomusic.Infos.SongInfo;
//import yomusic.developer.it.yomusic.SongsListActivity;

/**
 * Created by anupam on 10-12-2017.
 */

public class SongRecyclerAdapter extends RecyclerView.Adapter<SongRecyclerAdapter.SongHolder> {

    ArrayList<SongInfo> _songs;
    Context context;
    //SongsListActivity songsListActivity;
    SongListFragment songListFragment;
    AllSongsActivity allSongsActivity;
    ImageLoader imageLoader;
    static int song_position, real_song_position;

    public SongRecyclerAdapter(ArrayList<SongInfo> _songs, Context context) {
        this._songs = _songs;
        this.context = context;
        //this.songsListActivity = (SongsListActivity) context;
        imageLoader = new ImageLoader(context,1);
    }

    OnItemClickListener onItemClickListner;

    public interface OnItemClickListener{
        void OnItemClick(View v, SongInfo obj, int position);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener){
        this.onItemClickListner = onItemClickListener;
    }


    @Override
    public SongHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View myView = LayoutInflater.from(context).inflate(R.layout.song_row_item,parent,false);
        return new SongHolder(myView,context,_songs,songListFragment);
    }

    @Override
    public void onBindViewHolder(final SongHolder holder, final int position) {
        final SongInfo songInfo = _songs.get(position);
        holder.songName.setText(songInfo.getSongName());
        holder.artistName.setText(songInfo.getArtistName());

        imageLoader.displayImage(songInfo.getContentUris().toString(),holder.albumArt);

        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //song_position = holder.getAdapterPosition();
                real_song_position = AllSongsActivity.getRealSongPosition2(songInfo);
                if(onItemClickListner != null){
                    onItemClickListner.OnItemClick(v,songInfo,holder.getAdapterPosition());
                }
            }
        });

        holder.overFlow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                song_position = holder.getAdapterPosition();
                showPopupMenu(holder.overFlow);
            }
        });
    }

    private void highlightView(SongHolder holder){
        SongInfo obj = _songs.get(real_song_position);
        if(holder.songName.getText() == obj.getSongName()) {
            holder.songName.setTextColor(context.getResources().getColor(R.color.colorLikeYellow));
            holder.artistName.setTextColor(context.getResources().getColor(R.color.colorLikeYellow));
        }
        else {
            holder.songName.setTextColor(Color.GRAY);
            holder.artistName.setTextColor(Color.GRAY);
        }
    }


    private void showPopupMenu(View view) {
        PopupMenu popup = new PopupMenu(context, view);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.menu_song, popup.getMenu());
        popup.setOnMenuItemClickListener(new MyMenuItemClickListener());
        popup.show();
    }

    /**
     * Click listener for popup menu items
     */
    class MyMenuItemClickListener implements PopupMenu.OnMenuItemClickListener {

        public MyMenuItemClickListener() { }

        @Override
        public boolean onMenuItemClick(MenuItem menuItem) {
          /*
            switch (menuItem.getItemId()) {
                case R.id.action_add_favourite:
                    // TO add songs in shared preferences..
                    try {
                        SongInfo songObj = _songs.get(song_position);
                        Utility.addToFavouriteList(context,songObj);
                        Toast.makeText(context, "Added to favourite", Toast.LENGTH_SHORT).show();
                    }catch (Exception e){
                        Log.d("FAVOURITE ERROR :",e.getMessage());
                    }
                    return true;
                case R.id.action_play_next:
                    try {
                        SongsListActivity.playNext();
                        SongInfo songInfo = SongsListActivity._songs.get(SongsListActivity.currSongPosition);
                        SongsListActivity.playSong(songInfo,SongsListActivity.currSongPosition,context);
                        SongsListActivity.checkIfUiNeedsToUpdate = true;
                        PlayerActivity.checkIfPlayerUiNeedsToUpdate = true;
                    }catch (Exception e){
                        Log.d("NEXT_SONG ERROR :",e.getMessage());
                    }
                    return true;
                case R.id.action_share:
                    try {
                        Utility.shareSong(context);
                    }catch (Exception e){
                        Log.d("SHARE_DETAILS ERROR :",e.getMessage());
                    }
                    return true;
                case R.id.action_details:
                    try {
                        displaySongDetails();
                    }catch (Exception e) {
                        Log.d("SHOW_DETAILS ERROR :",e.getMessage());
                    }
                    return true;
                case R.id.action_delete:
                    try {
                        SongInfo obj = _songs.get(song_position);
                        String path = obj.getSongUrl().toString();
                        boolean deleted = Utility.deleteSong(path);
                        if (deleted)
                            Toast.makeText(context, "Deleted from device", Toast.LENGTH_SHORT).show();
                        else
                            Toast.makeText(context, "Could not delete file", Toast.LENGTH_SHORT).show();
                    }catch (Exception e){
                        Log.d("DELETE ERROR : ",e.getMessage());
                    }
                    return true;
                default:
            }*/
            return false;
        }

    }

    private void displaySongDetails() {
       /*
        //Views and Variables
        final TextView path,name, size,length,bitrate,format;
        String song_size,song_length,song_bitrate,song_format,songDetails;
        SongInfo songInfo = _songs.get(song_position);

        //Dialog Creation
        final Dialog myDialog = new Dialog(context);
        myDialog.setContentView(R.layout.song_details);
        myDialog.setCancelable(true);

        //Defining height and width of Dialog
        int width = (int)(context.getResources().getDisplayMetrics().widthPixels*0.90);
        int height = (int)(context.getResources().getDisplayMetrics().heightPixels*0.65);
        myDialog.getWindow().setLayout(width, height);

        //Initializing Views
        final Button ok = myDialog.findViewById(R.id.btn_ok);
        path = myDialog.findViewById(R.id.file_path1);
        name = myDialog.findViewById(R.id.file_name1);
        size = myDialog.findViewById(R.id.size1);
        length = myDialog.findViewById(R.id.length1);
        bitrate = myDialog.findViewById(R.id.bitrate1);
        format = myDialog.findViewById(R.id.format1);

        try {
            //Getting song details
            songDetails = Utility.getSongDetails(context, songInfo);
            String[] details = songDetails.split(",");
            song_size = details[0];
            song_length = details[1];
            song_bitrate = details[2];
            song_format = details[3];

            //Updating Views
            path.setText(songInfo.getSongUrl().toString());
            name.setText(songInfo.getSongName());
            Float sizeOfSong = Utility.calculateFileSizeInMB(song_size);
            size.setText(String.format("%s MB", sizeOfSong));
            length.setText(PlayerActivity.findTotalDuration(false, Long.valueOf(song_length)));
            bitrate.setText(String.format("%s kbps", song_bitrate));
            format.setText(song_format);

            //Showing Dialog
            myDialog.show();
            //Onclick
            ok.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    myDialog.dismiss();
                }
            });
        }catch (Exception e){
            Log.i("GET DETAILS ERROR",e.getMessage());
            Toast.makeText(context,"Can't Show Details",Toast.LENGTH_SHORT).show();
        }

        */
    }


    @Override
    public int getItemCount() {
        return _songs.size();
    }

    public class SongHolder extends RecyclerView.ViewHolder /*implements View.OnClickListener*/ {

        ImageView albumArt,overFlow;
        TextView songName, artistName;
        CardView cardView;

        //SongsListActivity songsListActivity;
        SongListFragment songListFragment;
        Context context;
        ArrayList<SongInfo> _songs = new ArrayList<SongInfo>();

        public SongHolder(View itemView,Context context, ArrayList<SongInfo> _songs ,final SongListFragment songListFragment) {
            super(itemView);

            this.songListFragment = songListFragment;
            this.context = context;
            this._songs = _songs;

            songName = itemView.findViewById(R.id.songName);
            artistName =  itemView.findViewById(R.id.artistName);
            albumArt =  itemView.findViewById(R.id.albumArt);
            cardView = itemView.findViewById(R.id.songCard);
            overFlow = itemView.findViewById(R.id.overflow);

            //itemView.setOnLongClickListener(songListFragment);
        }

    }

    public void setFilter(ArrayList<SongInfo> searchList){
        _songs = new ArrayList<>();
        _songs.addAll(searchList);
        notifyDataSetChanged();
    }

}

