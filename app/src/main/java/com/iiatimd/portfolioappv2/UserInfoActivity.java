package com.iiatimd.portfolioappv2;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.iiatimd.portfolioappv2.Entities.AccessToken;
import com.iiatimd.portfolioappv2.Entities.User;
import com.iiatimd.portfolioappv2.Network.ApiService;
import com.iiatimd.portfolioappv2.Network.RetrofitBuilder;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserInfoActivity extends AppCompatActivity {

    private TextInputLayout layoutName,layoutLastname;
    private TextInputEditText txtName,txtLastname;
    private TextView txtSelectPhoto;
    private Button btnContinue;
    private CircleImageView circleImageView;

    private static final String TAG = "UserInfoActivity";

    ApiService protectedService;
    TokenManager tokenManager;
    UserManager userManager;
    Call<AccessToken> call;
    Call<User> userCall;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);

        tokenManager = TokenManager.getInstance(getSharedPreferences("prefs", MODE_PRIVATE));
        userManager = UserManager.getInstance(getSharedPreferences("user", MODE_PRIVATE));
        // Service waarbij access token automatisch wordt meegegeven
        protectedService = RetrofitBuilder.createServiceWithAuth(ApiService.class, tokenManager);

        init();
    }

    private void init() {
        layoutLastname = findViewById(R.id.txtLayoutLastnameameUserInfo);
        layoutName = findViewById(R.id.txtLayoutNameUserInfo);
        txtName = findViewById(R.id.txtNameUserInfo);
        txtLastname = findViewById(R.id.txtLastnameUserInfo);
        btnContinue = findViewById(R.id.btnContinue);
        circleImageView = findViewById(R.id.imgUserInfo);

        btnContinue.setOnClickListener(v->{
            if (validate()) {
                saveUserInfo();
            }
        });
    }

    private void saveUserInfo() {
        String name = txtName.getText().toString();
        String lastname = txtLastname.getText().toString();

        call = protectedService.save_user_info(name, lastname);
        call.enqueue(new Callback<AccessToken>() {
            @Override
            public void onResponse(Call<AccessToken> call, Response<AccessToken> response) {
                if (response.isSuccessful()) {
                    user();
                    startActivity(new Intent(UserInfoActivity.this, HomeActivity.class));
                    finish();
                }
            }

            @Override
            public void onFailure(Call<AccessToken> call, Throwable t) {

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

    private boolean validate() {
        if (txtName.getText().toString().isEmpty()) {
            layoutName.setErrorEnabled(true);
            layoutName.setError("Name is Required");
            return false;
        }
        if (txtLastname.getText().toString().isEmpty()) {
            layoutLastname.setErrorEnabled(true);
            layoutLastname.setError("Lastname is Required");
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