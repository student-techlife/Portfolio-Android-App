package com.iiatimd.portfolioappv2;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.iiatimd.portfolioappv2.Entities.AccessToken;
import com.iiatimd.portfolioappv2.Entities.ApiError;
import com.iiatimd.portfolioappv2.Network.ApiService;
import com.iiatimd.portfolioappv2.Network.RetrofitBuilder;

import java.util.List;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditProfilePasswordActivity extends AppCompatActivity {

    private TextInputLayout layoutHuidigPassword,layoutNieuwPassword,layoutBevestigNieuwWachtwoord;
    private TextInputEditText txtHuidigWachtwoord,txtNieuwWachtwoord,txtBevestigNieuwWachtwoord;
    private Button btnSavePassword;
    private static final String TAG = "EditProfilePasswordActi";

    ApiService service;
    Call<AccessToken> call;
    TokenManager tokenManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile_password);

        tokenManager = TokenManager.getInstance(getSharedPreferences("prefs", MODE_PRIVATE));
        service = RetrofitBuilder.createServiceWithAuth(ApiService.class, tokenManager);

        init();
    }

    private void init() {

        layoutHuidigPassword            = findViewById(R.id.txtLayoutHuidigPassword);
        layoutNieuwPassword             = findViewById(R.id.txtLayoutNieuwPassword);
        layoutBevestigNieuwWachtwoord   = findViewById(R.id.txtLayoutBevestigNieuwPassword);

        txtHuidigWachtwoord             = findViewById(R.id.txtHuidigPassword);
        txtNieuwWachtwoord              = findViewById(R.id.txtNieuwPassword);
        txtBevestigNieuwWachtwoord      = findViewById(R.id.txtBevestigNieuwPassword);

        btnSavePassword                 = findViewById(R.id.btnSavePassword);

        btnSavePassword.setOnClickListener(v->{
            if (validate()) {
                savePassword();
            }
        });

        txtHuidigWachtwoord.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!txtHuidigWachtwoord.getText().toString().isEmpty()) {
                    layoutHuidigPassword.setErrorEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        txtNieuwWachtwoord.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (txtNieuwWachtwoord.getText().toString().length()>7) {
                    layoutNieuwPassword.setErrorEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        txtBevestigNieuwWachtwoord.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (txtBevestigNieuwWachtwoord.getText().toString().equals(txtNieuwWachtwoord.getText().toString())) {
                    layoutBevestigNieuwWachtwoord.setErrorEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

    }

    private boolean validate() {
        if (txtHuidigWachtwoord.getText().toString().isEmpty()){
            layoutHuidigPassword.setErrorEnabled(true);
            layoutHuidigPassword.setError("Huidig wachtwoord is verplicht");
            return false;
        }
        if (txtNieuwWachtwoord.getText().toString().length()<8){
            layoutNieuwPassword.setErrorEnabled(true);
            layoutNieuwPassword.setError("Wachtwoord vereist minimaal 8 tekens");
            return false;
        }
        if (!txtBevestigNieuwWachtwoord.getText().toString().equals(txtNieuwWachtwoord.getText().toString())){
            layoutBevestigNieuwWachtwoord.setErrorEnabled(true);
            layoutBevestigNieuwWachtwoord.setError("Nieuwe wachtwoorden komen niet overeen");
            return false;
        }

        return true;
    }

    // Verander je wachtwoord
    private void savePassword() {
        String old_wachtwoord       = txtHuidigWachtwoord.getText().toString();
        String wachtwoord           = txtNieuwWachtwoord.getText().toString();
        String confirm_wachtwoord   = txtBevestigNieuwWachtwoord.getText().toString();

        txtHuidigWachtwoord.setError(null);
        txtNieuwWachtwoord.setError(null);
        txtBevestigNieuwWachtwoord.setError(null);

        call = service.change_password(old_wachtwoord,wachtwoord,confirm_wachtwoord);
        call.enqueue(new Callback<AccessToken>() {
            @Override
            public void onResponse(Call<AccessToken> call, Response<AccessToken> response) {
                Log.w(TAG, "onResponse: " + response );
                if (response.isSuccessful()) {
                    Toast.makeText(EditProfilePasswordActivity.this, "Wachtwoord succesvol aangepast", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(EditProfilePasswordActivity.this, HomeActivity.class));
                    finish();
                } else {
                    handleErrors(response.errorBody());
                }
            }

            @Override
            public void onFailure(Call<AccessToken> call, Throwable t) {

            }
        });

    }
    private void handleErrors(ResponseBody response) {
        ApiError apiError = Utils.converErrors(response);
        for (Map.Entry<String, List<String>> error : apiError.getErrors().entrySet()) {
            if (error.getKey().equals("old_password")) {
                layoutHuidigPassword.setError(error.getValue().get(0));
            }
        }
    }

}