package com.example.mytasks.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mytasks.Models.RosterStats;
import com.example.mytasks.R;

import java.util.ArrayList;
import java.util.List;

public class RosterAdapter extends RecyclerView.Adapter<RosterAdapter.RosterViewHolder> {

    private List<RosterStats> rosterStatsList = new ArrayList<>();

    public void setRosterStatsList(List<RosterStats> rosterStatsList) {
        this.rosterStatsList = rosterStatsList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RosterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_roster_performance, parent, false);
        return new RosterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RosterViewHolder holder, int position) {
        RosterStats stats = rosterStatsList.get(position);
        holder.bind(stats);
    }

    @Override
    public int getItemCount() {
        return rosterStatsList.size();
    }

    static class RosterViewHolder extends RecyclerView.ViewHolder {
        TextView tvUsername, tvTaskRatio, tvPercentage;

        public RosterViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUsername = itemView.findViewById(R.id.tvRosterUsername);
            tvTaskRatio = itemView.findViewById(R.id.tvRosterTaskRatio);
            tvPercentage = itemView.findViewById(R.id.tvRosterPercentage);
        }

        public void bind(RosterStats stats) {
            tvUsername.setText(stats.username);
            
            if (stats.totalTasks == 0) {
                tvTaskRatio.setText("No tasks assigned");
                tvPercentage.setText("0%");
            } else {
                tvTaskRatio.setText("Tasks: " + stats.completedTasks + " / " + stats.totalTasks + " Completed");
                tvPercentage.setText(stats.percentage + "%");
            }
        }
    }
}
