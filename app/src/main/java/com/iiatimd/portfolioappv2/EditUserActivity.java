package com.iiatimd.portfolioappv2;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.iiatimd.portfolioappv2.Entities.AccessToken;
import com.iiatimd.portfolioappv2.Entities.User;
import com.iiatimd.portfolioappv2.Network.ApiService;
import com.iiatimd.portfolioappv2.Network.RetrofitBuilder;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditUserActivity extends AppCompatActivity {

    private TextInputLayout layoutName,layoutLastname,layoutEmail;
    private TextInputEditText txtName,txtLastname,txtEmail;
    private CircleImageView circleImageView;
    private static final int GALLERY_ADD_PROFILE = 1;
    private Bitmap bitmap = null;
    private static final String TAG = "EditUserActivity";

    ApiService protectedService;
    TokenManager tokenManager;
    UserManager userManager;
    Call<AccessToken> call;
    Call<User> userCall;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_edit_profile);
        tokenManager = TokenManager.getInstance(getSharedPreferences("prefs", MODE_PRIVATE));
        userManager = UserManager.getInstance(getSharedPreferences("user", MODE_PRIVATE));
        protectedService = RetrofitBuilder.createServiceWithAuth(ApiService.class, tokenManager);
        init();
    }

    private void init() {
        // Getters
        layoutLastname = findViewById(R.id.txtLayoutLastnameameUserInfo);
        layoutName = findViewById(R.id.txtLayoutNameUserInfo);
        layoutEmail = findViewById(R.id.txtLayoutEmail);
        txtEmail = findViewById(R.id.txtEmail);
        txtName = findViewById(R.id.txtNameUserInfo);
        txtLastname = findViewById(R.id.txtLastnameUserInfo);
        Button btnSave = findViewById(R.id.btnSave);
        TextView txtSelectPhoto = findViewById(R.id.txtSelectPhoto);
        circleImageView = findViewById(R.id.imgUserInfo);
        SharedPreferences preferences = getApplicationContext().getSharedPreferences("user", Context.MODE_PRIVATE);
        String photo = preferences.getString("photo", "");

        // Setters
        txtName.setText(preferences.getString("name", null));
        txtEmail.setText(preferences.getString("email", null));
        txtLastname.setText(preferences.getString("lastname", null));
        if (photo != null) {
            Picasso.get().load(RetrofitBuilder.URL + "profiles/" + photo).into(circleImageView);
        }

        btnSave.setOnClickListener(v->{
            if (validate()) {
                saveUserInfo();
            }
        });

        txtSelectPhoto.setOnClickListener(v->{
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, GALLERY_ADD_PROFILE);
        });
    }

    private void saveUserInfo() {
        String name = Objects.requireNonNull(txtName.getText()).toString();
        String lastname = Objects.requireNonNull(txtLastname.getText()).toString();
        String email = Objects.requireNonNull(txtEmail.getText()).toString();
        String photo = convertToString(bitmap);

        call = protectedService.edit_user_info(name, lastname, email, photo);
        call.enqueue(new Callback<AccessToken>() {
            @Override
            public void onResponse(@NotNull Call<AccessToken> call, @NotNull Response<AccessToken> response) {
                if (response.isSuccessful()) {
                    user();
                    startActivity(new Intent(EditUserActivity.this, HomeActivity.class));
                    finish();
                }
            }

            @Override
            public void onFailure(@NotNull Call<AccessToken> call, @NotNull Throwable t) {

            }
        });
    }

    void user() {
        userCall = protectedService.user();
        userCall.enqueue(new Callback<User>() {
            @Override
            public void onResponse(@NotNull Call<User> call, @NotNull Response<User> response) {
                if (response.isSuccessful()) {
                    assert response.body() != null;
                    userManager.saveUser(response.body());
                }
            }

            @Override
            public void onFailure(@NotNull Call<User> call, @NotNull Throwable t) {

            }
        });
    }

    private boolean validate() {
        if (Objects.requireNonNull(txtName.getText()).toString().isEmpty()) {
            layoutName.setErrorEnabled(true);
            layoutName.setError("Naam is verplicht");
            return false;
        }
        if (Objects.requireNonNull(txtLastname.getText()).toString().isEmpty()) {
            layoutLastname.setErrorEnabled(true);
            layoutLastname.setError("Achternaam is verplicht");
            return false;
        }
        if (Objects.requireNonNull(txtEmail.getText()).toString().isEmpty()) {
            layoutEmail.setErrorEnabled(true);
            layoutEmail.setError("Email is verplicht");
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLERY_ADD_PROFILE && resultCode == RESULT_OK) {
            assert data != null;
            Uri imgUri = data.getData();
            circleImageView.setImageURI(imgUri);

            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imgUri);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String convertToString(Bitmap bitmap) {
        if (bitmap != null) {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 25, byteArrayOutputStream);
            byte [] array = byteArrayOutputStream.toByteArray();
            return Base64.encodeToString(array, Base64.DEFAULT);
        }

        return "";
    }
}