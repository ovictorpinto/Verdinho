package com.github.ovictorpinto.verdinho.ui.linha;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.ovictorpinto.verdinho.R;
import com.github.ovictorpinto.verdinho.to.Estimativa;

import java.util.Date;
import java.util.List;

import br.com.tcsistemas.common.date.DataHelper;
import br.com.tcsistemas.common.string.StringHelper;

/**
 * Created by Suleiman on 14-04-2015.
 */
public class EstimativaLinhaRecyclerAdapter extends RecyclerView.Adapter<EstimativaLinhaRecyclerAdapter.ViewHolder> {
    
    Context context;
    private List<Estimativa> estimativas;
    private long horarioDoServidor;
    
    public EstimativaLinhaRecyclerAdapter(Context context, List<Estimativa> estimativas, long horarioDoServidor) {
        this.context = context;
        this.estimativas = estimativas;
        this.horarioDoServidor = horarioDoServidor;
    }
    
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.ly_item_linha, viewGroup, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }
    
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int i) {
        
        Estimativa item = estimativas.get(i);
        
        TextView textView = viewHolder.textviewHorario;
        ImageView imageView = viewHolder.imageview;
        imageView.setVisibility(item.getAcessibilidade() ? View.VISIBLE : View.GONE);
        
        String hora = item.getHorarioText(context, horarioDoServidor);
        String horario = DataHelper.format(new Date(item.getHorarioNaOrigem()), "HH:mm");
        
        textView.setText(StringHelper.mergeSeparator(" - ", horario, hora));
        textView.setBackgroundResource(item.getBackgroundConfiabilidade(horarioDoServidor));
        
        viewHolder.textviewOnibus.setText(context.getString(R.string.onibus_, item.getVeiculo()));
    }
    
    @Override
    public int getItemCount() {
        return estimativas.size();
    }
    
    class ViewHolder extends RecyclerView.ViewHolder {
        TextView textviewHorario;
        TextView textviewOnibus;
        ImageView imageview;
        
        public ViewHolder(View itemView) {
            super(itemView);
            textviewHorario = (TextView) itemView.findViewById(R.id.textview_horario);
            imageview = (ImageView) itemView.findViewById(R.id.image_acessibilidade);
            textviewOnibus = (TextView) itemView.findViewById(R.id.textview_onibus);
        }
    }
}