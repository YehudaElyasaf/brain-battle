package com.example.trivia;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.mikhaellopez.circularprogressbar.CircularProgressBar;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ScoreFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ScoreFragment extends Fragment {
    RecyclerView scoreRv;
    CircularProgressBar successPercentagePb;
    TextView totalScoreLbl;
    TextView totalCorrectLbl;
    TextView totalWrongLbl;
    TextView successPercentageLbl;

    ArrayList<User> users;
    User currentUser;

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
        successPercentagePb = v.findViewById(R.id.successPercentagePb);
        totalScoreLbl = v.findViewById(R.id.totalScoreLbl);
        totalCorrectLbl = v.findViewById(R.id.totalCorrectLbl);
        totalWrongLbl = v.findViewById(R.id.totalWrongLbl);
        successPercentageLbl = v.findViewById(R.id.successPercentageLbl);

        Fragment loadingFragment = new LoadingFragment();
        getChildFragmentManager().beginTransaction().replace(R.id.scoreLayout, loadingFragment).commit();

        //fetch user list
        FirebaseFirestore.getInstance().
                collection(GameActivity.USERS_COLLECTION_PATH).get().addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getContext(), "Connection error!", Toast.LENGTH_SHORT).show();
                        //back to main manu
                        FragmentTransaction ft = getChildFragmentManager().beginTransaction();
                        ft.replace(R.id.mainFragmentContainer, new NewGameFragment()).commit();
                    }
                }).addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        ArrayList<User> fetchedUserList = new ArrayList<>();
                        for(DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments())
                            if(documentSnapshot.exists())
                                fetchedUserList.add(documentSnapshot.toObject(User.class));

                        users = fetchedUserList;
                        //find current user
                        for(User user : users)
                            if(user.getUid().equals(FirebaseAuth.getInstance().getUid()))
                                currentUser = user;
                        if(currentUser == null)
                            Toast.makeText(getContext(), "Current user not found!", Toast.LENGTH_SHORT).show();

                        getChildFragmentManager().beginTransaction().hide(loadingFragment).commit();
                        showScore();
                    }
                });

        return v;
    }

    private void showScore() {
        totalScoreLbl.setText(Integer.toString(currentUser.getScore()));
        totalCorrectLbl.setText(Integer.toString(currentUser.getTotalCorrect()));
        totalWrongLbl.setText(Integer.toString(currentUser.getTotalWrong()));

        //show correct percentage progress bar
        successPercentagePb.setProgressBarColor(MyColor.CORRECT_GREEN);
        successPercentagePb.setProgressBarWidth(15);
        successPercentagePb.setBackgroundProgressBarColor(MyColor.WRONG_RED);
        successPercentagePb.setBackgroundProgressBarWidth(10);

        int totalAnswers = currentUser.getTotalCorrect() + currentUser.getTotalWrong();
        int progress;
        if(totalAnswers > 0)
            progress = (100 * currentUser.getTotalCorrect()) / totalAnswers;
        else
            //avoid division by zero
            progress = 0;

        successPercentagePb.setProgressWithAnimation(progress, (long)1000);
        successPercentageLbl.setText(Integer.toString(progress) + "%");

        //show user list
        users.sort((o1, o2) -> (int)(o2.getScore() - o1.getScore()));
        ScoreListAdapter scoreListAdapter = new ScoreListAdapter(getContext(), users);
        scoreRv.setAdapter(scoreListAdapter);
        scoreRv.setLayoutManager(new LinearLayoutManager(getContext()));
    }
}