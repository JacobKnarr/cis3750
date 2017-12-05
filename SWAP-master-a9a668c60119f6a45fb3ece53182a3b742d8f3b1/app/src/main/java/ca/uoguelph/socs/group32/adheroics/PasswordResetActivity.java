package ca.uoguelph.socs.group32.adheroics;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import ca.uoguelph.socs.group32.adheroics.Resources.Constants;
import es.dmoral.toasty.Toasty;


public class PasswordResetActivity extends AppCompatActivity {
    private static final String TAG = "PasswordResetActivity";


    Dialog dialog;
    EditText _resetEmailText;
    Button _resetPasswordButton;
    TextView _loginLink;

    Button _resetCodeButton;
    TextView _codeCancelLink;
    EditText _resetCodeText;
    EditText _resetNewPass;
    EditText _resetConfirmPass;

    private class SubProgressDialog extends ProgressDialog {
        public SubProgressDialog(Context context) {
            super(context);
        }
        public SubProgressDialog(Context context, int theme) {
            super(context, theme);
        }
        @Override
        public void onBackPressed() {
            onResetFailed();
        }
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        SharedPreferences app_settings = PreferenceManager.getDefaultSharedPreferences(this);

        String app_theme = app_settings.getString("app_theme", "light");
        Log.d("FOOBAR", "app_theme: " + app_theme);
        if (app_theme.equals("dark")){
            setTheme(R.style.AppTheme_Dark);
        } else {
            setTheme(R.style.AppTheme_Light);
        }
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_password_reset);

        _resetEmailText = findViewById(R.id.reset_input_email);
        _resetPasswordButton = findViewById(R.id.btn_reset_password);
        _loginLink = findViewById(R.id.link_login);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        _resetPasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("FOO", "Reset Listener");
                sendPasswordResetCode();
                _resetPasswordButton.setEnabled(true);
            }
        });

        _loginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Finish the registration screen and return to the Login activity
                finish();
            }
        });
    }

    public boolean sendPasswordResetCode() {
        Log.d(TAG, "sendPasswordResetCode");

        if (!validate()) {
            return false;
        }

        _resetPasswordButton.setEnabled(false);

        final SubProgressDialog progressDialog;
        if (BaseActivity.currentTheme.equals("dark")){
            progressDialog = new SubProgressDialog(PasswordResetActivity.this,
                    R.style.AppTheme_Dark_Dialog);
        } else {
            progressDialog = new SubProgressDialog(PasswordResetActivity.this,
                    R.style.AppTheme_Light_Dialog);
        }
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Resetting Password...");
        progressDialog.show();
        progressDialog.setCancelable(true);
        progressDialog.setCanceledOnTouchOutside(false);

        /* Password Reset Logic */
        Response.Listener<JSONObject> dataListener = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d(TAG, response.toString());
                try{
                    Boolean success = response.getBoolean("success");
                    if (success == false){
                        throw new Error("Unable To reset password");
                    }
                    Toasty.success(getApplicationContext(), "If the account exists, a reset link was sent!", Toast.LENGTH_SHORT, true).show();
                    onResetSuccess();
                } catch (JSONException | Error e) {
                    onResetFailed();
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
                        case 404:
                            res = new String(response.data);
                            Log.d(TAG, "res code: " + response.statusCode + " res: " + res);
                            Toasty.success(getApplicationContext(), "If the account exists, an reset link was sent!", Toast.LENGTH_SHORT, true).show();
                            onResetSuccess();
                            return;
                        default:
                            res = new String(response.data);
                            Log.d(TAG, "res code: " + response.statusCode + " res: " + res);
                            Toasty.error(getApplicationContext(), "Password Reset Failed Due To Server Error", Toast.LENGTH_SHORT).show();
                            break;
                    }
                    //Additional cases
                } else {
                    Toasty.error(getApplicationContext(), "Please ensure you have an active internet connection!", Toast.LENGTH_SHORT, true).show();
                }
                onResetFailed();
            }
        };

        JSONObject account_info = new JSONObject();
        try {
            account_info.put("email", _resetEmailText.getText().toString());
        } catch (JSONException e){
            e.printStackTrace();
        }

        JsonObjectRequest jsonObjectRequest=new JsonObjectRequest(Request.Method.POST, Constants.SERVER_IP + Constants.PORT + "/api/auth/forgot",
                account_info, // Data
                dataListener, // onSuccess
                errorListener // onError
        ) {
            /**
             * Passing some request headers
             */
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Content-Type", "application/json; charset=utf-8");
                return headers;
            }

        };
        NetworkAPI.getInstance(getApplicationContext()).addToRequestQueue(jsonObjectRequest);
        return true;
    }


    public void onResetSuccess() {
        _resetPasswordButton.setEnabled(true);
        setResult(RESULT_OK, null);
    }

    public void onResetFailed() {
        _resetPasswordButton.setEnabled(true);
    }

    public boolean validate() {
        boolean valid = true;

        String email = _resetEmailText.getText().toString();

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _resetEmailText.setError("Enter a valid email address");
            valid = false;
        } else {
            _resetEmailText.setError(null);
        }

        return valid;
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}