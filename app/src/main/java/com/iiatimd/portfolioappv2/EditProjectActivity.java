package com.iiatimd.portfolioappv2;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.iiatimd.portfolioappv2.Entities.Project;
import com.iiatimd.portfolioappv2.Entities.ProjectResponse;
import com.iiatimd.portfolioappv2.Fragments.HomeFragment;
import com.iiatimd.portfolioappv2.Network.ApiService;
import com.iiatimd.portfolioappv2.Network.RetrofitBuilder;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static java.security.AccessController.getContext;

public class EditProjectActivity extends AppCompatActivity {

    private ImageView imgProject,goBack;
    private int id,aantalUur,position;
    private String photo;
    private TextView dateEditProjOplevering;
    private Boolean backToDefault = false;
    private Bitmap bitmap = null;
    private Button saveButton,photoSelect,photoRemove,dateTextButton;
    private static final int GALLERY_SELECT_PROJECT = 2;
    private EditText txtProjectName,txtWebsite,txtClient,txtAantalUur,txtDesc;

    ApiService service;
    TokenManager tokenManager;
    Call<ProjectResponse> editProject;
    Calendar calendar;
    DatePickerDialog datePickerDialog;

    private static final String TAG = "EditProjectActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_project);

        Intent intent=getIntent();
        id              = intent.getIntExtra("id", 0);
        aantalUur       = intent.getIntExtra("aantalUur", 0);
        position        = intent.getIntExtra("position", 0);
        photo           = intent.getStringExtra("photo");

        tokenManager = TokenManager.getInstance(getSharedPreferences("prefs", MODE_PRIVATE));
        service = RetrofitBuilder.createServiceWithAuth(ApiService.class, tokenManager);

        // Start init function
        init();
    }

    @SuppressLint("SetTextI18n")
    private void init() {
        txtProjectName  = findViewById(R.id.txtEditProjectNaam);
        txtWebsite      = findViewById(R.id.txtEditWebsite);
        txtClient       = findViewById(R.id.txtEditOpdrachtgever);
        txtAantalUur    = findViewById(R.id.txtEditNumAantalUur);
        txtDesc         = findViewById(R.id.txtEditDescProject);
        saveButton      = findViewById(R.id.btnEditProjectSave);
        photoSelect     = findViewById(R.id.btnEditSelectPhoto);
        photoRemove     = findViewById(R.id.btnEditRemovePhoto);
        imgProject      = findViewById(R.id.imgEditProject);
        goBack          = findViewById(R.id.imgEditProjectGoBack);

        // Date
        dateEditProjOplevering  = findViewById(R.id.dateEditProjOplevering);
        dateTextButton          = findViewById(R.id.selectProjectDate);

        // Get variables from Adapter
        txtProjectName.setText(getIntent().getStringExtra("projectName"));
        txtWebsite.setText(getIntent().getStringExtra("website"));
        txtClient.setText(getIntent().getStringExtra("client"));
        dateEditProjOplevering.setText(getIntent().getStringExtra("opleverDatum"));
        txtAantalUur.setText(String.valueOf(aantalUur));
        txtDesc.setText(getIntent().getStringExtra("desc"));

        // Datum picker (erg fancy)
        dateTextButton.setOnClickListener(v->{
            calendar = Calendar.getInstance();

            int day = calendar.get(Calendar.DAY_OF_MONTH);
            int month = calendar.get(Calendar.MONTH);
            int year = calendar.get(Calendar.YEAR);

            datePickerDialog = new DatePickerDialog(EditProjectActivity.this, (view1, year1, month1, dayOfMonth) -> dateEditProjOplevering.setText(dayOfMonth + "-" + (month1 +1) + "-" + year1), day, month, year);
            datePickerDialog.updateDate(year,month,day);
            datePickerDialog.show();
        });

        // Load project image
        Picasso.get().load(RetrofitBuilder.URL + "projects/" + photo).into(imgProject);

        saveButton.setOnClickListener(v->{
            // Validation?
            saveProject();
        });

        goBack.setOnClickListener(v->{
            startActivity(new Intent(EditProjectActivity.this, HomeActivity.class));
        });

        photoSelect.setOnClickListener(v-> selectPhoto());

        photoRemove.setOnClickListener(v-> removePhoto());
    }

    // Selecteer een foto
    private void selectPhoto() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, GALLERY_SELECT_PROJECT);
    }

    // Verwijder een foto
    private void removePhoto() {
        imgProject.setImageResource(0);
        backToDefault = true;
        bitmap = null;
    }

    private void saveProject() {
        String name             = txtProjectName.getText().toString();
        String website          = txtWebsite.getText().toString();
        String client           = txtClient.getText().toString();
        String completion_date  = dateEditProjOplevering.getText().toString();
        String hours            = txtAantalUur.getText().toString();
        String photo            = convertToString(bitmap);
        String desc             = txtDesc.getText().toString();

        editProject = service.edit_project(id,name,website,client,completion_date,photo,hours,desc);
        editProject.enqueue(new Callback<ProjectResponse>() {
            @Override
            public void onResponse(@NotNull Call<ProjectResponse> call, @NotNull Response<ProjectResponse> response) {
                Log.w(TAG, "onResponse: " + response.body() );
                assert response.body() != null;
                Project project = HomeFragment.arrayList.get(position);

                // Waardes aanpassen in project
                project.setProjectName(name);
                project.setWebsite(website);
                project.setOpdrachtgever(client);
                project.setDatumOplevering(completion_date);
                project.setPhoto(response.body().getData().getPhoto());
                project.setDesc(desc);

                HomeFragment.arrayList.set(position,project);
                Objects.requireNonNull(HomeFragment.recyclerViewHome.getAdapter()).notifyItemChanged(position);
                HomeFragment.recyclerViewHome.getAdapter().notifyDataSetChanged();
                Toast.makeText(EditProjectActivity.this, "Project is aangepast", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(EditProjectActivity.this, HomeActivity.class));
                finish();
            }

            @Override
            public void onFailure(@NotNull Call<ProjectResponse> call, @NotNull Throwable t) {
                Log.w(TAG, "onFailure: " + t.getMessage() );
            }
        });
    }

    private String convertToString(Bitmap bitmap) {
        if (bitmap != null && !backToDefault) {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG,25, byteArrayOutputStream);
            byte[] imgByte = byteArrayOutputStream.toByteArray();
            return Base64.encodeToString(imgByte,Base64.DEFAULT);
        } if (backToDefault) {
            return "default";
        }
        return "empty";
    }

    // Change photo in view
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLERY_SELECT_PROJECT && resultCode == RESULT_OK) {
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