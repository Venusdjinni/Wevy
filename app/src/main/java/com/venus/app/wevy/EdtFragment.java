package com.venus.app.wevy;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.ListViewCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.venus.app.Adapters.CoursWIAdapter;
import com.venus.app.Base.AbstractInformation;
import com.venus.app.Base.Cours;
import com.venus.app.Base.ListeCours;
import com.venus.app.IO.Asyncable;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by arnold on 23/07/17.
 */
public class EdtFragment extends Fragment implements Asyncable, CoursWIAdapter.OnListItemSelectedListener {
    private static final String ARG_EDTF_COURS = "cours";
    private static final String ARG_EDTF_MODE = "mode";
    private static final String ARG_EDTF_POSITION = "position";
    private static final String LFD_INFOS = "infos";
    private ListeCours cours;
    private AbstractInformation[] infos;
    private AppCompatButton b_prev;
    private AppCompatButton b_next;
    private ListViewCompat[] listViewCompats;
    private Calendar lundi; // lundi de la semaine en cours d'affichage
    private String mode;
    private int position;

    public static EdtFragment newInstance(ListeCours cours, String mode, int position) {
        Bundle args = new Bundle();
        args.putParcelable(ARG_EDTF_COURS, cours);
        args.putString(ARG_EDTF_MODE, mode);
        args.putInt(ARG_EDTF_POSITION, position);
        EdtFragment fragment = new EdtFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        cours = getArguments().getParcelable(ARG_EDTF_COURS);
        mode = getArguments().getString(ARG_EDTF_MODE);
        position = getArguments().getInt(ARG_EDTF_POSITION);
        lundi = Calendar.getInstance();
        lundi.add(Calendar.DAY_OF_MONTH,
                (((EdtActivity) getActivity()).transition + (mode.equals(EdtActivity.EDT_PAGER_MODE_H) ? position - 1: 0)) * 7);
        lundi.add(Calendar.DAY_OF_MONTH, - (lundi.get(Calendar.DAY_OF_WEEK) - Calendar.MONDAY));
        Calendar dimanche = (Calendar) lundi.clone();
        dimanche.add(Calendar.DAY_OF_MONTH, 6);
        // recherche des informations
        new LoadFromDbAsc(this, LFD_INFOS).execute(LoadFromDbAsc.PARAM_ABSINFO_WEEK, Utilities.parseCalendar(lundi), Utilities.parseCalendar(dimanche));
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        onCreate(getArguments());
        View v = inflater.inflate(R.layout.fragment_edt, container, false);
        b_prev = (AppCompatButton) v.findViewById(R.id.edt_prev);
        b_prev.setOnClickListener(prev_listener);
        b_next = (AppCompatButton) v.findViewById(R.id.edt_next);
        b_next.setOnClickListener(next_listener);
        listViewCompats = new ListViewCompat[5];
        listViewCompats[0] = (ListViewCompat) v.findViewById(R.id.edt_f_listview1);
        listViewCompats[1] = (ListViewCompat) v.findViewById(R.id.edt_f_listview2);
        listViewCompats[2] = (ListViewCompat) v.findViewById(R.id.edt_f_listview3);
        listViewCompats[3] = (ListViewCompat) v.findViewById(R.id.edt_f_listview4);
        listViewCompats[4] = (ListViewCompat) v.findViewById(R.id.edt_f_listview5);

        if (mode.equals(EdtActivity.EDT_PAGER_MODE_H)) {
            b_next.setVisibility(View.GONE);
            b_prev.setVisibility(View.GONE);
        } else {
            b_next.setVisibility(View.VISIBLE);
            b_prev.setVisibility(View.VISIBLE);
        }

        return v;
    }

    private View.OnClickListener prev_listener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            ((EdtActivity) getActivity()).onPrevButtonPressed();
            // on rafraichit le fragment
            getActivity().getSupportFragmentManager().beginTransaction()
                    .detach(EdtFragment.this).attach(EdtFragment.this).commit();
        }
    };

    private View.OnClickListener next_listener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            ((EdtActivity) getActivity()).onNextButtonPressed();
            // on rafraichit le fragment
            getActivity().getSupportFragmentManager().beginTransaction()
                    .detach(EdtFragment.this).attach(EdtFragment.this).commit();
        }
    };

    @Override
    public void fetchOnlineResult(Object result, String code) {
        if (code.equals(LFD_INFOS)) {
            infos = new AbstractInformation[((Object[]) result).length];
            for (int i = 0; i < infos.length; i++) infos[i] = (AbstractInformation) ((Object[]) result)[i];
            updateGUI();
        }
    }

    void updateGUI() {
        for (int i = 0; i < cours.size(); i++) {
            int key = cours.keyAt(i);
            Cours[] c = cours.get(key).toArray(new Cours[]{});

            // on fabrique la liste des infos du jour concerné
            Calendar cal = (Calendar) lundi.clone();
            cal.add(Calendar.DAY_OF_MONTH, key);
            ArrayList<AbstractInformation> list = new ArrayList<>();
            for (AbstractInformation ai : infos)
                if (ai.getEcheance().equals(Utilities.parseCalendar(cal)))
                    list.add(ai);
            listViewCompats[key].setAdapter(new CoursWIAdapter(this, c, cal, list.toArray(new AbstractInformation[]{})));
            Utilities.justifyListViewHeightBasedOnChildren(listViewCompats[key]);
        }
    }

    @Override
    public void onListItemSelected(int position, Cours cours, Calendar cal) {}

    @Override
    public void onListItemSelected(int position, Cours cours, Calendar cal, AbstractInformation[] infos) {
        // le onClickListener des cours
        Intent i = new Intent(getActivity(), DetailsCoursActivity.class);
        i.putExtra("cours", cours);
        i.putExtra("cal", cal);
        i.putExtra("infos", infos);
        startActivity(i);
    }
}
