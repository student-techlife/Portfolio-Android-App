package com.iiatimd.portfolioappv2.Fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Layout;
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
import com.iiatimd.portfolioappv2.Entities.Project;
import com.iiatimd.portfolioappv2.HomeActivity;
import com.iiatimd.portfolioappv2.R;

import java.util.ArrayList;

import javax.annotation.Nullable;

import de.hdodenhof.circleimageview.CircleImageView;

public class AccountFragment extends Fragment {

    private View view;
    private MaterialToolbar toolbar;
    private CircleImageView imgProfile;
    private TextView txtName,txtPostsCount;
    private Button btnEditAccount;
    private RecyclerView recyclerView;
    private ArrayList<Project> arrayList;
    private SharedPreferences preferences;
//    private AccountPostAdapter adapter;
    private String imgUrl = "";

    public AccountFragment() {}

    @Nullable
    @Override
    public View onCreateView(@Nullable LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.layout_account, container, false);
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
        txtPostsCount = view.findViewById(R.id.txtAccountPostCount);
        recyclerView = view.findViewById(R.id.recyclerAccount);
        btnEditAccount = view.findViewById(R.id.btnEditAccount);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(),2));
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
            builder.setMessage("Do you want to logout?");
            builder.setPositiveButton("Logout", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
//                    logout();
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
            builder.show();
        }

        return super.onOptionsItemSelected(item);
    }
}
