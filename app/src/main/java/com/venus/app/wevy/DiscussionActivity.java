package com.venus.app.wevy;

import android.animation.LayoutTransition;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDialogFragment;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.ListViewCompat;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import com.firebase.ui.database.FirebaseListAdapter;
import com.google.firebase.database.*;
import com.venus.app.Base.Message;
import com.venus.app.DAO.DAODiscussion;
import com.venus.app.IO.Asyncable;
import com.venus.app.IO.SendToServerAsc;
import com.venus.app.Utils.SimpleMessageDialog;
import com.venus.app.Utils.Terminating;
import com.venus.app.services.MyFirebaseDatabaseListenerService;

import java.text.DateFormat;
import java.util.Calendar;

public class DiscussionActivity extends AppCompatActivity implements Asyncable, Terminating {
    private static final String FOA_SUPPR = "supp disc";
    private ListViewCompat listView;
    private AppCompatEditText input;
    private FloatingActionButton fab;
    private LinearLayout bar_ll;
    private FirebaseListAdapter<Message> adapter;
    private String userName;
    private DAODiscussion.DiscItem discussion;
    private boolean isExpanded = false;
    private ProgressDialog loading;
    private MyFirebaseDatabaseListenerService service;
    private ServiceConnection mfConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            DiscussionActivity.this.service = ((MyFirebaseDatabaseListenerService.LocalBinder) service).getService();
            // On reinitialise le compteur des non lus
            DiscussionActivity.this.service.resetDiscNonlus(discussion.getNode());
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_discussion);
        ((AppBarLayout) findViewById(R.id.appbarlayout)).getLayoutTransition().enableTransitionType(LayoutTransition.CHANGING);
        loading = Utilities.newLoadingDialog(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        discussion = getIntent().getParcelableExtra("discussion");
        getSupportActionBar().setTitle(discussion.getTitre());
        ((AppCompatTextView) findViewById(R.id.disc_auteur)).setText("Par " + discussion.getAuteur());
        ((AppCompatTextView) findViewById(R.id.disc_description)).setText(discussion.getDescription());

        listView = (ListViewCompat) findViewById(R.id.disc_listview);
        input = (AppCompatEditText) findViewById(R.id.input);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(fab_listener);
        bar_ll = (LinearLayout) findViewById(R.id.disc_bar_ll);
        userName = MainActivity.preferences.getString(MainActivity.PREF_NAME, MainActivity.PREF_NAME_DEF);

        FirebaseDatabase.getInstance()
                .getReference("Active")
                .child(discussion.getNode())
                .addChildEventListener(child_listener);
        adapter = new MessageListAdapter(this, Message.class, R.layout.item_disc_message, FirebaseDatabase.getInstance().getReference("Active").child(discussion.getNode()));
        listView.setAdapter(adapter);

        DetailsInfoActivity.toRecreate = true;
        bindService(new Intent(this, MyFirebaseDatabaseListenerService.class), mfConnection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {
        service.discActivityUnbounded(discussion.getNode());
        unbindService(mfConnection);
        super.onDestroy();
    }

    private View.OnClickListener fab_listener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            String s = input.getText().toString();
            while (s.startsWith(" "))
                s = s.substring(1);
            if (!s.isEmpty()) {
                // On envoie le message
                //SimpleDateFormat df =  new SimpleDateFormat("hh:mm, dd/MM");
                //df.setTimeZone(Calendar.getInstance().getTimeZone());
                DateFormat df =  DateFormat.getDateTimeInstance();
                df.setTimeZone(Calendar.getInstance().getTimeZone());
                FirebaseDatabase.getInstance()
                        .getReference()
                        .child("Active")
                        .child(discussion.getNode())
                        .push()
                        .setValue(new Message(userName, s, df.format(Calendar.getInstance().getTime())));

                new SendToServerAsc(DiscussionActivity.this, MainActivity.PREF_URL_VALUE + "setMessageCount.php", "MCount")
                        .execute("idDiscussion=" + discussion.getIdDiscussion());
            }
            // et on clear le champ de texte
            input.setText("");
        }
    };

    private ChildEventListener child_listener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            listView.post(listView_runnable);
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {

        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    private Runnable listView_runnable = new Runnable() {
        @Override
        public void run() {
            listView.setSelection(listView.getAdapter().getCount() - 1);
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (MainActivity.preferences.getBoolean(MainActivity.PREF_IS_ADMIN, false) ||
                discussion.getEmailAuteur().equals(MainActivity.preferences.getString(MainActivity.PREF_EMAIL, MainActivity.PREF_EMAIL_DEF)))
            menu.add(Menu.NONE, "Supprimer".hashCode(), Menu.NONE, "Supprimer");
        getMenuInflater().inflate(isExpanded ? R.menu.activity_discussion_expanded : R.menu.activity_discussion_collapsed, menu);
        return super.onCreateOptionsMenu(menu);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) finish();

        else if (item.getItemId() == R.id.ab_disc_coll) {
            // On reduit
            bar_ll.setVisibility(View.GONE);
            //bar_ll.setAnimation(up_disappear);
            isExpanded = false;
            invalidateOptionsMenu();
        } else if (item.getItemId() == R.id.ab_disc_exp) {
            // On étend
            bar_ll.setVisibility(View.VISIBLE);
            //bar_ll.setAnimation(down_appear);
            isExpanded = true;
            invalidateOptionsMenu();
        } else if (item.getItemId() == "Supprimer".hashCode()) {
            new SupprDiscussionDialog().show(getSupportFragmentManager(), "suppr");
        }

        return super.onOptionsItemSelected(item);
    }

    private void supprimer() {
        loading.show();
        DAODiscussion dao = new DAODiscussion(this);
        dao.open();
        dao.removeDisc(discussion.getNode());
        dao.close();
        new SendToServerAsc(this, MainActivity.PREF_URL_VALUE + "deleteDiscussion.php", FOA_SUPPR).execute("id=" + discussion.getIdDiscussion());
    }

    @Override
    public void fetchOnlineResult(Object result, String code) {
        loading.dismiss();
        if (code.equals(FOA_SUPPR)) {
            if (result == null) new SimpleMessageDialog().putArguments("Echec de la connexion.\n Veuillez réessayer plus tard")
                    .show(getSupportFragmentManager(), "echec_supp");
            else if (!result.toString().isEmpty() && result.toString().startsWith("1")) {
                Utilities.moveFirebaseRecord(discussion.getNode());
                new SimpleMessageDialog().putArguments("La discussion a été supprimée", true).show(getSupportFragmentManager(), "enreg_ok");
            }
            else new SimpleMessageDialog().putArguments("Une erreur est survenue").show(getSupportFragmentManager(), "erreur");
        }
    }

    @Override
    public void terminer() {
	finish();
    }

    public class MessageListAdapter extends FirebaseListAdapter<Message> {

        /**
         * @param activity    The activity containing the ListView
         * @param modelClass  Firebase will marshall the data at a location into
         *                    an instance of a class that you provide
         * @param modelLayout This is the layout used to represent a single list item.
         *                    You will be responsible for populating an instance of the corresponding
         *                    view with the data from an instance of modelClass.
         * @param ref         The Firebase location to watch for data changes. Can also be a slice of a location,
         *                    using some combination of {@code limit()}, {@code startAt()}, and {@code endAt()}.
         */
        public MessageListAdapter(Activity activity, Class<Message> modelClass, int modelLayout, Query ref) {
            super(activity, modelClass, modelLayout, ref);
        }

        @Override
        protected void populateView(View v, Message model, int position) {
            AppCompatTextView auteur = (AppCompatTextView) v.findViewById(R.id.item_m_auteur);
            auteur.setText(model.getAuteur());
            ((AppCompatTextView) v.findViewById(R.id.item_m_message)).setText(model.getMessage());
            ((AppCompatTextView) v.findViewById(R.id.item_m_heure)).setText(model.getDate());

            View cv = v.findViewById(R.id.item_m_cv);
            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(cv.getLayoutParams());
            // On colorie le background
            if (model.getAuteur().equals(userName)) { // un message de l'utlisateur
                v.findViewById(R.id.item_m_ll).setBackgroundResource(R.color.colorAdminSupprimer);

                lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
                lp.addRule(RelativeLayout.ALIGN_PARENT_END, RelativeLayout.TRUE);
            } else {
                v.findViewById(R.id.item_m_ll).setBackgroundResource(android.R.color.darker_gray);
                lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
                lp.addRule(RelativeLayout.ALIGN_PARENT_START, RelativeLayout.TRUE);
            }
            cv.setLayoutParams(lp);
        }
    }

    public static class SupprDiscussionDialog extends AppCompatDialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return new AlertDialog.Builder(getContext())
                    .setMessage("Voulez-vous vraiment supprimer cette discussion?")
                    .setPositiveButton("Oui", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            ((DiscussionActivity) getActivity()).supprimer();
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
