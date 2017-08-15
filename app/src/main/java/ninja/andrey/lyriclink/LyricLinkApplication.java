package ninja.andrey.lyriclink;

import android.app.Application;
import android.content.Context;

/**
 * Created by Andrey on 8/15/2017.
 */

public class LyricLinkApplication extends Application {
    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
    }

    public static Context getContext() {
        return context;
    }
}
