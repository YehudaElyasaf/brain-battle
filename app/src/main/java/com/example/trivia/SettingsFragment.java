package com.example.trivia;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SettingsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SettingsFragment extends Fragment implements View.OnClickListener {
    public static MutableLiveData<Float> backgroundMusicVolume;

    private TextView usernameLbl;
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

    private void initViews(View v) {
        usernameLbl = v.findViewById(R.id.usernameLbl);
        deleteUserBtn = v.findViewById(R.id.deleteUserImgBtn);
        editUsernameBtn = v.findViewById(R.id.editUsernameImgBtn);
        logoutBtn = v.findViewById(R.id.logoutImgBtn);
        muteBtn = v.findViewById(R.id.muteImgBtn);
        initVolumeSb(v);

        deleteUserBtn.setOnClickListener(this);
        editUsernameBtn.setOnClickListener(this);
        logoutBtn.setOnClickListener(this);
        muteBtn.setOnClickListener(this);
    }

    private void initVolumeSb(View v) {
        volumeSb = v.findViewById(R.id.volumeSb);
        //multiplied by 100 because volume is between 0-1 and progress is between 0-100
        volumeSb.setProgress(backgroundMusicVolume.getValue().intValue() * 100);
        volumeSb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                backgroundMusicVolume.setValue((float) progress / 100);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        initViews(view);

        String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        String username = User.emailToUsername(email);
        usernameLbl.setText(username);

        return view;
    }


    private void toggleMute() {
        if (volumeSb.isEnabled()) {
            //mute
            backgroundMusicVolume.setValue(0f);
            muteBtn.setImageDrawable(getActivity().getDrawable(R.drawable.ic_volume_off));
            volumeSb.setEnabled(false);
        } else {
            //unmute
            backgroundMusicVolume.setValue(1f);
            muteBtn.setImageDrawable(getActivity().getDrawable(R.drawable.ic_volume));
            volumeSb.setEnabled(true);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.deleteUserImgBtn:
                tryDeleteUser();
                break;
            case R.id.editUsernameImgBtn:
                break;
            case R.id.logoutImgBtn:
                tryLogout();
                break;
            case R.id.muteImgBtn:
                toggleMute();
                break;
        }
    }

    private void tryLogout() {
        FirebaseAuth.getInstance().signOut();

        Intent intent = new Intent(getContext(), LoginActivity.class);
        startActivity(intent);
        getActivity().finish();
    }

    private void tryDeleteUser() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Enter password:");
        EditText passwordTxt = new EditText(getContext());
        builder.setView(passwordTxt);
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

                        if (passwordTxt.getText().length() == 0)
                            Toast.makeText(getContext(), "Please enter password", Toast.LENGTH_SHORT).show();
                        else
                            // sign in again, necessary before deleting user
                            firebaseAuth.signInWithEmailAndPassword(firebaseAuth.getCurrentUser().getEmail(), passwordTxt.getText().toString())
                                    .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                                        @Override
                                        public void onSuccess(AuthResult authResult) {
                                            //delete user
                                            FirebaseAuth.getInstance().getCurrentUser().delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void unused) {
                                                    Intent intent = new Intent(getContext(), LoginActivity.class);
                                                    startActivity(intent);
                                                    getActivity().finish();
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Toast.makeText(getContext(), "Couldn't delete user", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(getContext(), "Couldn't delete user", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                    }
                })
                .setNegativeButton("Cancel", null).show();
    }
}