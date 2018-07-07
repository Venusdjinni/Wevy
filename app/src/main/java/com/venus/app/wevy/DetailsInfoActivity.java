package com.venus.app.wevy;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDialogFragment;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.ListViewCompat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import com.venus.app.Adapters.DiscussionAdapter;
import com.venus.app.Base.AbstractInformation;
import com.venus.app.Base.Discussion;
import com.venus.app.Base.Information;
import com.venus.app.DAO.DAODiscussion;
import com.venus.app.IO.Asyncable;
import com.venus.app.IO.FetchOnlineAsc;
import com.venus.app.IO.SendToServerAsc;
import com.venus.app.Utils.SimpleMessageDialog;
import com.venus.app.Utils.Terminating;
import com.venus.app.services.MyFirebaseDatabaseListenerService;
import com.venus.app.services.PinnedInformationsService;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.json.JSONArray;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collections;

public class DetailsInfoActivity extends AppCompatActivity
        implements DiscussionAdapter.OnDiscListInteractionListener, Asyncable, Terminating {
    private static final String FOA_NEWDISC = "new discussion";
    private static final String FOA_GETDISC = "discussions";
    private static final String FOA_SUPPINFO = "supp info";
    private AppCompatTextView enreg;
    private AppCompatTextView echeance;
    private AppCompatTextView auteur;
    private AppCompatTextView description;
    private AppCompatTextView type;
    private ListViewCompat listview;
    private ProgressBar progressBar;
    private ProgressDialog loading;
    private FloatingActionButton fab;
    static boolean toRecreate = false;
    private Discussion[] discs = new Discussion[]{};
    private Information information;
    private boolean fromPin;
    private MyFirebaseDatabaseListenerService fService;
    private ServiceConnection mfConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            DetailsInfoActivity.this.fService = ((MyFirebaseDatabaseListenerService.LocalBinder) service).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details_info);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        loading = Utilities.newLoadingDialog(this);

        information = getIntent().getParcelableExtra("info");

        enreg = (AppCompatTextView) findViewById(R.id.details_a_enreg);
        echeance = (AppCompatTextView) findViewById(R.id.details_a_echeance);
        auteur = (AppCompatTextView) findViewById(R.id.details_a_auteur);
        description = (AppCompatTextView) findViewById(R.id.details_a_description);
        type = (AppCompatTextView) findViewById(R.id.details_a_type);
        listview = (ListViewCompat) findViewById(R.id.details_a_listview);
        fab = (FloatingActionButton) findViewById(R.id.details_a_fab);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        //
        getSupportActionBar().setTitle(information.getTitre());
        enreg.setText(Utilities.invertDate(information.getDateEnreg()) + " (" +
                Utilities.dayToAbrev(Utilities.parseCalendar(information.getDateEnreg())) + ")");
        echeance.setText(Utilities.invertDate(information.getEcheance()) + " (" +
                Utilities.dayToAbrev(Utilities.parseCalendar(information.getEcheance())) + ")");
        description.setText(information.getDescription());
        type.setText(information.getTypeInformation().toString());
        auteur.setText(information.getAuteur());
        fab.setOnClickListener(fab_listener);
        fromPin = getIntent().getBooleanExtra("fromPin", false);

        if (!information.getValide())
            fab.setVisibility(View.GONE);

        listview.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        new FetchOnlineAsc(this, MainActivity.PREF_URL_VALUE + "getDiscussions.php", FOA_GETDISC).execute("idInfo=" + information.getIdInformation());

        bindService(new Intent(this, MyFirebaseDatabaseListenerService.class), mfConnection, BIND_AUTO_CREATE);
        EventBus.getDefault().register(this);
    }

    private View.OnClickListener fab_listener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            // on cree une nouvelle discussion
            NewDiscussionDialog.newInstance(information.getIdInformation()).show(getSupportFragmentManager(), "new disc");
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        if (toRecreate) {
            toRecreate = false;
            new FetchOnlineAsc(this, MainActivity.PREF_URL_VALUE + "getDiscussions.php", FOA_GETDISC).execute("idInfo=" + information.getIdInformation());
        }
    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        unbindService(mfConnection);
        super.onDestroy();
    }

    @Subscribe
    public void onMessageEvent(MyFirebaseDatabaseListenerService.DIAMessageEvent event) {
        // On actualise la vue
        System.out.println("on message event details infos");
        updateDiscussionCount(event.idToUp);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_details_info, menu);
        if(PinnedInformationsService.getInformations().contains(information))
            menu.getItem(0).setTitle("Désépingler");
        else menu.getItem(0).setTitle("Epingler");
        if (MainActivity.preferences.getBoolean(MainActivity.PREF_IS_ADMIN, false) ||
                information.getAuteur().equals(MainActivity.preferences.getString(MainActivity.PREF_EMAIL, MainActivity.PREF_EMAIL_DEF)))
            menu.removeItem(1);

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
        if (item.getItemId() == android.R.id.home) {
            if (fromPin)
                startActivity(new Intent(this, MainActivity.class));
            finish();
        } else if (item.getItemId() == R.id.ab_details_info_pin) {
            if (PinnedInformationsService.getInformations().contains(information))
                PinnedInformationsService.startService(this, null, Collections.singletonList((AbstractInformation) information));
            else PinnedInformationsService.startService(this, information);
            invalidateOptionsMenu();
        } else if (item.getItemId() == R.id.ab_details_info_supp) {
            new ConfSupprInfoDialog().show(getSupportFragmentManager(), "supp");
            HomeActivity.toRecreate = true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onListInteraction(Discussion discussion) {
        // on affiche la discussion
        fService.discActivityBounded(discussion.getNode());
        startActivity(new Intent(this, DiscussionActivity.class).putExtra("discussion", discussion));
    }

    @Override
    public void fetchOnlineResult(Object result, String code) {
        loading.dismiss();
        progressBar.setVisibility(View.GONE);
        switch (code) {
            case FOA_GETDISC:
                listview.setVisibility(View.VISIBLE);
                if (result instanceof JSONArray) {
                    // On demande à FbDbLS de sauvegarder les donnees
                    fService.saveDatas();
                    discs = DAODiscussion.setExtraDatas(this, Utilities.convertToDiscs((JSONArray) result));
                } else if (result == null || result.toString().isEmpty())
                    ((AppCompatTextView) findViewById(R.id.details_a_no_discussion)).setText("Information indisponible");
                findViewById(R.id.details_a_no_discussion).setVisibility(discs.length == 0 ? View.VISIBLE : View.GONE);
                listview.setAdapter(new DiscussionAdapter(discs, this));
                Utilities.justifyListViewHeightBasedOnChildren(listview);

                //
                for (Discussion d : discs) System.out.println(d.getNode() + ": nb = " + ((DAODiscussion.DiscItem) d).getNonLus());
                break;

            case FOA_NEWDISC:
                if (result != null && !result.toString().isEmpty()) {
                    if (result.toString().startsWith("1")) {
                        (new SimpleMessageDialog()).putArguments("Nouvelle discussion créée!")
                                .show(getSupportFragmentManager(), "enreg_ok");

                        new FetchOnlineAsc(this, MainActivity.PREF_URL_VALUE + "getDiscussions.php", FOA_GETDISC).execute("idInfo=" + information.getIdInformation());
                    } else (new SimpleMessageDialog()).putArguments("Une erreur est survenue. Veuillez reesayer plus tard")
                            .show(getSupportFragmentManager(), "enreg_pb");
                } else (new SimpleMessageDialog()).putArguments(getString(R.string.echec_connexion))
                        .show(getSupportFragmentManager(), "enreg_echec");
                break;

            case FOA_SUPPINFO:
                if (result != null && !result.toString().isEmpty()) {
                    if (result.toString().startsWith("1")) {
                        for (Discussion d : discs) Utilities.moveFirebaseRecord(d.getNode());
                        (new SimpleMessageDialog()).putArguments("Information supprimée!", true)
                                .show(getSupportFragmentManager(), "info_sup");
                    } else (new SimpleMessageDialog()).putArguments("Une erreur est survenue. Veuillez reesayer plus tard")
                            .show(getSupportFragmentManager(), "suppinfo_pb");
                } else (new SimpleMessageDialog()).putArguments(getString(R.string.echec_connexion))
                        .show(getSupportFragmentManager(), "suppinfo_echec");
                break;
        }
    }

    private void updateDiscussionCount(int idDisc) {
        for (int i = 0; i < listview.getCount(); i++)
            if (((DAODiscussion.DiscItem) listview.getAdapter().getItem(i)).getIdDiscussion() == idDisc) {
                ((DAODiscussion.DiscItem) listview.getAdapter().getItem(i)).upvoteNonLus();
                break;
            }
        ((BaseAdapter) listview.getAdapter()).notifyDataSetChanged();
    }

    private void createNewDiscussion(Discussion dis) {
        // On enregistre l'entree dans la bd et on cree un nouveau noeud pour la discussion dans FbDb
        String data = "", utf = "UTF-8";
        try {
            data = "idInfo=" + dis.getIdInfo() +
                    "&" + "titre=" + URLEncoder.encode(dis.getTitre(), utf) +
                    "&" + "description=" + URLEncoder.encode(dis.getDescription(), utf) +
                    "&" + "emailAuteur=" + URLEncoder.encode(MainActivity.preferences.getString(MainActivity.PREF_EMAIL, MainActivity.PREF_EMAIL_DEF), utf);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        loading.show();
        new SendToServerAsc(this, MainActivity.PREF_URL_VALUE + "newDiscussion.php", FOA_NEWDISC)
                            .execute(data);
    }

    @Override
    public void terminer() {
        HomeActivity.toRecreate = true;
        finish();
    }

    public static final class NewDiscussionDialog extends AppCompatDialogFragment {
        private static final String ARG_IDINFO = "id info";
        AppCompatEditText titre, description;
        int idInfo;

        public static NewDiscussionDialog newInstance(int idInfo) {
            Bundle args = new Bundle();
            args.putInt(ARG_IDINFO, idInfo);
            NewDiscussionDialog fragment = new NewDiscussionDialog();
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            this.idInfo = getArguments().getInt(ARG_IDINFO);
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            View v = getActivity().getLayoutInflater().inflate(R.layout.dialog_new_discussion, null, false);

            titre = (AppCompatEditText) v.findViewById(R.id.nd_titre);
            description = (AppCompatEditText) v.findViewById(R.id.nd_description);

            AlertDialog dialog = new AlertDialog.Builder(getContext())
                    .setView(v)
                    .setTitle("Nouvelle discussion")
                    .setPositiveButton("OK", null)
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
                            if (titre.getText().toString().isEmpty()) {
                                titre.setError("Le champ est requis");
                                isOk = false;
                            }
                            if (description.getText().toString().isEmpty()) {
                                description.setError("Le champ est requis");
                                isOk = false;
                            }
                            if (isOk) {
                                // on cree une nouvelle discussion qu'on va enregistrer
                                Discussion d = new Discussion(idInfo, titre.getText().toString(), description.getText().toString());
                                ((DetailsInfoActivity) getActivity()).createNewDiscussion(d);
                                dismiss();
                            }
                        }
                    });
                }
            });

            return dialog;
        }
    }

    public static class ConfSupprInfoDialog extends AppCompatDialogFragment {
        private void supprimer() {
            ((DetailsInfoActivity) getActivity()).loading.show();
            new SendToServerAsc((Asyncable) getActivity(), MainActivity.PREF_URL_VALUE + "deleteInformation.php", FOA_SUPPINFO)
                    .execute("idInfo=" + ((DetailsInfoActivity) getActivity()).information.getIdInformation());
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return new AlertDialog.Builder(getActivity())
                    .setMessage("Voulez-vous vraiment supprimer cette information?\nL'information et toutes ses discussions seront perdues pour tout le monde")
                    .setPositiveButton("Supprimer", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            supprimer();
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
}
