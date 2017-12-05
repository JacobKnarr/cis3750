package ca.uoguelph.socs.group32.adheroics;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import android.content.Intent;
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
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import ca.uoguelph.socs.group32.adheroics.Resources.Constants;
import es.dmoral.toasty.Toasty;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private static final int REQUEST_SIGNUP = 0;
    private static final int REQUEST_RESET= 1;
    SharedPreferences app_settings;

    EditText _emailText;
    EditText _passwordText;
    Button _loginButton;
    TextView _signupLink;
    TextView _resetPasswordLink;
    String currentTheme;

    @Override
    public void onCreate(Bundle savedInstanceState) {
         app_settings = PreferenceManager.getDefaultSharedPreferences(this);

        RequestQueue initialQueue = NetworkAPI.getInstance(this.getApplicationContext()).
                getRequestQueue();

        initializeToasty();
        String app_theme = app_settings.getString("app_theme", "light");
        currentTheme = app_theme;
        if (app_theme.equals("dark")){
            setTheme(R.style.AppTheme_Dark);
            BaseActivity.currentTheme = "dark";
        } else {
            setTheme(R.style.AppTheme_Light);
            BaseActivity.currentTheme = "light";
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        _emailText = findViewById(R.id.input_email);
        _passwordText = findViewById(R.id.input_password);
        _loginButton = findViewById(R.id.btn_login);
        _signupLink = findViewById(R.id.link_signup);
        _resetPasswordLink = findViewById(R.id.link_password_reset);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        _loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });

        _signupLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start the Signup activity
                Intent intent = new Intent(getApplicationContext(), SignupActivity.class);
                startActivityForResult(intent, REQUEST_SIGNUP);
            }
        });
        _resetPasswordLink.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                // Start the Signup activity
                Intent intent = new Intent(getApplicationContext(), PasswordResetActivity.class);
                startActivityForResult(intent, REQUEST_RESET);
            }
        });

        final String auth_token = app_settings.getString("Auth_Token", "");
        Log.d(TAG, "Auth_Token: " + auth_token);
        if (!auth_token.toString().isEmpty()){

            final SubProgressDialog progressDialog;
            if (currentTheme.equals("dark")){
                progressDialog = new SubProgressDialog(LoginActivity.this,
                        R.style.AppTheme_Dark_Dialog);
            } else {
                progressDialog = new SubProgressDialog(LoginActivity.this,
                        R.style.AppTheme_Light_Dialog);
            }
            progressDialog.setIndeterminate(true);
            progressDialog.setMessage("Checking Login Status...");
            progressDialog.show();
            progressDialog.setCancelable(true);
            progressDialog.setCanceledOnTouchOutside(false);

        /* Authentication Logic */
            Response.Listener<JSONObject> dataListener = new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    Log.d(TAG, response.toString());
                    try{
                        Boolean auth = response.getBoolean("auth");
                        if (auth == false){
                            throw new AuthFailureError("Invalid Token");
                        }
                        String token = response.getString("token");
                        String scope = response.getString("scope");
                        Toasty.success(getApplicationContext(), "Already Signed In!", Toast.LENGTH_SHORT, true).show();
                        onLoginSuccess(token, scope);
                    } catch (JSONException | AuthFailureError e) {
                        Toasty.info(getApplicationContext(), "Session Expired!", Toast.LENGTH_SHORT, true).show();
                        onLoginFailed();
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
                                Toasty.error(getApplicationContext(), "Session Expired!", Toast.LENGTH_SHORT, true).show();
                                break;
                        }
                        //Additional cases
                    } else {
                        Toasty.error(getApplicationContext(), "Please ensure you have an active internet connection!", Toast.LENGTH_SHORT, true).show();
                    }
                    onLoginFailed();
                }
            };

            JsonObjectRequest jsonObjectRequest=new JsonObjectRequest(Request.Method.GET, Constants.SERVER_IP + Constants.PORT + "/api/auth/refresh",
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


        /* TODO: If Auth_Token is set, attempt to hit API to check if still valid session, if so, refresh and save new token, else, set token to "" */

    }

    private void initializeToasty() {
        Toasty.Config.reset();
        Toasty.Config.getInstance()
                .setErrorColor(getResources().getColor(R.color.red)) // optional
                .setInfoColor(getResources().getColor(R.color.CLCBlue)) // optional
                .setSuccessColor(getResources().getColor(R.color.CLCGreen)) //optional
                .setWarningColor(getResources().getColor(R.color.darkRed)) // optional
                .setTextColor(getResources().getColor(R.color.black)) // optional
                .tintIcon(false) // optional (apply textColor also to the icon)
                // .setToastTypeface(Typeface.DEFAULT)
                // .setTextSize(int sizeInSp) // optional
                .apply(); // required
    }

    private class SubProgressDialog extends ProgressDialog {
        public SubProgressDialog(Context context) {
            super(context);
        }
        public SubProgressDialog(Context context, int theme) {
            super(context, theme);
        }
        @Override
        public void onBackPressed() {
            onLoginFailed();
        }
    }

    public void login() {
        Log.d(TAG, "Login");

        if (!validate()) {
            onLoginFailed();
            return;
        }

        _loginButton.setEnabled(false);

        final SubProgressDialog progressDialog;
        if (currentTheme.equals("dark")){
            progressDialog = new SubProgressDialog(LoginActivity.this,
                    R.style.AppTheme_Dark_Dialog);
        } else {
            progressDialog = new SubProgressDialog(LoginActivity.this,
                    R.style.AppTheme_Light_Dialog);
        }
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Authenticating...");
        progressDialog.show();
        progressDialog.setCancelable(true);
        progressDialog.setCanceledOnTouchOutside(false);

        /* Authentication Logic */
        Response.Listener<JSONObject> dataListener = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d(TAG, response.toString());
                try{
                    Boolean auth = response.getBoolean("auth");
                    if (auth == false){
                        throw new AuthFailureError("Invalid Credentials");
                    }
                    String token = response.getString("token");
                    String scope = response.getString("scope");
                    Toasty.success(getApplicationContext(), "Successfully Signed In!", Toast.LENGTH_SHORT, true).show();
                    onLoginSuccess(token, scope);
                } catch (JSONException | AuthFailureError e) {
                    Toasty.error(getApplicationContext(), "Invalid Email/Password Combination", Toast.LENGTH_SHORT, true).show();
                    onLoginFailed();
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
                Log.d("Error", "Error reponse:" + response.statusCode);
                if(response != null && response.data != null){
                    String res;
                    switch(response.statusCode){
                        case 401:
                            res = new String(response.data);
                            Log.d(TAG, "res code: " + response.statusCode + " res: " + res);
                            Toasty.error(getApplicationContext(), "Invalid Email/Password Combination", Toast.LENGTH_SHORT, true).show();
                            break;
                    }
                    //Additional cases
                }
                onLoginFailed();
            }
        };
        JSONObject email_and_pass = new JSONObject();
        try {
            email_and_pass.put("email", _emailText.getText().toString());
            email_and_pass.put("password", _passwordText.getText().toString());
        } catch (JSONException e){
            e.printStackTrace();
        }

        JsonObjectRequest jsonObjectRequest=new JsonObjectRequest(Request.Method.POST, Constants.SERVER_IP + Constants.PORT + "/api/auth/login",
                email_and_pass, // Data
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


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_SIGNUP) {
            if (resultCode == RESULT_OK) {
                Toasty.success(getApplicationContext(), "Please Visit Email To Activate!", Toast.LENGTH_SHORT, true).show();
                /* Handled Externally */
            }
        } else if (requestCode == REQUEST_RESET) {
            if (resultCode == RESULT_OK) {
                Toasty.success(getApplicationContext(), "Please Visit Email To Reset!", Toast.LENGTH_SHORT, true).show();
                /* Handled Externally */
            }
        }
    }

    @Override
    public void onBackPressed() {
        // disable going back to the MainActivity
        moveTaskToBack(true);
    }

    public void onLoginSuccess(String token, String scope) {
        _loginButton.setEnabled(true);
        switch(scope){
            case "admin":
                BaseActivity.user_level = Constants.IS_ADMIN;
                break;
            case "sw":
                BaseActivity.user_level = Constants.IS_SW;
                break;
            case "user":
                BaseActivity.user_level = Constants.IS_USER;
                break;
            default:
                Toasty.info(getApplicationContext(), "Unknown User Permissions!", Toast.LENGTH_SHORT, true).show();
                break;
        }
        SharedPreferences.Editor prefsEditor = app_settings.edit();
        prefsEditor.putString("Auth_Token", token);
        prefsEditor.commit();
        startActivity(new Intent (this, Dashboard.class));
        finish();
    }

    public void onLoginFailed() {
        _loginButton.setEnabled(true);
    }

    public boolean validate() {
        boolean valid = true;

        String email = _emailText.getText().toString();
        String password = _passwordText.getText().toString();

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _emailText.setError("enter a valid email address");
            valid = false;
        } else {
            _emailText.setError(null);
        }

        if (password.isEmpty() || password.length() < 4) {
            _passwordText.setError("between 4 and 10 alphanumeric characters");
            valid = false;
        } else {
            _passwordText.setError(null);
        }

        return valid;
    }
}