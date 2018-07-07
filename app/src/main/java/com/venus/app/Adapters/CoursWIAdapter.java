package com.venus.app.Adapters;

import android.support.v7.widget.AppCompatTextView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.venus.app.Base.AbstractInformation;
import com.venus.app.Base.Cours;
import com.venus.app.wevy.R;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by arnold on 22/09/17.
 * Cours With Infos Adapter
 */
public class CoursWIAdapter extends CoursAdapter {
    private AbstractInformation[] infos;
    private OnListItemSelectedListener mListener;

    public CoursWIAdapter(OnListItemSelectedListener listener, Cours[] cours, Calendar cal, AbstractInformation[] infos) {
        super(listener, cours, cal);
        this.infos = infos;
        this.mListener = listener;
    }

    private class Holder {
        AppCompatTextView tv1;
        AppCompatTextView tv2;
        AppCompatTextView tv3;
        AppCompatTextView tvCount;
    }

    @Override
    public View getView(final int i, View view, ViewGroup viewGroup) {
        Holder holder = new Holder();
        final ArrayList<AbstractInformation> liste = new ArrayList<>();
        if(view == null){
            view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_cours_w_infos, viewGroup, false);
            holder.tv1 = (AppCompatTextView) view.findViewById(R.id.cours_nom);
            holder.tv2 = (AppCompatTextView) view.findViewById(R.id.cours_prof);
            holder.tv3 = (AppCompatTextView) view.findViewById(R.id.cours_heure);
            holder.tvCount = (AppCompatTextView) view.findViewById(R.id.cours_count);
            view.setTag(holder);
        } else holder = (Holder) view.getTag();
        final Cours c = (Cours) getItem(i);
        if(c != null) {
            holder.tv1.setText(c.getNom());
            holder.tv2.setText(c.getNomProf());
            holder.tv3.setText(c.getHeureD() + " - " + c.getHeureF());
            for (AbstractInformation ai : infos)
                if (ai.getTitre().equals(c.getNom())) liste.add(ai);
            if (liste.isEmpty()) holder.tvCount.setVisibility(View.GONE);
            else holder.tvCount.setText(String.valueOf(liste.size()));
        }

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener != null)
                    mListener.onListItemSelected(i, c, cal, liste.toArray(new AbstractInformation[]{}));
            }
        });

        return view;
    }

    public interface OnListItemSelectedListener extends CoursAdapter.OnListItemSelectedListener {
        void onListItemSelected(int position, Cours cours, Calendar cal, AbstractInformation[] infos);
    }
}
