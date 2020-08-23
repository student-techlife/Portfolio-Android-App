package com.iiatimd.portfolioappv2;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.iiatimd.portfolioappv2.Entities.AccessToken;
import com.iiatimd.portfolioappv2.Entities.Project;
import com.iiatimd.portfolioappv2.Entities.ProjectCall;
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
    private static final int GALLERY_ADD_PROJECT = 2;
//    private SharedPreferences userPref;

    private static final String TAG = "HomeActivity";

    ApiService service;
    TokenManager tokenManager;
    UserManager userManager;
    Call<ProjectCall> callProject;
    Call<AccessToken> callLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.frameHomeContainer,new HomeFragment(), HomeFragment.class.getSimpleName()).commit();

        tokenManager = TokenManager.getInstance(getSharedPreferences("prefs", MODE_PRIVATE));
        userManager = UserManager.getInstance(getSharedPreferences("user", MODE_PRIVATE));

        // Als je geen tokens meer hebt, moet je weer opnieuw inloggen
        if (tokenManager.getToken() == null) {
            startActivity(new Intent(HomeActivity.this, LoginActivity.class));
            finish();
        }

        // Service waarbij access token automatisch wordt meegegeven
        service = RetrofitBuilder.createServiceWithAuth(ApiService.class, tokenManager);
        init();
    }

    public TokenManager getToken() {
        TokenManager token = TokenManager.getInstance(getSharedPreferences("prefs", MODE_PRIVATE));
        return token;
    }

    private void init() {
        navigationView = findViewById(R.id.bottom_nav);
        fab = findViewById(R.id.fab);

        fab.setOnClickListener(v->{
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, GALLERY_ADD_PROJECT);

            // TODO Misschien dit toch weglaten?
//            setContentView(R.layout.activity_add_project);
        });

//        userPref = getApplicationContext().getSharedPreferences("user", Context.MODE_PRIVATE);

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
//                            getProjects();
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

    // Optie voor de user om uit te loggen
    public void logout() {
        callLogout = service.logout(tokenManager.getToken());
        callLogout.enqueue(new Callback<AccessToken>() {
            @Override
            public void onResponse(Call<AccessToken> call, Response<AccessToken> response) {
                Log.w(TAG, "onResponse: " + response);
                if (response.isSuccessful()) {
//                    userPref.edit().clear().apply();

                    // Verwijder user en tokens uit memory
                    userManager.deleteUser();
                    tokenManager.deleteToken();
                    // Zet recyclerview op null, belangrijk anders ontstaat er een memory leak!
                    HomeFragment.recyclerViewHome = null;
                    // Keer terug naar LoginActivity
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
        // Logout
        if (callLogout != null) {
            callLogout.cancel();
            callLogout = null;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLERY_ADD_PROJECT && resultCode == RESULT_OK) {
            Uri imgUri = data.getData();
            Intent intent = new Intent(HomeActivity.this, AddProjectActivity.class);
            intent.setData(imgUri);
            startActivity(intent);
        }
    }
}