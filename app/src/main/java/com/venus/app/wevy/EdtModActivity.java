package com.venus.app.wevy;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDialogFragment;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.CardView;
import android.support.v7.widget.ListViewCompat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import com.venus.app.Adapters.CoursAdapter;
import com.venus.app.Base.Cours;
import com.venus.app.Base.ListeCours;
import com.venus.app.IO.Asyncable;
import com.venus.app.IO.SendToServerAsc;
import com.venus.app.Utils.SimpleMessageDialog;
import com.venus.app.Utils.Terminating;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;

public class EdtModActivity extends AppCompatActivity
        implements Asyncable, Terminating, CoursAdapter.OnListItemSelectedListener {
    private static final String FOA_SAVE_COURS = "save cours";
    private static final String LFD_COURS_SEMAINE = "cours semaine";
    private CardView[] cardViews;
    private boolean[] isJourOk;
    private ListeCours coursSemaine;
    private ProgressDialog loading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edt_mod);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        loading = Utilities.newLoadingDialog(this);

        // chargement des vues
        cardViews = new CardView[5];
        cardViews[0] = (CardView) findViewById(R.id.edt_mod_lundi);
        cardViews[1] = (CardView) findViewById(R.id.edt_mod_mardi);
        cardViews[2] = (CardView) findViewById(R.id.edt_mod_mercredi);
        cardViews[3] = (CardView) findViewById(R.id.edt_mod_jeudi);
        cardViews[4] = (CardView) findViewById(R.id.edt_mod_vendredi);
        for (int i = 0; i < cardViews.length; i++) ((AppCompatTextView) cardViews[i].findViewById(R.id.lje_jour)).setText(Utilities.DAYS[i]);
        isJourOk = new boolean[] {true, true, true, true, true};

        // on collecte les données
        loading.show();
        new LoadFromDbAsc(this, LFD_COURS_SEMAINE).execute(LoadFromDbAsc.PARAM_COURS);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_edt_mod, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
        } else if (id == R.id.ab_edt_mod_valider) {
            // On verifie que toutes les listes sont ok
            boolean flag = true;
            for (int i = 0; i < 5; i++)
                if (!isJourOk[i]) flag = false;
            if (flag) new ConfEnregDialog().show(getSupportFragmentManager(), "enreg");
            else new SimpleMessageDialog().putArguments("Vérifiez l'emploi du temps!")
                    .show(getSupportFragmentManager(), "verif_edt");
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        // on demande confirmation de quitter avant de le faire, à cause de la perte eventuelle de données
        new ConfDismissDialog().show(getSupportFragmentManager(), "quit");
    }

    void updateGUI(int jour) {
        // on remplit la liste du jour
        try{
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.DAY_OF_WEEK, jour + 1);
            ListViewCompat lv = (ListViewCompat) cardViews[jour].findViewById(R.id.lje_listview);
            lv.setAdapter(new CoursAdapter(this, coursSemaine.get(jour).toArray(new Cours[]{}), cal));
            Utilities.justifyListViewHeightBasedOnChildren(lv);

            checkCours(jour);
        } catch (NullPointerException ignored) {}

        final int finalJour = jour;
        // le listener d'ajout
        cardViews[jour].findViewById(R.id.lje_add)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        EditCoursEdtDialog.newInstance(null, finalJour, -1).show(getSupportFragmentManager(), "editcours");
                    }
                });
    }

    void updateGUI() {
        for (int i = 0; i < 5; i++)
            updateGUI(i);
    }

    void addCoursToJour(Cours cours, int jour, int position) {
        // on ajoute le nouveau cours à liste des cours de la journée
        if (coursSemaine.indexOfKey(jour) < 0) coursSemaine.put(jour, new ArrayList<Cours>());
        if (position != -1) coursSemaine.get(jour).remove(position);
        /**/
        int pos = coursSemaine.get(jour).size();
        for (int i = 0; i < coursSemaine.get(jour).size(); i++) {
            if (cours.getHeureD().compareTo(coursSemaine.get(jour).get(i).getHeureF()) >= 0) {
                if (i + 1 < coursSemaine.get(jour).size()) {
                    if (cours.getHeureF().compareTo(coursSemaine.get(jour).get(i + 1).getHeureD()) <= 0) {
                        pos = i + 1;
                        break;
                    }
                } else {
                    pos = i + 1;
                    break;
                }
            } else if (cours.getHeureF().compareTo(coursSemaine.get(jour).get(i).getHeureD()) <= 0) {
                pos = i;
                break;
            }
        }
        /**/
        coursSemaine.get(jour).add(pos, cours);
        updateGUI(jour);
    }

    void checkCours(int jour) {
        boolean okJour = true;
        for (int j = 0; j < coursSemaine.get(jour).size() - 1; j++) {
            if (!(coursSemaine.get(jour).get(j).getHeureF().compareTo(coursSemaine.get(jour).get(j + 1).getHeureD()) <= 0)) {
                okJour = false;
                break;
            }
        }
        if (okJour) {
            for (int j = 1; j < coursSemaine.get(jour).size(); j++) {
                if (!(coursSemaine.get(jour).get(j).getHeureD().compareTo(coursSemaine.get(jour).get(j - 1).getHeureF()) >= 0)) {
                    okJour = false;
                    break;
                }
            }
        }
        if (!okJour) {
            isJourOk[jour] = false;
            ((AppCompatTextView) cardViews[jour].findViewById(R.id.lje_jour)).setTextColor(getResources().getColor(R.color.colorAccent));
        } else {
            isJourOk[jour] = true;
            ((AppCompatTextView) cardViews[jour].findViewById(R.id.lje_jour)).setTextColor(getResources().getColor(R.color.colorPrimary));
        }
    }

    void deleteCours(int jour, int position) {
        coursSemaine.get(jour).remove(position);
        updateGUI(jour);
    }

    void enregistrerCours() {
        loading.show();

        // on compresse les donnees en json et on les envoient auu serveur
        JSONArray array = new JSONArray();
        JSONObject json;
        try {
            for (int i = 0; i < coursSemaine.size(); i++)
                for (int j = 0; j < coursSemaine.get(coursSemaine.keyAt(i)).size(); j++) {
                    Cours c = coursSemaine.get(coursSemaine.keyAt(i)).get(j);
                    json = new JSONObject();
                    json.put("nomCours", c.getNom());
                    json.put("nomProf", c.getNomProf());
                    json.put("heureD", c.getHeureD());
                    json.put("heureF", c.getHeureF());
                    json.put("typeCours", c.getTypeCours().abbr());
                    json.put("jour", c.getJour());
                    array.put(json);
                }
            System.out.println(array);
            String data = URLEncoder.encode(array.toString(), "UTF-8");
            new SendToServerAsc(this, MainActivity.PREF_URL_VALUE + "editCours.php", FOA_SAVE_COURS)
                    .execute("classe=" + MainActivity.preferences.getString(MainActivity.PREF_CLASSE, MainActivity.PREF_CLASSE_DEF) + "&array=" + data);
        } catch (JSONException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void fetchOnlineResult(Object result, String code) {
        loading.dismiss();
        switch (code) {
            case FOA_SAVE_COURS:
                /*DAOCours dao = new DAOCours(this);
                dao.open();
                dao.drop();
                for (int i = 0; i < coursSemaine.size(); i++)
                    for (int j = 0; j < coursSemaine.get(coursSemaine.keyAt(i)).size(); j++)
                        dao.addCours(coursSemaine.get(coursSemaine.keyAt(i)).get(j));
                dao.close();*/
                if (result == null) new SimpleMessageDialog().putArguments("Echec de la connexion.\n Veuillez réessayer plus tard")
                                        .show(getSupportFragmentManager(), "echec_enreg");
                else if (!result.toString().isEmpty() && result.toString().startsWith("1"))
                    new SimpleMessageDialog().putArguments("Les modifications ont été enregistrées", true).show(getSupportFragmentManager(), "enreg_ok");
                else new SimpleMessageDialog().putArguments("Une erreur est survenue").show(getSupportFragmentManager(), "erreur");
                break;
            case LFD_COURS_SEMAINE:
                coursSemaine = ListeCours.parseArray((Object[]) result);
                updateGUI();
                break;
        }
    }

    @Override
    public void terminer() {
        HomeActivity.toRecreate = true;
        finish();
    }

    @Override
    public void onListItemSelected(int position, Cours cours, Calendar cal) {
        // on determine le jour (int) pour appeler le dialog avec les bons parametres
        // Ici, on utilise une petite astuce: plutot que de retrouver le jour par calcul,
        // on le stocke dans la variable DAY_OF_WEEK, en y ajoutant 1 car ils ne commencent pas à 0
        // on recupere donc le jour par DAY_OF_WEEK - 1
        int jour = cal.get(Calendar.DAY_OF_WEEK) - 1;

        EditCoursEdtDialog.newInstance(cours, jour, position).show(getSupportFragmentManager(), "cours");
    }

    public static class ConfEnregDialog extends AppCompatDialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return new AlertDialog.Builder(getActivity())
                    .setMessage("Voulez-vous vraiment enregistrer ces informations?")
                    .setPositiveButton("Oui",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    ((EdtModActivity) getActivity()).enregistrerCours();
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

    public static class ConfDismissDialog extends AppCompatDialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return new AlertDialog.Builder(getActivity())
                    .setMessage("Voulez-vous vraiment quitter sans enregistrer?")
                    .setPositiveButton("Oui",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    ((Terminating) getActivity()).terminer();
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
