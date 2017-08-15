package ninja.andrey.lyriclink;

/**
 * Created by Andrey on 8/15/2017.
 */

enum Provider {
    DEFAULT("Automatic", " "),
    GOOGLE_PLAY("Google Play", "play.google.com"),
    DARK_LYRICS("Dark Lyrics", "darklyrics.com"),
    AZ_LYRICS("AZ Lyrics", "azlyrics.com"),
    GENIUS("Genius", "genius.com"),
    SONG_LYRICS("Song Lyrics", "songlyrics.com");

    private String name;
    private String value;

    Provider(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String getUrl() {
        return this.value;
    }

    public String getName() {
        return this.name;
    }

    public static Provider getProviderByUrl(String url) {
        for(Provider provider : Provider.values()) {
            if(provider.getUrl().equals(url))
                return provider;
        }

        return DEFAULT;
    }
}
