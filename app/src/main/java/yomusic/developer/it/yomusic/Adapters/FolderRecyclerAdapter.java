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

import yomusic.developer.it.yomusic.Infos.FolderInfo;
import yomusic.developer.it.yomusic.R;


/**
 * Created by anupam on 03-02-2018.
 */

public class FolderRecyclerAdapter extends RecyclerView.Adapter<FolderRecyclerAdapter.FolderHolder> {

    Context mContext;
    ArrayList<FolderInfo> mFolderList = new ArrayList<>();
    //FoldersActivity foldersActivity;

    public FolderRecyclerAdapter(Context context, ArrayList<FolderInfo> folderList) {
        mContext = context;
        mFolderList = folderList;
        //foldersActivity = (FoldersActivity)context;
    }

    OnItemClickListener onItemClickListner;

    public interface OnItemClickListener{
        void OnItemClick(View v, FolderInfo obj, int position);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener){
        this.onItemClickListner = onItemClickListener;
    }


    @Override
    public FolderHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View myView = LayoutInflater.from(mContext).inflate(R.layout.folder_row_item,parent,false);
        return new FolderRecyclerAdapter.FolderHolder(myView,mContext, mFolderList);
    }

    @Override
    public void onBindViewHolder(final FolderHolder holder, int position) {
        final FolderInfo folderInfo = mFolderList.get(position);
        holder.folderName.setText(folderInfo.getFolderName());
        holder.folderPth.setText(folderInfo.getFolderPath());
       // holder.noOfSongs.setText(String.valueOf(folderInfo.getNoOfFiles()+" Songs"));
        holder.noOfSongs.setText(String.valueOf(
                folderInfo.getNoOfRecentFiles(folderInfo.getFolderName(),folderInfo.getFolderPath())+" Songs"));

        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //song_position = holder.getAdapterPosition();
                if(onItemClickListner != null){
                    onItemClickListner.OnItemClick(v,folderInfo,holder.getAdapterPosition());
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
        return mFolderList.size();
    }

    public class FolderHolder extends RecyclerView.ViewHolder {

        ImageView overFlow;
        TextView folderName, folderPth, noOfSongs;
        CardView cardView;

        Context context;
        ArrayList<FolderInfo> mFolderList = new ArrayList<FolderInfo>();

        public FolderHolder(View itemView, Context context, ArrayList<FolderInfo> folderList) {
            super(itemView);

            this.context = context;
            mFolderList = folderList;

            folderName = itemView.findViewById(R.id.folderName);
            folderPth = itemView.findViewById(R.id.folderPath);
            noOfSongs = itemView.findViewById(R.id.noOfSongs);
            cardView = itemView.findViewById(R.id.folderCard);
            overFlow = itemView.findViewById(R.id.overflow);
        }

    }
}
