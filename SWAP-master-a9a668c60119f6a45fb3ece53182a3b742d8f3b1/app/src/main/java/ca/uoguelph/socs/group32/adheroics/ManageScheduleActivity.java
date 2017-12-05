package ca.uoguelph.socs.group32.adheroics;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class ManageScheduleActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences app_settings = PreferenceManager.getDefaultSharedPreferences(this);

        String app_theme = app_settings.getString("app_theme", "light");
        Log.d("FOOBAR", "app_theme: " + app_theme);
        if (app_theme.equals("dark")){
            setTheme(R.style.AppTheme_Dark);
        } else {
            setTheme(R.style.AppTheme_Light);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_schedule);

        //Call this toolbar code and it will load the proper toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        TextView text = (TextView) findViewById(R.id.toolbar_title);
        //Set your toolbar title to the correct page
        text.setText("Schedules");

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        Button add_medication = (Button) findViewById(R.id.add_participant_schedule);
        add_medication.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToAddMedicationActivity();
            }
        });

        Button remove_medication = (Button) findViewById(R.id.remove_particpant_schedule);
        remove_medication.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToRemoveMedicationActivity();
            }
        });
        Button edit_medication_schedule = (Button) findViewById(R.id.edit_particpant_schedule);
        edit_medication_schedule.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), MissingPage.class));
            }
        });
        Button view_medication_schedule = (Button) findViewById(R.id.view_particpant_schedule);
        view_medication_schedule.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), MissingPage.class));
            }
        });
    }


    private void goToAddMedicationActivity() {
        Intent intent = new Intent(this, AdminAddMedicatonActivity.class);
        startActivity(intent);
    }

    private void goToRemoveMedicationActivity() {
        Intent intent = new Intent(this, RemoveMedicationActivity.class);
        startActivity(intent);
    }
}
