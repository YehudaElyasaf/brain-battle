package com.example.trivia;

import android.graphics.Color;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mikhaellopez.circularprogressbar.CircularProgressBar;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ScoreFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ScoreFragment extends Fragment {
    RecyclerView scoreRv;

    public ScoreFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_score, container, false);
        scoreRv = v.findViewById(R.id.scoreRv);

        ArrayList<User> users = new ArrayList<User>();
        users.add(new User("yehuda", "",0,  100, 0));
        users.add(new User("arie", "", 0,21400, 7500));

        users.sort((o1, o2) -> (int)(o2.calculateScore() - o1.calculateScore()));
        ScoreListAdapter scoreListAdapter = new ScoreListAdapter(requireContext(), users);
        scoreRv.setAdapter(scoreListAdapter);
        scoreRv.setLayoutManager(new LinearLayoutManager(requireContext()));

        CircularProgressBar successPercentagePb = v.findViewById(R.id.successPercentagePb);
        successPercentagePb.setProgressWithAnimation(60, (long)1000);
        successPercentagePb.setProgressBarColor(MyColor.CORRECT_GREEN);
        successPercentagePb.setProgressBarWidth(15);
        successPercentagePb.setBackgroundProgressBarColor(MyColor.WRONG_RED);
        successPercentagePb.setBackgroundProgressBarWidth(10);

        return v;
    }
}