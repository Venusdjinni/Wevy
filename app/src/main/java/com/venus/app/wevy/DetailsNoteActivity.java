package com.venus.app.wevy;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDialogFragment;
import android.support.v7.widget.AppCompatTextView;
import android.view.Menu;
import android.view.MenuItem;
import com.venus.app.Base.AbstractInformation;
import com.venus.app.Base.Note;
import com.venus.app.DAO.DAONote;
import com.venus.app.Utils.SimpleMessageDialog;
import com.venus.app.Utils.Terminating;
import com.venus.app.services.PinnedInformationsService;

import java.util.Collections;

public class DetailsNoteActivity extends AppCompatActivity implements Terminating {
    private AppCompatTextView enreg;
    private AppCompatTextView echeance;
    private AppCompatTextView description;
    private Note note;
    private boolean fromPin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details_note);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        note = getIntent().getParcelableExtra("note");

        enreg = (AppCompatTextView) findViewById(R.id.details_n_enreg);
        echeance = (AppCompatTextView) findViewById(R.id.details_n_echeance);
        description = (AppCompatTextView) findViewById(R.id.details_n_description);

        //
        getSupportActionBar().setTitle(note.getTitre());
        enreg.setText(Utilities.invertDate(note.getDateEnreg()) + " (" +
                Utilities.dayToAbrev(Utilities.parseCalendar(note.getDateEnreg())) + ")");
        echeance.setText(Utilities.invertDate(note.getEcheance()) + " (" +
                Utilities.dayToAbrev(Utilities.parseCalendar(note.getEcheance())) + ")");
        description.setText(note.getDescription());

        fromPin = getIntent().getBooleanExtra("fromPin", false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_details_note, menu);
        if (PinnedInformationsService.getInformations().contains(note))
            menu.getItem(0).setTitle("Désépingler");
        else menu.getItem(0).setTitle("Epingler");
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onBackPressed() {
        if (fromPin)
            startActivity(new Intent(this, MainActivity.class));
        super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.ab_details_note_pin) {
            if (PinnedInformationsService.getInformations().contains(note))
                PinnedInformationsService.startService(this, null, Collections.singletonList((AbstractInformation) note));
            else PinnedInformationsService.startService(this, note);
            invalidateOptionsMenu();
        } else if (item.getItemId() == R.id.ab_details_note_supp) {
            // dialogue de confirmation
            (new ConfSupprNoteDialog()).show(getSupportFragmentManager(), "conf_sup_note");
        } else if (item.getItemId() == android.R.id.home) {
            if (fromPin)
                startActivity(new Intent(this, MainActivity.class));
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void terminer() {
        finish();
    }

    public static class ConfSupprNoteDialog extends AppCompatDialogFragment {
        private void supprimer() {
            DAONote dao = new DAONote(getContext());
            dao.open();
            dao.removeNote(((DetailsNoteActivity) getActivity()).note.getIdNote());
            (new SimpleMessageDialog()).putArguments("Note supprimée!", true)
                    .show(getActivity().getSupportFragmentManager(), "note_supp");
            dao.close();
            PinnedInformationsService.startService(getContext(), null, Collections.singletonList((AbstractInformation) ((DetailsNoteActivity) getActivity()).note));
            HomeActivity.toRecreate = true;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return new AlertDialog.Builder(getActivity())
                    .setMessage("Voulez-vous vraiment supprimer cette note?")
                    .setPositiveButton("Oui", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            supprimer();
                        }
                    })
                    .setNegativeButton("Non", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dismiss();
                        }
                    })
                    .create();
        }
    }
}
