package com.example.exson.another2;

import android.app.DialogFragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

public class RegisterDialog extends DialogFragment {

    private final String TAG = "NSDHelper";

    private Button mAccept, mCancel;

    private EditText mUserText;
    private String mUser;

    SharedPreferences mPreferences;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.main_regitration_fragment, container, false);
        getDialog().setTitle("Register User");

        mPreferences = getActivity().getSharedPreferences("WiShare", Context.MODE_PRIVATE);

        mUserText = (EditText) rootView.findViewById(R.id.user_ET);
        mAccept = (Button) rootView.findViewById(R.id.user_accept);
        mCancel = (Button) rootView.findViewById(R.id.user_cancel);

        mAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mUser = mUserText.getText().toString();

                if(mUser.equals("")){
                    mUserText.setError("Enter Fullname");
                }

                SharedPreferences.Editor mEditor = mPreferences.edit();
                mEditor.putString("Fullname", mUser);
                mEditor.apply();

                dismiss();
            }
        });

        mCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                getActivity().finish();
            }
        });

        return rootView;

    }
}
