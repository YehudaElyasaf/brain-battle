package com.example.trivia;

import static com.example.trivia.Category.ALL;
import static com.example.trivia.Category.COMPUTER_SCIENCE;
import static com.example.trivia.Category.GENERAL_KNOWLEDGE;
import static com.example.trivia.Category.SCIENCE;
import static com.example.trivia.DifficultyLevel.EASY;
import static com.example.trivia.DifficultyLevel.HARD;
import static com.example.trivia.DifficultyLevel.MEDIUM;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


enum DifficultyLevel{
    EASY,
    MEDIUM,
    HARD
}
enum Category{
    ALL,
    GENERAL_KNOWLEDGE,
    SCIENCE,
    COMPUTER_SCIENCE
}

public class StartGameFragment extends Fragment
        implements View.OnClickListener {
    private static final float UNSELECTED_BUTTON_ALPHA = (float) 0.4;
    private static final int MIN_QUESTION_COUNT = 2;
    private static final int MAX_QUESTION_COUNT = 15;

    private Button allCategoriesBtn, generalKnowledgeCategoryBtn,
            scienceCategoryBtn, computerScienceCategoryBtn;
    private Category category;
    private Button easyDifficultyLevelBtn, mediumDifficultyLevelBtn, hardDifficultyLevelBtn;
    private DifficultyLevel difficultyLevel;
    private Button questionCountDecBtn, questionCountIncBtn;
    private TextView questionCountValueLbl;
    private int questionCount;
    private Button playBtn;

    public StartGameFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment StartGameFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static StartGameFragment newInstance(String param1, String param2) {
        StartGameFragment fragment = new StartGameFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    void setCategory(Category newCategory){
        int selectedColor = R.color.purple_500;
        int unselectedColor = R.color.purple_200;

        allCategoriesBtn.setAlpha(UNSELECTED_BUTTON_ALPHA);
        generalKnowledgeCategoryBtn.setAlpha(UNSELECTED_BUTTON_ALPHA);
        scienceCategoryBtn.setAlpha(UNSELECTED_BUTTON_ALPHA);
        computerScienceCategoryBtn.setAlpha(UNSELECTED_BUTTON_ALPHA);

        switch (newCategory){
            case ALL:
                allCategoriesBtn.setAlpha(1);
                category = ALL;
                break;
            case GENERAL_KNOWLEDGE:
                generalKnowledgeCategoryBtn.setAlpha(1);
                category = GENERAL_KNOWLEDGE;
                break;
            case SCIENCE:
                scienceCategoryBtn.setAlpha(1);
                category = SCIENCE;
                break;
            case COMPUTER_SCIENCE:
                computerScienceCategoryBtn.setAlpha(1);
                category = COMPUTER_SCIENCE;
                break;
        }
    }
    void setDifficultyLevel(DifficultyLevel newDifficultyLevel){
        easyDifficultyLevelBtn.setAlpha(UNSELECTED_BUTTON_ALPHA);
        mediumDifficultyLevelBtn.setAlpha(UNSELECTED_BUTTON_ALPHA);
        hardDifficultyLevelBtn.setAlpha(UNSELECTED_BUTTON_ALPHA);

        switch (newDifficultyLevel){
            case EASY:
                easyDifficultyLevelBtn.setAlpha(1);
                difficultyLevel = EASY;
                break;
            case MEDIUM:
                mediumDifficultyLevelBtn.setAlpha(1);
                difficultyLevel = MEDIUM;
                break;
            case HARD:
                hardDifficultyLevelBtn.setAlpha(1);
                difficultyLevel = HARD;
                break;
        }
    }
    void questionCountDec(){
        questionCount--;
        questionCountValueLbl.setText(Integer.toString(questionCount));

        if(questionCount == MIN_QUESTION_COUNT)
            questionCountDecBtn.setEnabled(false);

        questionCountIncBtn.setEnabled(true);
    }
    void questionCountInc(){
        questionCount++;
        questionCountValueLbl.setText(Integer.toString(questionCount));

        if(questionCount == MAX_QUESTION_COUNT)
            questionCountIncBtn.setEnabled(false);

        questionCountDecBtn.setEnabled(true);
    }

    private void initViews(View v){
        allCategoriesBtn = v.findViewById(R.id.allCategoriesBtn);
        generalKnowledgeCategoryBtn = v.findViewById(R.id.generalKnowledgeCategoryBtn);
        scienceCategoryBtn = v.findViewById(R.id.scienceCategoryBtn);
        computerScienceCategoryBtn = v.findViewById(R.id.computerScienceCategoryBtn);;

        easyDifficultyLevelBtn = v.findViewById(R.id.easyDifficultyLevelBtn);
        mediumDifficultyLevelBtn = v.findViewById(R.id.mediumDifficultyLevelBtn);
        hardDifficultyLevelBtn = v.findViewById(R.id.hardDifficultyLevelBtn);

        questionCountDecBtn = v.findViewById(R.id.questionCountDecBtn);
        questionCountIncBtn = v.findViewById(R.id.questionCountIncBtn);
        questionCountValueLbl = v.findViewById(R.id.questionCountValueLbl);

        playBtn = v.findViewById(R.id.playBtn);


        allCategoriesBtn.setOnClickListener(this);
        generalKnowledgeCategoryBtn.setOnClickListener(this);
        scienceCategoryBtn.setOnClickListener(this);
        computerScienceCategoryBtn.setOnClickListener(this);
        setCategory(ALL);

        easyDifficultyLevelBtn.setOnClickListener(this);
        mediumDifficultyLevelBtn.setOnClickListener(this);
        hardDifficultyLevelBtn.setOnClickListener(this);
        setDifficultyLevel(EASY);

        questionCountDecBtn.setOnClickListener(this);
        questionCountIncBtn.setOnClickListener(this);
        questionCount = 10;
        questionCountValueLbl.setText(Integer.toString(questionCount));

        playBtn.setOnClickListener(this);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_start_game, container, false);
        initViews(view);

        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.easyDifficultyLevelBtn:
                setDifficultyLevel(EASY);
                break;
            case R.id.mediumDifficultyLevelBtn:
                setDifficultyLevel(MEDIUM);
                break;
            case R.id.hardDifficultyLevelBtn:
                setDifficultyLevel(HARD);
                break;

            case R.id.allCategoriesBtn:
                setCategory(ALL);
                break;
            case R.id.generalKnowledgeCategoryBtn:
                setCategory(GENERAL_KNOWLEDGE);
                break;
            case R.id.scienceCategoryBtn:
                setCategory(SCIENCE);
                break;
            case R.id.computerScienceCategoryBtn:
                setCategory(COMPUTER_SCIENCE);
                break;

            case R.id.questionCountDecBtn:
                questionCountDec();
                break;
            case R.id.questionCountIncBtn:
                questionCountInc();
                break;

            case R.id.playBtn:
                Intent intent = new Intent(requireActivity(), GameActivity.class);
                intent.putExtra("CATEGORY", category);
                intent.putExtra("DIFFICULTY_LEVEL", difficultyLevel);
                intent.putExtra("QUESTION_COUNT", questionCount);

                startActivity(intent);
                requireActivity().finish();
        }
    }
}