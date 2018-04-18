package com.github.ovictorpinto.verdinho.ui.main;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.github.ovictorpinto.verdinho.R;
import com.github.ovictorpinto.verdinho.to.PontoTO;

import java.util.List;

import br.com.tcsistemas.common.string.StringHelper;

/**
 * Created by Suleiman on 14-04-2015.
 */
public class FavoritoRecyclerAdapter extends RecyclerView.Adapter<FavoritoRecyclerAdapter.ViewHolder> {
    
    public interface FavoritoListener {
        
        void onClick(PontoTO pontoTO);
        
        void onEnableNotification(PontoTO pontoTO);
        
        void onDisableNotification(PontoTO pontoTO);
        
        void onRename(PontoTO pontoTO);
    }
    
    private Context context;
    private List<PontoTO> pontos;
    private FavoritoListener favoritoListener;
    
    public FavoritoRecyclerAdapter(Context context, List<PontoTO> pontos, FavoritoListener listener) {
        this.context = context;
        this.pontos = pontos;
        this.favoritoListener = listener;
    }
    
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.ly_item_favorito, viewGroup, false);
        return new ViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, int i) {
        
        final PontoTO item = pontos.get(i);
        
        String nome = StringHelper
                .coalesce(item.getApelido(), StringHelper.mergeSeparator(" - ", item.getIdentificador(), item.getDescricao()));
        
        viewHolder.textviewReferencia.setText(nome);
        viewHolder.textviewLogradouro.setText(item.getLogradouro());
        if (favoritoListener != null) {
            viewHolder.mainView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    favoritoListener.onClick(item);
                }
            });
        }
        viewHolder.switchCompat.setChecked(item.getNotificacao());
        viewHolder.switchCompat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    favoritoListener.onEnableNotification(item);
                } else {
                    favoritoListener.onDisableNotification(item);
                }
            }
        });
        viewHolder.optionMenu.setOnClickListener(view -> {
            PopupMenu popup = new PopupMenu(context, view);
            popup.inflate(R.menu.menu_renomear);
            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem) {
                    favoritoListener.onRename(item);
                    return false;
                }
            });
            popup.show();
        });
    }
    
    @Override
    public int getItemCount() {
        return pontos.size();
    }
    
    class ViewHolder extends RecyclerView.ViewHolder {
        TextView textviewReferencia;
        TextView textviewLogradouro;
        SwitchCompat switchCompat;
        View mainView;
        View optionMenu;
        
        public ViewHolder(View itemView) {
            super(itemView);
            textviewReferencia = itemView.findViewById(R.id.textview_referencia);
            textviewLogradouro = itemView.findViewById(R.id.textview_logradouro);
            switchCompat = itemView.findViewById(R.id.switch_);
            optionMenu = itemView.findViewById(R.id.textview_options);
            mainView = itemView;
        }
    }
}