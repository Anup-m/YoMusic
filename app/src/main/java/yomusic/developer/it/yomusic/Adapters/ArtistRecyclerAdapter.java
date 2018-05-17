package yomusic.developer.it.yomusic.Adapters;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import yomusic.developer.it.yomusic.Infos.ArtistInfo;
import yomusic.developer.it.yomusic.R;

/**
 * Created by anupam on 03-02-2018.
 */

public class ArtistRecyclerAdapter extends RecyclerView.Adapter<ArtistRecyclerAdapter.ArtistHolder>{

    Context mContext;
    ArrayList<ArtistInfo> mArtistList = new ArrayList<>();

    public ArtistRecyclerAdapter(Context context, ArrayList<ArtistInfo> _artist) {
        mContext = context;
        mArtistList = _artist;
    }

    ArtistRecyclerAdapter.OnItemClickListener onItemClickListner;

    public interface OnItemClickListener {
        void OnItemClick(View v, ArtistInfo obj, int position);
    }

    public void setOnItemClickListener(ArtistRecyclerAdapter.OnItemClickListener onItemClickListener) {
        this.onItemClickListner = onItemClickListener;
    }


    @Override
    public ArtistRecyclerAdapter.ArtistHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View myView = LayoutInflater.from(mContext).inflate(R.layout.artist_row_item, parent, false);
        return new ArtistRecyclerAdapter.ArtistHolder(myView, mContext, mArtistList);
    }

    @Override
    public void onBindViewHolder(final ArtistRecyclerAdapter.ArtistHolder holder, int position) {
        final ArtistInfo artistInfo = mArtistList.get(position);
        if(artistInfo.getaName().length()>10){
            String name = artistInfo.getaName().substring(0,8) +"..";
            holder.artistName.setText(name);
        }
        else {
            holder.artistName.setText(artistInfo.getaName());
        }
        holder.noOfAlbums.setText(String.valueOf(artistInfo.getaNoOfAlbums() + " Albums"));
        holder.noOfSongs.setText(String.valueOf(artistInfo.getaNoOfTracks() + " Songs"));

        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //song_position = holder.getAdapterPosition();
                if (onItemClickListner != null) {
                    onItemClickListner.OnItemClick(v, artistInfo, holder.getAdapterPosition());
                }
            }
        });

        holder.overFlow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //song_position = holder.getAdapterPosition();
                //showPopupMenu(holder.overFlow);
            }
        });

    }

    @Override
    public int getItemCount() {
        return mArtistList.size();
    }

    public class ArtistHolder extends RecyclerView.ViewHolder {

        ImageView overFlow,artistImage;
        TextView artistName, noOfSongs, noOfAlbums;
        CardView cardView;

        Context context;
        ArrayList<ArtistInfo> mArtistList = new ArrayList<>();

        public ArtistHolder(View itemView, Context context, ArrayList<ArtistInfo> artistList) {
            super(itemView);

            this.context = context;
            mArtistList = artistList;

            artistName = itemView.findViewById(R.id.artistName);
            noOfAlbums = itemView.findViewById(R.id.no_of_albums);
            noOfSongs = itemView.findViewById(R.id.no_of_songs);
            cardView = itemView.findViewById(R.id.artistCard);
            overFlow = itemView.findViewById(R.id.overflow);
            artistImage = itemView.findViewById(R.id.artistImage);
        }
    }

}