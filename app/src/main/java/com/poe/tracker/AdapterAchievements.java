package com.poe.tracker;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.poe.tracker.databinding.AchievementRowBinding;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class AdapterAchievements extends RecyclerView.Adapter {

    private final Context context;
    public ArrayList<ModelAchievements> achievementsArrayList;
    private AchievementRowBinding binding;



    public AdapterAchievements(Context context, ArrayList<ModelAchievements> achievementsArrayList) {
        this.context = context;
        this.achievementsArrayList = achievementsArrayList;
    }

    @NonNull
    @NotNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        binding = AchievementRowBinding.inflate(LayoutInflater.from(context),parent,false);
        return new AdapterAchievements.AchievementHolderOne(binding.getRoot());
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull @NotNull RecyclerView.ViewHolder holder, int position) {

        ModelAchievements achievements_model = achievementsArrayList.get(position);
        String name = achievements_model.getName();
        String isCompleted = achievements_model.getCompleted();

        AdapterAchievements.AchievementHolderOne achievementHolderOne = (AdapterAchievements.AchievementHolderOne) holder;
        achievementHolderOne.tvAchievementName.setText(name);
        if(isCompleted.equals("false")){
            achievementHolderOne.tvAchievementCompleted.setText("Not Completed");
        }
        else{
            achievementHolderOne.tvAchievementCompleted.setText("Completed");
        }

    }


    @Override
    public int getItemCount() {
        return achievementsArrayList.size();
    }


    class AchievementHolderOne extends RecyclerView.ViewHolder {

        //ui views of collection_row.xml
        TextView tvAchievementName,tvAchievementCompleted;


        public AchievementHolderOne(@NonNull View itemView){
            super(itemView);

            //Ui Views
            tvAchievementName = binding.tvAchievementName;
            tvAchievementCompleted = binding.tvAchievementCompleted;
        }
    }
}
