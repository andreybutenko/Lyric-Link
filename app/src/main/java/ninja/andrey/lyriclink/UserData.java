package ninja.andrey.lyriclink;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Andrey on 8/7/2017.
 */

public class UserData {
    private Context context;

    public static final String LYRIC_LINK_SETTINGS = "LYRIC_LINK_SETTINGS";
    public static final String LATEST_LYRIC_LOOKUP_TIME = "LATEST_LYRIC_LOOKUP_TIME";

    UserData(Context context) {
        this.context = context;
    }

    private SharedPreferences getSharedPreferences() {
        return context.getSharedPreferences(LYRIC_LINK_SETTINGS, context.MODE_PRIVATE);
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
}
