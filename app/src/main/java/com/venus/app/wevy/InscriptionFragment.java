package com.venus.app.wevy;


import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialogFragment;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatSpinner;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.*;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import com.venus.app.IO.Asyncable;
import com.venus.app.IO.FetchOnlineAsc;
import com.venus.app.IO.SendToServerAsc;
import com.venus.app.Utils.SimpleMessageDialog;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link InscriptionFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class InscriptionFragment extends Fragment implements Asyncable {
    private static final String FOA_INSCRIPTION = "inscription";
    private static final String FOA_CLASSES = "classes";
    private static final String ARG_POSITION = "position";
    private int position;
    private static AutoCompleteTextView email, mdp, mdp2, nom;
    private AppCompatButton button;
    private AppCompatSpinner classe;
    private ProgressDialog loading;

    public InscriptionFragment() {
        // Required empty public constructor
    }

    public static InscriptionFragment newInstance(int position) {
        InscriptionFragment fragment = new InscriptionFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_POSITION, position);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        loading = Utilities.newLoadingDialog(getContext());
        position = getArguments().getInt(ARG_POSITION);
        if (position == 1) {
            // on va chercher la liste des classes
            loading.show();
            new FetchOnlineAsc(this, MainActivity.PREF_URL_VALUE + "getClasses.php", FOA_CLASSES).execute();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        if (position == 1) inflater.inflate(R.menu.activity_new_info, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (position == 1) // on rentre au fragment 0
                ((InscriptionActivity) getActivity()).goToPrevious();
        } else if (item.getItemId() == R.id.ab_newi_valider) {
            // On enregistre les informations
            boolean flag = true;
            if (nom.getText().toString().isEmpty()) {
                flag = false;
                nom.setError("Champ requis");
            }
            if (classe.getSelectedItemPosition() == 0) {
                flag = false;
                new SimpleMessageDialog().putArguments("Choisissez une classe!")
                        .show(getActivity().getSupportFragmentManager(), "choix classe");
            }
            if (flag) enregister();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = null;
        if (position == 0) {
            view = inflater.inflate(R.layout.fragment_inscription1, container, false);
            email = (AutoCompleteTextView) view.findViewById(R.id.insc_email);
            mdp = (AutoCompleteTextView) view.findViewById(R.id.insc_mdp);
            mdp2 = (AutoCompleteTextView) view.findViewById(R.id.insc_mdp2);
            mdp2.addTextChangedListener(mdp2_listener);
            button = (AppCompatButton) view.findViewById(R.id.insc_suivant);
            button.setOnClickListener(button_listener);
        } else if (position == 1) {
            view = inflater.inflate(R.layout.fragment_inscription2, container, false);
            nom = (AutoCompleteTextView) view.findViewById(R.id.insc_nom);
            classe = (AppCompatSpinner) view.findViewById(R.id.insc_classe);
        }

        return view;
    }

    private TextWatcher mdp2_listener = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            if (!charSequence.toString().equals(mdp.getText().toString()))
                mdp2.setError("Mots de passe différents");
        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    };

    private View.OnClickListener button_listener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            boolean flag = true;
            if (email.getText().toString().isEmpty()) {
                flag = false;
                email.setError("Champ requis");
            }
            if (!Utilities.isEmailValid(email.getText())) {
                flag = false;
                email.setError("Email incorrect");
            }
            if (mdp.getText().toString().isEmpty()) {
                flag = false;
                mdp.setError("Champ requis");
            }
            if (mdp.getText().length() < 8)
            {
                flag = false;
                mdp.setError("Minimum 8 caractères");
            }
            if (mdp2.getText().toString().isEmpty()) {
                flag = false;
                mdp2.setError("Champ requis");
            }
            if (!mdp2.getText().toString().equals(mdp.getText().toString())) {
                flag = false;
                mdp2.setError("Mots de passe différents");
            }
            if (flag) ((InscriptionActivity) getActivity()).goToNext();
        }
    };

    void enregister() {
        String data = "", utf = "UTF-8";
        try {
            data = "email=" + URLEncoder.encode(email.getText().toString(), utf) +
                    "&" + "mdp=" + URLEncoder.encode(String.valueOf(mdp.getText().toString().hashCode()), utf) +
                    "&" + "nom=" + URLEncoder.encode(nom.getText().toString(), utf) +
                    "&" + "classe=" + URLEncoder.encode(classe.getSelectedItem().toString(), utf);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        loading.show();
        new SendToServerAsc(this, MainActivity.PREF_URL_VALUE + "inscription.php", FOA_INSCRIPTION).execute(data);
    }

    @Override
    public void fetchOnlineResult(Object result, String code) {
        loading.dismiss();
        if (getActivity() == null) return;
        switch (code) {
            case FOA_CLASSES:
                if (result == null) // de la connexion
                    new SimpleMessageDialog().putArguments("Echec de la connexion.\n Veuillez réessayer plus tard", true).show(getActivity().getSupportFragmentManager(), "echec_conn");
                else if (result instanceof JSONArray) {
                    JSONArray array = (JSONArray) result;
                    ArrayList<String> classes = new ArrayList<>();
                    classes.add("");
                    try {
                        for (int i = 0; i < array.length(); i++)
                            classes.add(array.getJSONObject(i).getString("nomClasse"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    classes.remove(MainActivity.PREF_CLASSE_DEF);
                    // on ajoute l'adapter
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, classes);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    classe.setAdapter(adapter);
                }
                break;
            case FOA_INSCRIPTION:
                if (result == null || result.toString().isEmpty()) // echec de connexion
                    new SimpleMessageDialog().putArguments("Echec de la connexion.\n Veuillez réessayer plus tard").show(getActivity().getSupportFragmentManager(), "echec_conn");
                else if (result.toString().startsWith("1")) {
                    // on enregistre les données de compte
                    SharedPreferences.Editor editor = MainActivity.preferences.edit();
                    editor.putString(MainActivity.PREF_NAME, nom.getText().toString())
                            .putString(MainActivity.PREF_EMAIL, email.getText().toString())
                            .putString(MainActivity.PREF_CLASSE, classe.getSelectedItem().toString())
                            .apply();
                    // on active les alarmes
                    MainActivity.setAlarms(getContext());
                    // enregistrement réussi
                    new InscriptionOkDialog().show(getActivity().getSupportFragmentManager(), "insc_ok");

                } else if(result.toString().startsWith("0"))
                    new SimpleMessageDialog().putArguments(result.toString().substring(1)).show(getActivity().getSupportFragmentManager(), "insc_pb");
                else new SimpleMessageDialog().putArguments("Un problème est survenu...").show(getActivity().getSupportFragmentManager(), "insc_failed");
                break;
        }
    }

    public static class InscriptionOkDialog extends AppCompatDialogFragment {
        private void goToHomeActivity() {
            // on actualise le token
            new ResetFirebaseToken().execute();

            startActivity(new Intent(getActivity(), HomeActivity.class));
            ActivityCompat.finishAffinity(getActivity());
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return new AlertDialog.Builder(getContext())
                    .setTitle("Information")
                    .setMessage("Votre compte a été créé")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            goToHomeActivity();
                        }
                    })
                    .create();
        }
    }
}
