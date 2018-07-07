package com.venus.app.wevy;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialogFragment;
import android.support.v7.widget.AppCompatEditText;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.RadioGroup;
import com.venus.app.Base.Cours;
import com.venus.app.Base.TypeCours;

/**
 * Created by arnold on 13/08/17.
 */
public class EditCoursEdtDialog extends AppCompatDialogFragment {
    private static final String ARG_COURS = "cours";
    private static final String ARG_JOUR = "jour";
    private static final String ARG_POSITION = "position";
    private Cours cours;
    private int jour;
    private int position;
    private AutoCompleteTextView nom, prof;
    private AppCompatEditText debut, fin;
    private RadioGroup type;

    public static EditCoursEdtDialog newInstance(Cours cours, int jour, int position) {

        Bundle args = new Bundle();
        args.putParcelable(ARG_COURS, cours);
        args.putInt(ARG_JOUR, jour);
        args.putInt(ARG_POSITION, position);
        EditCoursEdtDialog fragment = new EditCoursEdtDialog();
        fragment.setArguments(args);
        return fragment;
    }

    private void enregistrer() {
        // on ajoute le cours au jour concerné
        cours.setNom(nom.getText().toString());
        cours.setNomProf(prof.getText().toString());
        cours.setHeureD(debut.getText().toString());
        cours.setHeureF(fin.getText().toString());
        cours.setJour(jour);
        cours.setTypeCours(TypeCours.buildFromAbbr
                (type.getCheckedRadioButtonId() == R.id.f_editcoursedt_t_cm ? "CM" : "TD"));
        // et on enregistre
        ((EdtModActivity) getActivity()).addCoursToJour(cours, jour, position);
    }

    private void supprimer() {
        ((EdtModActivity) getActivity()).deleteCours(jour, position);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        cours = getArguments().getParcelable(ARG_COURS);
        position = getArguments().getInt(ARG_POSITION);
        jour = getArguments().getInt(ARG_JOUR);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View v = getActivity().getLayoutInflater().inflate(R.layout.dialog_editcoursedt, null, false);
        nom = (AutoCompleteTextView) v.findViewById(R.id.f_editcoursedt_nom);
        prof = (AutoCompleteTextView) v.findViewById(R.id.f_editcoursedt_prof);
        debut = (AppCompatEditText) v.findViewById(R.id.f_editcoursedt_heured);
        debut.setOnClickListener(heure_listener);
        fin = (AppCompatEditText) v.findViewById(R.id.f_editcoursedt_heuref);
        fin.setOnClickListener(heure_listener);
        type = (RadioGroup) v.findViewById(R.id.f_editcoursedt_type);

        if (cours == null) {
            // un nouveau cours à créer
            cours = new Cours();
        } else {
            // on recopie les données
            nom.setText(cours.getNom());
            prof.setText(cours.getNomProf());
            debut.setText(cours.getHeureD());
            fin.setText(cours.getHeureF());
            switch (cours.getTypeCours()) {
                case CM: type.check(R.id.f_editcoursedt_t_cm); break;
                case TD: type.check(R.id.f_editcoursedt_t_td); break;
                default: type.clearCheck(); break;
            }
        }

        AlertDialog.Builder adb = new AlertDialog.Builder(getActivity())
                .setView(v)
                .setPositiveButton("OK", null)
                .setNegativeButton("Annuler", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dismiss();
                    }
                });
        if (position != -1) adb.setNeutralButton("Supprimer", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                supprimer();
            }
        });
        AlertDialog dialog = adb.create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                Button button = ((AlertDialog) dialogInterface).getButton(AlertDialog.BUTTON_POSITIVE);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // contrôles
                        boolean flag = true;
                        if (nom.getText().toString().isEmpty()) {
                            nom.setError("Champ requis");
                            flag = false;
                        }
                        if (prof.getText().toString().isEmpty()) {
                            prof.setError("Champ requis");
                            flag = false;
                        }
                        if (debut.getText().toString().isEmpty()) {
                            debut.setError("Champ requis");
                            flag = false;
                        }
                        if (fin.getText().toString().isEmpty()) {
                            fin.setError("Champ requis");
                            flag = false;
                        }
                        if (debut.getText().toString().compareTo(fin.getText().toString()) >= 0) {
                            debut.setError("");
                            fin.setError("");
                            flag = false;
                        }
                        if (type.getCheckedRadioButtonId() == -1) flag = false;

                        if (flag) {
                            enregistrer();
                            dismiss();
                        }
                    }
                });
            }
        });

        return dialog;
    }

    private View.OnClickListener heure_listener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            TimePickerFragment timepicker = new TimePickerFragment();
            timepicker.setView((AppCompatEditText) view);
            timepicker.show(getActivity().getSupportFragmentManager(), "heure");
        }
    };
}
