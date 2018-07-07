package com.venus.app.Adapters;

import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.venus.app.Base.AbstractInformation;
import com.venus.app.Base.Information;
import com.venus.app.Base.Note;
import com.venus.app.BaseView.VInformation;
import com.venus.app.wevy.R;

/**
 * Created by arnold on 26/07/17.
 */
public class AbsInfoAdapter extends RecyclerView.Adapter<AbsInfoAdapter.ViewHolder> {
    private AbstractInformation[] infos;
    private OnInfoListInteractionListener mListener;

    public AbsInfoAdapter(AbstractInformation[] infos, OnInfoListInteractionListener listener) {
        this.infos = infos;
        this.mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_info, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.item = infos[position];
        holder.tv1.setText(infos[position].getTitre());
        holder.tv2.setText(infos[position].getDescription());
        holder.drw.setText(infos[position].getTitre().substring(0, 1));
        // choix de la couleur en fonction du type d'info
        if (holder.item instanceof Information) {
            // couleurs
            if (((Information) holder.item).getValide()) {
                switch (((Information) holder.item).getTypeInformation()) {
                    case DEVOIR: holder.drw.setBackgroundResource(R.drawable.circle_letter_devoir); break;
                    case EVALUATION: holder.drw.setBackgroundResource(R.drawable.circle_letter_evaluation); break;
                    case RATTRAPPAGE: holder.drw.setBackgroundResource(R.drawable.circle_letter_rattrapage); break;
                    default: holder.drw.setBackgroundResource(R.drawable.circle_letter_autre); break;
                }
            } else {
                holder.tv1.setText("(Non valide) " + holder.item.getTitre());
                holder.drw.setBackgroundResource(R.drawable.circle_letter_autre);
                holder.drw.setText("?");
            }

            // count
            if (holder.item instanceof VInformation && ((VInformation) holder.item).getCount() > 0) {
                holder.tvCount.setVisibility(View.VISIBLE);
                holder.tvCount.setText(String.valueOf(((VInformation) holder.item).getCount()));
            } else holder.tvCount.setVisibility(View.GONE);
        } else if (holder.item instanceof Note) {
            // couleurs
            holder.drw.setBackgroundResource(R.drawable.circle_letter_note);
        }
        final int p = position;
        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener != null)
                    mListener.onListInteraction(p, holder.item);
            }
        });
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public int getItemCount() {
        return infos.length;
    }

    public void updateVInformation(int id) {
        for (AbstractInformation ai : infos)
            if (ai instanceof VInformation && ((VInformation) ai).getIdInformation() == id) {
                ((VInformation) ai).setCount(((VInformation) ai).getCount() + 1);
                notifyDataSetChanged();
                return;
            }
    }

    public void resetVInformation(int id) {
        for (AbstractInformation ai : infos)
            if (ai instanceof VInformation && ((VInformation) ai).getIdInformation() == id) {
                ((VInformation) ai).setCount(0);
                notifyDataSetChanged();
                return;
            }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public View view;
        public AppCompatTextView tv1;
        public AppCompatTextView tv2;
        public AppCompatTextView drw;
        public AppCompatTextView tvCount;
        public AbstractInformation item;

        public ViewHolder(View itemView) {
            super(itemView);
            this.view = itemView;
            tv1 = (AppCompatTextView) view.findViewById(R.id.info_titre);
            tv2 = (AppCompatTextView) view.findViewById(R.id.info_details);
            drw = (AppCompatTextView) view.findViewById(R.id.info_ic);
            tvCount = (AppCompatTextView) view.findViewById(R.id.info_count);
        }
    }

    public interface OnInfoListInteractionListener {
        void onListInteraction(int position, AbstractInformation info);
    }
}
