package ninja.andrey.lyriclink;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by Andrey on 8/7/2017.
 */

public class UserData {
    private SharedPreferences sharedPreferences;

    public static final String LATEST_LYRIC_LOOKUP_TIME = "LATEST_LYRIC_LOOKUP_TIME";
    public static final String INSTANT_LYRICS_ENABLED = "INSTANT_LYRICS_ENABLED";

    UserData(Context context) {
        this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);;
    }

    UserData(SharedPreferences sharedPreferences) {
        this.sharedPreferences = sharedPreferences;
    }

    private SharedPreferences getSharedPreferences() {
        return sharedPreferences;
    }

    public void setLatestLyricLookupTime(Long latestLyricLookupTime) {
        getSharedPreferences()
                .edit()
                .putLong(LATEST_LYRIC_LOOKUP_TIME, latestLyricLookupTime)
                .apply();
    }

    public Long getLatestLyricLookupTime() {
        return getSharedPreferences()
                .getLong(LATEST_LYRIC_LOOKUP_TIME, -1);
    }

    public void setInstantLyricsEnabled(boolean instantLyricsEnabled) {
        getSharedPreferences()
                .edit()
                .putBoolean(INSTANT_LYRICS_ENABLED, instantLyricsEnabled)
                .apply();
    }

    public boolean getInstantLyricsEnabled() {
        return getSharedPreferences()
                .getBoolean(INSTANT_LYRICS_ENABLED, true);
    }
}
