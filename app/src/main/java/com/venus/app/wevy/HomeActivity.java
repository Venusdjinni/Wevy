package com.venus.app.wevy;

import android.animation.LayoutTransition;
import android.app.SearchManager;
import android.content.*;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import com.venus.app.Adapters.AbsInfoAdapter;
import com.venus.app.Adapters.HomeRecyclerViewAdapter;
import com.venus.app.Base.AbstractInformation;
import com.venus.app.Base.Information;
import com.venus.app.Base.Note;
import com.venus.app.DAO.DAOCours;
import com.venus.app.DAO.DAODiscussion;
import com.venus.app.DAO.DAOInformation;
import com.venus.app.IO.Asyncable;
import com.venus.app.IO.FetchOnlineAsc;
import com.venus.app.IO.SendToServerAsc;
import com.venus.app.services.DailyPinnedInfosService;
import com.venus.app.services.MyFireBaseMessagingService;
import com.venus.app.services.MyFirebaseDatabaseListenerService;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.json.JSONArray;

import java.util.*;

public class HomeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        AbsInfoAdapter.OnInfoListInteractionListener,
        Asyncable {
    private static final String FOA_INFOS = "foa_infos";
    private static final String FOA_COURS = "foa_cours";
    private static final String LFD_INFOS = "lfd_infos";
    private static final String FOA_ISADMIN = "isAdmin";
    static boolean toRecreate = false;
    private RecyclerView home_rv;
    private AbstractInformation[] absInfos = new AbstractInformation[]{};
    private MyFirebaseDatabaseListenerService fbService;
    private ServiceConnection mfConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            HomeActivity.this.fbService = ((MyFirebaseDatabaseListenerService.LocalBinder) service).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        EventBus.getDefault().register(this);
        MyFireBaseMessagingService.countIN = 0;
        MyFireBaseMessagingService.countND = 0;
        home_rv = (RecyclerView) findViewById(R.id.home_rv);
        home_rv.setAdapter(new HomeRecyclerViewAdapter(this));
        ((AppBarLayout) findViewById(R.id.appbarlayout)).getLayoutTransition().enableTransitionType(LayoutTransition.CHANGING);

        // actualisation des données de connectivité
        MainActivity.PREF_URL_VALUE = getString(R.string.servername);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        // on affecte le menu au nav_drawer
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.getMenu().clear();
        navigationView.inflateMenu(MainActivity.preferences.getBoolean(MainActivity.PREF_IS_ADMIN, false) ? R.menu.activity_home_drawer_admin : R.menu.activity_home_drawer);
        navigationView.setNavigationItemSelectedListener(this);

        // Remplissage de l'header
        View view = navigationView.getHeaderView(0);
        ((AppCompatTextView) view.findViewById(R.id.header_nom)).setText(MainActivity.preferences.getString(MainActivity.PREF_NAME, MainActivity.PREF_NAME_DEF));
        ((AppCompatTextView) view.findViewById(R.id.header_email)).setText(MainActivity.preferences.getString(MainActivity.PREF_EMAIL, MainActivity.PREF_EMAIL_DEF));
        ((AppCompatTextView) findViewById(R.id.home_classe)).setText(MainActivity.preferences.getString(MainActivity.PREF_CLASSE, MainActivity.PREF_CLASSE_DEF));
        findViewById(R.id.home_fab_info).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(HomeActivity.this, NewInfoActivity.class));
            }
        });
        findViewById(R.id.home_fab_note).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(HomeActivity.this, MyNotesActivity.class).putExtra("newNote", true));
            }
        });


        if (!Utilities.isMyServiceRunning(this, MyFirebaseDatabaseListenerService.class))
            startService(new Intent(this, MyFirebaseDatabaseListenerService.class));

        if (MainActivity.preferences.getBoolean(MainActivity.PREF_NEW_USER, true)) {
            MainActivity.preferences.edit().putBoolean(MainActivity.PREF_NEW_USER, false).apply();
            (new FetchOnlineAsc(this, MainActivity.PREF_URL_VALUE + "getAllInformations.php", FOA_INFOS)).execute("classe=" + MainActivity.preferences.getString(MainActivity.PREF_CLASSE, MainActivity.PREF_CLASSE_DEF));
        } else
        (new LoadFromDbAsc(this, LFD_INFOS)).execute(LoadFromDbAsc.PARAM_ABSINFO);
        (new SendToServerAsc(this, MainActivity.PREF_URL_VALUE + "isAdmin.php", FOA_ISADMIN)).execute("email=" + MainActivity.preferences.getString(MainActivity.PREF_EMAIL, MainActivity.PREF_EMAIL_DEF));
        //(new FetchOnlineAsc(this, MainActivity.PREF_URL_VALUE + "getInformations.php", FOA_INFOS)).execute("classe=" + MainActivity.preferences.getString(MainActivity.PREF_CLASSE, MainActivity.PREF_CLASSE_DEF));
        (new FetchOnlineAsc(this, MainActivity.PREF_URL_VALUE + "getEdt.php", FOA_COURS)).execute("classe=" + MainActivity.preferences.getString(MainActivity.PREF_CLASSE, MainActivity.PREF_CLASSE_DEF));
    }

    @Override
    protected void onStart() {
        super.onStart();
        bindService(new Intent(this, MyFirebaseDatabaseListenerService.class), mfConnection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        unbindService(mfConnection);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        stopService(new Intent(this, MyFirebaseDatabaseListenerService.class));
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (toRecreate) {
            toRecreate = false;
            recreate();
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer != null && drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_home, menu);

        // Get the SearchView and set the searchable configuration
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.ab_home_search).getActionView();
        // Assumes current activity is the searchable activity
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // On modifie la liste des resultats
                doMySearch(newText);
                return false;
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.ab_home_edt) {
            startActivity(new Intent(HomeActivity.this, EdtActivity.class));
        } else if (id == R.id.ab_home_search) {
            // on appelle la recherche
            MenuItemCompat.setOnActionExpandListener(item, new MenuItemCompat.OnActionExpandListener() {
                @Override
                public boolean onMenuItemActionExpand(MenuItem item) {
                    // On cache la classe
                    findViewById(R.id.home_classe).setVisibility(View.GONE);
                    return true;
                }

                @Override
                public boolean onMenuItemActionCollapse(MenuItem item) {
                    // on reaffiche la classe
                    findViewById(R.id.home_classe).setVisibility(View.VISIBLE);
                    updateContentView(decoupeListe(absInfos));
                    return true;
                }
            });
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_edt) {
            // Handle the camera action
            startActivity(new Intent(HomeActivity.this, EdtActivity.class));
        } else if (id == R.id.nav_ninfo) {
            startActivity(new Intent(HomeActivity.this, NewInfoActivity.class));
        } else if (id == R.id.nav_mod_edt) {
            startActivity(new Intent(HomeActivity.this, EdtModActivity.class));
        } else if (id == R.id.nav_val_info) {
            startActivity(new Intent(HomeActivity.this, AdminInfosActivity.class));
        } else if (id == R.id.nav_mnotes) {
            startActivity(new Intent(HomeActivity.this, MyNotesActivity.class));
        } else if (id == R.id.nav_mod_compte) {
            startActivity(new Intent(HomeActivity.this, CompteActivity.class));
        } else if (id == R.id.nav_settings) {
            startActivity(new Intent(HomeActivity.this, SettingsActivity.class));
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onListInteraction(int position, AbstractInformation info) {
        if (info instanceof Information)
            startActivity(new Intent(HomeActivity.this, DetailsInfoActivity.class).putExtra("info", info));
        else if (info instanceof Note)
            startActivity(new Intent(HomeActivity.this, DetailsNoteActivity.class).putExtra("note", info));
    }

    @Override
    public void fetchOnlineResult(Object result, String code) {
        switch (code) {
            case  FOA_ISADMIN:
                if (result != null && !result.toString().isEmpty()) {
                    SharedPreferences.Editor editor = MainActivity.preferences.edit();

                    // On verifie si on doit actualiser la vue
                    boolean isAdmin = MainActivity.preferences.getBoolean(MainActivity.PREF_IS_ADMIN, false);
                    boolean adminResult = result.toString().charAt(0) == '1';
                    if (adminResult != isAdmin) {
                        // S'il y a changement, on enregistre et on recrée l'activité
                        editor.putBoolean(MainActivity.PREF_IS_ADMIN, result.toString().charAt(0) == '1');
                        editor.apply();
                        recreate();
                    }
                }
            case FOA_INFOS:
                if (result == null) // pas de connexion
                    Snackbar.make(findViewById(R.id.coordinator_layout), "Vous êtes hors connexion", Snackbar.LENGTH_LONG).show();
                else if (result instanceof JSONArray && ((JSONArray) result).length() > 0) {
                    // on recupere la liste des cours, actualise la vue, et on enregistre les donnees
                    Information[] infos = Utilities.convertToInfos((JSONArray) result);

                    System.out.println("foa save datas");
                    fbService.saveDatas();
                    System.out.println("foa set extra");
                    infos = DAODiscussion.setExtraDatas(this, infos);
                    // On fusionne cette liste avec celle des notes deja chargées
                    System.out.println("foa update view");
                    updateContentView(decoupeListe(Utilities.updateAbsInfos(absInfos, infos)));
                    DAOInformation.asyncSave(this, infos);
                    System.out.println("foa end");
                }
                break;
            case LFD_INFOS:
                if (result != null) {
                    System.out.println("lfd result");
                    // on copie les notes dans la liste des notes, puis on affiche le tout
                    absInfos = new AbstractInformation[((Object[]) result).length];
                    Information[] infos = new Information[(((ArrayList[]) result)[0]).size()];
                    for(int i = 0; i < ((ArrayList[]) result)[0].size(); i++)
                        infos[i] = (Information) ((ArrayList[]) result)[0].get(i);
                    infos = DAODiscussion.setExtraDatas(this, infos);

                    Note[] notes = new Note[(((ArrayList[]) result)[1]).size()];
                    for(int i = 0; i < ((ArrayList[]) result)[1].size(); i++)
                        notes[i] = (Note) ((ArrayList[]) result)[1].get(i);

                    ArrayList<AbstractInformation> ai = new ArrayList<>();
                    Collections.addAll(ai, infos);
                    Collections.addAll(ai, notes);
                    absInfos = ai.toArray(new AbstractInformation[]{});
                    (new FetchOnlineAsc(this, MainActivity.PREF_URL_VALUE + "getInformations.php", FOA_INFOS)).execute("classe=" + MainActivity.preferences.getString(MainActivity.PREF_CLASSE, MainActivity.PREF_CLASSE_DEF));
                    updateContentView(decoupeListe(absInfos));
                    System.out.println("lfd result end");

                    /*for (int i = 0; i < ((Object[]) result).length; i++)
                        absInfos[i] = (AbstractInformation) ((Object[]) result)[i];
                    (new FetchOnlineAsc(this, MainActivity.PREF_URL_VALUE + "getInformations.php", FOA_INFOS)).execute("classe=" + MainActivity.preferences.getString(MainActivity.PREF_CLASSE, MainActivity.PREF_CLASSE_DEF));
                    updateContentView(decoupeListe(absInfos));*/
                }
                break;
            case FOA_COURS:
                if (result != null && result instanceof JSONArray)
                    DAOCours.asyncSave(this, Utilities.convertToCours((JSONArray) result));
                break;
        }
    }

    private void doMySearch(String query) {
        // On trie les resultats et on les envoie à updateContentView
        // On trie parmi les titres et parmi les descriptions
        ArrayList<AbstractInformation> result = new ArrayList<>();
        Collections.addAll(result, absInfos);
        for (AbstractInformation ai : absInfos)
            if (!Utilities.stripAccents(ai.getTitre().toLowerCase()).contains(Utilities.stripAccents(query.toLowerCase())) &&
                    !Utilities.stripAccents(ai.getDescription().toLowerCase()).contains(Utilities.stripAccents(query.toLowerCase())))
                result.remove(ai);

        // et on actualise la vue
        updateContentView(decoupeListe(result.toArray(new Object[]{})));
    }

    @Subscribe
    public void onMessageEvent(MyFirebaseDatabaseListenerService.HAMessageEvent event) {
        // On actualise la vue
        System.out.println("on message event home act.");
        if (event.reset) ((HomeRecyclerViewAdapter) home_rv.getAdapter()).resetVInformation(event.idToUp);
        else ((HomeRecyclerViewAdapter) home_rv.getAdapter()).updateVInformation(event.idToUp);
    }

    private Object[] sortInfosValides(Object[] objects) {
        if (!MainActivity.preferences.getBoolean("PREF_INFOS_NV", false)) {
            ArrayList<Object> a = new ArrayList<>();

            for (Object o : objects) {
                if (o instanceof Information) {
                    if (((Information) o).getValide()) a.add(o);
                } else a.add(o);
            }

            return a.toArray(new AbstractInformation[]{});
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
        System.out.println("update content view");
        ((HomeRecyclerViewAdapter) home_rv.getAdapter()).setData(liste);
    }

}
