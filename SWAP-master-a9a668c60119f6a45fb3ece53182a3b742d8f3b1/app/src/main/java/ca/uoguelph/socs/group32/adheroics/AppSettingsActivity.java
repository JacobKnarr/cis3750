package ca.uoguelph.socs.group32.adheroics;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class AppSettingsActivity extends AppCompatPreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String TAG = "ProfileSettings";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        SharedPreferences app_settings = PreferenceManager.getDefaultSharedPreferences(this);

        String app_theme = app_settings.getString("app_theme", "light");
        if (app_theme.equals("dark")){
            setTheme(R.style.AppTheme_Dark_Dialog);
            BaseActivity.currentTheme = "dark";
        } else {
            setTheme(R.style.AppTheme_Light_Dialog);
            BaseActivity.currentTheme = "light";
        }
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_app_settings);
        PreferenceManager.setDefaultValues(this, R.xml.pref_app_settings, false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Set up a listener whenever a key changes
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Unregister the listener whenever a key changes
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,String key) {
        // do stuff
        if (key.equals("app_theme")){
            if (Dashboard.instance != null){
                Dashboard.instance.finish();
            }
            Intent intent = new Intent(getApplicationContext(), Dashboard.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK  | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        }
    }
}
