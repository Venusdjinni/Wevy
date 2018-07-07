package com.venus.app.wevy;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDialogFragment;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import com.venus.app.Adapters.AdminInfoAdapter;
import com.venus.app.Base.Information;
import com.venus.app.DAO.DAOInformation;
import com.venus.app.IO.Asyncable;
import com.venus.app.IO.SendToServerAsc;
import com.venus.app.Utils.SimpleMessageDialog;
import com.venus.app.Utils.Terminating;
import com.venus.app.services.MyFireBaseMessagingService;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

public class AdminInfosActivity extends AppCompatActivity
        implements AdminInfoAdapter.OnInfoListInteractionListener, Asyncable, Terminating {
    private static final String LFD_INFOS = "infos";
    private static final String FOA_ADMIN = "admin infos";
    private LinearLayout ll_nval, ll_val;
    private RecyclerView rv_nval, rv_val;
    private Information[] infos;
    private ArrayList<Information> nval, val;
    private ArrayList<Integer> idsValider;
    private ArrayList<Integer> idsSuppr;
    private ProgressDialog loading;
    private int state = 0;// garde l'etat de l'affichege actuel: 0 pour all, 1 pour only non valide, 2 pour only valide

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_infos);
        MyFireBaseMessagingService.countIA = 0;

        ll_nval = (LinearLayout) findViewById(R.id.admin_nv_ll);
        ll_val = (LinearLayout) findViewById(R.id.admin_v_ll);
        rv_nval = (RecyclerView) findViewById(R.id.admin_rv_nv);
        rv_val = (RecyclerView) findViewById(R.id.admin_rv_v);
        idsValider = new ArrayList<>();
        idsSuppr = new ArrayList<>();
        nval = new ArrayList<>();
        val = new ArrayList<>();
        loading = Utilities.newLoadingDialog(this);
        loading.show();

        // recherche des donnees
        new LoadFromDbAsc(this, LFD_INFOS).execute(LoadFromDbAsc.PARAM_INFO);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_admin_infos, menu);
        switch (state) {
            // l'item 1 c'est le bouton valider
            case 1: // non validées uniquement
                menu.getItem(1).setTitle("Tout afficher");
                menu.getItem(2).setTitle("Validées uniquement");
                break;
            case 2: // validées uniquement
                menu.getItem(1).setTitle("Tout afficher");
                menu.getItem(2).setTitle("Non validées uniquement");
                break;
            default:
                menu.getItem(1).setTitle("Non validées uniquement");
                menu.getItem(2).setTitle("Validées uniquement");
                break;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.ab_ai_aide) {
            String aide = "Selectionnez les informations et définissez si elles doivent être supprimées ou " +
                            "validées. Une fois terminé, cliquez sur \"Valider\".";

            new SimpleMessageDialog().putArguments(aide).show(getSupportFragmentManager(), "aide");
        } else if (id == R.id.ab_ai_enreg) {
            new ConfEnregDialog().show(getSupportFragmentManager(), "conf enreg");
        } else if (id == R.id.ab_ai_nonvalide) {
            if (state == 0) {
                // On n'affiche que les infos non valides
                ll_nval.setVisibility(View.VISIBLE);
                ll_val.setVisibility(View.GONE);
                rv_nval.invalidate();
                state = 1;
            } else {
                // On affiche tout
                ll_nval.setVisibility(View.VISIBLE);
                ll_val.setVisibility(View.VISIBLE);
                rv_nval.invalidate();
                rv_val.invalidate();
                state = 0;
            }
            // et on modifie le menu
            invalidateOptionsMenu();
        } else if (id == R.id.ab_ai_valide) {
            if (state == 2) {
                // On n'affiche que les infos non valides
                ll_nval.setVisibility(View.VISIBLE);
                ll_val.setVisibility(View.GONE);
                rv_nval.invalidate();
                state = 1;
            } else {
                // On n'affiche que les infos valides
                ll_nval.setVisibility(View.GONE);
                ll_val.setVisibility(View.VISIBLE);
                rv_val.invalidate();
                state = 2;
            }

            // et on modifie le menu
            invalidateOptionsMenu();
        } else if (id == R.id.ab_ai_reset) {
            // on reset toutes les etats des infos
            idsValider.clear();
            idsSuppr.clear();
            for (int i = 0; i < rv_nval.getAdapter().getItemCount(); i++) {
                rv_nval.findViewHolderForAdapterPosition(i).itemView.findViewById(R.id.info_rl).setBackgroundResource(R.color.colorWhite);
                ((AdminInfoAdapter.ViewHolder) rv_nval.findViewHolderForAdapterPosition(i)).setState(AdminInfoAdapter.AdminState.NONE);
            }
            for (int i = 0; i < rv_val.getAdapter().getItemCount(); i++) {
                rv_val.findViewHolderForAdapterPosition(i).itemView.findViewById(R.id.info_rl).setBackgroundResource(R.color.colorWhite);
                ((AdminInfoAdapter.ViewHolder) rv_val.findViewHolderForAdapterPosition(i)).setState(AdminInfoAdapter.AdminState.NONE);
            }
        }

        return super.onOptionsItemSelected(item);
    }

    private void enregistrer() {
        // on envoie les identifiants au serveur
        String data = null;
        try {
            JSONArray jArray = new JSONArray();
            JSONObject json;
            for (int i : idsValider) {
                json = new JSONObject();
                json.put(Integer.toString(i), i);
                jArray.put(json);
            }
            data = "idVal=" + URLEncoder.encode(new JSONArray(idsValider).toString(), "UTF-8") +
                    "&" + "idSup=" + URLEncoder.encode(new JSONArray(idsSuppr).toString(), "UTF-8");
        } catch (UnsupportedEncodingException | JSONException e) {
            e.printStackTrace();
        }
        loading.show();
        new SendToServerAsc(this, MainActivity.PREF_URL_VALUE + "adminInfo.php", FOA_ADMIN)
                .execute(data);
    }

    @Override
    public void onListInteraction(int position, AdminInfoAdapter.ViewHolder holder) {
        OnListInteractionDialog.newInstance(holder).show(getSupportFragmentManager(), "interaction");
    }

    @Override
    public void fetchOnlineResult(Object result, String code) {
        loading.dismiss();
        if (code.equals(LFD_INFOS)) {
            if (result == null) new SimpleMessageDialog().putArguments(getString(R.string.echec_connexion), true)
                    .show(getSupportFragmentManager(), "echec co");
            else if (result instanceof Object[]) {
                // on separe les valides des non valides

                infos = new Information[((Object[]) result).length];
                for (int i = 0; i < ((Object[]) result).length; i++) {
                    infos[i] = (Information) ((Object[]) result)[i];
                    // on separe la liste
                    if (infos[i].getValide()) val.add(infos[i]);
                    else nval.add(infos[i]);
                }

                // on connecte aux adapters
                ll_nval.setVisibility(View.VISIBLE);
                ll_val.setVisibility(View.VISIBLE);
                //if (!nval.isEmpty()) {
                    rv_nval.setAdapter(new AdminInfoAdapter(nval.toArray(new Information[]{}), this));
                //}
                //if (!val.isEmpty()) {
                    rv_val.setAdapter(new AdminInfoAdapter(val.toArray(new Information[]{}), this));
                //}
            }
        } else if (code.equals(FOA_ADMIN)) {
            if (result == null || result.toString().isEmpty()) {
                new SimpleMessageDialog().putArguments("Echec de l'enregistrement, veuillez reessayer plus tard")
                        .show(getSupportFragmentManager(), "echec_modif");
            } else if (result.toString().startsWith("1")) {
                // on verifie que tout s'est bien passé et on modifie egalement la bd interne
                DAOInformation dao = new DAOInformation(this);
                dao.open();
                for (int id : idsValider) dao.setInfoValide(id);
                for (int id : idsSuppr) dao.removeInfo(id);
                dao.close();

                // et on sort
                new SimpleMessageDialog().putArguments("Les modifications ont été prises en compte", true)
                        .show(getSupportFragmentManager(), "admin terminé");
            } else new SimpleMessageDialog().putArguments("Une erreur est survenue").show(getSupportFragmentManager(), "erreur");
        }
    }

    @Override
    public void terminer() {
        HomeActivity.toRecreate = true;
        finish();
    }

    public  static class OnListInteractionDialog extends AppCompatDialogFragment {
        private static final String ARG_INFO = "info";
        private Information info;
        private AdminInfoAdapter.ViewHolder mHolder;

        public static OnListInteractionDialog newInstance(AdminInfoAdapter.ViewHolder holder) {
            Bundle args = new Bundle();
            args.putParcelable(ARG_INFO, holder.item);
            OnListInteractionDialog fragment = new OnListInteractionDialog();
            fragment.setArguments(args);
            fragment.setHolder(holder);
            return fragment;
        }

        private void setHolder(AdminInfoAdapter.ViewHolder holder) {
            mHolder = holder;
        }

        private void valider() {
            mHolder.setState(AdminInfoAdapter.AdminState.AVALIDER);
            mHolder.view.findViewById(R.id.info_rl).setBackgroundResource(R.color.colorAdminValider);
            ((AdminInfosActivity) getActivity()).idsValider.add(info.getIdInformation());
            ((AdminInfosActivity) getActivity()).idsSuppr.remove(Integer.valueOf(info.getIdInformation()));
        }

        private void supprimer() {
            mHolder.setState(AdminInfoAdapter.AdminState.ASUPPRIMER);
            mHolder.view.findViewById(R.id.info_rl).setBackgroundResource(R.color.colorAdminSupprimer);
            ((AdminInfosActivity) getActivity()).idsSuppr.add(info.getIdInformation());
            ((AdminInfosActivity) getActivity()).idsValider.remove(Integer.valueOf(info.getIdInformation()));
        }

        private void retablir() {
            mHolder.setState(AdminInfoAdapter.AdminState.NONE);
            mHolder.view.findViewById(R.id.info_rl).setBackgroundResource(R.color.colorWhite);
            ((AdminInfosActivity) getActivity()).idsValider.remove(Integer.valueOf(info.getIdInformation()));
            ((AdminInfosActivity) getActivity()).idsSuppr.remove(Integer.valueOf(info.getIdInformation()));
        }

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            info = getArguments().getParcelable(ARG_INFO);
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            View v = getActivity().getLayoutInflater().inflate(R.layout.dialog_details_info, null, false);
            // remplissage des champs
            ((AppCompatTextView) v.findViewById(R.id.details_a_type)).setText(info.getTypeInformation().toString());
            ((AppCompatTextView) v.findViewById(R.id.details_a_enreg)).setText(info.getDateEnreg());
            ((AppCompatTextView) v.findViewById(R.id.details_a_echeance)).setText(info.getEcheance());
            ((AppCompatTextView) v.findViewById(R.id.details_a_auteur)).setText(info.getAuteur());
            ((AppCompatTextView) v.findViewById(R.id.details_a_description)).setText(info.getDescription());

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                    .setView(v)
                    .setNegativeButton("Annuler", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dismiss();
                        }
                    });
            if (info.getValide()) {
                if (mHolder.getState().equals(AdminInfoAdapter.AdminState.ASUPPRIMER))
                    builder.setPositiveButton("Rétablir", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            retablir();
                        }
                    });
                else builder.setPositiveButton("Supprimer", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        supprimer();
                    }
                });
            } else {
                if (mHolder.getState().equals(AdminInfoAdapter.AdminState.ASUPPRIMER)) {
                    builder.setPositiveButton("Valider", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            valider();
                        }
                    });
                    builder.setNeutralButton("Rétablir", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            retablir();
                        }
                    });
                } else if (mHolder.getState().equals(AdminInfoAdapter.AdminState.AVALIDER)) {
                    builder.setPositiveButton("Rétablir", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            retablir();
                        }
                    });
                    builder.setNeutralButton("Supprimer", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            supprimer();
                        }
                    });
                } else {
                    builder.setPositiveButton("Valider", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            valider();
                        }
                    });
                    builder.setNeutralButton("Supprimer", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            supprimer();
                        }
                    });
                }
            }

            return builder.create();
        }
    }

    public static class ConfEnregDialog extends AppCompatDialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return new AlertDialog.Builder(getActivity())
                    .setMessage("Voulez-vous vraiment enregistrer les modifications?")
                    .setPositiveButton("Oui",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    ((AdminInfosActivity) getActivity()).enregistrer();
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
