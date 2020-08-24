package com.iiatimd.portfolioappv2;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.iiatimd.portfolioappv2.Entities.Project;
import com.iiatimd.portfolioappv2.Entities.ProjectResponse;
import com.iiatimd.portfolioappv2.Entities.User;
import com.iiatimd.portfolioappv2.Fragments.HomeFragment;
import com.iiatimd.portfolioappv2.Network.ApiService;
import com.iiatimd.portfolioappv2.Network.RetrofitBuilder;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddProjectActivity extends AppCompatActivity {

    private ImageView imgProject;
    private EditText txtDescProject,txtProjectName,txtWebsite,txtOpdrachtgever, txtNumAantalUur;
    private TextView dateProjOplevering;
    private Button dateTextButton;
    private Bitmap bitmap = null;
    private static final int GALLERY_CHANGE_PROJECT = 3;
    private SharedPreferences sharedPreferences;


    private static final String TAG = "AddProjectActivity";

    ApiService protectedService;
    TokenManager tokenManager;
    Calendar calendar;
    DatePickerDialog datePickerDialog;
    Call<ProjectResponse> call;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_project);

        sharedPreferences = getApplicationContext().getSharedPreferences("user", MODE_PRIVATE);
        tokenManager = TokenManager.getInstance(getSharedPreferences("prefs", MODE_PRIVATE));
        protectedService = RetrofitBuilder.createServiceWithAuth(ApiService.class, tokenManager);

        init();
    }

    private void init() {

        Button btnProjectSave = findViewById(R.id.btnAddProject);

        // Project image view
        imgProject = findViewById(R.id.imgAddProject);

        // Basic
        txtProjectName = findViewById(R.id.txtProjectNaam);
        txtWebsite = findViewById(R.id.txtWebsite);
        txtOpdrachtgever = findViewById(R.id.txtOpdrachtgever);
        txtNumAantalUur = findViewById(R.id.txtNumAantalUur);

        // Beschrijving
        txtDescProject = findViewById(R.id.txtDescProject);

        // Date
        dateProjOplevering = findViewById(R.id.dateProjOplevering);
        dateTextButton = findViewById(R.id.selectProjectDate);

        // Datum picker (erg fancy)
        dateTextButton.setOnClickListener(v->{
            calendar = Calendar.getInstance();

            int day = calendar.get(Calendar.DAY_OF_MONTH);
            int month = calendar.get(Calendar.MONTH);
            int year = calendar.get(Calendar.YEAR);

            datePickerDialog = new DatePickerDialog(AddProjectActivity.this, (view1, year1, month1, dayOfMonth) -> dateProjOplevering.setText(dayOfMonth + "-" + (month1 +1) + "-" + year1), day, month, year);
            datePickerDialog.updateDate(year,month,day);
            datePickerDialog.show();
        });

//        // Image
//        imgProject.setImageURI(getIntent().getData());
//        try {
//            bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(),getIntent().getData());
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        // Check en start save functie
        // TODO misschien validation in aparte functie
        btnProjectSave.setOnClickListener(v->{
            if (!txtDescProject.getText().toString().isEmpty()) {
                saveProject();
            } else {
                Toast.makeText(this, "Project beschrijving is verplicht", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveProject() {

        String name = txtProjectName.getText().toString();
        String website = txtWebsite.getText().toString();
        String client = txtOpdrachtgever.getText().toString();
//        String completion_date = dateProjOplevering.getText().toString();
        String hours = txtNumAantalUur.getText().toString();
        String photo = convertToString(bitmap);
        String description = txtDescProject.getText().toString();

        call = protectedService.save_project(name,website,client,photo,hours,description);
        call.enqueue(new Callback<ProjectResponse>() {
            @Override
            public void onResponse(Call<ProjectResponse> call, Response<ProjectResponse> response) {
                Log.w(TAG, "onResponse: " + response );

//                Log.w(TAG, "onResponse: " + response.body().getData().getWebsite() );
                // Maak user object aan
                User user = new User();
                user.setId(sharedPreferences.getInt("id", 0));
                user.setName(sharedPreferences.getString("name", ""));
                user.setLastname(sharedPreferences.getString("lastname", ""));

                // Make project object aan
                Project project = new Project();
                project.setUser(user);
                project.setId(response.body().getData().getId());
                project.setPhoto(response.body().getData().getPhoto());
                project.setProjectName(response.body().getData().getProjectName());
                project.setWebsite(response.body().getData().getWebsite());
                project.setOpdrachtgever(response.body().getData().getOpdrachtgever());
                project.setAantalUur(response.body().getData().getAantalUur());
                project.setDesc(response.body().getData().getDesc());

//                Log.w(TAG, "onResponse: " + project.getWebsite());

                // Voeg project object toe aan arraylist in HomeFragment
                HomeFragment.arrayList.add(0,project);
                // Toon Toast dat aanmaken gelukt is.
                Toast.makeText(AddProjectActivity.this, "Project aangemaakt!", Toast.LENGTH_SHORT).show();
                Objects.requireNonNull(HomeFragment.recyclerViewHome.getAdapter()).notifyItemInserted(0);
                HomeFragment.recyclerViewHome.getAdapter().notifyDataSetChanged();
                finish();
            }

            @Override
            public void onFailure(Call<ProjectResponse> call, Throwable t) {
                Log.w(TAG, "onFailure: " + t.getMessage() );
            }
        });
    }

    // Wanneer je terug wil gaan naar home scherm
    public void cancelProject(View view) {
        startActivity(new Intent(AddProjectActivity.this, HomeActivity.class));
        finish();
    }

    // Change photo action
    public void changePhoto(View view) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, GALLERY_CHANGE_PROJECT);
    }

    private String convertToString(Bitmap bitmap) {
        if (bitmap != null) {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG,25, byteArrayOutputStream);
            byte[] imgByte = byteArrayOutputStream.toByteArray();
            return Base64.encodeToString(imgByte,Base64.DEFAULT);
        }
        return "";
    }

    // Change photo in view
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLERY_CHANGE_PROJECT && resultCode == RESULT_OK) {
            assert data != null;
            Uri imgUri = data.getData();
            imgProject.setImageURI(imgUri);

            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(),imgUri);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}