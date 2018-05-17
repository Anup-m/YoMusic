package yomusic.developer.it.yomusic.Infos;

import android.content.ContentUris;
import android.net.Uri;
import android.provider.MediaStore;

/**
 * Created by anupam on 03-02-2018.
 */

public class ArtistInfo {
    long aId;
    String aName;
    int aNoOfTracks, aNoOfAlbums;

    public ArtistInfo(long aId, String aName, int aNoOfTracks, int aNoOfAlbums) {
        this.aId = aId;
        this.aName = aName;
        this.aNoOfTracks = aNoOfTracks;
        this.aNoOfAlbums = aNoOfAlbums;
    }

    public long getaId() {
        return aId;
    }

    public String getaName() {
        return aName;
    }

    public Uri getURI() {
        Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");
        Uri uri = ContentUris.withAppendedId(sArtworkUri, aId);
        return uri;
        //return ContentUris.withAppendedId(
                //MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI, aId);
    }

    public int getaNoOfTracks() {
        return aNoOfTracks;
    }

    public int getaNoOfAlbums() {
        return aNoOfAlbums;
    }
}
