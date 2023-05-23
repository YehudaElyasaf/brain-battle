package com.example.trivia;

import static com.example.trivia.GameActivity.CATEGORY_INDEX;
import static com.example.trivia.GameActivity.DIFFICULTY_LEVEL_INDEX;
import static com.example.trivia.GameActivity.IS_NEW_GAME_EXTRA;
import static com.example.trivia.GameActivity.QUESTIONS_COUNT_INDEX;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link JoinGameFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class JoinGameFragment extends Fragment implements View.OnClickListener {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    public static final int GAME_ID_LENGTH = 7;

    private String mParam1;
    private String mParam2;

    private EditText gameIdTxt;
    private Button joinGameBtn;

    public JoinGameFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment JoinGameFragment.
     */
    public static JoinGameFragment newInstance(String param1, String param2) {
        JoinGameFragment fragment = new JoinGameFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
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
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_join_game, container, false);

        gameIdTxt = view.findViewById(R.id.gameIdTxt);
        joinGameBtn = view.findViewById(R.id.joinGameBtn);
        joinGameBtn.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.joinGameBtn:
                joinGame();
        }
    }

    private void joinGame(){
        String id = gameIdTxt.getText().toString();
        int intId;

        try{
            intId = Integer.parseInt(id);
            if(id.length() != GAME_ID_LENGTH)
                throw new Exception();
        }
        catch (Exception e){
            Toast.makeText(getContext(), "Invalid ID entered!", Toast.LENGTH_SHORT).show();
            gameIdTxt.setText("");
            return;
        }

        Intent intent = new Intent(getActivity(), GameActivity.class);
        intent.putExtra(GameActivity.IS_NEW_GAME_EXTRA, false);
        intent.putExtra(GameActivity.GAME_ID_EXTRA, intId);

        startActivity(intent);
    }
}