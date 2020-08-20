package com.iiatimd.portfolioappv2.Fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.appbar.MaterialToolbar;
import com.iiatimd.portfolioappv2.Adapters.ProjectsAdapter;
import com.iiatimd.portfolioappv2.Entities.Project;
import com.iiatimd.portfolioappv2.Entities.ProjectCall;
import com.iiatimd.portfolioappv2.Entities.User;
import com.iiatimd.portfolioappv2.HomeActivity;
import com.iiatimd.portfolioappv2.Network.ApiService;
import com.iiatimd.portfolioappv2.Network.RetrofitBuilder;
import com.iiatimd.portfolioappv2.R;
import com.iiatimd.portfolioappv2.TokenManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {
    
    private View view;
    public static RecyclerView recyclerView;
    public static ArrayList<Project> arrayList;
    private SwipeRefreshLayout refreshLayout;
    private ProjectsAdapter projectAdapter;
    private SharedPreferences sharedPreferences;

    private static final String TAG = "HomeFragment";

    ApiService service;
    TokenManager tokenManager;
    Call<ProjectCall> callProject;

    public HomeFragment(){}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.layout_home,container,false);

        service = RetrofitBuilder.createServiceWithAuth(ApiService.class, ((HomeActivity)getContext()).getToken());
        init();
        return view;
    }

    private void init(){
        sharedPreferences = Objects.requireNonNull(getContext()).getApplicationContext().getSharedPreferences("user", Context.MODE_PRIVATE);
        recyclerView = view.findViewById(R.id.recyclerHome);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        refreshLayout = view.findViewById(R.id.swipeHome);
        MaterialToolbar toolbar = view.findViewById(R.id.toolbarHome);
        ((HomeActivity)getContext()).setSupportActionBar(toolbar);
        setHasOptionsMenu(true);

        getProjects();

        // Swipe down voor een refresh van de pagina
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getProjects();
            }
        });
    }

    private void getProjects() {
        arrayList = new ArrayList<>();
        refreshLayout.setRefreshing(true);

//        ((HomeActivity) getActivity()).getProjects();
        callProject = service.projects();
        callProject.enqueue(new Callback<ProjectCall>() {
            @Override
            public void onResponse(Call<ProjectCall> call, Response<ProjectCall> response) {
                Log.w(TAG, "onResponse: " + response);

//                Log.w(TAG, "onResponse: " + response.body().getProjects().get(2).getWebsite());
//                Log.w(TAG, "onResponse: " + response.body().getProjects());

                for (int i = 0; i < response.body().getProjects().toArray().length; i++) {

                    User user = new User();
                    user.setId(sharedPreferences.getInt("id", 0));
                    user.setName(sharedPreferences.getString("name", ""));
                    user.setLastname(sharedPreferences.getString("lastname", ""));

                    Project project = new Project();
                    project.setUser(user);
                    project.setId(response.body().getProjects().get(i).getId());
                    project.setProjectName(response.body().getProjects().get(i).getProjectName());
                    project.setDate(response.body().getProjects().get(i).getDate());
                    project.setDesc(response.body().getProjects().get(i).getDesc());

                    arrayList.add(project);
                }
                projectAdapter = new ProjectsAdapter(Objects.requireNonNull(getContext()), arrayList);
                recyclerView.setAdapter(projectAdapter);

//                Log.w(TAG, "onResponse: " + arrayList );
            }

            @Override
            public void onFailure(Call<ProjectCall> call, Throwable t) {
                Log.w(TAG, "onFailure: " + t.getMessage() );
            }
        });
        refreshLayout.setRefreshing(false);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_search,menu);
        MenuItem item = menu.findItem(R.id.search);
        SearchView searchView = (SearchView)item.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                projectAdapter.getFilter().filter(newText);
                return false;
            }
        });
        super.onCreateOptionsMenu(menu, inflater);
    }
}
