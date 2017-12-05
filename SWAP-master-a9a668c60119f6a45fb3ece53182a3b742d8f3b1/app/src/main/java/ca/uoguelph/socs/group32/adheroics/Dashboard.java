package ca.uoguelph.socs.group32.adheroics;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.content.Intent;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.support.v7.widget.Toolbar;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import ca.uoguelph.socs.group32.adheroics.Resources.Constants;
import ca.uoguelph.socs.group32.adheroics.Resources.User;
import devs.mulham.horizontalcalendar.HorizontalCalendar;
import devs.mulham.horizontalcalendar.HorizontalCalendarListener;
import es.dmoral.toasty.Toasty;

public class Dashboard extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener {
    private static final String TAG = "Dashboard";
    public static BaseActivity instance = null;

    ImageView _messages;
    ImageView _notifications;

    private class SubProgressDialog extends ProgressDialog {
        public SubProgressDialog(Context context) {
            super(context);
        }
        public SubProgressDialog(Context context, int theme) {
            super(context, theme);
        }
        @Override
        public void onBackPressed() {
            /* Should cancel or something? */
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        instance = this;
        final SharedPreferences app_settings = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.OnSharedPreferenceChangeListener listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
                // Implementation
                Log.d(TAG, "key: " + key);
            }
        };

        app_settings.registerOnSharedPreferenceChangeListener(listener);

        String app_theme = app_settings.getString("app_theme", "light");
        Log.d(TAG, "set to: " + app_theme);
        if (app_theme.equals("dark")){
            setTheme(R.style.AppTheme_Dark);
            BaseActivity.currentTheme = "dark";
        } else {
            setTheme(R.style.AppTheme_Light);
            BaseActivity.currentTheme = "light";
        }
        super.onCreate(savedInstanceState);

        if (BaseActivity.user_level == Constants.IS_ADMIN) {
            setContentView(R.layout.activity_dashboard_admin);
        } else {
            setContentView(R.layout.activity_dashboard);
        }

        DownloadLatestUser(app_settings);

        //Call this toolbar code and it will load the proper toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        TextView text = (TextView) findViewById(R.id.toolbar_title);
        //Set your toolbar title to the correct page
        text.setText("Dashboard");

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        if (BaseActivity.user_level == Constants.IS_ADMIN) {
            Button edit_accounts = (Button) findViewById(R.id.edit_clc_account);
            edit_accounts.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    goToAdminEditAccountActivity();
                }
            });

            Button create_accounts = (Button) findViewById(R.id.create_clc_account);
            create_accounts.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    goToAdminAddAccountActivity();
                }
            });

            _messages = findViewById(R.id.messages_admin_dashboard);
            _notifications = findViewById(R.id.notifications_admin_dashboard);
        } else {
            //Horizontal Calendar Code

            /** end after 1 month from now */
            Calendar endDate = Calendar.getInstance();
            endDate.add(Calendar.MONTH, 1);

            /** start before 1 month from now */
            Calendar startDate = Calendar.getInstance();
            startDate.add(Calendar.MONTH, -1);

            HorizontalCalendar horizontalCalendar = new HorizontalCalendar.Builder(this, R.id.calendarView)
                    .startDate(startDate.getTime())
                    .endDate(endDate.getTime())
                    .build();

            horizontalCalendar.setCalendarListener(new HorizontalCalendarListener() {
                @Override
                public void onDateSelected(Date date, int position) {
                    //do something
                }


            });
            _messages = findViewById(R.id.messages_client_dashboard);
            _notifications = findViewById(R.id.notifications_client_dashboard);
            Button _contactSupportWorker = findViewById(R.id.contact_support_button);
            _contactSupportWorker.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(getApplicationContext(), MissingPage.class));
                }
            });
        }
        _messages.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), MissingPage.class));
            }
        });
        _notifications.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), MissingPage.class));
            }
        });
    }

    private void DownloadLatestUser(SharedPreferences prefs) {
        final SharedPreferences app_settings = prefs;
        final SubProgressDialog progressDialog;
        if (currentTheme.equals("dark")){
            progressDialog = new SubProgressDialog(Dashboard.this,
                    R.style.AppTheme_Dark_Dialog);
        } else {
            progressDialog = new SubProgressDialog(Dashboard.this,
                    R.style.AppTheme_Light_Dialog);
        }
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Loading User Info...");
        progressDialog.show();
        progressDialog.setCancelable(true);
        progressDialog.setCanceledOnTouchOutside(false);

        /* Authentication Logic */
        Response.Listener<JSONObject> dataListener = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d(TAG, response.toString());
                try{
                    String email = response.getString("email");
                    String fname = response.getString("fname");
                    String lname = response.getString("lname");
                    BaseActivity.active_user = new User(fname, lname, "FOO", email, "123-456-7890");
                } catch (JSONException e) {
                    Toasty.info(getApplicationContext(), "Failed to load user info, will try again!", Toast.LENGTH_SHORT, true).show();
                    e.printStackTrace();
                }
                progressDialog.dismiss();
            }
        };
        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressDialog.dismiss();
                Log.d("Error", "Error:" + error.getMessage());
                NetworkResponse response = error.networkResponse;
                if (response != null && response.data != null) {
                    Log.d("Error", "Error reponse:" + response.statusCode);
                    String res;
                    switch (response.statusCode) {
                        case 401:
                            res = new String(response.data);
                            Log.d(TAG, "res code: " + response.statusCode + " res: " + res);
                            Toasty.error(getApplicationContext(), "Failed to load user info, will try again!", Toast.LENGTH_SHORT, true).show();
                            break;
                    }
                    //Additional cases
                } else {
                    Toasty.error(getApplicationContext(), "Please ensure you have an active internet connection!", Toast.LENGTH_SHORT, true).show();
                }
            }
        };

        JsonObjectRequest jsonObjectRequest=new JsonObjectRequest(Request.Method.GET, Constants.SERVER_IP + Constants.PORT + "/api/auth/me",
                null, // Data
                dataListener, // onSuccess
                errorListener // onError
        ) {
            /**
             * Passing some request headers
             */
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                final String auth_token = app_settings.getString("Auth_Token", "");
                headers.put("x-access-token", auth_token);
                return headers;
            }
        };
        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                3000,
                5,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        NetworkAPI.getInstance(getApplicationContext()).addToRequestQueue(jsonObjectRequest);
    }

    public void loadAdherenceGraphs(View view) {
        Intent intent = new Intent(this, AdherenceGraphsActivity.class);
        startActivity(intent);
    }

    public void loadClientSchedule(View view) {
        Intent intent = new Intent(this, ClientScheduleActivity.class);
        startActivity(intent);
    }


    private void goToAdminEditAccountActivity() {
        Intent intent = new Intent(this, AdminEditAccountActivity.class);
        startActivity(intent);
    }

    private void goToAdminAddAccountActivity() {
        Intent intent = new Intent(this, AdminAddAccountActivity.class);
        startActivity(intent);
    }




}
