package com.github.ovictorpinto.verdinho;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.ovictorpinto.verdinho.to.PontoTO;

import java.util.List;

/**
 * Created by Suleiman on 14-04-2015.
 */
public class FavoritoRecyclerAdapter extends RecyclerView.Adapter<FavoritoRecyclerAdapter.ViewHolder> {

    private Context context;
    private List<PontoTO> pontos;

    public FavoritoRecyclerAdapter(Context context, List<PontoTO> pontos) {
        this.context = context;
        this.pontos = pontos;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.ly_item_favorito, viewGroup, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, int i) {

        final PontoTO item = pontos.get(i);

        viewHolder.textviewReferencia.setText(item.getDescricao());
        viewHolder.textviewLogradouro.setText(item.getLogradouro());
        viewHolder.textviewNumero.setText(item.getIdentificador());
        viewHolder.mainView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(context, PontoDetalheActivity.class);
                i.putExtra(PontoTO.PARAM, item);
                context.startActivity(i);
            }
        });
    }

    @Override
    public int getItemCount() {
        return pontos.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView textviewReferencia;
        TextView textviewLogradouro;
        TextView textviewNumero;
        View mainView;

        public ViewHolder(View itemView) {
            super(itemView);
            textviewReferencia = (TextView) itemView.findViewById(R.id.textview_referencia);
            textviewLogradouro = (TextView) itemView.findViewById(R.id.textview_logradouro);
            textviewNumero = (TextView) itemView.findViewById(R.id.textview_numero);
            mainView = itemView;
        }
    }
}