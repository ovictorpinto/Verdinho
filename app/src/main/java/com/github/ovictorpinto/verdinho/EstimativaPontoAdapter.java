package com.github.ovictorpinto.verdinho;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.ovictorpinto.verdinho.to.Estimativa;
import com.github.ovictorpinto.verdinho.to.LinhaTO;

import java.util.Date;
import java.util.List;
import java.util.Map;

import br.com.mobilesaude.androidlib.widget.helper.ViewHolder;
import br.com.tcsistemas.common.date.DataHelper;
import br.com.tcsistemas.common.date.HoraHelper;
import br.com.tcsistemas.common.string.StringHelper;

public class EstimativaPontoAdapter extends ArrayAdapter<Estimativa> {

    private Map<Integer, LinhaTO> mapLinhas;
    private long horarioServidor;

    public EstimativaPontoAdapter(Context context, List<Estimativa> lista, long horarioServidor, Map<Integer, LinhaTO> mapLinhas) {
        super(context, View.NO_ID, lista);
        this.horarioServidor = horarioServidor;
        this.mapLinhas = mapLinhas;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Estimativa item = getItem(position);
        LinhaTO linha = mapLinhas.get(item.getItinerarioId());

        ViewHolder holder;
        if (convertView == null) {
            // LayoutInflater class is used to instantiate layout XML file into its corresponding View objects.
            LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.ly_item_ponto, null);
        }

        TextView textView = ViewHolder.get(convertView, R.id.textview_numero_linha);
        textView.setText(linha.getIdentificadorLinha());

        textView = ViewHolder.get(convertView, R.id.textview_nome_linha);
        textView.setText(linha.getBandeira());

        ImageView view = ViewHolder.get(convertView, R.id.image_acessibilidade);
        view.setVisibility(item.getAcessibilidade() ? View.VISIBLE : View.GONE);

        textView = ViewHolder.get(convertView, R.id.textview_horario);

        String hora;
        long miliRestante = item.getHorarioNaOrigem() - horarioServidor;
        if (miliRestante < HoraHelper.MINUTO_IN_MILI) {
            hora = getContext().getString(R.string.meno_minuto);
        } else if (miliRestante < HoraHelper.HORA_IN_MILI) {
            int minutos = (int) (miliRestante / HoraHelper.MINUTO_IN_MILI);
            hora = getContext().getResources().getQuantityString(R.plurals.minutos, minutos, minutos);
        } else {
            long minutos = miliRestante / HoraHelper.MINUTO_IN_MILI;
            long horaRestante = minutos / 60;
            minutos %= 60L;
            hora = getContext().getString(R.string.mais_hora, horaRestante, minutos);
        }
        textView.setText(StringHelper.mergeSeparator(" - ", DataHelper.format(new Date(item.getHorarioNaOrigem()), "HH:mm"), hora));

        textView.setBackgroundResource(item.getBackgroundConfiabilidade(horarioServidor));

        return convertView;
    }
}
