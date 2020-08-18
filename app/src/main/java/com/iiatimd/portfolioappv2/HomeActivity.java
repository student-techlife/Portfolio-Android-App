package com.iiatimd.portfolioappv2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.iiatimd.portfolioappv2.Entities.AccessToken;
import com.iiatimd.portfolioappv2.Entities.ProjectResponse;
import com.iiatimd.portfolioappv2.Fragments.AccountFragment;
import com.iiatimd.portfolioappv2.Fragments.HomeFragment;
import com.iiatimd.portfolioappv2.Network.ApiService;
import com.iiatimd.portfolioappv2.Network.RetrofitBuilder;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeActivity extends AppCompatActivity {

    private FragmentManager fragmentManager;
    private FloatingActionButton fab;
    private BottomNavigationView navigationView;
    private static final String TAG = "HomeActivity";

    ApiService service;
    TokenManager tokenManager;
    Call<ProjectResponse> callProject;
    Call<AccessToken> callLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.frameHomeContainer,new HomeFragment(), HomeFragment.class.getSimpleName()).commit();

        tokenManager = TokenManager.getInstance(getSharedPreferences("prefs", MODE_PRIVATE));

        if (tokenManager.getToken() == null) {
            startActivity(new Intent(HomeActivity.this, LoginActivity.class));
            finish();
        }

        service = RetrofitBuilder.createServiceWithAuth(ApiService.class, tokenManager);
        init();
    }

    private void init() {
        navigationView = findViewById(R.id.bottom_nav);
        fab = findViewById(R.id.fab);

        navigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                switch (item.getItemId()) {
                    // Als je voor homepage kiest
                    case R.id.item_home: {
                        Fragment account = fragmentManager.findFragmentByTag(AccountFragment.class.getSimpleName());
                        if (account != null) {
                            // Verberg Account fragment
                            fragmentManager.beginTransaction().hide(fragmentManager.findFragmentByTag(AccountFragment.class.getSimpleName())).commit();
                            // Show Home fragment
                            fragmentManager.beginTransaction().show(fragmentManager.findFragmentByTag(HomeFragment.class.getSimpleName())).commit();
                            getProjects();
                        }
                        break;
                    }

                    // Als je voor account kiest
                    case R.id.item_account: {
                        Fragment account = fragmentManager.findFragmentByTag(AccountFragment.class.getSimpleName());
                        fragmentManager.beginTransaction().hide(fragmentManager.findFragmentByTag(HomeFragment.class.getSimpleName())).commit();
                        if (account!=null){
                            // Show account fragment
                            fragmentManager.beginTransaction().show(fragmentManager.findFragmentByTag(AccountFragment.class.getSimpleName())).commit();
                        }
                        else {
                            fragmentManager.beginTransaction().add(R.id.frameHomeContainer,new AccountFragment(),AccountFragment.class.getSimpleName()).commit();
                        }
                        break;
                    }
                }
                return true;
            }
        });
    }

    // Ophalen van projecten in de Laravel backend
    public void getProjects() {
        callProject = service.projects();
        callProject.enqueue(new Callback<ProjectResponse>() {
            @Override
            public void onResponse(Call<ProjectResponse> call, Response<ProjectResponse> response) {
                Log.w(TAG, "onResponse: " + response);

                if (response.isSuccessful()) {

                } else {
                    if (response.code() == 400) {
                        tokenManager.deleteToken();
                        startActivity(new Intent(HomeActivity.this, LoginActivity.class));
                        finish();
                    }
                }
            }

            @Override
            public void onFailure(Call<ProjectResponse> call, Throwable t) {
                Log.w(TAG, "onFailure: " + t.getMessage());
            }
        });
    }

    // Optie voor de user om uit te loggen
    public void logout() {
        callLogout = service.logout();
        callLogout.enqueue(new Callback<AccessToken>() {
            @Override
            public void onResponse(Call<AccessToken> call, Response<AccessToken> response) {
                Log.w(TAG, "onResponse: " + response);
                if (response.isSuccessful()) {
                    tokenManager.deleteToken();
                    startActivity(new Intent(HomeActivity.this, LoginActivity.class));
                    finish();
                }
            }

            @Override
            public void onFailure(Call<AccessToken> call, Throwable t) {
                Log.w(TAG, "onFailure: " + t.getMessage());
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Projects
        if (callProject != null) {
            callProject.cancel();
            callProject = null;
        }
        // Logout
        if (callLogout != null) {
            callLogout.cancel();
            callLogout = null;
        }
    }
}