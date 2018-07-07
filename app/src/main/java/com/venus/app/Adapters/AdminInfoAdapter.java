package com.venus.app.Adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.venus.app.Base.Information;
import com.venus.app.Base.Note;
import com.venus.app.wevy.R;

/**
 * Created by arnold on 08/10/17.
 */
public class AdminInfoAdapter extends RecyclerView.Adapter<AdminInfoAdapter.ViewHolder> {
    private Information[] infos;
    private OnInfoListInteractionListener mListener;

    public AdminInfoAdapter(Information[] infos, OnInfoListInteractionListener listener) {
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
    public void onBindViewHolder(final AdminInfoAdapter.ViewHolder holder, int position) {
        final int pos = position;
        holder.item = infos[position];
        holder.tv1.setText(infos[position].getTitre());
        holder.tv2.setText(infos[position].getDescription());
        holder.drw.setText(infos[position].getTitre().substring(0, 1));
        // choix de la couleur en fonction du type d'info
        if (holder.item instanceof Information) {
            // couleurs
            switch (((Information) holder.item).getTypeInformation()) {
                case DEVOIR: holder.drw.setBackgroundResource(R.drawable.circle_letter_devoir); break;
                case EVALUATION: holder.drw.setBackgroundResource(R.drawable.circle_letter_evaluation); break;
                case RATTRAPPAGE: holder.drw.setBackgroundResource(R.drawable.circle_letter_rattrapage); break;
                default: holder.drw.setBackgroundResource(R.drawable.circle_letter_autre); break;
            }
        } else if (holder.item instanceof Note) {
            // couleurs
            holder.drw.setBackgroundResource(R.drawable.circle_letter_note);
        }
        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener != null)
                    mListener.onListInteraction(pos, holder);
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

    public class ViewHolder extends AbsInfoAdapter.ViewHolder {
        private AdminState state = AdminState.NONE;

        public AdminState getState() {
            return state;
        }

        public void setState(AdminState state) {
            this.state = state;
        }

        public ViewHolder(View itemView) {
            super(itemView);
        }
    }

    public enum AdminState {
        NONE,
        ASUPPRIMER,
        AVALIDER
    }

    public interface OnInfoListInteractionListener {
        void onListInteraction(int position, AdminInfoAdapter.ViewHolder holder);
    }
}
