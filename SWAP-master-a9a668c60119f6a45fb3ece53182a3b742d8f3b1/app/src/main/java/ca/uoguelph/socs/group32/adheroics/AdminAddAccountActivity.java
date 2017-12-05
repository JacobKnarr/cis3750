package ca.uoguelph.socs.group32.adheroics;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import es.dmoral.toasty.Toasty;

public class AdminAddAccountActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_add_account);

        Button add_account = (Button) findViewById(R.id.btn_signup);
        add_account.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toasty.success(getApplicationContext(), "Successfully added the account!", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

    }
}
