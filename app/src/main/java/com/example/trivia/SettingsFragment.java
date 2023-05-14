package com.example.trivia;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.SeekBar;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SettingsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SettingsFragment extends Fragment implements View.OnClickListener {
    private ImageButton deleteUserBtn;
    private ImageButton editUsernameBtn;
    private ImageButton logoutBtn;
    private ImageButton muteBtn;
    private SeekBar volumeSb;

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    public SettingsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SettingsFragment.
     */
    public static SettingsFragment newInstance(String param1, String param2) {
        SettingsFragment fragment = new SettingsFragment();
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

    private void initViews(View v){
        deleteUserBtn = v.findViewById(R.id.deleteUserImgBtn);
        editUsernameBtn = v.findViewById(R.id.editUsernameImgBtn);
        logoutBtn = v.findViewById(R.id.logoutImgBtn);
        muteBtn = v.findViewById(R.id.muteImgBtn);
        volumeSb = v.findViewById(R.id.volumeSb);

        deleteUserBtn.setOnClickListener(this);
        editUsernameBtn.setOnClickListener(this);
        muteBtn.setOnClickListener(this);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =inflater.inflate(R.layout.fragment_settings, container, false);
        initViews(view);

        return view;
    }


    private void toggleMute(){
        if(volumeSb.isEnabled()){
            muteBtn.setImageDrawable(requireActivity().getDrawable(R.drawable.ic_volume_off));
            volumeSb.setEnabled(false);
        }else{
            muteBtn.setImageDrawable(requireActivity().getDrawable(R.drawable.ic_volume));
            volumeSb.setEnabled(true);
        }
    }
    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.deleteUserImgBtn:
                break;
            case R.id.editUsernameImgBtn:
                break;
            case R.id.logoutImgBtn:
                break;
            case R.id.muteImgBtn:
                toggleMute();
                break;
        }
    }
}