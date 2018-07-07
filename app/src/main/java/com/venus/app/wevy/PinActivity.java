package com.venus.app.wevy;

import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.RecyclerView;
import android.view.*;
import android.widget.LinearLayout;
import com.venus.app.Adapters.AbsInfoAdapter;
import com.venus.app.Base.AbstractInformation;
import com.venus.app.Base.Information;
import com.venus.app.Base.Note;
import com.venus.app.services.PinnedInformationsService;

import java.util.*;

public class PinActivity extends AppCompatActivity implements AbsInfoAdapter.OnInfoListInteractionListener {
    public static final String ARG_INTENT_DATA = "intent data";
    private AbstractInformation[] infos;
    private LinearLayout content_pin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setUpWindow();
        setContentView(R.layout.activity_pin);
        content_pin = (LinearLayout) findViewById(R.id.content_pin_ll);

        ArrayList<AbstractInformation> ai = new ArrayList<>();
        for(Parcelable p : getIntent ().getParcelableArrayListExtra(ARG_INTENT_DATA))
            ai.add((AbstractInformation) p);
        infos = ai.toArray(new AbstractInformation[]{});

        updateContentView(decoupeListe(infos));
        GestureDetectorCompat s = new GestureDetectorCompat(this, new GestureDetector.OnGestureListener() {
            @Override
            public boolean onDown(MotionEvent motionEvent) {
                return false;
            }

            @Override
            public void onShowPress(MotionEvent motionEvent) {

            }

            @Override
            public boolean onSingleTapUp(MotionEvent motionEvent) {
                return false;
            }

            @Override
            public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
                return false;
            }

            @Override
            public void onLongPress(MotionEvent motionEvent) {

            }

            @Override
            public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
                return false;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_pin, menu);

        return super.onCreateOptionsMenu(menu);
    }

    public void setUpWindow() {

        // Creates the layout for the window and the look of it
        requestWindowFeature(Window.FEATURE_ACTION_BAR);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND,
                WindowManager.LayoutParams.FLAG_DIM_BEHIND);

        // Params for the window.
        // You can easily set the alpha and the dim behind the window from here
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.alpha = 1.0f;    // lower than one makes it more transparent
        params.dimAmount = 0.7f;  // set it higher if you want to dim behind the window
        getWindow().setAttributes(params);

        // Gets the display size so that you can set the window to a percent of that
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;

        // You could also easily used an integer value from the shared preferences to set the percent
        if (height > width) {
            getWindow().setLayout((int) (width * .9), (int) (height * .8));
        } else {
            getWindow().setLayout((int) (width * .8), (int) (height * .8));
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.ab_pin_close) {
            stopService(new Intent(this, PinnedInformationsService.class));
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onListInteraction(int position, AbstractInformation info) {
        // on construit un intent qu'on va envoyer à MainActivity qui va ouvrir les details de l'info
        // et "créer" la HomeActivity en arriere
        if (info instanceof Information)
            startActivity(new Intent(PinActivity.this, DetailsInfoActivity.class).putExtra("info", info).putExtra("fromPin", true));
        else if (info instanceof Note)
            startActivity(new Intent(PinActivity.this, DetailsNoteActivity.class).putExtra("note", info).putExtra("fromPin", true));
        finish();
    }

    private Object[] sortInfosValides(Object[] objects) {
        if (!PreferenceManager.getDefaultSharedPreferences(this).getBoolean("PREF_INFOS_NV", false)) {
            ArrayList<Object> a = new ArrayList<>();

            for (Object o : objects) {
                if (o instanceof Information) {
                    if (((Information) o).getValide()) a.add(o);
                } else a.add(o);
            }

            return a.toArray();
        } else return objects;
    }

    private TreeMap<String, ArrayList<AbstractInformation>> decoupeListe(Object[] infos) {
        // On decoupe la liste des infos par jour pour les afficher
        TreeMap<String, ArrayList<AbstractInformation>> liste = new TreeMap<>(Utilities.InfosComparator());

        // On profite pour trier les informations non valides

        for (Object o : sortInfosValides(infos)) {
            Iterator<Map.Entry<String, ArrayList<AbstractInformation>>> it = liste.entrySet().iterator();
            boolean flag = false;
            AbstractInformation i = (AbstractInformation) o;
            while (it.hasNext()) {
                Map.Entry next = it.next();
                if (i.getEcheance().equals(next.getKey())) {
                    liste.get(next.getKey()).add(i);
                    flag = true;
                    break;
                }
            }
            if (!flag) { // si je n'ai pas pu inserer l'element
                liste.put(i.getEcheance(), new ArrayList<AbstractInformation>());
                liste.get(i.getEcheance()).add(i);
            }
        }

        return liste;
    }

    private void updateContentView(TreeMap<String, ArrayList<AbstractInformation>> liste) {
        // le String est la date à afficher, la liste, celle des informations du jour
        Iterator<Map.Entry<String, ArrayList<AbstractInformation>>> it = liste.entrySet().iterator();
        View v;
        while (it.hasNext()) {
            Map.Entry<String, ArrayList<AbstractInformation>> next = it.next();
            // la TreeMap est deja triee par dates, on trie les arraylists par echeance
            //
            v = getLayoutInflater().inflate(R.layout.layout_content_home, null, false);
            RecyclerView rv = (RecyclerView) v.findViewById(R.id.home_listview);
            AppCompatTextView tv = (AppCompatTextView) v.findViewById(R.id.home_textview);

            // On modifie certaines entrées (aujourd'hui, demain, hier, ...)
            int daysBetween = Utilities.daysBetween(Utilities.parseCalendar(next.getValue().get(0).getEcheance()), Calendar.getInstance());
            if (daysBetween == 0) tv.setText("Aujourd'hui");
            else if (daysBetween == 1) tv.setText("Demain");
            else if (daysBetween == -1) tv.setText("Hier");
            else if (daysBetween > 1 && daysBetween <= 5) tv.setText("Dans " + daysBetween + " jours");
            else if (daysBetween < -1 && daysBetween >= -5) tv.setText("Il y a " + Math.abs(daysBetween) + " jours");
            else tv.setText(Utilities.invertDate(next.getKey()));

            rv.setAdapter(new AbsInfoAdapter(next.getValue().toArray(new AbstractInformation[]{}), this));
            content_pin.addView(v);
        }
    }
}
