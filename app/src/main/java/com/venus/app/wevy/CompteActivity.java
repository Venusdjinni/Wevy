package com.venus.app.wevy;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDialogFragment;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.CardView;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import com.venus.app.DAO.DAO;
import com.venus.app.IO.Asyncable;
import com.venus.app.IO.FetchOnlineAsc;
import com.venus.app.IO.SendToServerAsc;
import com.venus.app.Utils.SimpleMessageDialog;
import com.venus.app.Utils.Terminating;
import com.venus.app.services.MyFirebaseDatabaseListenerService;
import com.venus.app.services.PinnedInformationsService;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;

public class CompteActivity extends AppCompatActivity implements Asyncable, Terminating {
    private static final String FOA_CLASSES = "classes";
    private static final String FOA_SAVE_ACCOUNT = "save account";
    public static boolean deconnexion = false;
    private AppCompatTextView nom;
    private AppCompatTextView classe;
    private CardView cv_nom, cv_mdp, cv_classe, cv_deco;
    private String[] classes;
    private String mdp = null;
    private ProgressDialog loading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compte);
        loading = Utilities.newLoadingDialog(this);

        nom = (AppCompatTextView) findViewById(R.id.compte_nom);
        classe = (AppCompatTextView) findViewById(R.id.compte_classe);
        cv_nom = (CardView) findViewById(R.id.compte_cv_nom);
        cv_mdp = (CardView) findViewById(R.id.compte_cv_mdp);
        cv_classe = (CardView) findViewById(R.id.compte_cv_classe);
        cv_deco = (CardView) findViewById(R.id.compte_cv_deco);

        // remplissage des champs
        nom.setText(MainActivity.preferences.getString(MainActivity.PREF_NAME, MainActivity.PREF_NAME_DEF));
        classe.setText(MainActivity.preferences.getString(MainActivity.PREF_CLASSE, MainActivity.PREF_CLASSE_DEF) +
                (MainActivity.preferences.getBoolean(MainActivity.PREF_IS_ADMIN, false) ? ", admin" : ""));
        classes = new String[]{MainActivity.preferences.getString(MainActivity.PREF_CLASSE, MainActivity.PREF_CLASSE_DEF)};
        cv_nom.setOnClickListener(cv_nom_listener);
        cv_mdp.setOnClickListener(cv_mdp_listener);
        cv_classe.setOnClickListener(cv_classe_listener);
        cv_deco.setOnClickListener(cv_deco_listener);

        (new FetchOnlineAsc(this, MainActivity.PREF_URL_VALUE + "getClasses.php", FOA_CLASSES)).execute();
    }

    private View.OnClickListener cv_nom_listener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            new NewNomDialog().show(getSupportFragmentManager(), "new nom");
        }
    };

    private View.OnClickListener cv_mdp_listener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            new NewMdpDialog().show(getSupportFragmentManager(), "new mdp");
        }
    };

    private View.OnClickListener cv_classe_listener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            ChangeClasseDialog.newInstance(classes).show(getSupportFragmentManager(), "change classe");
        }
    };

    private View.OnClickListener cv_deco_listener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            new DeconnexionDialog().show(getSupportFragmentManager(), "deco");
        }
    };

    @Override
    public void onBackPressed() {
        new ConfDismissDialog().show(getSupportFragmentManager(), "conf dismiss");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_compte, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.ab_compte_valider) {
            // on enregistre les modifications
            new ConfEnregDialog().show(getSupportFragmentManager(), "enreg");
        } else if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    void enregistrer(String oldMdp) {
        String data = "", utf = "UTF-8";
        try {
            data = "nom=" + URLEncoder.encode(nom.getText().toString(), utf) +
                    "&" + "oldMdp=" + URLEncoder.encode(String.valueOf(oldMdp.hashCode()), utf) +
                    "&" + "email=" + MainActivity.preferences.getString(MainActivity.PREF_EMAIL, MainActivity.PREF_EMAIL_DEF) +
                    "&" + "classe=" + URLEncoder.encode(classe.getText().toString().indexOf(',') == -1 ? classe.getText().toString() : classe.getText().toString().substring(0, classe.getText().toString().indexOf(',')), utf);
            if (mdp != null)
                data += "&" + "newMdp=" + URLEncoder.encode(String.valueOf(mdp.hashCode()), utf);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        loading.show();
        new SendToServerAsc(this, MainActivity.PREF_URL_VALUE + "editCompte.php", FOA_SAVE_ACCOUNT).execute(data);
    }

    private String[] convertToStrings(JSONArray array) {
        ArrayList<String> strings = new ArrayList<>();
        try {
            for (int i = 0; i < array.length(); i++)
                strings.add(array.getJSONObject(i).getString("nomClasse"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        strings.remove(0);
        return strings.toArray(new String[]{});
    }

    @Override
    public void fetchOnlineResult(final Object result, String code) {
        loading.dismiss();
        if (code.equals(FOA_CLASSES) && result != null) {
            classes = convertToStrings((JSONArray) result);
        } else if (code.equals(FOA_SAVE_ACCOUNT)) {
            if (result == null || result.toString().isEmpty()) {
                new SimpleMessageDialog().putArguments("Echec de l'enregistrement, veuillez reessayer plus tard")
                        .show(getSupportFragmentManager(), "echec_enreg");
            } else if (result.toString().startsWith("1")) {
                // on enregistre dans les preferences
                SharedPreferences.Editor editor = MainActivity.preferences.edit();
                editor.putString(MainActivity.PREF_NAME, nom.getText().toString());
                editor.putString(MainActivity.PREF_CLASSE, classe.getText().toString().indexOf(',') == -1 ? classe.getText().toString() : classe.getText().toString().substring(0, classe.getText().toString().indexOf(',')));
                editor.apply();
                new SimpleMessageDialog().putArguments("Enregistrement réussi!", true)
                        .show(getSupportFragmentManager(), "enreg_ok");
            } else new SimpleMessageDialog().putArguments("Une erreur est survenue").show(getSupportFragmentManager(), "erreur");
        }
    }

    @Override
    public void terminer() {
        HomeActivity.toRecreate = true;
        finish();
    }

    public static class NewNomDialog extends AppCompatDialogFragment {
        private AppCompatEditText v;

        private void enregistrer() {
            ((CompteActivity) getActivity()).nom.setText(v.getText());
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_edittext, null, false);
            v = (AppCompatEditText) view.findViewById(R.id.edittext);
            v.setInputType(InputType.TYPE_CLASS_TEXT);
            Dialog d = new AlertDialog.Builder(getContext())
                    .setView(view)
                    .setTitle("Nouveau pseudonyme")
                    .setPositiveButton("OK", null)
                    .setNegativeButton("Annuler", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dismiss();
                        }
                    })
                    .create();
            d.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface dialogInterface) {
                    Button button = ((AlertDialog) dialogInterface).getButton(AlertDialog.BUTTON_POSITIVE);
                    button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (v.getText().toString().isEmpty())
                                v.setError("Le champ est obligatoire");
                            else {
                                enregistrer();
                                dismiss();
                            }
                        }
                    });
                }
            });
            return d;
        }
    }

    public static class NewMdpDialog extends AppCompatDialogFragment {
        private static final String ARG_CONF = "conf";
        private AppCompatEditText v;
        private boolean conf;
        private String mdp;

        public static NewMdpDialog newInstance(String mdp) {
            Bundle args = new Bundle();
            args.putString(ARG_CONF, mdp);
            NewMdpDialog fragment = new NewMdpDialog();
            fragment.setArguments(args);
            return fragment;
        }

        private void enregistrer() {
            ((CompteActivity) getActivity()).mdp = v.getText().toString();
        }

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            conf = getArguments() != null && getArguments().containsKey(ARG_CONF);
            if (conf) mdp = getArguments().getString(ARG_CONF);
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_edittext, null, false);
            v = (AppCompatEditText) view.findViewById(R.id.edittext);
            Dialog d = new AlertDialog.Builder(getContext())
                    .setView(view)
                    .setTitle(conf ? "Retaper le mot de passe" : "Nouveau mot de passe")
                    .setPositiveButton("OK", null)
                    .setNegativeButton("Annuler", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dismiss();
                        }
                    })
                    .create();
            d.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface dialogInterface) {
                    Button button = ((AlertDialog) dialogInterface).getButton(AlertDialog.BUTTON_POSITIVE);
                    button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (v.getText().toString().isEmpty())
                                v.setError("Le champ est obligatoire");
                            else if (v.getText().length() < 8)
                                v.setError("Minimum 8 caractères");
                            else if(conf && !v.getText().toString().equals(mdp))
                                v.setError("Mots de passe différents");
                            else {
                                if(conf) enregistrer(); else NewMdpDialog.newInstance(v.getText().toString()).show(getActivity().getSupportFragmentManager(), "conf mdp");
                                dismiss();
                            }
                        }
                    });
                }
            });
            return d;
        }
    }

    public static class ChangeClasseDialog extends AppCompatDialogFragment {
        private String[] classes;

        public static ChangeClasseDialog newInstance(String[] classes) {
            Bundle args = new Bundle();
            args.putStringArray("classes", classes);
            ChangeClasseDialog fragment = new ChangeClasseDialog();
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            classes = getArguments().getStringArray("classes");
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
                    android.R.layout.select_dialog_singlechoice,
                    classes);
            int pos = Arrays.asList(classes).indexOf(MainActivity.preferences.getString(MainActivity.PREF_CLASSE, MainActivity.PREF_CLASSE_DEF));

            DialogInterface.OnClickListener adapter_listener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    ((CompteActivity) getActivity()).classe.setText(classes[i]);
                    dismiss();
                }
            };

            return new AlertDialog.Builder(getActivity())
                    .setSingleChoiceItems(adapter, pos, adapter_listener)
                    .setNegativeButton("Annuler", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dismiss();
                        }
                    })
                    .create();

        }
    }

    public static class ConfEnregDialog extends AppCompatDialogFragment {
        private AppCompatEditText v;

        private void enregistrer() {
            ((CompteActivity) getActivity()).enregistrer(v.getText().toString());
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_edittext, null, false);
            v = (AppCompatEditText) view.findViewById(R.id.edittext);
            return new AlertDialog.Builder(getContext())
                    .setView(view)
                    .setTitle("Vérification: Ancien mot de passe")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            enregistrer();
                        }
                    })
                    .setNegativeButton("Annuler", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dismiss();
                        }
                    })
                    .create();
        }
    }

    public static class DeconnexionDialog extends AppCompatDialogFragment {
        private void deconnexion() {
            // on supprime les preferences, les bd et on rentre à la MainActivity
            ProgressDialog loading = Utilities.newLoadingDialog(getContext());
            loading.show();
            MainActivity.stopAlarms(getContext());
            getActivity().stopService(new Intent(getActivity(), MyFirebaseDatabaseListenerService.class));
            getActivity().stopService(new Intent(getActivity(), PinnedInformationsService.class));
            PinnedInformationsService.getInformations().clear();
            getActivity().deleteDatabase(DAO.NOM);
            MainActivity.preferences.edit().clear().apply();
            loading.dismiss();
            startActivity(new Intent(getActivity(), MainActivity.class));
            ActivityCompat.finishAffinity(getActivity());
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            CompteActivity.deconnexion = true;
            return new AlertDialog.Builder(getContext())
                    .setTitle("Déconnexion")
                    .setMessage("Voulez-vous vraiment vous déconnecter?")
                    .setPositiveButton("Oui", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            deconnexion();
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

    public static class ConfDismissDialog extends AppCompatDialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return new AlertDialog.Builder(getActivity())
                    .setMessage("Voulez-vous vraiment quitter sans enregistrer?")
                    .setPositiveButton("Oui",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    getActivity().finish();
                                }
                            }
                    )
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
