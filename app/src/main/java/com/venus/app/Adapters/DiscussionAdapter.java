package com.venus.app.Adapters;

import android.support.v7.widget.AppCompatTextView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import com.venus.app.Base.Discussion;
import com.venus.app.DAO.DAODiscussion;
import com.venus.app.wevy.R;

/**
 * Created by arnold on 27/07/17.
 */
public class DiscussionAdapter extends BaseAdapter {
    private Discussion[] discussions;
    private OnDiscListInteractionListener mListener;

    public DiscussionAdapter(Discussion[] discussions, OnDiscListInteractionListener listener) {
        this.discussions = discussions;
        this.mListener = listener;
    }

    @Override
    public int getCount() {
        return discussions.length;
    }

    @Override
    public Object getItem(int i) {
        return discussions[i];
    }

    @Override
    public long getItemId(int i) {
        return discussions[i].getIdDiscussion();
    }

    private class Holder {
        AppCompatTextView tv1;
        AppCompatTextView tv2;
        AppCompatTextView drw;
        AppCompatTextView count;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        Holder holder = new Holder();
        if(view == null){
            view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_discussion, viewGroup, false);
            holder.tv1 = (AppCompatTextView) view.findViewById(R.id.disc_titre);
            holder.tv2 = (AppCompatTextView) view.findViewById(R.id.disc_details);
            holder.drw = (AppCompatTextView) view.findViewById(R.id.disc_ic);
            holder.count = (AppCompatTextView) view.findViewById(R.id.disc_count);
            view.setTag(holder);
        } else holder = (Holder) view.getTag();
        final Discussion disc = (Discussion) getItem(i);
        if(disc != null) {
            holder.tv1.setText(disc.getTitre());
            holder.tv2.setText(disc.getMessageCount() + " message" + (disc.getMessageCount() > 1 ? "s" : ""));
            holder.drw.setText(Integer.toString(i + 1));
            if (((DAODiscussion.DiscItem) disc).getNonLus() > 0) {
                holder.count.setText(Integer.toString(((DAODiscussion.DiscItem) disc).getNonLus()));
                holder.count.setVisibility(View.VISIBLE);
            }
        }
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener != null)
                    mListener.onListInteraction(disc);
            }
        });

        return view;
    }

    public void clear() {
        discussions = new Discussion[]{};
        notifyDataSetChanged();
    }

    public interface OnDiscListInteractionListener {
        void onListInteraction(Discussion discussion);
    }
}
