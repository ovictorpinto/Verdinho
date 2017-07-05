package com.github.ovictorpinto.verdinho.ui.main;

import android.app.DialogFragment;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.ovictorpinto.verdinho.R;

public class ProximidadeDialogFrag extends DialogFragment {
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        
        getDialog().getWindow()
                   .setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT)); //remove fundo do dialog e obedece o shape
        getDialog().setCanceledOnTouchOutside(true);
        
        View viewPrincipal = inflater.inflate(R.layout.ly_proximidade_dialog, null);
        View button = viewPrincipal.findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        
        return viewPrincipal;
    }
}