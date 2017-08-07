package ninja.andrey.lyriclink;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Created by Andrey on 8/6/2017.
 */

public class CurrentSongService extends Service {
    private static final String TAG = "SongService";
    private static final String[] actions = new String[]{
            "com.android.music.metachanged",
            "com.htc.music.metachanged",
            "fm.last.android.metachanged",
            "com.sec.android.app.music.metachanged",
            "com.nullsoft.winamp.metachanged",
            "com.amazon.mp3.metachanged",
            "com.miui.player.metachanged",
            "com.real.IMP.metachanged",
            "com.sonyericsson.music.metachanged",
            "com.rdio.android.metachanged",
            "com.samsung.sec.android.MusicPlayer.metachanged",
            "com.andrew.apollo.metachanged",

            "com.android.music.playstatechanged",
            "com.htc.music.playstatechanged",
            "fm.last.android.playstatechanged",
            "com.sec.android.app.music.playstatechanged",
            "com.nullsoft.winamp.playstatechanged",
            "com.amazon.mp3.playstatechanged",
            "com.miui.player.playstatechanged",
            "com.real.IMP.playstatechanged",
            "com.sonyericsson.music.playstatechanged",
            "com.rdio.android.playstatechanged",
            "com.samsung.sec.android.MusicPlayer.playstatechanged",
            "com.andrew.apollo.playstatechanged"
    };

    private static CurrentSongService INSTANCE = null;

    private List<SongListener> songListenerList = new ArrayList<>();

    private String currentTrack = "";
    private String currentAlbum = "";
    private String currentArtist = "";

    @Override
    public void onCreate() {
        IntentFilter intentFilter = new IntentFilter();

        Log.d(TAG, "Starting service!");

        for(String action : actions) {
            intentFilter.addAction(action);
        }

        registerReceiver(broadcastReceiver, intentFilter);

        CurrentSongService.INSTANCE = this;
    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            String artist = intent.getStringExtra("artist");
            String album = intent.getStringExtra("album");
            String track = intent.getStringExtra("track");

            Log.d(TAG, "Action: " + action + ". Now playing " + track + " by " + artist + " from " + album);

            CurrentSongService.this.currentTrack = track;
            CurrentSongService.this.currentAlbum = album;
            CurrentSongService.this.currentArtist = artist;

            notifyListeners(track, album, artist);
        }
    };

    // Getters

    public String getCurrentTrack() {
        return currentTrack;
    }

    public String getCurrentAlbum() {
        return currentAlbum;
    }

    public String getCurrentArtist() {
        return currentArtist;
    }

    public boolean isMusicPlaying() {
        return !currentTrack.equals("");
    }

    public static CurrentSongService getInstance() {
        return INSTANCE;
    }

    // Interface

    public interface SongListener {
        void onSongChanged(String track, String album, String artist);
    }

    public void addListener(SongListener songListener) {
        songListenerList.add(songListener);
    }

    public void removeListener(SongListener songListener) {
        songListenerList.remove(songListener);
    }

    public boolean hasListener(SongListener songListener) {
        return songListenerList.contains(songListener);
    }

    private void notifyListeners(String track, String album, String artist) {
        for(SongListener songListener : songListenerList) {
            songListener.onSongChanged(track, album, artist);
        }
    }

    // Misc

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
