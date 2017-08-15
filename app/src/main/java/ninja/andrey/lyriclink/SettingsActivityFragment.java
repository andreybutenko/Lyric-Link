package ninja.andrey.lyriclink;

import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.os.Bundle;
import android.util.Log;

/**
 * A placeholder fragment containing a simple view.
 */
public class SettingsActivityFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        UserData userData = new UserData(getPreferenceManager().getSharedPreferences());

        final Preference preferredProvider = findPreference(UserData.PREFERRED_PROVIDER);
        preferredProvider.setSummary(getPreferredProviderSummary(userData.getPreferredProvider()));

        preferredProvider.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                Provider newProvider = Provider.getProviderByUrl((String) newValue);
                preferredProvider.setSummary(getPreferredProviderSummary(newProvider));
                return true;
            }
        });
    }

    private String getPreferredProviderSummary(Provider provider) {
        Resources res = getResources();

        return provider != Provider.DEFAULT ?
                res.getString(R.string.preferred_provider_custom_desc, provider.getName())
                : res.getString(R.string.preferred_provider_default_desc);
    }
}
