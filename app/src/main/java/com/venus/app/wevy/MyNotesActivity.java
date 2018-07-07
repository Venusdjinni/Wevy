package com.venus.app.wevy;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDialogFragment;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import com.venus.app.Adapters.AbsInfoAdapter;
import com.venus.app.Base.AbstractInformation;
import com.venus.app.Base.Note;
import com.venus.app.DAO.DAOCours;
import com.venus.app.DAO.DAONote;
import com.venus.app.Utils.SimpleMessageDialog;
import com.venus.app.services.PinnedInformationsService;

import java.util.ArrayList;
import java.util.Calendar;

import static com.venus.app.DAO.DAOCours.COURS_NOM;

public class MyNotesActivity extends AppCompatActivity
        implements AbsInfoAdapter.OnInfoListInteractionListener {
    private RecyclerView recyclerView;
    private FloatingActionButton fab;
    private AppCompatTextView no_notes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_notes);

        recyclerView = (RecyclerView) findViewById(R.id.notes_recyclerview);
        fab = (FloatingActionButton) findViewById(R.id.notes_fab);
        fab.setOnClickListener(fab_listener);
        no_notes = (AppCompatTextView) findViewById(R.id.notes_no_notes);
        updateNotes();
        if (getIntent().getBooleanExtra("newNote", false))
            new NewNoteDialog().show(getSupportFragmentManager(), "newnote");
    }

    private View.OnClickListener fab_listener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            (new NewNoteDialog()).show(getSupportFragmentManager(), "newnote");
        }
    };

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home)
            finish();

        return super.onOptionsItemSelected(item);
    }

    void updateNotes() {
        DAONote dao = new DAONote(this);
        dao.open();
        //
        Note[] notes = dao.getAllNotes();
        dao.close();
        recyclerView.setAdapter(new AbsInfoAdapter(notes, this));
        if (notes.length == 0) no_notes.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateNotes();
    }

    @Override
    public void onListInteraction(int position, AbstractInformation info) {
        startActivity(new Intent(MyNotesActivity.this, DetailsNoteActivity.class).putExtra("note", info));
    }

    public static class NewNoteDialog extends AppCompatDialogFragment {
        private AppCompatSpinner spinner;
        private AppCompatEditText echeance;
        private AppCompatEditText description;
        private LinearLayout linearLayout;
        private AppCompatEditText titre;

        private void enregistrer() {
            // on enregistre et on actualise la liste des notes
            Note note = new Note();
            note.setTitre(linearLayout.getVisibility() == View.VISIBLE ? titre.getText().toString() : spinner.getSelectedItem().toString());
            note.setDateEnreg(Utilities.parseCalendar(Calendar.getInstance()));
            note.setEcheance(Utilities.invertDate(echeance.getText().toString()));
            note.setDescription(description.getText().toString());

            // on enregistre dans la bd interne
            DAONote dao = new DAONote(getContext());
            dao.open();
            int id = dao.addNote(note);

            // On ajoute dans la liste epinglée
            if (note.getEcheance().equals(Utilities.parseCalendar(Calendar.getInstance())))
                PinnedInformationsService.startService(getContext(), dao.getNote(id));

            dao.close();
            (new SimpleMessageDialog()).putArguments("Enregistrement réussi!");
            HomeActivity.toRecreate = true;

            // On actualise l'activité
            ((MyNotesActivity) getActivity()).updateNotes();
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            View v = getActivity().getLayoutInflater().inflate(R.layout.activity_new_info, null, false);
            v.findViewById(R.id.type_ll).setVisibility(View.GONE);
            spinner = (AppCompatSpinner) v.findViewById(R.id.newi_spinner);
            echeance = (AppCompatEditText) v.findViewById(R.id.newi_echeance);
            echeance.setOnClickListener(echeance_listener);
            description = (AppCompatEditText) v.findViewById(R.id.newi_description);
            linearLayout = (LinearLayout) v.findViewById(R.id.newi_ll);
            titre = (AppCompatEditText) v.findViewById(R.id.newi_titre);

            // on cherche la liste des cours
            //

            ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, getAllDistinctsCoursName());
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(adapter);
            spinner.setOnItemSelectedListener(spinner_listener);

            AlertDialog dialog = new AlertDialog.Builder(getActivity())
                    .setView(v)
                    .setPositiveButton("Ok", null)
                    .setNegativeButton("Annuler", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dismiss();
                        }
                    })
                    .create();
            dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface dialogInterface) {
                    Button button = ((AlertDialog) dialogInterface).getButton(AlertDialog.BUTTON_POSITIVE);
                    button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            boolean isOk = true;
                            if (linearLayout.getVisibility() == View.VISIBLE && titre.getText().toString().isEmpty()) {
                                titre.setError("Champ requis");
                                isOk = false;
                            }
                            if (echeance.getText().toString().isEmpty() || Utilities.parseCalendar(Utilities.invertDate(echeance.getText().toString())).compareTo(Calendar.getInstance()) < 0) {
                                // l'echeance est antérieure à la date du jour
                                echeance.setError("La date est invalide");
                                isOk = false;
                            }
                            if (description.getText().toString().isEmpty()) {
                                description.setError("Le champ est requis");
                                isOk = false;
                            }
                            if (isOk) {
                                enregistrer();
                                dismiss();
                            }
                        }
                    });
                }
            });

            return dialog;
        }

        private View.OnClickListener echeance_listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                (new DatePickerFragment()).
                        setEditText(echeance)
                        .show(getActivity().getSupportFragmentManager(), "datePicker");
            }
        };

        String[] getAllDistinctsCoursName() {
            DAOCours dao = new DAOCours(getContext());
            dao.open();
            ArrayList<String> cours = new ArrayList<>();
            cours.add("(Aucune)");
            Cursor cursor = dao.rawQuery("select distinct " + COURS_NOM + " from " + dao.TABLE_NAME()
                    + " order by " + COURS_NOM + " asc", new String[]{});

            while (cursor.moveToNext())
                cours.add(cursor.getString(0));
            cursor.close();

            return cours.toArray(new String[]{});
        }

        private AdapterView.OnItemSelectedListener spinner_listener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (i == 0) {
                    // la case "aucune"
                    linearLayout.setVisibility(View.VISIBLE);
                    titre.setText("");
                } else linearLayout.setVisibility(View.GONE);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        };
    }
}
