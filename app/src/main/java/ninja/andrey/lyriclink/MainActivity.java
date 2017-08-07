package ninja.andrey.lyriclink;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements CurrentSongService.SongListener, Search.SearchListener {

    private static final long TIMER_RATE = 100; // how often to check if service started
    private static final long INSTANT_LYRICS_COOLDOWN = 60 * 1000; // how long before instantly opening lyrics again

    TextView musicTrack;
    TextView musicAlbum;
    TextView musicArtist;
    LinearLayout musicPlayingContainer;
    Button seeLyricsBtn;
    Button preferredBtn;
    ProgressBar progressBar;
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
        preferredBtn = (Button) findViewById(R.id.preferredBtn);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        noMusicText = (TextView) findViewById(R.id.noMusicText);

        seeLyricsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(CurrentSongService.getInstance() != null) {
                    openCurrentSongLyrics();
                }
            }
        });

        if(CurrentSongService.getInstance() == null) {
            waitForServiceStart();
            Intent intent = new Intent(this, CurrentSongService.class);
            startService(intent);
        }
        else {
            onServiceStarted();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        UserData userData = new UserData(this);

        if(CurrentSongService.getInstance() != null &&
                CurrentSongService.getInstance().isMusicPlaying() &&
                System.currentTimeMillis() - userData.getLatestLyricLookupTime() > INSTANT_LYRICS_COOLDOWN) {
            openCurrentSongLyrics();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(CurrentSongService.getInstance() != null && CurrentSongService.getInstance().hasListener(this)) {
            CurrentSongService.getInstance().removeListener(this);
        }
    }

    // Methods

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
    }

    private void openCurrentSongLyrics() {
        CurrentSongService currentSongService = CurrentSongService.getInstance();
        showLoadingDialog(currentSongService.getCurrentTrack(), currentSongService.getCurrentArtist());
        Search.addListener(MainActivity.this);
        Search.loadLyricsUrl(currentSongService.getCurrentTrack(), currentSongService.getCurrentArtist());
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

    // Services and Listeners

    private void waitForServiceStart() {
        final Timer timer = new Timer();

        final Runnable completedRunnable = new Runnable() {
            @Override
            public void run() {
                MainActivity.this.onServiceStarted();
            }
        };

        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                if(CurrentSongService.getInstance() != null) {
                    timer.cancel();
                    runOnUiThread(completedRunnable);
                }
            }
        };

        timer.scheduleAtFixedRate(timerTask, TIMER_RATE, TIMER_RATE);
    }

    private void onServiceStarted() {
        CurrentSongService.getInstance().addListener(MainActivity.this);

        progressBar.setVisibility(View.GONE);
        updateCurrentlyPlaying();
    }

    @Override
    public void onSongChanged(String track, String album, String artist) {
        updateCurrentlyPlaying();
    }

    @Override
    public void onSearchUrlLoaded(String url) {
        UserData userData = new UserData(this);
        userData.setLatestLyricLookupTime(System.currentTimeMillis());

        dismissLoadingDialog();
        Search.removeListener(MainActivity.this);
        Intent intent = Search.getLyricIntent(url);
        startActivity(intent);
    }
}
