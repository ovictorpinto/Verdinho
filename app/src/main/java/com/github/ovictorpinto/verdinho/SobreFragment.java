package com.github.ovictorpinto.verdinho;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class SobreFragment extends Fragment {

    public SobreFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getActivity().setTitle(R.string.informacoes);
        return inflater.inflate(R.layout.ly_sobre, null);
    }
}
