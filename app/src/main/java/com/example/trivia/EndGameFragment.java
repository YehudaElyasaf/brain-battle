package com.example.trivia;

import static com.example.trivia.GameActivity.CATEGORY_INDEX;
import static com.example.trivia.GameActivity.DIFFICULTY_LEVEL_INDEX;
import static com.example.trivia.GameActivity.IS_NEW_GAME_EXTRA;
import static com.example.trivia.GameActivity.QUESTIONS_COUNT_INDEX;

import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link EndGameFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class EndGameFragment extends Fragment implements View.OnClickListener {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private GameViewModel gameVM;

    private TextView yourScoreCountLbl;
    private TextView enemyScoreCountLbl;
    private TextView winnerLbl;

    private ImageButton endGameReplayBtn;
    private ImageButton endGameHomeBtn;
    private ImageButton endGameScoreBtn;

    private String mParam1;
    private String mParam2;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment EndGameFragment.
     */
    public static EndGameFragment newInstance(String param1, String param2) {
        EndGameFragment fragment = new EndGameFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }


    public EndGameFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_end_game, container, false);

        gameVM = new ViewModelProvider(getActivity()).get(GameViewModel.class);
        yourScoreCountLbl = view.findViewById(R.id.yourScoreCountLbl);
        enemyScoreCountLbl = view.findViewById(R.id.enemyScoreCountLbl);
        winnerLbl = view.findViewById(R.id.winnerLbl);

        endGameReplayBtn = view.findViewById(R.id.endGameReplayBtn);
        endGameHomeBtn = view.findViewById(R.id.endGameHomeBtn);
        endGameScoreBtn = view.findViewById(R.id.endGameScoreBtn);
        endGameReplayBtn.setOnClickListener(this);
        endGameHomeBtn.setOnClickListener(this);
        endGameScoreBtn.setOnClickListener(this);

        showResults(view);

        // Inflate the layout for this fragment
        return view;
    }

    private void showResults(View view) {
        int yourScore = gameVM.getMyPlayer().calculatePoints();
        int enemyScore = gameVM.getOtherPlayer().calculatePoints();

        yourScoreCountLbl.setText(Integer.toString(yourScore));
        enemyScoreCountLbl.setText(Integer.toString(enemyScore));

        if(yourScore > enemyScore) {
            //you won
            winnerLbl.setText("You won!");
            yourScoreCountLbl.setTextSize(TypedValue.COMPLEX_UNIT_SP, 84);

            //TODO: decrease score
            //decreaseScore(enemyScore, enemyScoreCountLbl);
        }
        else if(yourScore < enemyScore){
            //you lost
            winnerLbl.setText(gameVM.getOtherPlayer().getUsername() + " won!");
            enemyScoreCountLbl.setTextSize(TypedValue.COMPLEX_UNIT_SP, 84);
            //decreaseScore(yourScore, yourScoreCountLbl);
        }
        else{
            //draw
            winnerLbl.setText("Draw!");

            yourScoreCountLbl.setTextSize(enemyScoreCountLbl.getTextSize());
            //decreaseScore(yourScore, yourScoreCountLbl);
            //decreaseScore(enemyScore, enemyScoreCountLbl);
        }
    }

    @Override
    public void onClick(View v) {
        Intent intent;

        switch (v.getId()){
            case R.id.endGameReplayBtn:
                intent = new Intent(getActivity(), GameActivity.class);

                int extras[] = new int[3];
                extras[QUESTIONS_COUNT_INDEX] = gameVM.getGame().getQuestions().size();
                extras[DIFFICULTY_LEVEL_INDEX]= gameVM.getGame().getQuestions().get(0).difficultyLevel.ordinal();
                extras[CATEGORY_INDEX] = gameVM.getGame().getQuestions().get(0).category.ordinal();
                intent.putExtra(GameActivity.NEW_GAME_EXTRAS, extras);

                intent.putExtra(IS_NEW_GAME_EXTRA, true);
                startActivity(intent);
                getActivity().finish();
                break;
            case R.id.endGameHomeBtn:
                //goto new game fragment
                intent = new Intent(getContext(), MainMenuActivity.class);

                startActivity(intent);
                getActivity().finish();
                break;
            case R.id.endGameScoreBtn:
                //goto score fragment
                intent = new Intent(getContext(), MainMenuActivity.class);

                intent.putExtra(MainMenuActivity.EXTRA_FRAGMENT_NAME, MainMenuActivity.EXTRA_SCORE_FRAGMENT);
                startActivity(intent);
                getActivity().finish();
                break;
        }
    }
}