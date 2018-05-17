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

import yomusic.developer.it.yomusic.ImageLoader;
import yomusic.developer.it.yomusic.Infos.AlbumInfo;
import yomusic.developer.it.yomusic.R;

/**
 * Created by anupam on 03-02-2018.
 */

public class AlbumRecyclerAdapter extends RecyclerView.Adapter<AlbumRecyclerAdapter.AlbumHolder>{

    Context mContext;
    ArrayList<AlbumInfo> mAlbumList = new ArrayList<>();
    ImageLoader imageLoader;

    public AlbumRecyclerAdapter(Context context, ArrayList<AlbumInfo> _album) {
        mContext = context;
        mAlbumList = _album;
        imageLoader = new ImageLoader(context,2);
    }

    AlbumRecyclerAdapter.OnItemClickListener onItemClickListner;

    public interface OnItemClickListener {
        void OnItemClick(View v, AlbumInfo obj, int position);
    }

    public void setOnItemClickListener(AlbumRecyclerAdapter.OnItemClickListener onItemClickListener) {
        this.onItemClickListner = onItemClickListener;
    }


    @Override
    public AlbumRecyclerAdapter.AlbumHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View myView = LayoutInflater.from(mContext).inflate(R.layout.album_row_item, parent, false);
        return new AlbumRecyclerAdapter.AlbumHolder(myView, mContext, mAlbumList);
    }

    @Override
    public void onBindViewHolder(final AlbumRecyclerAdapter.AlbumHolder holder, int position) {
        final AlbumInfo albumInfo = mAlbumList.get(position);

        if(albumInfo.getaName().length()>10){
            String name = albumInfo.getaName().substring(0,8) +"..";
            holder.albumName.setText(name);
        }
        else {
            holder.albumName.setText(albumInfo.getaName());
        }

        if(albumInfo.getArtistName().length()>10) {
            String artistname = albumInfo.getArtistName().substring(0, 8) + "..";
            holder.artistName.setText(String.valueOf(artistname));
        }
        else {
            holder.artistName.setText(String.valueOf(albumInfo.getArtistName()));
        }

        holder.noOfSongs.setText(String.valueOf(albumInfo.getaNoOfTracks() + " Songs"));

//        Bitmap bitmap = Utility.getAlbumThumbnails(mContext,albumInfo.getContentUris());
//        if(bitmap !=null){
//            holder.albumImage.setImageBitmap(bitmap);
//        }
//        else {
//            holder.albumImage.setImageResource(R.drawable.album_cd);
//            //holder.albumImage.setImageURI(albumInfo.getContentUris());
//        }

        imageLoader.displayImage(albumInfo.getURI().toString(),holder.albumImage);

        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //song_position = holder.getAdapterPosition();
                if (onItemClickListner != null) {
                    onItemClickListner.OnItemClick(v, albumInfo, holder.getAdapterPosition());
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
        return mAlbumList.size();
    }

    public class AlbumHolder extends RecyclerView.ViewHolder {

        ImageView overFlow,albumImage;
        TextView albumName, artistName, noOfSongs;
        CardView cardView;

        Context context;
        ArrayList<AlbumInfo> mAlbumList = new ArrayList<>();

        public AlbumHolder(View itemView, Context context, ArrayList<AlbumInfo> albumList) {
            super(itemView);

            this.context = context;
            mAlbumList = albumList;

            artistName = itemView.findViewById(R.id.artistName);
            albumName = itemView.findViewById(R.id.album_Name);
            noOfSongs = itemView.findViewById(R.id.no_of_songs);
            cardView = itemView.findViewById(R.id.albumCard);
            overFlow = itemView.findViewById(R.id.overflow);
            albumImage = itemView.findViewById(R.id.albumImage);
        }
    }

}
