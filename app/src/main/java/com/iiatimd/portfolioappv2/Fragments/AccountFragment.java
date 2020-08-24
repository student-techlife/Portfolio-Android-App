package com.iiatimd.portfolioappv2.Fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.iiatimd.portfolioappv2.EditUserActivity;
import com.iiatimd.portfolioappv2.Entities.AccessToken;
import com.iiatimd.portfolioappv2.Entities.Project;
import com.iiatimd.portfolioappv2.Entities.ProjectCall;
import com.iiatimd.portfolioappv2.Entities.User;
import com.iiatimd.portfolioappv2.HomeActivity;
import com.iiatimd.portfolioappv2.LoginActivity;
import com.iiatimd.portfolioappv2.Network.ApiService;
import com.iiatimd.portfolioappv2.Network.RetrofitBuilder;
import com.iiatimd.portfolioappv2.R;
import com.iiatimd.portfolioappv2.UserInfoActivity;
import com.iiatimd.portfolioappv2.UserManager;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

import javax.annotation.Nullable;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AccountFragment extends Fragment {

    private View view;
    private MaterialToolbar toolbar;
    private CircleImageView imgProfile;
    private TextView txtName,txtProjectsCount;
    private Button btnEditAccount;
    private RecyclerView recyclerViewAccount;
    private ArrayList<Project> arrayList;
    private SharedPreferences preferences;
//    private AccountPostAdapter adapter;
    private String imgUrl = "";

    ApiService service;
    UserManager userManager;
    Call<ProjectCall> callProject;

    private static final String TAG = "AccountFragment";

    public AccountFragment() {}

    @Nullable
    @Override
    public View onCreateView(@Nullable LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.layout_account, container, false);

        service = RetrofitBuilder.createServiceWithAuth(ApiService.class, ((HomeActivity)getContext()).getToken());
        init();
        return view;
    }

    private void init() {
        preferences = getContext().getSharedPreferences("user", Context.MODE_PRIVATE);
        toolbar = view.findViewById(R.id.toolbarAccount);
        ((HomeActivity)getContext()).setSupportActionBar(toolbar);
        setHasOptionsMenu(true);
        imgProfile = view.findViewById(R.id.imgAccountProfile);
        txtName = view.findViewById(R.id.txtAccountName);
        txtProjectsCount = view.findViewById(R.id.txtAccountProjectCount);
        recyclerViewAccount = view.findViewById(R.id.recyclerAccount);
        btnEditAccount = view.findViewById(R.id.btnEditAccount);
        btnEditAccount.setOnClickListener(v->{
            Intent i = new Intent(((HomeActivity)getContext()), EditUserActivity.class);
            i.putExtra("imgUrl",imgUrl);
            startActivity(i);
        });
        recyclerViewAccount.setHasFixedSize(true);
        recyclerViewAccount.setLayoutManager(new GridLayoutManager(getContext(),2));
    }

    private void getData() {
        arrayList = new ArrayList<>();

        // Set naam account
        txtName.setText(preferences.getString("name",null) +" "+ preferences.getString("lastname", null));
        // Set profiel foto
        Picasso.get().load(RetrofitBuilder.URL + "profiles/" + preferences.getString("photo", "")).into(imgProfile);

        callProject = service.myProjects();
        callProject.enqueue(new Callback<ProjectCall>() {
            @Override
            public void onResponse(Call<ProjectCall> call, Response<ProjectCall> response) {
                Log.w(TAG, "onResponse: " + response);

                for (int i = 0; i < response.body().getProjects().toArray().length; i++) {

                    User user = new User();
                    user.setId(preferences.getInt("id", 0));
                    user.setName(preferences.getString("name", ""));
                    user.setLastname(preferences.getString("lastname", ""));
                    user.setPhoto(preferences.getString("photo", ""));

                    Project project = new Project();
                    project.setUser(user);
                    project.setId(response.body().getProjects().get(i).getId());
                    project.setProjectName(response.body().getProjects().get(i).getProjectName());

                    arrayList.add(project);
                }
                // Save data?
                txtProjectsCount.setText(arrayList.size()+"");
            }

            @Override
            public void onFailure(Call<ProjectCall> call, Throwable t) {
                Log.w(TAG, "onFailure: " + t.getMessage() );
            }
        });


        // Set project counter
        txtProjectsCount.setText(HomeFragment.arrayList.size()+"");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        recyclerViewAccount.setAdapter(null);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_account,menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == R.id.item_logout) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setMessage(R.string.warn_logout);
            builder.setPositiveButton(R.string.btn_logout, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    ((HomeActivity)getActivity()).logout();
                }
            });
            builder.setNegativeButton(R.string.btn_cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
            builder.show();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    // NOTE Wordt enkel gebruikt als hidden state veranderd of de fragment
    public void onHiddenChanged(boolean hidden) {

        if (!hidden){
            getData();
        }

        super.onHiddenChanged(hidden);
    }

    @Override
    public void onResume() {
        super.onResume();
        getData();
    }
}
