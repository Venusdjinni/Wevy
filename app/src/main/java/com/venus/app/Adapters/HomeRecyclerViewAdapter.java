package com.venus.app.Adapters;

import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.venus.app.Base.AbstractInformation;
import com.venus.app.Base.Information;
import com.venus.app.wevy.R;
import com.venus.app.wevy.Utilities;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.TreeMap;

/**
 * Created by arnold on 25/12/17.
 */
public class HomeRecyclerViewAdapter extends RecyclerView.Adapter<HomeRecyclerViewAdapter.ViewHolder> {
    private TreeMap<String, ArrayList<AbstractInformation>> liste;
    private ArrayList<String> mKeys;
    private ArrayList<ViewHolder> viewHolders;
    private AbsInfoAdapter.OnInfoListInteractionListener listener;

    public HomeRecyclerViewAdapter(AbsInfoAdapter.OnInfoListInteractionListener listener) {
        this.listener = listener;
        liste = new TreeMap<>();
    }

    public HomeRecyclerViewAdapter(TreeMap<String, ArrayList<AbstractInformation>> liste, AbsInfoAdapter.OnInfoListInteractionListener listener) {
        this.liste = liste;
        this.listener = listener;
        mKeys = new ArrayList<>(liste.keySet());
        viewHolders = new ArrayList<>();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.layout_content_home, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.item = liste.get(mKeys.get(position));

        int daysBetween = Utilities.daysBetween(Utilities.parseCalendar(holder.item.get(0).getEcheance()), Calendar.getInstance());
        if (daysBetween == 0) holder.tv.setText("Aujourd'hui");
        else if (daysBetween == 1) holder.tv.setText("Demain");
        else if (daysBetween == -1) holder.tv.setText("Hier");
        else if (daysBetween > 1 && daysBetween <= 5) holder.tv.setText("Dans " + daysBetween + " jours");
        else if (daysBetween < -1 && daysBetween >= -5) holder.tv.setText("Il y a " + Math.abs(daysBetween) + " jours");
        else holder.tv.setText(Utilities.invertDate(holder.item.get(0).getEcheance()));

        holder.rv.setAdapter(new AbsInfoAdapter(holder.item.toArray(new AbstractInformation[]{}), listener));
        viewHolders.add(position, holder);
    }

    @Override
    public int getItemCount() {
        return liste.size();
    }

    public void setData(TreeMap<String, ArrayList<AbstractInformation>> liste) {
        this.liste = liste;
        mKeys = new ArrayList<>(liste.keySet());
        viewHolders = new ArrayList<>();
        notifyDataSetChanged();
    }

    public void updateVInformation(int idInfo) {
        for (int i = 0; i < mKeys.size(); i++) {
            ArrayList<AbstractInformation> a = liste.get(mKeys.get(i));
            for (AbstractInformation ai : a)
                if (ai instanceof Information && ((Information) ai).getIdInformation() == idInfo) {
                    ((AbsInfoAdapter) viewHolders.get(i).rv.getAdapter()).updateVInformation(idInfo);
                    return;
                }
        }
    }

    public void resetVInformation(int idInfo) {
        for (int i = 0; i < mKeys.size(); i++) {
            ArrayList<AbstractInformation> a = liste.get(mKeys.get(i));
            for (AbstractInformation ai : a)
                if (ai instanceof Information && ((Information) ai).getIdInformation() == idInfo) {
                    ((AbsInfoAdapter) viewHolders.get(i).rv.getAdapter()).resetVInformation(idInfo);
                    return;
                }
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        View view;
        AppCompatTextView tv;
        RecyclerView rv;
        ArrayList<AbstractInformation> item;

        public ViewHolder(View itemView) {
            super(itemView);
            this.view = itemView;
            tv = (AppCompatTextView) view.findViewById(R.id.home_textview);
            rv = (RecyclerView) view.findViewById(R.id.home_listview);
        }
    }
}
