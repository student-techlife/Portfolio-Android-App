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
import com.iiatimd.portfolioappv2.Entities.Project;
import com.iiatimd.portfolioappv2.HomeActivity;
import com.iiatimd.portfolioappv2.Network.RetrofitBuilder;
import com.iiatimd.portfolioappv2.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import okhttp3.Request;

public class HomeFragment extends Fragment {
    
    private View view;
    public static RecyclerView recyclerView;
    public static ArrayList<Project> arrayList;
    private SwipeRefreshLayout refreshLayout;
//    private ProjectAdapter projectAdapter;
    private SharedPreferences sharedPreferences;

    private static final String TAG = "HomeFragment";

    public HomeFragment(){}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.layout_home,container,false);
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

        ((HomeActivity)getActivity()).getProjects();

        // Swipe down voor een refresh
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                ((HomeActivity)getActivity()).getProjects();
            }
        });
    }

//    private void getPosts() {
//        arrayList = new ArrayList<>();
//        refreshLayout.setRefreshing(true);
//
//        StringRequest request = new StringRequest(Request.Method.GET, RetrofitBuilder.PROJECTS, response -> {
//
//            try {
//                JSONObject object = new JSONObject(response);
//                if (object.getBoolean("success")){
//                    JSONArray array = new JSONArray(object.getString("projects"));
//                    for (int i = 0; i < array.length(); i++) {
//                        JSONObject projectObject = array.getJSONObject(i);
//                        JSONObject userObject = projectObject.getJSONObject("user");
//
//                        User user = new User();
//                        user.setId(userObject.getInt("id"));
//                        user.setUserName(userObject.getString("name")+" "+userObject.getString("lastname"));
//                        user.setPhoto(userObject.getString("photo"));
//
//                        Project project = new Project();
//                        project.setId(projectObject.getInt("id"));
//                        project.setUser(user);
//                        project.setLikes(projectObject.getInt("likesCount"));
//                        project.setComments(projectObject.getInt("commentsCount"));
//                        project.setDate(projectObject.getString("created_at"));
//                        project.setDesc(projectObject.getString("desc"));
//                        project.setPhoto(projectObject.getString("photo"));
//                        project.setSelfLike(projectObject.getBoolean("selfLike"));
//
//                        arrayList.add(project);
//                    }
//
//                    projectAdapter = new ProjectAdapter(Objects.requireNonNull(getContext()),arrayList);
//                    recyclerView.setAdapter(projectAdapter);
//                }
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//
//            refreshLayout.setRefreshing(false);
//
//        },error -> {
//            error.printStackTrace();
//            refreshLayout.setRefreshing(false);
//        }){
//
//            // provide token in header
//
//            @Override
//            public Map<String, String> getHeaders() throws AuthFailureError {
//                String token = sharedPreferences.getString("token","");
//                HashMap<String,String> map = new HashMap<>();
//                map.put("Authorization","Bearer "+token);
//                return map;
//            }
//        };
//
//        RequestQueue queue = Volley.newRequestQueue(Objects.requireNonNull(getContext()));
//        queue.add(request);
//    }
//
//    @Override
//    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
//        inflater.inflate(R.menu.menu_search,menu);
//        MenuItem item = menu.findItem(R.id.search);
//        SearchView searchView = (SearchView)item.getActionView();
//        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
//            @Override
//            public boolean onQueryTextSubmit(String query) {
//
//                return false;
//            }
//
//            @Override
//            public boolean onQueryTextChange(String newText) {
//                postsAdapter.getFilter().filter(newText);
//                return false;
//            }
//        });
//        super.onCreateOptionsMenu(menu, inflater);
//    }
}
