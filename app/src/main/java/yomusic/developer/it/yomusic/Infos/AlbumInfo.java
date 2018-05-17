package yomusic.developer.it.yomusic.Infos;

import android.content.ContentUris;
import android.net.Uri;
import android.provider.MediaStore;

/**
 * Created by anupam on 03-02-2018.
 */

public class AlbumInfo {
    long aId;
    String aName,artistName;
    int aNoOfTracks, aNoOfTracksForArtist;


    public AlbumInfo(long aId, String aName, String artistName,int aNoOfTracks) {
        this.aId = aId;
        this.aName = aName;
        this.artistName = artistName;
        this.aNoOfTracks = aNoOfTracks;
        //this.aNoOfTracksForArtist = aNoOfTracksForArtist;
    }

    public long getaId() {
        return aId;
    }

    public String getaName() {
        return aName;
    }

    public String getArtistName() {
        return artistName;
    }

    public Uri getURI() {
        Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");
        Uri uri = ContentUris.withAppendedId(sArtworkUri, aId);

        return uri;
    }

    public int getaNoOfTracks() {
        return aNoOfTracks;
    }

    public int getaNoOfTracksForArtist() {
        return aNoOfTracksForArtist;
    }
}
