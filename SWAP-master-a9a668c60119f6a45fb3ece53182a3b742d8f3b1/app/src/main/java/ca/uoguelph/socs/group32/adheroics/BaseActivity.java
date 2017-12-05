package ca.uoguelph.socs.group32.adheroics;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import ca.uoguelph.socs.group32.adheroics.Resources.Constants;
import ca.uoguelph.socs.group32.adheroics.Resources.User;
import es.dmoral.toasty.Toasty;

/**
 * Created by christophermarcotte on 2017-11-07.
 */

public class BaseActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    public static String currentTheme = "light";
    public static int user_level = Constants.IS_NOT_VALID;
    public static User active_user;

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.menu_profile_button) {

        }

        return super.onOptionsItemSelected(item);
    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_client_profile_settings) {
            startActivity(new Intent(this, ProfileSettingsActivity.class));

        } else if (id == R.id.nav_app_settings){
            startActivity(new Intent(this, AppSettingsActivity.class));

        } else if (id == R.id.nav_client_logout) {
            AlertDialog.Builder builder;
            if (currentTheme.equals("dark")){
                builder = new AlertDialog.Builder(this, R.style.AppTheme_Dark_Dialog);
            } else {
                builder = new AlertDialog.Builder(this, R.style.AppTheme_Light_Dialog);
            }
            builder.setTitle("Logout")
                    .setMessage("Are you sure?")
                    .setCancelable(true)
                    .setNegativeButton("Cancel", null)
                    .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent(BaseActivity.this, LoginActivity.class);
                    intent.setFlags(intent.getFlags() | Intent.FLAG_ACTIVITY_NO_HISTORY);

                    SharedPreferences app_settings = PreferenceManager.getDefaultSharedPreferences(BaseActivity.this);
                    SharedPreferences.Editor prefsEditor = app_settings.edit();
                    prefsEditor.putString("Auth_Token", "");
                    prefsEditor.commit();
                    BaseActivity.user_level = 0;

                    startActivity(new Intent(BaseActivity.this, LoginActivity.class));
                    finish();
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();

            TextView textView = (TextView) dialog.findViewById(android.R.id.message);
            textView.setTextSize(25);

            Button b = dialog.getButton(DialogInterface.BUTTON_NEGATIVE);
            b.setTextColor(getResources().getColor(R.color.red));

        } else if (id == R.id.nav_client_dashboard) {
            finish();
            startActivity(new Intent(this, Dashboard.class));

        } else {
            Toasty.warning(getApplicationContext(), "Woah! Unhandled ID Selected", Toast.LENGTH_SHORT).show();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
