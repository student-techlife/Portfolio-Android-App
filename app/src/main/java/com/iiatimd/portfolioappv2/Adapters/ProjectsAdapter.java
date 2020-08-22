package com.iiatimd.portfolioappv2.Adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.iiatimd.portfolioappv2.Entities.Project;
import com.iiatimd.portfolioappv2.R;

import java.util.ArrayList;
import java.util.Collection;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProjectsAdapter extends RecyclerView.Adapter<ProjectsAdapter.ProjectsHolder> {

    private Context context;
    private ArrayList<Project> list;
    private ArrayList<Project> listAll;
    private SharedPreferences preferences;

    private static final String TAG = "ProjectsAdapter";

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
        return new ProjectsHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProjectsHolder holder, int position) {
        Project project = list.get(position);
//        Log.w(TAG, "onBindViewHolder: Test!");
        holder.txtName.setText(project.getProjectName());
        holder.txtDate.setText(project.getDate());
        holder.imgProject.setImageResource(R.drawable.project);
//        holder.txtDesc.setText(project.getDesc());

        if (project.getUser().getId()==preferences.getInt("id",0)) {
            holder.btnProjectOption.setVisibility(View.VISIBLE);
        } else {
            holder.btnProjectOption.setVisibility(View.GONE);
        }

        holder.btnProjectOption.setOnClickListener(v->{
            PopupMenu popupMenu = new PopupMenu(context,holder.btnProjectOption);
            popupMenu.inflate(R.menu.menu_project_options);
            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {

                    switch (item.getItemId()) {
                        case R.id.item_edit: {
//                            return true;
                        }
                        case R.id.item_delete: {
//                            deleteProject(project.getId(),position);
//                            return true;
                        }
                    }
                    return false;
                }
            });
            popupMenu.show();
        });
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
                    if(project.getDesc().toLowerCase().contains(constraint.toString().toLowerCase())
                            || project.getUser().getName().toLowerCase().contains(constraint.toString().toLowerCase())){
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

        private TextView txtName,txtDate,txtDesc,txtLikes,txtComments;
        private CircleImageView imgProfile;
        private ImageView imgProject;
        private ImageButton btnProjectOption,btnLike,btnComment;

        public ProjectsHolder(@NonNull View itemView) {
            super(itemView);
            txtName = itemView.findViewById(R.id.txtPostName);
            txtDate = itemView.findViewById(R.id.txtPostDate);
//            txtDesc = itemView.findViewById(R.id.txtPostDesc);
//            imgProfile = itemView.findViewById(R.id.imgPostProfile);
            imgProject = itemView.findViewById(R.id.imgProjectPhoto);
            btnProjectOption = itemView.findViewById(R.id.btnProjectOption);
            btnProjectOption.setVisibility(View.GONE);
        }
    }
}
