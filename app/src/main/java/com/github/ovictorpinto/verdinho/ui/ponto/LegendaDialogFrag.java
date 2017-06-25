package com.github.ovictorpinto.verdinho.ui.ponto;

import android.app.Dialog;
import android.app.DialogFragment;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.ovictorpinto.verdinho.R;

public class LegendaDialogFrag extends DialogFragment {
    
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.setTitle(R.string.legenda);
        return dialog;
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        
        //        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);//remove título
        getDialog().getWindow()
                   .setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT)); //remove fundo do dialog e obedece o shape
        getDialog().setCanceledOnTouchOutside(true);
        
        View viewPrincipal = inflater.inflate(R.layout.ly_legenda_dialog, null);
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