package com.iiatimd.portfolioappv2.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.iiatimd.portfolioappv2.Network.InternetCheck;

import com.iiatimd.portfolioappv2.EditProjectActivity;
import com.iiatimd.portfolioappv2.Entities.Project;
import com.iiatimd.portfolioappv2.Entities.ProjectResponse;
import com.iiatimd.portfolioappv2.HomeActivity;
import com.iiatimd.portfolioappv2.Network.ApiService;
import com.iiatimd.portfolioappv2.Network.RetrofitBuilder;
import com.iiatimd.portfolioappv2.ProjectShowActivity;
import com.iiatimd.portfolioappv2.R;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProjectsAdapter extends RecyclerView.Adapter<ProjectsAdapter.ProjectsHolder> {

    private Context context;
    private ArrayList<Project> list;
    private ArrayList<Project> listAll;
    private SharedPreferences preferences;

    private static final String TAG = "ProjectsAdapter";

    ApiService service;
    Call<ProjectResponse> deleteProject;
    InternetCheck internetCheck;

    public ProjectsAdapter(Context context, ArrayList<Project> list) {
        this.context = context;
        this.list = list;
        this.listAll = new ArrayList<>(list);
        preferences = context.getApplicationContext().getSharedPreferences("user",Context.MODE_PRIVATE);
    }

    @NonNull
    @Override
    public ProjectsHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_project,parent,false);
        internetCheck = new InternetCheck(context.getApplicationContext());

        if (internetCheck.isInternetAvailable()) {
            service = RetrofitBuilder.createServiceWithAuth(ApiService.class, ((HomeActivity)context).getToken());
        } else {
            service = RetrofitBuilder.createService(ApiService.class);
        }

        return new ProjectsHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull ProjectsHolder holder, int position) {
        Project project = list.get(position);
        // Get en show project image
        Picasso.get().load(RetrofitBuilder.URL + "projects/" + project.getPhoto()).into(holder.imgProject);
        // Get en show profiel image die hoort bij het project
        if (project.getUser().getPhoto() != null) {
            Picasso.get().load(RetrofitBuilder.URL + "profiles/" + project.getUser().getPhoto()).into(holder.imgProfile);
        }

        holder.txtName.setText(project.getProjectName());
        if (project.getDesc().length() > 45) {
            String desc = project.getDesc().substring(0, Math.min(project.getDesc().length(), 45));
            holder.txtDate.setText(desc.concat("..."));
        } else {
            holder.txtDate.setText(project.getDesc());
        }
//        holder.txtDate.setText(project.getDate());
//        holder.txtDesc.setText(project.getDesc());

        if (project.getUser().getId()==preferences.getInt("id",0)) {
            holder.btnProjectOption.setVisibility(View.VISIBLE);
        } else {
            holder.btnProjectOption.setVisibility(View.GONE);
        }

        holder.imgProject.setOnClickListener(v->{
            Log.w(TAG, "onBindViewHolder: Je hebt op een card geklikt " + project.getId());

            // Activity intent - Stuur data door naar Show pagina
            Intent intent = new Intent(v.getContext(),ProjectShowActivity.class);
            intent.putExtra("id", project.getId());
            intent.putExtra("projectName", project.getProjectName());
            intent.putExtra("website", project.getWebsite());
            intent.putExtra("client", project.getOpdrachtgever());
            intent.putExtra("aantalUur", project.getAantalUur());
            intent.putExtra("opleverDatum", project.getDatumOplerving());
            intent.putExtra("desc", project.getDesc());
            intent.putExtra("photo", project.getPhoto());
            intent.putExtra("position", position);
            intent.putExtra("projectUserId", project.getUser().getId());
            v.getContext().startActivity(intent);
        });

        if (internetCheck.isInternetAvailable()) {
            holder.btnProjectOption.setOnClickListener(v -> {
                PopupMenu popupMenu = new PopupMenu(context, holder.btnProjectOption);
                popupMenu.inflate(R.menu.menu_project_options);
                popupMenu.setOnMenuItemClickListener(item -> {

                    switch (item.getItemId()) {
                        case R.id.item_edit: {
                            Intent intent = new Intent(context, EditProjectActivity.class);
                            intent.putExtra("id", project.getId());
                            intent.putExtra("projectName", project.getProjectName());
                            intent.putExtra("website", project.getWebsite());
                            intent.putExtra("client", project.getOpdrachtgever());
                            intent.putExtra("aantalUur", project.getAantalUur());
                            intent.putExtra("opleverDatum", project.getDatumOplerving());
                            intent.putExtra("desc", project.getDesc());
                            intent.putExtra("photo", project.getPhoto());
                            intent.putExtra("position", position);
                            context.startActivity(intent);
                            return true;
                        }
                        case R.id.item_delete: {
                            deleteProject(project.getId(), position);
                            return true;
                        }
                    }
                    return false;
                });
                popupMenu.show();
            });
        }
    }

    // Verwijder een project
    private void deleteProject(int projectId, int position) {
        Log.w(TAG, "onClick: Je wil verwijderen! " + projectId + " " + position);
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Weet je het zeker?");
        builder.setMessage("Wanneer je bevestigd is er geen weg meer terug en wordt je project verwijderd.");
        builder.setPositiveButton("Verwijder", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteProject = service.delete_project(projectId);
                deleteProject.enqueue(new Callback<ProjectResponse>() {
                    @Override
                    public void onResponse(@NotNull Call<ProjectResponse> call, @NotNull Response<ProjectResponse> response) {
                        list.remove(position);
                        notifyItemRemoved(position);
                        notifyDataSetChanged();
                        listAll.clear();
                        listAll.addAll(list);
                    }

                    @Override
                    public void onFailure(@NotNull Call<ProjectResponse> call, @NotNull Throwable t) {
                        Log.w(TAG, "onFailure: " + t.getMessage() );
                    }
                });

            }
        });
        builder.setNegativeButton(R.string.btn_cancel, (dialog, which) -> {});
        builder.show();
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    // Optie om te kunnen zoeken in al je projecten
    Filter filter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            ArrayList<Project> filteredList = new ArrayList<>();
            if (constraint.toString().isEmpty()){
                filteredList.addAll(listAll);
            } else {
                for (Project project : listAll){
                    if(project.getProjectName().toLowerCase().contains(constraint.toString().toLowerCase())
                            || project.getOpdrachtgever().toLowerCase().contains(constraint.toString().toLowerCase())){
                        filteredList.add(project);
                    }
                }

            }

            FilterResults results = new FilterResults();
            results.values = filteredList;
            return  results;
        }

        @Override
        // Toon de lijst van zoekresultaten
        protected void publishResults(CharSequence constraint, FilterResults results) {
            list.clear();
            list.addAll((Collection<? extends Project>) results.values);
            notifyDataSetChanged();
        }
    };

    public Filter getFilter() {
        return filter;
    }

    class ProjectsHolder extends RecyclerView.ViewHolder {

        private TextView txtName,txtDate,txtDesc;
        private CircleImageView imgProfile;
        private ImageView imgProject;
        private ImageButton btnProjectOption;
        private LinearLayout projectCard;

        public ProjectsHolder(@NonNull View itemView) {
            super(itemView);
            txtName = itemView.findViewById(R.id.txtPostName);
            txtDate = itemView.findViewById(R.id.txtPostDate);
//            txtDesc = itemView.findViewById(R.id.txtPostDesc);
            imgProfile = itemView.findViewById(R.id.imgProjectProfile);
            imgProject = itemView.findViewById(R.id.imgProjectPhoto);
            btnProjectOption = itemView.findViewById(R.id.btnProjectOption);
            projectCard = itemView.findViewById(R.id.projectCard);
            btnProjectOption.setVisibility(View.GONE);
        }
    }
}
