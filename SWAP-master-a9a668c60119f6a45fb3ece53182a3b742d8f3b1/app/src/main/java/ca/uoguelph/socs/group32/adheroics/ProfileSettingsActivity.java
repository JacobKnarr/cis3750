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
import android.widget.TextView;

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
public class ProfileSettingsActivity extends AppCompatPreferenceActivity {
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

        addPreferencesFromResource(R.xml.pref_profile_settings);
        PreferenceManager.setDefaultValues(this, R.xml.pref_profile_settings, false);

        Preference edit_profile_image_preference = findPreference("edit_profile_image");
        edit_profile_image_preference.setOnPreferenceClickListener (new Preference.OnPreferenceClickListener(){
            public boolean onPreferenceClick(Preference preference){
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                int PICK_IMAGE = 1;
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
                return true;
            }
        });
        Preference edit_username_preference = findPreference("edit_username");
        edit_username_preference.setTitle(edit_username_preference.getTitle() + " | No Backend");
        edit_username_preference.setSummary(BaseActivity.active_user.getUsername());
        Preference edit_first_name_preference = findPreference("edit_first_name");
        edit_first_name_preference.setSummary(BaseActivity.active_user.getFirstName());
        Preference edit_last_name_preference = findPreference("edit_last_name");
        edit_last_name_preference.setSummary(BaseActivity.active_user.getLastName());
        Preference edit_phone_number_preference = findPreference("edit_phone_number");
        edit_phone_number_preference.setSummary(BaseActivity.active_user.getPhoneNumber());
        Preference edit_email_preference = findPreference("edit_email");
        edit_email_preference.setTitle(edit_email_preference.getTitle() + " | No Backend");
        edit_email_preference.setSummary(BaseActivity.active_user.getEmail());
        edit_email_preference.setOnPreferenceClickListener (new Preference.OnPreferenceClickListener(){
            public boolean onPreferenceClick(Preference preference){
                startActivity(new Intent(getApplicationContext(), ChangeEmailConfirmation.class));
                return true;
            }
        });
        Preference edit_password_preference = findPreference("edit_password");
        edit_password_preference.setTitle(edit_password_preference.getTitle() + " | No Backend");
        edit_password_preference.setSummary("*******");
        edit_password_preference.setOnPreferenceClickListener (new Preference.OnPreferenceClickListener(){
            public boolean onPreferenceClick(Preference preference){
                startActivity(new Intent(getApplicationContext(), ChangePasswordConfirmation.class));
                return true;
            }
        });
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);

        if (resultCode == RESULT_OK) {
            Uri selectedImage = imageReturnedIntent.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};

            Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String filePath = cursor.getString(columnIndex);
            cursor.close();

            /*TODO: Use this filePath to set the profile icon image (We don't need to worry about saving yet, just so long as the app is open */
            Log.d(TAG, "Image Selected! " + filePath);

        }
    }
}
