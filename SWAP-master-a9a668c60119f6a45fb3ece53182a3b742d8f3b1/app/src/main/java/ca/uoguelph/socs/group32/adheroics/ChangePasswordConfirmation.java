package ca.uoguelph.socs.group32.adheroics;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.InsetDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import es.dmoral.toasty.Toasty;

public class ChangePasswordConfirmation extends AppCompatActivity {

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

        setContentView(R.layout.activity_password_confirmation);

        final EditText curPass = findViewById(R.id.currentPass);
        final EditText newPass = findViewById(R.id.newPass);
        final EditText confirmPass = findViewById(R.id.confirmPass);
        final TextView passMessage = findViewById(R.id.passMessage);

        final Button cancelButton = findViewById(R.id.cancelButton);
        final Button changeButton = findViewById(R.id.changeButton);

        cancelButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        });

        changeButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (curPass.getText().toString().isEmpty()){
                    passMessage.setText("You must enter your current password!");
                } else if (newPass.getText().toString().isEmpty()){
                    passMessage.setText("You must enter a new password!");
                } else if (!newPass.getText().toString().equals(confirmPass.getText().toString())){
                    passMessage.setText("New passwords do not match!");
                } else {
                    /* Equal*/
                    /* TODO: Actually save the password */
                    Toasty.success(getApplicationContext(), "Successfully set new password!", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        });
    }

}
