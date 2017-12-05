package ca.uoguelph.socs.group32.adheroics;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import es.dmoral.toasty.Toasty;

public class AdminAddMedicatonActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_add_medicaton);

    }
    public void loadSubmitMedication(View view) {
        Toasty.success(getApplicationContext(), "Successfully added the medication!", Toast.LENGTH_SHORT).show();
        finish();
    }
}
