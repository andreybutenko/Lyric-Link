package ninja.andrey.lyriclink;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements CurrentSongService.SongListener, Search.SearchListener {

    private static final String TAG = "LyricLinkMain";

    private static final long TIMER_RATE = 100; // how often to check if service started
    private static final long INSTANT_LYRICS_COOLDOWN = 60 * 1000; // how long before instantly opening lyrics again

    boolean loadingLyrics = false;

    TextView musicTrack;
    TextView musicAlbum;
    TextView musicArtist;
    LinearLayout musicPlayingContainer;
    Button seeLyricsBtn;
    Button settingsBtn;
    TextView noMusicText;

    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        musicTrack = (TextView) findViewById(R.id.musicTrack);
        musicAlbum = (TextView) findViewById(R.id.musicAlbum);
        musicArtist = (TextView) findViewById(R.id.musicArtist);
        musicPlayingContainer = (LinearLayout) findViewById(R.id.musicPlayingContainer);
        seeLyricsBtn = (Button) findViewById(R.id.seeLyricsBtn);
        settingsBtn = (Button) findViewById(R.id.settingsBtn);
        noMusicText = (TextView) findViewById(R.id.noMusicText);

        seeLyricsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(CurrentSongService.isStarted()) {
                    openCurrentSongLyrics();
                }
            }
        });

        settingsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
            }
        });

        refreshCurrentSong();
    }

    @Override
    protected void onResume() {
        super.onResume();

        refreshCurrentSong();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(CurrentSongService.isStarted() && CurrentSongService.getInstance().hasListener(this)) {
            CurrentSongService.getInstance().removeListener(this);
        }

        if(CurrentSongService.isStarted()) {
            CurrentSongService.getInstance().killService();
        }
    }

    private void refreshCurrentSong() {
        // If music is playing, play/pause it to trigger the intent we need

        Log.d(TAG, "Refreshing current song...");
        AudioManager audioManager;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            audioManager = MainActivity.this.getSystemService(AudioManager.class);
        }
        else {
            audioManager = (AudioManager) MainActivity.this.getSystemService(Context.AUDIO_SERVICE);
        }

        if (audioManager.isMusicActive() && !CurrentSongService.isStarted()) {
            KeyEvent playEvent = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PLAY);
            KeyEvent pauseEvent = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PAUSE);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                audioManager.dispatchMediaKeyEvent(pauseEvent);
                audioManager.dispatchMediaKeyEvent(playEvent);
            }

            Intent intent = new Intent(this, CurrentSongService.class);
            startService(intent);

            final Timer timer = new Timer();

            TimerTask timerTask = new TimerTask() {
                @Override
                public void run() {
                    if(CurrentSongService.isStarted()) {
                        timer.cancel();
                        CurrentSongService.getInstance().addListener(MainActivity.this);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                updateCurrentlyPlaying();
                            }
                        });
                    }
                }
            };

            timer.scheduleAtFixedRate(timerTask, TIMER_RATE, TIMER_RATE);
        }

    }

    private boolean readyForInstantLyrics() {
        UserData userData = new UserData(this);

        return CurrentSongService.isStarted() &&
            CurrentSongService.getInstance().isMusicPlaying() &&
            System.currentTimeMillis() - userData.getLatestLyricLookupTime() > INSTANT_LYRICS_COOLDOWN &&
            userData.getInstantLyricsEnabled();
    }

    private void updateCurrentlyPlaying() {
        CurrentSongService currentSongService = CurrentSongService.getInstance();
        if(currentSongService.isMusicPlaying()) {
            noMusicText.setVisibility(View.GONE);
            musicPlayingContainer.setVisibility(View.VISIBLE);
            musicTrack.setText(currentSongService.getCurrentTrack());
            musicAlbum.setText(currentSongService.getCurrentAlbum());
            musicArtist.setText(currentSongService.getCurrentArtist());
        }
        else {
            noMusicText.setVisibility(View.VISIBLE);
            musicPlayingContainer.setVisibility(View.GONE);
        }

        if(readyForInstantLyrics()) {
            openCurrentSongLyrics();
        }
    }

    private void openCurrentSongLyrics() {
        Log.d(TAG, "Opening current song lyrics!");
        if(!loadingLyrics) {
            loadingLyrics = true;
            CurrentSongService currentSongService = CurrentSongService.getInstance();
            showLoadingDialog(currentSongService.getCurrentTrack(), currentSongService.getCurrentArtist());
            Search.addListener(MainActivity.this);
            Search.loadLyricsUrl(currentSongService.getCurrentTrack(), currentSongService.getCurrentArtist());
        }
    }

    private void showLoadingDialog(String track, String artist) {
        progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setTitle(track + " by " + artist);
        progressDialog.setMessage(getResources().getString(R.string.loading_lyrics_desc));
        progressDialog.setIndeterminate(true);
        progressDialog.show();
    }

    private void dismissLoadingDialog() {
        progressDialog.dismiss();
    }

    @Override
    public void onSongChanged(String track, String album, String artist) {
        updateCurrentlyPlaying();
    }

    @Override
    public void onSearchUrlLoaded(String url) {
        UserData userData = new UserData(this);
        userData.setLatestLyricLookupTime(System.currentTimeMillis());
        loadingLyrics = false;

        dismissLoadingDialog();
        Intent intent = Search.getLyricIntent(url);
        startActivity(intent);
    }
}
