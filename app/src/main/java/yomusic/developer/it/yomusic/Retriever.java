/*   
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package yomusic.developer.it.yomusic;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import yomusic.developer.it.yomusic.Infos.AlbumInfo;
import yomusic.developer.it.yomusic.Infos.ArtistInfo;
import yomusic.developer.it.yomusic.Infos.FolderInfo;
import yomusic.developer.it.yomusic.Infos.SongInfo;
import yomusic.developer.it.yomusic.Utils.PreferencesUtils;
import yomusic.developer.it.yomusic.Utils.Utility;

import static yomusic.developer.it.yomusic.Utils.Utility.folderList;

/**
 * Retrieves and organizes media to play. Before being used, you must call {@link #prepareMusicRetriever()},
 * which will retrieve all of the music on the user's device (by performing a query on a content
 * resolver). After that, it's ready to retrieve a random song, with its title and URI, upon
 * request.
 */
public class Retriever {
    private final String TAG = "Retriever";

    private PreferencesUtils mPreferencesUtils;
    private ContentResolver mContentResolver;
    private Context mContext;

    public Retriever(Context ctx, ContentResolver cr) {
        mContentResolver = cr;
        mContext = ctx;
    }

    public ContentResolver getContentResolver() {
        return mContentResolver;
    }


    public void prepareMusicRetriever() {
        long id,duration;
        String name,artist,album,folder;
        Uri songUri;
        SongInfo songInfo;

        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Log.i(TAG, "Querying media...");
        Log.i(TAG, "URI: " + uri.toString());

        String selection = MediaStore.Audio.Media.IS_MUSIC + "!=0";
        Cursor cursor = getContentResolver().query(uri,null,selection,null,null);
        Log.i(TAG, "Query finished. " + (cursor == null ? "Returned NULL." : "Returned a cursor."));

        if (cursor == null) {
            // Query failed...
            Log.e(TAG, "Failed to retrieve music: cursor is null :-(");
            return;
        }
        if (!cursor.moveToFirst()) {
            // Nothing to query. There is no music on the device. How boring.
            Log.e(TAG, "Failed to move cursor to first row (no query results).");
            return;
        }

        Log.i(TAG, "Listing...");

        do {
            id = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media._ID));
            name = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
            artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
            songUri = Uri.parse(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA)));
            album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
            duration = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));

            folder = Utility.getFolderNameOutOfUrl(songUri.toString());

            boolean filtered = Utility.checkIfFilteredByNameExtensionOrFolder(mContext,name,folder,duration);
            if(!filtered) {
                Log.i(TAG, "ID: " + id + " Title: " + name);
                songInfo = new SongInfo(id, name, artist, album, songUri);
                if(!Utility._songs.contains(songInfo))
                    Utility._songs.add(songInfo);
            }

        }while (cursor.moveToNext());
        Log.i(TAG, "Done querying media. Retriever is ready.");

        Collections.sort(Utility._songs, new Comparator<SongInfo>(){
            public int compare(SongInfo a, SongInfo b){
                return a.getSongName().compareTo(b.getSongName());
            }
        });
    }

    public void prepareFolderRetriever(){
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Log.i(TAG, "Querying Folders...");
        Log.i(TAG, "URI: " + uri.toString());

        String selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0";
        String sortOrder = MediaStore.Audio.Media.TITLE + " ASC";
        Cursor cur = getContentResolver().query(uri, null, selection, null, sortOrder);
        int count = 0;
        ArrayList<String> folderNames = new ArrayList<>();

        if(cur != null) {
            count = cur.getCount();
            if(count > 0) {
                while(cur.moveToNext()) {
                    String data = cur.getString(cur.getColumnIndex(MediaStore.Audio.Media.DATA));
                    String folderName = Utility.getFolderNameOutOfUrl(data);

                    boolean blocked = Utility.checkIfFolderIsBlocked(mContext,folderName);
                    //if(!blocked) {
                        File file = new File(data);
                        String path = file.getParent();
                        int audioCount = countAudioFiles(path);
                        FolderInfo folderInfo = new FolderInfo(folderName, path, audioCount);

                        if (!folderList.isEmpty()) {
                            for (FolderInfo folder : folderList) {
                                folderNames.add(folder.getFolderName());
                                folderNames.add(folder.getFolderPath());
                            }

                            if (!folderNames.contains(folderName) && !folderNames.contains(path)) {
                                folderList.add(folderInfo);
                            }
                        } else {
                            folderList.add(folderInfo);
                        }
                    //}
                }
            }
            cur.close();
        }
        Collections.sort(folderList, new Comparator<FolderInfo>(){
            public int compare(FolderInfo a, FolderInfo b){
                return a.getFolderName().toLowerCase().compareTo(b.getFolderName().toLowerCase());
            }
        });
    }

    public void prepareAlbumRetriever() {

        AlbumInfo albumInfo;
        Uri uri =  MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI;
        Log.i(TAG, "Querying Albums...");
        Log.i(TAG, "URI: " + uri.toString());

        String[] mProjection = {
                MediaStore.Audio.Albums._ID,
                MediaStore.Audio.Albums.ALBUM,
                MediaStore.Audio.Albums.NUMBER_OF_SONGS,
                MediaStore.Audio.Albums.ARTIST
        };
        String mSortOder = MediaStore.Audio.Albums.DEFAULT_SORT_ORDER;

        Cursor cursor = getContentResolver().query(uri,mProjection,null,null,mSortOder);
        Log.i(TAG, "Query finished. " + (cursor == null ? "Returned NULL." : "Returned a cursor."));

        if (cursor == null) {
            // Query failed...
            Log.e(TAG, "Failed to retrieve music: cursor is null :-(");
            return;
        }
        if (!cursor.moveToFirst()) {
            // Nothing to query. There is no music on the device. How boring.
            Log.e(TAG, "Failed to move cursor to first row (no query results).");
            return;
        }

        Log.i(TAG, "Listing...");

        do {
            long aId = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Albums._ID));
            String albumName = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM));
            String artistName = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Albums.ARTIST));
            int aNoOfTracks = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Albums.NUMBER_OF_SONGS));
           // int aNoOfTracksForArtist = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Albums.NUMBER_OF_SONGS_FOR_ARTIST));

            Log.i(TAG, "ID: " + aId + ", Album: " + albumName +", Artist: " + artistName + ", NoOfTracks: " + aNoOfTracks);

            albumInfo = new AlbumInfo(aId,albumName,artistName,aNoOfTracks);
            if(!Utility._albums.contains(albumInfo))
                Utility._albums.add(albumInfo);

        }while (cursor.moveToNext());
        Log.i(TAG, "Done querying media. Retriever is ready.");
    }

    public void prepareArtistsRetriever() {

        ArtistInfo artistInfo;
        Uri uri =  MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI;
        Log.i(TAG, "Querying Artists...");
        Log.i(TAG, "URI: " + uri.toString());

        String[] mProjection = {
                MediaStore.Audio.Artists._ID,
                MediaStore.Audio.Artists.ARTIST,
                MediaStore.Audio.Artists.NUMBER_OF_TRACKS,
                MediaStore.Audio.Artists.NUMBER_OF_ALBUMS,
        };
        String mSortOder = MediaStore.Audio.Artists.ARTIST + " ASC";

        Cursor cursor = getContentResolver().query(uri,mProjection,null,null,mSortOder);
        Log.i(TAG, "Query finished. " + (cursor == null ? "Returned NULL." : "Returned a cursor."));

        if (cursor == null) {
            // Query failed...
            Log.e(TAG, "Failed to retrieve music: cursor is null :-(");
            return;
        }
        if (!cursor.moveToFirst()) {
            // Nothing to query. There is no music on the device. How boring.
            Log.e(TAG, "Failed to move cursor to first row (no query results).");
            return;
        }

        Log.i(TAG, "Listing...");

        do {
            long aId = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Artists._ID));
            String aName = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Artists.ARTIST));
            int aNoOfTracks = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Artists.NUMBER_OF_TRACKS));
            int aNoOfAlbums = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Artists.NUMBER_OF_ALBUMS));

            Log.i(TAG, "ID: " + aId + ", Artist: " + aName + ", NoOfTracks: " + aNoOfTracks +
                    ", NoOfAlbums : " + aNoOfAlbums);

            artistInfo = new ArtistInfo(aId,aName,aNoOfTracks,aNoOfAlbums);
            if(!Utility._artists.contains(artistInfo))
                Utility._artists.add(artistInfo);

        }while (cursor.moveToNext());
        Log.i(TAG, "Done querying media. Retriever is ready.");
    }

    private int countAudioFiles(String path){
        int count = 0;
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0 AND " + MediaStore.Audio.Media.DATA + " LIKE '"+ path +"%'";
        String sortOrder = MediaStore.Audio.Media.TITLE + " ASC";
        Cursor cursor = getContentResolver().query(uri, null, selection, null, sortOrder);
        if(cursor != null) {
            count = cursor.getCount();
        }
        return count;
    }

    public void prepareLastPlayedRetriever(){
        ArrayList<String> list = Utility.loadLastPlayedList(mContext);
        for(String songName : list){
            SongInfo item = SongInfo.getSongInfoObject(songName);
            if(item != null) {
                Utility._lastPlayed.add(item);
                Log.i("TAG", item.getSongName());
            }
        }
    }

    public void prepareFavouritesRetriever(){
        mPreferencesUtils = new PreferencesUtils(mContext,"favPlaylist");
        ArrayList<String> list = mPreferencesUtils.loadListPreference();
        for(String songName : list){
            SongInfo item = SongInfo.getSongInfoObject(songName);
            if(item != null) {
                Utility._favourites.add(item);
                Log.i("TAG", item.getSongName());
            }
        }
    }

    public void prepareRecentlyAddedRetriever() {
//        mPreferencesUtils = new PreferencesUtils(mContext, "recentlyAddedList");
//        ArrayList<String> list = mPreferencesUtils.loadListPreference();
//        for (String songName : list) {
//            SongInfo item = SongInfo.getSongInfoObject(songName);
//            if (item != null) {
//                Utility._recentlyAdded.add(item);
//                Log.i("TAG", item.getSongName());
//            }
//        }

        //TODO get the recently added songs i don't know how but have to....
    }

}

