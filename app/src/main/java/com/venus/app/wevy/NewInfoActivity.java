package com.venus.app.wevy;

import android.app.ProgressDialog;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.AppCompatSpinner;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import com.venus.app.Base.Information;
import com.venus.app.Base.TypeInformation;
import com.venus.app.DAO.DAOCours;
import com.venus.app.IO.Asyncable;
import com.venus.app.IO.SendToServerAsc;
import com.venus.app.Utils.SimpleMessageDialog;
import com.venus.app.Utils.Terminating;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;

import static com.venus.app.DAO.DAOCours.COURS_NOM;

public class NewInfoActivity extends AppCompatActivity implements Asyncable, Terminating {
    private static final String FOA_SENDTOSERVER = "sendtoserver";
    private AppCompatSpinner spinner;
    private AppCompatSpinner type;
    private AppCompatEditText echeance;
    private AppCompatEditText description;
    private LinearLayout linearLayout;
    private AppCompatEditText titre;
    private Information information;
    private ProgressDialog loading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_info);

        loading = Utilities.newLoadingDialog(this);
        spinner = (AppCompatSpinner) findViewById(R.id.newi_spinner);
        type = (AppCompatSpinner) findViewById(R.id.spinner_type);
        echeance = (AppCompatEditText) findViewById(R.id.newi_echeance);
        echeance.setOnClickListener(echeance_listener);
        description = (AppCompatEditText) findViewById(R.id.newi_description);
        linearLayout = (LinearLayout) findViewById(R.id.newi_ll);
        titre = (AppCompatEditText) findViewById(R.id.newi_titre);

        // Remplissage des spinners
        ArrayAdapter<TypeInformation> typeAdapter = new ArrayAdapter<TypeInformation>(this, android.R.layout.simple_spinner_item, TypeInformation.getValues());
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        type.setAdapter(typeAdapter);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, getAllDistinctsCoursName());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(spinner_listener);
    }

    String[] getAllDistinctsCoursName() {
        DAOCours dao = new DAOCours(this);
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

    private View.OnClickListener echeance_listener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            (new DatePickerFragment()).
                    setEditText(echeance)
                    .show(getSupportFragmentManager(), "datePicker");
        }
    };

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_new_info, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.ab_newi_valider) {
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
                loading.show();
                enregistrerInfo();
            }
        } else if (item.getItemId() == android.R.id.home)
            finish();
        return super.onOptionsItemSelected(item);
    }

    private void enregistrerInfo() {
        information = new Information(
                linearLayout.getVisibility() == View.VISIBLE ? titre.getText().toString() : spinner.getSelectedItem().toString(),
                Utilities.parseCalendar(Calendar.getInstance()),
                description.getText().toString(),
                Utilities.invertDate(echeance.getText().toString()),
                (TypeInformation) type.getSelectedItem());
        information.setAuteur(MainActivity.preferences.getString(MainActivity.PREF_EMAIL, MainActivity.PREF_EMAIL_DEF));
        information.setValide(MainActivity.preferences.getBoolean(MainActivity.PREF_IS_ADMIN, false));

        // et on envoie au serveur
        String data, utf = "UTF-8";
        try {
            data = "titre=" + URLEncoder.encode(information.getTitre(), utf) +
                    "&" + "enreg=" + URLEncoder.encode(information.getDateEnreg(), utf) +
                    "&" + "echeance=" + URLEncoder.encode(information.getEcheance(), utf) +
                    "&" + "valide=" + (information.getValide() ? "1" : "0") +
                    "&" + "auteur=" + URLEncoder.encode(information.getAuteur(), utf) +
                    "&" + "type=" + URLEncoder.encode(information.getTypeInformation().toString(), utf) +
                    "&" + "description=" + URLEncoder.encode(information.getDescription(), utf);
            (new SendToServerAsc(this, MainActivity.PREF_URL_VALUE + "editInformation.php", FOA_SENDTOSERVER)).execute(data);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void fetchOnlineResult(Object result, String code) {
        loading.dismiss();
        /*DAOInformation dao = new DAOInformation(this);
        dao.open();
        dao.addInfo(information);
        dao.close();*/
        if (result != null && !result.toString().isEmpty()) {
            switch (code) {
                case FOA_SENDTOSERVER:
                    if (result.toString().startsWith("1"))
                        (new SimpleMessageDialog()).putArguments("Enregistrement réussi!", true)
                                .show(getSupportFragmentManager(), "enreg_ok");
                    else (new SimpleMessageDialog()).putArguments("Une erreur est survenue")
                            .show(getSupportFragmentManager(), "enreg_pb");
                    break;
            }
        } else (new SimpleMessageDialog()).putArguments("Echec de l'enregistrement")
                    .show(getSupportFragmentManager(), "enreg_echec");
    }

    @Override
    public void terminer() {
        HomeActivity.toRecreate = true;
        finish();
    }
}
