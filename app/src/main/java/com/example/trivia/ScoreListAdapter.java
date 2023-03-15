package com.example.trivia;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.security.PublicKey;
import java.util.ArrayList;

public class ScoreListAdapter extends RecyclerView.Adapter<ScoreListAdapter.ScoreListViewHolder> {
    Context context;
    ArrayList<User> users;

    public ScoreListAdapter(Context context, ArrayList<User> users) {
        this.context = context;
        this.users = users;
    }

    @NonNull
    @Override
    public ScoreListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View view = layoutInflater.inflate(R.layout.recycler_view_score, parent, false);
        return new ScoreListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ScoreListViewHolder holder, int position) {
        User user = users.get(position);

        holder.place.setText(Integer.toString(position + 1));
        holder.username.setText(user.getUsername());
        holder.score.setText(Integer.toString((int) user.calculateScore()));
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public class ScoreListViewHolder extends RecyclerView.ViewHolder {
        TextView place;
        TextView username;
        TextView score;

        public ScoreListViewHolder(@NonNull View itemView) {
            super(itemView);

            place = itemView.findViewById(R.id.placeLbl);
            username = itemView.findViewById(R.id.usernameLbl);
            score = itemView.findViewById(R.id.scoreLbl);
        }
    }
}
