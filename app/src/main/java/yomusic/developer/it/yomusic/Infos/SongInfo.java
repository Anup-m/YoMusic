package yomusic.developer.it.yomusic.Infos;

import android.content.ContentUris;
import android.net.Uri;
import android.provider.MediaStore;

import yomusic.developer.it.yomusic.Fragments.SongListFragment;
import yomusic.developer.it.yomusic.Utils.Utility;

/**
 * Created by anupam on 10-12-2017.
 */

public class SongInfo {
    String songName, artistName, songAlbum;
    Uri songUri;
    long songId;

    public SongInfo(long songId,String songName,String artistName,String songAlbum,Uri songUri) {
        this.songId = songId;
        this.songName = songName;
        this.artistName = artistName;
        this.songAlbum = songAlbum;
        this.songUri = songUri;
    }

    public long getSongId() {
        return songId;
    }

    public String getSongName() {
        return songName;
    }

    public String getArtistName() {
        return artistName;
    }

    public Uri getContentUris() {
        return ContentUris.withAppendedId(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, songId);
    }

    public String getSongAlbum() {
        return songAlbum;
    }

    public Uri getSongUri() {
        return songUri;
    }

    //returns SongInfo object for a supplied songId
    public static SongInfo getSongInfoObject(long songId){
        for(SongInfo item : Utility._songs) {
            if(songId == item.getSongId()){
                return item;
            }
        }
        return null;
    }

    //returns SongInfo object for a supplied songName
    public static SongInfo getSongInfoObject(String songName){
        for(SongInfo item : Utility._songs) {
            if(songName.equals(item.getSongName())){
                return item;
            }
        }
        return null;
    }

    //Returns SongInfo object for the currently playing song in the _nowPlayingList...
    public static SongInfo getSongInfoObject(){
        SongInfo song = Utility._nowPlayingList.get(SongListFragment.currSongPosition);
        if(song != null){
            return song;
        }
        return null;
    }

    //returns songUri of the song for a supplied songId
    public static Uri getSongUri(long songId) {
        for (SongInfo item : Utility._songs) {
            if (songId == item.getSongId()) {
                return item.getSongUri() ;
            }
        }
        return null;
    }
}
