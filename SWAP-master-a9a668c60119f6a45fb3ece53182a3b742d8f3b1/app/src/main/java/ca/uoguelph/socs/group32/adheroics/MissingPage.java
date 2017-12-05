package ca.uoguelph.socs.group32.adheroics;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;

public class MissingPage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences app_settings = PreferenceManager.getDefaultSharedPreferences(this);

        String app_theme = app_settings.getString("app_theme", "light");
        Log.d("FOOBAR", "app_theme: " + app_theme);
        if (app_theme.equals("dark")){
            setTheme(R.style.AppTheme_Dark_Dialog);
        } else {
            setTheme(R.style.AppTheme_Light_Dialog);
        }
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_missing_page);
    }
}
