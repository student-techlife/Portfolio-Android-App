package com.iiatimd.portfolioappv2;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ActivityOptions;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.basgeekball.awesomevalidation.AwesomeValidation;
import com.basgeekball.awesomevalidation.ValidationStyle;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.iiatimd.portfolioappv2.Entities.AccessToken;
import com.iiatimd.portfolioappv2.Entities.ApiError;
import com.iiatimd.portfolioappv2.Entities.User;
import com.iiatimd.portfolioappv2.Network.ApiService;
import com.iiatimd.portfolioappv2.Network.RetrofitBuilder;

import java.util.List;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private TextInputLayout layoutEmail,layoutPassword;
    private TextInputEditText txtEmail,txtPassword;
    private TextView txtSignUp;
    private Button btnSignIn;

    private static final String TAG = "SignInActivity";

    ApiService service;
    ApiService protectedService;
    TokenManager tokenManager;
    UserManager userManager;
    AwesomeValidation validator;
    Call<AccessToken> call;
    Call<User> userCall;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        service = RetrofitBuilder.createService(ApiService.class);
        validator = new AwesomeValidation(ValidationStyle.TEXT_INPUT_LAYOUT);
        tokenManager = TokenManager.getInstance(getSharedPreferences("prefs", MODE_PRIVATE));
        userManager = UserManager.getInstance(getSharedPreferences("user", MODE_PRIVATE));

        // Als er al tokens bekend zijn ga dan door naar home scherm
        if (tokenManager.getToken().getAccessToken() != null) {
            startActivity(new Intent(LoginActivity.this, HomeActivity.class));
            finish();
        }

        protectedService = RetrofitBuilder.createServiceWithAuth(ApiService.class, tokenManager);

        layoutPassword = findViewById(R.id.txtLayoutPasswordSignIn);
        layoutEmail = findViewById(R.id.txtLayoutEmailSignIn);
        txtPassword = findViewById(R.id.txtPasswordSignIn);
        txtSignUp = findViewById(R.id.txtSignUp);
        txtEmail = findViewById(R.id.txtEmailSignIn);
        btnSignIn = findViewById(R.id.btnSignIn);

        // Geen account? Ga naar registratie scherm
        txtSignUp.setOnClickListener(v->{
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });

        // Valideer input en start login functie
        btnSignIn.setOnClickListener(v->{
//            Log.w(TAG, "test");
            if (validate()) {
                login();
            }
        });

        txtEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!txtEmail.getText().toString().isEmpty()){
                    layoutEmail.setErrorEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        txtPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (txtPassword.getText().toString().length()>7){
                    layoutPassword.setErrorEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    void login() {

        String email = txtEmail.getText().toString();
        String password = txtPassword.getText().toString();

        txtEmail.setError(null);
        txtPassword.setError(null);

        call = service.login(email, password);
        call.enqueue(new Callback<AccessToken>() {
            @Override
            public void onResponse(Call<AccessToken> call, Response<AccessToken> response) {

                Log.w(TAG, "onResponse: " + response );
                if (response.isSuccessful()) {
                    tokenManager.saveToken(response.body());
                    Toast.makeText(LoginActivity.this, "Logging succesvol!", Toast.LENGTH_SHORT).show();
                    user();

                    // Ga naar home scherm na inloggen
                    startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                    finish();
                } else {
                    // Toon foutmelding als Toast
                    if (response.code() == 422) {
                        handleErrors(response.errorBody());
                    }
                    if (response.code() == 400) {
                        ApiError apiError = Utils.converErrors(response.errorBody());
                        Toast.makeText(LoginActivity.this, "Uw gegevens zijn onjuist. Probeer het a.u.b. opnieuw", Toast.LENGTH_LONG).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<AccessToken> call, Throwable t) {
                Log.w(TAG, "onFailure: " + t.getMessage() );
            }
        });
    }

    void user() {
        userCall = protectedService.user();
        userCall.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful()) {
                    userManager.saveUser(response.body());
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {

            }
        });
    }

    // Ontvang errors van Backend en toon deze onder het input field
    private void handleErrors(ResponseBody response) {
        ApiError apiError = Utils.converErrors(response);
        for(Map.Entry<String, List<String>> error : apiError.getErrors().entrySet()) {
            if (error.getKey().equals("email")) {
                layoutEmail.setError(error.getValue().get(0));
            }
            if (error.getKey().equals("password")) {
                layoutPassword.setError(error.getValue().get(0));
            }
        }
    }

    private boolean validate (){
        if (txtEmail.getText().toString().isEmpty()){
            layoutEmail.setErrorEnabled(true);
            layoutEmail.setError("Email is Required");
            return false;
        }
        if (txtPassword.getText().toString().isEmpty()){
            layoutPassword.setErrorEnabled(true);
            layoutPassword.setError("Password is Required");
            return false;
        }

        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (call != null) {
            call.cancel();
            call = null;
        }
        if (userCall != null) {
            userCall.cancel();
            userCall = null;
        }
    }
}