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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.iiatimd.portfolioappv2.Adapters.ProjectsAdapter;
import com.iiatimd.portfolioappv2.Entities.Project;
import com.iiatimd.portfolioappv2.Entities.ProjectCall;
import com.iiatimd.portfolioappv2.Entities.User;
import com.iiatimd.portfolioappv2.HomeActivity;
import com.iiatimd.portfolioappv2.Network.ApiService;
import com.iiatimd.portfolioappv2.Network.RetrofitBuilder;
import com.iiatimd.portfolioappv2.R;
import com.iiatimd.portfolioappv2.TokenManager;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {
    
    private View view;
    public static RecyclerView recyclerViewHome;
    public static ArrayList<Project> arrayList;
    private SwipeRefreshLayout refreshLayout;
    private ProjectsAdapter projectAdapter;
    private SharedPreferences sharedPreferences;
    private SharedPreferences projectPreferences;

    private static final String TAG = "HomeFragment";

    ApiService service;
//    TokenManager tokenManager;
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
        projectPreferences = getContext().getApplicationContext().getSharedPreferences("projects", Context.MODE_PRIVATE);
        sharedPreferences = Objects.requireNonNull(getContext()).getApplicationContext().getSharedPreferences("user", Context.MODE_PRIVATE);
        recyclerViewHome = view.findViewById(R.id.recyclerHome);
        recyclerViewHome.setHasFixedSize(true);
        recyclerViewHome.setLayoutManager(new LinearLayoutManager(getContext().getApplicationContext()));
        refreshLayout = view.findViewById(R.id.swipeHome);
        MaterialToolbar toolbar = view.findViewById(R.id.toolbarHome);
        ((HomeActivity)getContext()).setSupportActionBar(toolbar);
        setHasOptionsMenu(true);

        getProjects();

        // Swipe down voor een refresh van de pagina
        refreshLayout.setOnRefreshListener(() -> getProjects());
    }

    private void getProjects() {
        arrayList = new ArrayList<>();
        refreshLayout.setRefreshing(true);

        // Maak een API request call
        callProject = service.projects();
        callProject.enqueue(new Callback<ProjectCall>() {
            @Override
            public void onResponse(Call<ProjectCall> call, Response<ProjectCall> response) {
                Log.w(TAG, "onResponse: " + response);

//                Log.w(TAG, "onResponse: " + response.body().getProjects().get(2).getWebsite());
//                Log.w(TAG, "onResponse: " + response.body().getProjects());

                for (int i = 0; i < response.body().getProjects().toArray().length; i++) {

                    User user = new User();
                    user.setId(response.body().getProjects().get(i).getUser().getId());
                    user.setName(response.body().getProjects().get(i).getUser().getName());
                    user.setLastname(response.body().getProjects().get(i).getUser().getLastname());
                    user.setPhoto(response.body().getProjects().get(i).getUser().getPhoto());

                    Project project = new Project();
                    project.setUser(user);
                    project.setId(response.body().getProjects().get(i).getId());
                    project.setPhoto(response.body().getProjects().get(i).getPhoto());
                    project.setProjectName(response.body().getProjects().get(i).getProjectName());
                    project.setWebsite(response.body().getProjects().get(i).getWebsite());
                    project.setOpdrachtgever(response.body().getProjects().get(i).getOpdrachtgever());
                    project.setAantalUur(response.body().getProjects().get(i).getAantalUur());
                    project.setDatumOplevering(response.body().getProjects().get(i).getDatumOplerving());
                    project.setDate(response.body().getProjects().get(i).getDate());
                    project.setDesc(response.body().getProjects().get(i).getDesc());

                    arrayList.add(project);
                }
                saveData();
                projectAdapter = new ProjectsAdapter(Objects.requireNonNull(getContext()), arrayList);
                recyclerViewHome.setAdapter(projectAdapter);

//                Log.w(TAG, "onResponse: " + arrayList );
            }

            @Override
            public void onFailure(Call<ProjectCall> call, Throwable t) {
                Log.w(TAG, "onFailure: " + t.getMessage() );
                Log.w(TAG, "onFailure: Project data wordt uit geheugen gehaald" );

                Gson gson = new Gson();
                String json = projectPreferences.getString("projects", null);
                Type type = new TypeToken<ArrayList<Project>>() {}.getType();
                arrayList = gson.fromJson(json, type);

                projectAdapter = new ProjectsAdapter(Objects.requireNonNull(getContext().getApplicationContext()), arrayList);
                recyclerViewHome.setAdapter(projectAdapter);

                // Laat weten dat je offline bent en dus data uit geheugen ziet
                Toast.makeText(getActivity().getApplicationContext(), "Je bent offline. Data is niet up to date en wordt opgehaald uit geheugen", Toast.LENGTH_LONG).show();
            }
        });
        refreshLayout.setRefreshing(false);
    }

    private void saveData() {
        Log.w(TAG, "saveData: Data word opgeslagen");
        SharedPreferences.Editor editor = projectPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(arrayList);
        editor.putString("projects", json);
        editor.commit();
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

    @Override
    // NOTE Wordt enkel gebruikt als hidden state veranderd of de fragment
    public void onHiddenChanged(boolean hidden) {

        if (!hidden){
            getProjects();
        }

        super.onHiddenChanged(hidden);
    }

    @Override
    public void onResume() {
        super.onResume();
        getProjects();
    }
}
