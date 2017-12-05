package ca.uoguelph.socs.group32.adheroics;
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
import com.android.volley.DefaultRetryPolicy;
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


public class SignupActivity extends AppCompatActivity {
    private static final String TAG = "SignupActivity";

    EditText _fnameText;
    EditText _lnameText;
    EditText _emailText;
    EditText _passwordText;
    EditText _confirmPasswordText;
    Button _signupButton;
    TextView _loginLink;

    private ProgressDialog progressDialog;

    private class SubProgressDialog extends ProgressDialog {
        public SubProgressDialog(Context context) {
            super(context);
        }
        public SubProgressDialog(Context context, int theme) {
            super(context, theme);
        }
        @Override
        public void onBackPressed() {
            onSignupFailed();
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

        setContentView(R.layout.activity_signup);

        _fnameText = findViewById(R.id.input_fname);
        _lnameText = findViewById(R.id.input_lname);
        _emailText = findViewById(R.id.input_email);
        _passwordText = findViewById(R.id.input_password);
        _confirmPasswordText = findViewById(R.id.input_confirm_password);
        _signupButton = findViewById(R.id.btn_signup);
        _loginLink = findViewById(R.id.link_login);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        final SubProgressDialog progressDialog;
        if (BaseActivity.currentTheme.equals("dark")){
            progressDialog = new SubProgressDialog(SignupActivity.this,
                    R.style.AppTheme_Dark_Dialog);
        } else {
            progressDialog = new SubProgressDialog(SignupActivity.this,
                    R.style.AppTheme_Light_Dialog);
        }
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Checking Login Status...");
        progressDialog.show();
        progressDialog.setCancelable(true);
        progressDialog.setCanceledOnTouchOutside(false);



        _signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signup();
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

    public void signup() {
        Log.d(TAG, "Signup");

        if (!validate()) {
            progressDialog.dismiss();
            onSignupFailed();
        }

        _signupButton.setEnabled(false);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Creating Account...");
        progressDialog.show();

        /* Signup Logic */
        Response.Listener<JSONObject> dataListener = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d(TAG, response.toString());
                try{
                    Boolean success = response.getBoolean("success");
                    if (success == false){
                        throw new AuthFailureError("Account Failure");
                    }
                    onSignupSuccess();
                } catch (JSONException | AuthFailureError e) {
                    onSignupFailed();
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
                        case 409:
                            res = new String(response.data);
                            Log.d(TAG, "res code: " + response.statusCode + " res: " + res);
                            Toasty.error(getApplicationContext(), "Account already exists for that email!", Toast.LENGTH_SHORT, true).show();
                            break;
                        default:
                            res = new String(response.data);
                            Log.d(TAG, "res code: " + response.statusCode + " res: " + res);
                            Toasty.error(getApplicationContext(), "Unable to Create Account", Toast.LENGTH_SHORT, true).show();
                            break;

                    }
                    //Additional cases
                } else {
                    Toasty.error(getApplicationContext(), "Please ensure you have an active internet connection!", Toast.LENGTH_SHORT, true).show();
                }
                onSignupFailed();
            }
        };

        JSONObject account_info = new JSONObject();
        try {
            account_info.put("fname", _fnameText.getText().toString());
            account_info.put("lname", _lnameText.getText().toString());
            account_info.put("email", _emailText.getText().toString());
            account_info.put("password", _passwordText.getText().toString());
        } catch (JSONException e){
            e.printStackTrace();
        }

        JsonObjectRequest jsonObjectRequest=new JsonObjectRequest(Request.Method.POST, Constants.SERVER_IP + Constants.PORT + "/api/auth/register",
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
        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                3000,
                5,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        NetworkAPI.getInstance(getApplicationContext()).addToRequestQueue(jsonObjectRequest);
    }

    public void onSignupSuccess() {
        Toasty.success(getApplicationContext(), getString(R.string.signup_succeed), Toast.LENGTH_SHORT, true).show();
        _signupButton.setEnabled(true);
        setResult(RESULT_OK, null);
        finish();
    }

    public void onSignupFailed() {
//        Toasty.error(getApplicationContext(), getString(R.string.signup_error), Toast.LENGTH_SHORT, true).show();
        _signupButton.setEnabled(true);
        setResult(RESULT_CANCELED, null);
    }

    public boolean validate() {
        boolean valid = true;

        String fname = _fnameText.getText().toString();
        String lname = _lnameText.getText().toString();
        String email = _emailText.getText().toString();
        String password = _passwordText.getText().toString();
        String confirmPassword = _confirmPasswordText.getText().toString();

        if (fname.isEmpty() || fname.length() < 2) {
            _fnameText.setError("First name must be at least 2 characters");
            valid = false;
        } else {
            _fnameText.setError(null);
        }

        if (lname.isEmpty() || lname.length() < 2) {
            _lnameText.setError("Last name must be at least 2 characters");
            valid = false;
        } else {
            _lnameText.setError(null);
        }

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _emailText.setError("Enter a valid email address");
            valid = false;
        } else {
            _emailText.setError(null);
        }

        if (password.isEmpty() || password.length() < 4) {
            _passwordText.setError("Must be greater than 4 alphanumeric characters");
            valid = false;
        } else {
            _passwordText.setError(null);
        }

        if (confirmPassword.isEmpty() || !password.equals(confirmPassword)) {
            _passwordText.setError("Passwords must be the same");
            _confirmPasswordText.setError("Passwords must be the same");
            valid = false;
        } else {
            _passwordText.setError(null);
            _confirmPasswordText.setError(null);
        }

        return valid;
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(progressDialog!=null && progressDialog.isShowing()){
            progressDialog.dismiss();
        }
    }
}