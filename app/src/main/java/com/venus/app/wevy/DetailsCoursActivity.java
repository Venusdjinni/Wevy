package com.venus.app.wevy;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import com.venus.app.Adapters.AbsInfoAdapter;
import com.venus.app.Base.AbstractInformation;
import com.venus.app.Base.Cours;
import com.venus.app.Base.Information;
import com.venus.app.Base.Note;

import java.util.ArrayList;
import java.util.Calendar;

public class DetailsCoursActivity extends AppCompatActivity implements AbsInfoAdapter.OnInfoListInteractionListener {
    private AppCompatTextView prof;
    private AppCompatTextView heure;
    private AppCompatTextView type;
    private AppCompatTextView date;
    private RecyclerView lv_infos;
    private RecyclerView lv_notes;
    private Cours cours;
    private Calendar cal;
    private ArrayList<Information> infos = new ArrayList<>();
    private ArrayList<Note> notes = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details_cours);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        cours = getIntent().getParcelableExtra("cours");
        cal = (Calendar) getIntent().getSerializableExtra("cal");
        prof = (AppCompatTextView) findViewById(R.id.details_prof);
        heure = (AppCompatTextView) findViewById(R.id.details_heure);
        type = (AppCompatTextView) findViewById(R.id.details_type);
        date = (AppCompatTextView) findViewById(R.id.details_date);
        lv_infos = (RecyclerView) findViewById(R.id.details_c_listview_info);
        lv_notes = (RecyclerView) findViewById(R.id.details_c_listview_note);

        // recuperation et separation des AbsInfos
        Parcelable[] ainfos = getIntent().getParcelableArrayExtra("infos");
        for (Parcelable ai : ainfos)
            if (ai instanceof Information)
                infos.add((Information) ai);
            else if (ai instanceof Note)
                notes.add((Note) ai);

        // actualisation des listviews
        if (infos.size() > 0) findViewById(R.id.details_c_no_info).setVisibility(View.GONE);
        if (notes.size() > 0) findViewById(R.id.details_c_no_note).setVisibility(View.GONE);
        lv_infos.setAdapter(new AbsInfoAdapter(infos.toArray(new AbstractInformation[]{}), this));
        lv_notes.setAdapter(new AbsInfoAdapter(notes.toArray(new AbstractInformation[]{}), this));

        // remplissage des champs
        getSupportActionBar().setTitle(cours.getNom());
        prof.setText(cours.getNomProf());
        heure.setText(cours.getHeureD().substring(0, 5) + " - " + cours.getHeureF().substring(0, 5));
        type.setText(cours.getTypeCours().toString());
        date.setText(Utilities.invertDate(Utilities.parseCalendar(cal)) + " (" +
                Utilities.dayToAbrev(cal) + ")");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) finish();

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onListInteraction(int position, AbstractInformation info) {
        if (info instanceof Information)
            startActivity(new Intent(DetailsCoursActivity.this, DetailsInfoActivity.class).putExtra("info", info));
        else if (info instanceof Note)
            startActivity(new Intent(DetailsCoursActivity.this, DetailsNoteActivity.class).putExtra("note", info));
    }
}
