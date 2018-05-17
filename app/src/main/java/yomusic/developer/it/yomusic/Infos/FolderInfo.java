package yomusic.developer.it.yomusic.Infos;

import static yomusic.developer.it.yomusic.Utils.Utility._songs;
import static yomusic.developer.it.yomusic.Utils.Utility.getFolderNameOutOfUrl;

/**
 * Created by anupam on 17-01-2018.
 */

public class FolderInfo {

    private String name,path;
    private int noOfFiles;

    public FolderInfo(String name, String path, int noOfFiles) {
        this.name = name;
        this.path = path;
        this.noOfFiles = noOfFiles;
    }

    public String getFolderName() {
        return name;
    }

    public String getFolderPath() {
        return path;
    }

    public int getNoOfFiles() {
        return noOfFiles;
    }

    public int getNoOfRecentFiles(String folderName,String folderPath){
        int count = 0;
        for(SongInfo song : _songs){
            String mPath = song.getSongUri().toString();
            String mFolderName = getFolderNameOutOfUrl(mPath);
            if(folderName.equals(mFolderName) && folderPath.equals(path)){
                count++;
            }
        }
        return count;
    }
}
