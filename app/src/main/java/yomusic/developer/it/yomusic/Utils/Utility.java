package yomusic.developer.it.yomusic.Utils;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.CountDownTimer;
import android.os.ParcelFileDescriptor;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;

import java.io.FileDescriptor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Set;

import yomusic.developer.it.yomusic.Infos.AlbumInfo;
import yomusic.developer.it.yomusic.Infos.ArtistInfo;
import yomusic.developer.it.yomusic.Infos.FolderInfo;
import yomusic.developer.it.yomusic.Infos.SongInfo;
import yomusic.developer.it.yomusic.NotificationGenerator;
import yomusic.developer.it.yomusic.R;
import yomusic.developer.it.yomusic.SettingsActivity;

/**
 * Created by anupam on 01-02-2018.
 */

public class Utility {

    final static String TAG = "Utility";
    public static FirebaseAuth auth;

    // ArrayLists
    public static ArrayList<SongInfo> _nowPlayingList = new ArrayList<>();
    public static ArrayList<Integer> random_list = new ArrayList<>();
    public  static ArrayList<SongInfo> _songs = new ArrayList<>();
    public  static ArrayList<ArtistInfo> _artists = new ArrayList<>();
    public  static ArrayList<AlbumInfo> _albums = new ArrayList<>();
    public  static ArrayList<FolderInfo> folderList = new ArrayList<>();
    public static ArrayList<SongInfo> _lastPlayed = new ArrayList<>();
    public static ArrayList<SongInfo> _favourites = new ArrayList<>();
    public static ArrayList<SongInfo> _recentlyAdded = new ArrayList<>();

    //Timer
    public static SleepTimerClass sleep_timer;
    public static boolean isTimerOn = false;

    /*
    **
    *     .....................METHODS......................
    **
     */

    //Returns song album art as bitmap.....
    public static Bitmap getSongAlbumArt(Context context, Uri uri) {
        Bitmap bm = null;
        String path = null;
        MediaMetadataRetriever mmr;
        try{

            Cursor cursor = context.getContentResolver().query(uri,null,null,null,null);
            if(cursor != null && cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
            }

            if(path != null) {
                mmr = new MediaMetadataRetriever();
                mmr.setDataSource(path);
                byte[] artBytes = mmr.getEmbeddedPicture();
                if (artBytes != null) {
                    //InputStream is = new ByteArrayInputStream(artBytes);
                    bm = BitmapFactory.decodeByteArray(artBytes, 0, artBytes.length);
                }
            }
            cursor.close();
        }
        catch (Exception problem) {
            Log.d("Bitmap Loading Problem",problem.getMessage());
        }
        return bm;
    }

    //Returns  album thumbnail as bitmap.....
    public static Bitmap getAlbumThumbnails(Context context, Uri uri) {
        Bitmap image = null;
        ParcelFileDescriptor parcelFileDescriptor = null;
        try {
            parcelFileDescriptor = context.getContentResolver().openFileDescriptor(uri, "r");
            FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
            image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
            parcelFileDescriptor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return image;
    }

    //Returns folder name for a given url.....
    public static String getFolderNameOutOfUrl(String url){
        String[] s = url.split("/");
        int len = s.length;
        return s[len-2];
    }

    //Returns ArrayList of songs for a given folder path.....
    public static ArrayList<SongInfo> getSongsInTheFolder(Context context, String path){
        int count = 0;
        long id,duration;
        String name,artist,album,folder;
        Uri songUri;
        SongInfo songInfo;
        ArrayList<SongInfo> _songsList = new ArrayList<>();

        ContentResolver cr = context.getContentResolver();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0 AND " +
                MediaStore.Audio.Media.DATA + " LIKE '"+ path +"%'";

        String sortOrder = MediaStore.Audio.Media.TITLE + " ASC";
        Cursor cursor = cr.query(uri, null, selection, null, sortOrder);

        if(cursor != null) {
            count = cursor.getCount();
            if(count > 0) {
                while(cursor.moveToNext()) {
                    id = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media._ID));
                    name = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
                    artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                    songUri = Uri.parse(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA)));
                    album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
                    duration = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));

                    folder = Utility.getFolderNameOutOfUrl(songUri.toString());

                    //boolean filtered = Utility.checkIfFilteredByNameOrExtension(this,name,folder,duration);
                    //if(!filtered) {
                    songInfo = new SongInfo(id, name, artist, album, songUri);
                    _songsList.add(songInfo);
                    //}
                }
            }
            cursor.close();
        }
        return _songsList;
    }

    //Returns ArrayList of songs for a given folder name.....
    public static ArrayList<SongInfo> getSongsInTheFolder(String folderName){
        ArrayList<SongInfo> _list = new ArrayList<>();
        for(SongInfo song : _songs){
            String mPath = song.getSongUri().toString();
            String mFolderName = getFolderNameOutOfUrl(mPath);
            if(folderName.equals(mFolderName)){
                _list.add(song);
            }
        }
        return _list;
    }

    //Returns ArrayList of songs for a given album.....
    public static ArrayList<SongInfo> getSongsOfTheAlbum(String albumName){
        ArrayList<SongInfo> _songsList = new ArrayList<>();
        for(SongInfo item : _songs){
            if(albumName.equals(item.getSongAlbum())){
                _songsList.add(item);
            }
        }
        return _songsList;
    }

    //Returns ArrayList of songs for a given artist.....
    public static ArrayList<SongInfo> getSongsOfTheArtist(String artistName){
        ArrayList<SongInfo> _songsList = new ArrayList<>();
        for(SongInfo item : _songs){
            if(artistName.equals(item.getArtistName())){
                _songsList.add(item);
            }
        }
        return _songsList;
    }

    //Returns ArrayList of albums for a given artist.....
    public static ArrayList<AlbumInfo> getAlbumsOfArtists(String artistName){
        ArrayList<AlbumInfo> _albumList = new ArrayList<>();
        for(AlbumInfo item : _albums){
            if(artistName.equals(item.getArtistName())){
                _albumList.add(item);
            }
        }
        return _albumList;
    }

    //Returns ArrayList of recently played [20]songs.....
    public static ArrayList<SongInfo> getLastPlayedSongs(){
        ArrayList<SongInfo> _list = new ArrayList<>();
        if(!_lastPlayed.isEmpty()) {
            _list.addAll(_lastPlayed);
        } else{
            try {
                Thread.sleep(400);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            _list.addAll(_lastPlayed);
        }
        return _list;
    }

    public static ArrayList<SongInfo> getFavouritesSongs(){
        ArrayList<SongInfo> _list = new ArrayList<>();
        _list.addAll(_favourites);
        return _list;
    }

    public static ArrayList<SongInfo> getRecentlyAddedSongs(){
        ArrayList<SongInfo> _list = new ArrayList<>();
        _list.addAll(_recentlyAdded);
        return _list;
    }

    //Adds songs to _lastPlayedSongList.....
    public static void addSongsToLastPlayedList(Context context, SongInfo obj){
        int length = 20;
        if(_lastPlayed.contains(obj)){
            _lastPlayed.remove(obj);
        }
        _lastPlayed.add(0, obj);
        addLastPlayedSongPreference(context,obj);
        Log.i(TAG, "SongName: " + obj.getSongName());

        if(_lastPlayed.size() > length){
            _lastPlayed.remove(length);
        }
    }

    //Adds lastPlayedSong to SharedPreferences.....
    private static void addLastPlayedSongPreference(Context context, SongInfo obj) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor lastPlayedSongEditor = sharedPreferences.edit();
        lastPlayedSongEditor.putString(context.getString(R.string.last_played_song),obj.getSongName());
        lastPlayedSongEditor.apply();

        addLastPlayedListPreference(context,_lastPlayed);
    }

    private static final String fileName = "lastPlaylist";
    private static void addLastPlayedListPreference(Context context, ArrayList<SongInfo> list){
        SharedPreferences sharedPreferences = context.getSharedPreferences(fileName,Context.MODE_PRIVATE);
        SharedPreferences.Editor lastPlayedSongEditor = sharedPreferences.edit();
        lastPlayedSongEditor.putInt("listSize", list.size());
        for(int i = 0; i < _lastPlayed.size(); i++){
            lastPlayedSongEditor.putString(String.valueOf(i),_lastPlayed.get(i).getSongName());
        }
        lastPlayedSongEditor.apply();
    }

    public static ArrayList<String> loadLastPlayedList(Context context){
        SharedPreferences sharedPreferences = context.getSharedPreferences(fileName,Context.MODE_PRIVATE);
        // Create new array to be returned
        ArrayList<String> savedSongs = new ArrayList<String>();
        // Get the number of saved songs
        int numOfSavedSongs = sharedPreferences.getInt("listSize", 0);
        // Get saved songs by their index
        for (int i = 0; i < numOfSavedSongs; i++) {
            savedSongs.add(sharedPreferences.getString(String.valueOf(i),null));
        }
        return savedSongs;
    }

    //Adds lastPlayedSong's length played up-to  to SharedPreferences.....
    public static void addLastPlayedSongLengthPreference(Context context,int pos) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor lastPlayedSongLengthEditor = sharedPreferences.edit();
        lastPlayedSongLengthEditor.putInt(context.getString(R.string.played_upto_length),pos);
        lastPlayedSongLengthEditor.apply();
    }

    //Adds sleep time to SharedPreferences.....
    private static void editSharedPreferenceForSleepTimer(Context ctx,String value,int id ){
        //SharedPreferences sharedPreferences = ctx.getSharedPreferences(ctx.getString(R.string.sleep_timer),Context.MODE_PRIVATE);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(ctx);
        SharedPreferences.Editor sleepTimeEditor = sharedPreferences.edit();
        sleepTimeEditor.putString(ctx.getString(id),value);
        sleepTimeEditor.apply();
    }

    //Returns size of _lastPlayedSongList.....
    public static int getLastPlayedListSize(){
        return _lastPlayed.size();
    }

    //Returns size of _favouritesSongList.....
    public static int getFavouriteListSize(){return _favourites.size(); }

    //Returns size of _recentlyAddedSongList.....
    public static int getRecentlyAddedListSize(){return _recentlyAdded.size(); }

    //Returns ArrayList<String> of folder names
    public static ArrayList<String> getMusicFoldersList(){
        ArrayList<String> musicFoldersList = new ArrayList<>();
        String folderName = "";
        for(FolderInfo folder : folderList){
            folderName = folder.getFolderName();
            musicFoldersList.add(folderName);
        }

        Collections.sort(musicFoldersList, new Comparator<String>() {
            @Override
            public int compare(String a, String b) {
                return a.toLowerCase().compareTo(b.toLowerCase());
            }
        });

        return musicFoldersList;
    }

    //Sets sleep time for the player......
    public static void setTimer(Context context, long mills){
        if(!isTimerOn) {
            sleep_timer = new SleepTimerClass(context, mills, 1000);
            sleep_timer.start();
            isTimerOn = true;
        }else {
            sleep_timer.cancel();
        }
    }

    // CountDownTimerClass for SleepTime..........
    public static class SleepTimerClass extends CountDownTimer {
        Context mCtx;
        public SleepTimerClass(Context context, long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
            mCtx = context;
        }

        @Override
        public void onTick(long millisUntilFinished) {
            String timeRemaining = getTime(millisUntilFinished/1000);
            //editSharedPreferenceForSleepTimer(mCtx,timeRemaining,R.string.sleep_time_remaining);
        }

        @Override
        public void onFinish() {
            isTimerOn = false;
            //SongsListActivity.mediaPlayer.pause();
            //PlayerActivity.isPlaying = false;
            //SongsListActivity.checkIfUiNeedsToUpdate = true;
            //PlayerActivity.checkIfPlayerUiNeedsToUpdate = true;
            //Utility.recallNotificationBuilder(mCtx);

            editSharedPreferenceForSleepTimer(mCtx,"00",R.string.sleep_timer);
        }
    }

    //Returns formatted time for given milli seconds....
    public static String getTime(long time) {
        String Sec = String.valueOf(time%60);
        time/=60;
        String Min = String.valueOf(time%60);
        time/=60;
        String Hour = "0" + String.valueOf(time%60);
        if(Sec.length() == 1)
            Sec = "0" + Sec;
        if(Min.length() == 1)
            Min = "0" + Min;
        return Hour + ":" + Min + ":" + Sec + " ";
    }

    //Checks if a song is filtered by name, extension or folder content and returns a boolean value...
    //Returns true if a song is filtered by name, extension or folder content
    //Or Returns false if not filtered
    public static boolean checkIfFilteredByNameExtensionOrFolder(Context context,String name,String folderName,long time) {
        boolean status = false;
        ArrayList<String> filterByNameList = null;
        ArrayList<String> filterByExtensionList = null;
        Set<String> filteredFoldersList = null;

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String filterByName = sharedPreferences.getString("filter_by_name", " ");
        String filterByExtension = sharedPreferences.getString("filter_by_Extension", " ");
        boolean filterShortTracks = sharedPreferences.getBoolean("ignore_short_tracks", true);

        filteredFoldersList = sharedPreferences.getStringSet("music_folders",null);
        String[] selected =  null;
        if (filteredFoldersList != null) {
            selected = filteredFoldersList.toArray(new String[] {});
            Arrays.sort(selected);
        }


        try {
            if(!filterByName.equals(" ") && !filterByName.equals("")) {
                filterByNameList = new ArrayList<>(Arrays.asList(filterByName.split(",")));
            }
            if(!filterByExtension.equals(" ") || !filterByExtension.equals("")) {
                filterByExtensionList = new ArrayList<>(Arrays.asList(filterByExtension.split(",")));
            }

            if(filterByNameList != null) {
                for (String filteredName : filterByNameList) {
                    if (name.toLowerCase().contains(filteredName.toLowerCase()) && !filteredName.equals(" ") && !filteredName.equals("")) {
                        status = true;
                        break;
                    }
                }
            }

            if(filterByExtensionList != null) {
                for (String filteredExtension : filterByExtensionList) {
                    if (name.toLowerCase().contains(filteredExtension.toLowerCase()) && !filteredExtension.equals(" ") && !filteredExtension.equals("")) {
                        status = true;
                        break;
                    }
                }
            }

            if(filterShortTracks){
                if(time < 60000){
                    status = true;
                }
            }

            if (filteredFoldersList != null && !filteredFoldersList.contains(folderName)) {
                ArrayList<String> foldersList = new ArrayList<>();
                for(String index : selected){
                    foldersList.add(SettingsActivity.folderContainingMusic.get(Integer.parseInt(index)));
                }

                if(!foldersList.contains(folderName))
                    status = true;
            }

        } catch(Exception e) { e.getMessage(); }

        return status;
    }


    public static boolean checkIfFolderIsBlocked(Context context, String folderName){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        Set<String> filteredFoldersList = null;
        String[] selectedFolders =  null;
        boolean check = false;

        filteredFoldersList = sharedPreferences.getStringSet("music_folders",null);
        if (filteredFoldersList != null) {
            selectedFolders = filteredFoldersList.toArray(new String[] {});
            Arrays.sort(selectedFolders);
        }

        if (selectedFolders != null) {
            for(String folder : selectedFolders){
                if(!folderName.equals(folder)){
                    check = true;
                    break;
                }
            }
        }
        return check;
    }

    //Give a share screen to share a song...
    public static void shareSong(Context context){
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("audio/*");
        context.startActivity(Intent.createChooser(sharingIntent, "Share With"));
    }

    // Sets ringtone
    public static void setRingtone(Context context, long id) {
        ContentResolver resolver = context.getContentResolver();
        // Set the flag in the database to mark this as a ringtone
        Uri ringUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id);
        try {
            ContentValues values = new ContentValues(2);
            values.put(MediaStore.Audio.Media.IS_RINGTONE, "1");
            values.put(MediaStore.Audio.Media.IS_ALARM, "1");
            resolver.update(ringUri, values, null, null);
        } catch (UnsupportedOperationException ex) {
            // most likely the card just got unmounted
            Log.e(TAG, "couldn't set ringtone flag for id " + id);
            return;
        }

        String[] cols = new String[] {
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.TITLE
        };

        String where = MediaStore.Audio.Media._ID + "=" + id;
        Cursor cursor = query(context, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                cols, where , null, null);
        try {
            if (cursor != null && cursor.getCount() == 1) {
                // Set the system setting to make this the current ringtone
                cursor.moveToFirst();
                Settings.System.putString(resolver, Settings.System.RINGTONE, ringUri.toString());
                //String message = context.getString(R.string.ringtone_set);
                //Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public static Cursor query(Context context, Uri uri, String[] projection,
                               String selection, String[] selectionArgs, String sortOrder) {
        try {
            ContentResolver resolver = context.getContentResolver();
            if (resolver == null) {
                return null;
            }
            return resolver.query(uri, projection, selection, selectionArgs, sortOrder);
        } catch (UnsupportedOperationException ex) {
            return null;
        }
    }

    //Notification
    public static void recallNotificationBuilder(Context context) {
        NotificationGenerator.customNotification(context);
    }

    // Returns a formatted time in string for given time in milliseconds.....
    public static String createTimeLabel(long time){
        String timeLabel ="";
        int min = (int) (time /1000/60);
        int sec = (int) (time/1000 % 60);
        timeLabel = min +":";
        if(sec < 10) timeLabel += "0";
        timeLabel += sec;

        return timeLabel;
    }





}
