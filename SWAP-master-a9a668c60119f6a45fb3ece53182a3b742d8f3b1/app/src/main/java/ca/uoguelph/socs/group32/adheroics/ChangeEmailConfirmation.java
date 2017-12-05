package ca.uoguelph.socs.group32.adheroics;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import es.dmoral.toasty.Toasty;

public class ChangeEmailConfirmation extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_email_confirmation);

        final EditText curPass = findViewById(R.id.currentPass);
        final EditText newEmail = findViewById(R.id.newPass);
        final EditText confirmEmail = findViewById(R.id.confirmPass);
        final TextView emailMessage = findViewById(R.id.passMessage);

        final Button cancelButton = findViewById(R.id.cancelButton);
        final Button changeButton = findViewById(R.id.changeButton);

        cancelButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        });

        changeButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String email = newEmail.getText().toString();
                if (curPass.getText().toString().isEmpty()){
                    emailMessage.setText("You must enter your current password!");
                } else if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                    emailMessage.setText("The new email must be valid!");
                } else if (!email.equals(confirmEmail.getText().toString())){
                    emailMessage.setText("New emails do not match!");
                } else {
                    /* Equal*/
                    /* TODO: Actually save the email */
                    Toasty.success(getApplicationContext(), "Successfully set new email!", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        });
    }

}
