package ca.uoguelph.socs.group32.adheroics;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.LabelFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

public class AdherenceGraphsActivity extends BaseActivity {

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
        setContentView(R.layout.activity_adherence_graphs);

        //Call this toolbar code and it will load the proper toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        TextView text = (TextView) findViewById(R.id.toolbar_title);
        //Set your toolbar title to the correct page
        text.setText("Adherence Graphs");

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        SetGraph();

    }

    private void SetGraph() {

        GraphView graph = (GraphView) findViewById(R.id.graph);
        LineGraphSeries<DataPoint> series = new LineGraphSeries<DataPoint>(new DataPoint[] {
                new DataPoint(0, 1),
                new DataPoint(1, 3),
                new DataPoint(2, 0),
                new DataPoint(3, 2),
                new DataPoint(4, 4)
        });
        series.setColor(Color.GREEN);
        graph.addSeries(series);

        LineGraphSeries<DataPoint> series2 = new LineGraphSeries<>(new DataPoint[] {
                new DataPoint(0, 3),
                new DataPoint(1, 4),
                new DataPoint(2, 5),
                new DataPoint(3, 0),
                new DataPoint(4, 1)
        });
        graph.addSeries(series2);

    }

    public void loadDashboard(View view) {
//        Intent intent = new Intent(this, Dashboard.class);
//        startActivity(intent);
        finish();
    }

}
