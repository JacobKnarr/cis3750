package ca.uoguelph.socs.group32.adheroics;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

import es.dmoral.toasty.Toasty;

public class RemoveMedicationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remove_medication);

        Button remove_medication = (Button) findViewById(R.id.remove_medication_button);
        remove_medication.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toasty.success(getApplicationContext(), "Successfully removed the Medication!", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }
}
