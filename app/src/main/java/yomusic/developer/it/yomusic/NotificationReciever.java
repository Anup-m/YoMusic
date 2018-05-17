package yomusic.developer.it.yomusic;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import yomusic.developer.it.yomusic.Infos.SongInfo;
import yomusic.developer.it.yomusic.Utils.Utility;

import static yomusic.developer.it.yomusic.Fragments.SongListFragment.currSongPosition;
import static yomusic.developer.it.yomusic.MusicService.mediaPlayer;
import static yomusic.developer.it.yomusic.MusicService.playedLength;
import static yomusic.developer.it.yomusic.Utils.Utility._songs;

/**
 * Created by anupam on 09-02-2018.
 */

public class NotificationReciever extends BroadcastReceiver {
    SongInfo songInfo;
    @Override
    public void onReceive(Context context, Intent intent) {

        switch (intent.getAction()) {
            case NotificationGenerator.NOTIFY_PAUSE:
               if(mediaPlayer != null) {
                   if (mediaPlayer.isPlaying()) {
                       mediaPlayer.pause();
                       MusicService.isPlaying = false;
                       playedLength = mediaPlayer.getCurrentPosition();
                   } else {
                       mediaPlayer.seekTo(playedLength);
                       mediaPlayer.start();
                       MusicService.isPlaying = true;
                   }

                   MainActivity.checkIfUiNeedsToUpdate = true;
                   PlayerActivity.checkIfPlayerUiNeedsToUpdate = true;
                   Utility.recallNotificationBuilder(context);
               }
               break;
            case NotificationGenerator.NOTIFY_NEXT:
                MusicService.playNext();
                songInfo = _songs.get(currSongPosition);
                MusicService.playSong(songInfo);
                MusicService.isPlaying = true;
                PlayerActivity.checkIfPlayerUiNeedsToUpdate = true;
                MainActivity.checkIfUiNeedsToUpdate = true;
                break;

            case NotificationGenerator.NOTIFY_PREVIOUS:
                MusicService.playPrevious();
                songInfo = _songs.get(currSongPosition);
                MusicService.playSong(songInfo);
                MusicService.isPlaying = true;
                PlayerActivity.checkIfPlayerUiNeedsToUpdate = true;
                MainActivity.checkIfUiNeedsToUpdate = true;
                break;
            case NotificationGenerator.NOTIFY_DUCK_UP:
                NotificationGenerator.isSimpleView = true;
                Utility.recallNotificationBuilder(context);
                break;
            case NotificationGenerator.NOTIFY_DUCK_DOWN:
                NotificationGenerator.isSimpleView = false;
                Utility.recallNotificationBuilder(context);
                break;
        }
    }
}
