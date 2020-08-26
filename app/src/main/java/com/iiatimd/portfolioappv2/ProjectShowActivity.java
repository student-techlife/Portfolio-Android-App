package com.iiatimd.portfolioappv2;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.iiatimd.portfolioappv2.Adapters.ProjectsAdapter;
import com.iiatimd.portfolioappv2.Entities.ProjectResponse;
import com.iiatimd.portfolioappv2.Fragments.HomeFragment;
import com.iiatimd.portfolioappv2.Network.ApiService;
import com.iiatimd.portfolioappv2.Network.RetrofitBuilder;
import com.squareup.picasso.Picasso;

import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProjectShowActivity extends AppCompatActivity {

    private int id,aantalUur,position,projectUserId;
    private String photo,projectName,website,client,desc;
    private TextView txtAantalUur,txtOplevering,txtProjectName,txtWebsite,txtClient,txtDesc;
    private Button removeProject,changeProject;
    private ImageView projectImage;
    private SharedPreferences preferences;

    ApiService service;
    TokenManager tokenManager;
    Call<ProjectResponse> deleteProject;

    private static final String TAG = "ProjectShowActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_show);
        Intent intent=getIntent();
        id              = intent.getIntExtra("id", 0);
        photo           = intent.getStringExtra("photo");
        aantalUur       = intent.getIntExtra("aantalUur", 0);
        projectName     = intent.getStringExtra("projectName");
        website         = intent.getStringExtra("website");
        client          = intent.getStringExtra("client");
        desc            = intent.getStringExtra("desc");
        position        = intent.getIntExtra("position", 0);
        projectUserId   = intent.getIntExtra("projectUserId", 0);

        // Load views by ID's
        projectImage    = findViewById(R.id.imgShowProjectImage);
        txtAantalUur    = findViewById(R.id.txtShowAantalUur);
        txtOplevering   = findViewById(R.id.txtShowOplevering);
        txtProjectName  = findViewById(R.id.txtProjectTitle);
        txtWebsite      = findViewById(R.id.txtShowWebsite);
        txtClient       = findViewById(R.id.txtShowOpdrachtgever);
        txtDesc         = findViewById(R.id.txtShowDesc);
        changeProject   = findViewById(R.id.btnEditProject);
        removeProject   = findViewById(R.id.btnRemoveProject);

        tokenManager = TokenManager.getInstance(getSharedPreferences("prefs", MODE_PRIVATE));
        service = RetrofitBuilder.createServiceWithAuth(ApiService.class, tokenManager);

        preferences = getApplicationContext().getSharedPreferences("user", Context.MODE_PRIVATE);
        init();
    }

    private void init() {
//        Log.w(TAG, "onCreate: je hebt gekozen voor project: " + id + " " + aantalUur);

        // Load project image
        Picasso.get().load(RetrofitBuilder.URL + "projects/" + photo).into(projectImage);
        txtProjectName.setText(projectName);
        txtAantalUur.setText(String.valueOf(aantalUur));
        txtWebsite.setText(website);
        txtClient.setText(client);
        txtDesc.setText(desc);

        // Pas zichtbaarheid aan van de knoppen, zodat alleen de eigenaar dingen kan aanpassen
        // Project user ID == account ID
        if (projectUserId == preferences.getInt("id",0)) {
            removeProject.setVisibility(View.VISIBLE);
            changeProject.setVisibility(View.VISIBLE);
        } else {
            removeProject.setVisibility(View.GONE);
            changeProject.setVisibility(View.GONE);
        }

        // Wanneer je product wilt gaan aanpassen
        changeProject.setOnClickListener(v->{
            Intent intent = new Intent(getApplicationContext(), EditProjectActivity.class);
            intent.putExtra("id", id);
            intent.putExtra("projectName", projectName);
            intent.putExtra("website", website);
            intent.putExtra("client", client);
            intent.putExtra("aantalUur", aantalUur);
            intent.putExtra("desc", desc);
            intent.putExtra("photo", photo);
            intent.putExtra("position", position);
            startActivity(intent);
        });

        // Verwijder het project
        removeProject.setOnClickListener(v->{
            AlertDialog.Builder builder = new AlertDialog.Builder(ProjectShowActivity.this);
            builder.setTitle("Weet je het zeker?");
            builder.setMessage("Wanneer je bevestigd is er geen weg meer terug en wordt je project verwijderd.");
            builder.setPositiveButton("Verwijder", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int i) {
//                    Log.w(TAG, "onClick: Je wil verwijderen!");

                    // Maak een delete call naar backend API
                    deleteProject = service.delete_project(id);
                    deleteProject.enqueue(new Callback<ProjectResponse>() {
                        @Override
                        public void onResponse(Call<ProjectResponse> call, Response<ProjectResponse> response) {
                            // Verwijder project van arraylisy
                            HomeFragment.arrayList.remove(position);
                            Objects.requireNonNull(HomeFragment.recyclerViewHome.getAdapter()).notifyItemRemoved(position);
                            HomeFragment.recyclerViewHome.getAdapter().notifyDataSetChanged();
                            // Sluit activity af
                            finish();
                        }

                        @Override
                        public void onFailure(Call<ProjectResponse> call, Throwable t) {
                            Log.w(TAG, "onFailure: " + t.getMessage() );
                        }
                    });
                }
            });
            builder.setNegativeButton(R.string.btn_cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                }
            });
            builder.show();
        });
    }


    // Wanneer je terug wil gaan naar home scherm
    public void returnIndex(View view) {
        startActivity(new Intent(ProjectShowActivity.this, HomeActivity.class));
        finish();
    }
}