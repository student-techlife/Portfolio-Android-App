package com.iiatimd.portfolioappv2;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import com.basgeekball.awesomevalidation.AwesomeValidation;
import com.basgeekball.awesomevalidation.ValidationStyle;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.iiatimd.portfolioappv2.Entities.AccessToken;
import com.iiatimd.portfolioappv2.Entities.ApiError;
import com.iiatimd.portfolioappv2.Network.ApiService;
import com.iiatimd.portfolioappv2.Network.RetrofitBuilder;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    private TextInputLayout layoutEmail,layoutPassword,layoutConfirm;
    private TextInputEditText txtEmail,txtPassword,txtConfirm;
    private TextView txtSignIn;
    private Button btnSignUp;

    private static final String TAG = "RegisterActivity";

    ApiService service;
    Call<AccessToken> call;
    AwesomeValidation validator;
    TokenManager tokenManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        service = RetrofitBuilder.createService(ApiService.class);
        validator = new AwesomeValidation(ValidationStyle.TEXT_INPUT_LAYOUT);
        tokenManager = TokenManager.getInstance(getSharedPreferences("prefs", MODE_PRIVATE));

        layoutPassword = findViewById(R.id.txtLayoutPasswordSignUp);
        layoutEmail = findViewById(R.id.txtLayoutEmailSignUp);
        layoutConfirm = findViewById(R.id.txtLayoutConfrimSignUp);
        txtPassword = findViewById(R.id.txtPasswordSignUp);
        txtConfirm = findViewById(R.id.txtConfirmSignUp);
        txtSignIn = findViewById(R.id.txtSignIn);
        txtEmail = findViewById(R.id.txtEmailSignUp);
        btnSignUp = findViewById(R.id.btnSignUp);

        // Valideer input en start register functie
        btnSignUp.setOnClickListener(v->{
//            Log.w(TAG, "test");
            if (validate()) {
                register();
            }
        });

        // ga naar login scherm
        txtSignIn.setOnClickListener(v->{
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
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

        txtConfirm.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (txtConfirm.getText().toString().equals(txtPassword.getText().toString())){
                    layoutConfirm.setErrorEnabled(false);
                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    void register() {
        String email = txtEmail.getText().toString();
        String password = txtPassword.getText().toString();

        txtEmail.setError(null);
        txtPassword.setError(null);

        call = service.register(email, password);
        call.enqueue(new Callback<AccessToken>() {
            @Override
            public void onResponse(Call<AccessToken> call, Response<AccessToken> response) {
                Log.w(TAG, "onResponse: " + response );
                if(response.isSuccessful()) {
                    Log.w(TAG, "onResponse: " + response.body() );
                    // Sla tokens op
                    tokenManager.saveToken(response.body());
                    // Ga door naar User Info scherm
                    startActivity(new Intent(RegisterActivity.this, UserInfoActivity.class));
                    finish();
                } else {
                    handleErrors(response.errorBody());
                }

            }

            @Override
            public void onFailure(Call<AccessToken> call, Throwable t) {
                Log.w(TAG, "onFailure: " + t.getMessage() );
            }
        });
    }

    private boolean validate (){
        if (txtEmail.getText().toString().isEmpty()){
            layoutEmail.setErrorEnabled(true);
            layoutEmail.setError("Email is Required");
            return false;
        }
        if (txtPassword.getText().toString().length()<8){
            layoutPassword.setErrorEnabled(true);
            layoutPassword.setError("Required at least 8 characters");
            return false;
        }
        if (!txtConfirm.getText().toString().equals(txtPassword.getText().toString())){
            layoutConfirm.setErrorEnabled(true);
            layoutConfirm.setError("Password does not match");
            return false;
        }

        return true;
    }

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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (call != null) {
            call.cancel();
            call = null;
        }
    }

}