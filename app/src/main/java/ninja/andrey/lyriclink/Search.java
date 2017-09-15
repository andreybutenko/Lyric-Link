package ninja.andrey.lyriclink;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Andrey on 8/6/2017.
 */

public class Search {
    private static final String TAG = "Search";

    private static List<SearchListener> searchListenerList = new ArrayList<>();

    public static Intent getLyricIntent(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        return intent;
    }

    public static void loadLyricsUrl(String title, String artist) {
        new SearchTask().execute(title, artist);
    }

    public static String getPreferredUrl(List<String> searchResults) {
        for(Provider provider : Provider.values()) {
            if(provider == Provider.DEFAULT) {
                UserData userData = new UserData(LyricLinkApplication.getContext());
                Provider preferredProvider = userData.getPreferredProvider();

                if(preferredProvider == Provider.DEFAULT)
                    continue;
                else
                    provider = preferredProvider;
            }

            for(String url : searchResults) {
                if(url.contains(provider.getUrl())) {
                    return url;
                }
            }
        }

        return searchResults.get(0);
    }

    static class SearchTask extends AsyncTask<String, Void, List<String>> {
        @Override
        protected List<String> doInBackground(String... params) {
            String title = params[0];
            String artist = params[1];

            List<String> searchResults =  new LinkedList<>();

            Document doc = null;
            try {
                doc = Jsoup.connect("https://duckduckgo.com/html/?q=" + title + " " + artist + " lyrics").get();
                Elements result = doc.select(".result__url");
                for(Element link : result) {
                    String rawUrl = URLDecoder.decode(link.attr("href"), "UTF-8");
                    String[] urlParts = rawUrl.split("(http|https)://");
                    String url = "http://" + urlParts[urlParts.length - 1];
                    searchResults.add(url);
                }
            } catch (IOException e) {
                e.printStackTrace();
                searchResults.add("https://duckduckgo.com/html/?q=" + title + " " + artist + " lyrics");
            }

            return searchResults;
        }

        @Override
        protected void onPostExecute(List<String> result) {
            String preferredUrl = getPreferredUrl(result);
            Search.notifyListeners(preferredUrl);
        }
    }

    // Interface

    public interface SearchListener {
        void onSearchUrlLoaded(String url);
    }

    public static void addListener(SearchListener searchListener) {
        searchListenerList.add(searchListener);
    }

    public static void removeListener(CurrentSongService.SongListener songListener) {
        searchListenerList.remove(songListener);
    }

    public static boolean hasListener(CurrentSongService.SongListener songListener) {
        return searchListenerList.contains(songListener);
    }

    private static void notifyListeners(String url) {
        Log.d(TAG, "Preferred URL result: " + url);
        for(SearchListener searchListener : searchListenerList) {
            searchListener.onSearchUrlLoaded(url);
        }
        searchListenerList.clear();
    }
}
