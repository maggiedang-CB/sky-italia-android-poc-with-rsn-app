package com.nbcsports.regional.nbc_rsn.authentic_fan;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.nbcsports.regional.nbc_rsn.R;

/**
 * Created by justin on 2018-03-26.
 */

public class AuthenticFanFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_authentic_fan, container, false);
    }
}
