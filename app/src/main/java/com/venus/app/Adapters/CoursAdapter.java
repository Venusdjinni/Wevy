package com.venus.app.Adapters;

import android.support.v7.widget.AppCompatTextView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import com.venus.app.Base.Cours;
import com.venus.app.wevy.R;

import java.util.Calendar;

/**
 * Created by arnold on 23/07/17.
 */
public class CoursAdapter extends BaseAdapter {
    private OnListItemSelectedListener mListener;
    protected Cours[] cours;
    protected Calendar cal;

    public CoursAdapter(OnListItemSelectedListener listener, Cours[] cours, Calendar cal) {
        this.mListener = listener;
        this.cours = cours;
        this.cal = cal;
    }

    @Override
    public int getCount() {
        return cours.length;
    }

    @Override
    public Object getItem(int i) {
        return cours[i];
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    private class Holder{
        AppCompatTextView tv1;
        AppCompatTextView tv2;
        AppCompatTextView tv3;
    }

    @Override
    public View getView(final int i, View view, ViewGroup viewGroup) {
        Holder holder = new Holder();
        if(view == null){
            view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_cours, viewGroup, false);
            holder.tv1 = (AppCompatTextView) view.findViewById(R.id.cours_nom);
            holder.tv2 = (AppCompatTextView) view.findViewById(R.id.cours_prof);
            holder.tv3 = (AppCompatTextView) view.findViewById(R.id.cours_heure);
            view.setTag(holder);
        } else holder = (Holder) view.getTag();
        final Cours cours = (Cours) getItem(i);
        if(cours != null) {
            holder.tv1.setText(cours.getNom());
            holder.tv2.setText(cours.getNomProf());
            holder.tv3.setText(cours.getHeureD().substring(0, 5) + " - " + cours.getHeureF().substring(0, 5));
        }
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.out.println("onclick1");
                if (mListener != null)
                    mListener.onListItemSelected(i, cours, cal);
            }
        });

        return view;
    }

    public interface OnListItemSelectedListener {
        void onListItemSelected(int position, Cours cours, Calendar cal);
    }
}
