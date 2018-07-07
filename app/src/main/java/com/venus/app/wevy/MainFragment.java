package com.venus.app.wevy;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import com.google.firebase.iid.FirebaseInstanceId;
import com.venus.app.IO.Asyncable;
import com.venus.app.IO.SendToServerAsc;
import com.venus.app.Utils.SimpleMessageDialog;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Created by arnold on 24/09/17.
 */
public class MainFragment extends Fragment implements Asyncable {
    private static String FOA_CONNEXION = "connexion";
    private static String ARG_POSITION = "position";
    private AppCompatImageView imageView;
    private AutoCompleteTextView email, mdp;
    private AppCompatButton button;
    private AppCompatTextView inscription;
    private int position;
    private ProgressDialog loading;

    public static MainFragment newInstance(int position) {

        Bundle args = new Bundle();
        args.putInt(ARG_POSITION, position);
        MainFragment fragment = new MainFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        position = getArguments().getInt(ARG_POSITION);
        loading = Utilities.newLoadingDialog(getContext());

        if (position == 1) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    ((MainActivity) getActivity()).swipeFirst();
                }
            }, 2200);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view;
        switch (position) {
            case 0:
                view = inflater.inflate(R.layout.fragment_main, container, false);
                break;

            case 1:
                view = inflater.inflate(R.layout.fragment_main2, container, false);
                break;

            default:
                view = inflater.inflate(R.layout.fragment_login, container, false);
                email = (AutoCompleteTextView) view.findViewById(R.id.login_email);
                mdp = (AutoCompleteTextView) view.findViewById(R.id.login_mdp);
                button = (AppCompatButton) view.findViewById(R.id.login_connexion);
                button.setOnClickListener(button_listener);
                inscription = (AppCompatTextView) view.findViewById(R.id.login_inscription);
                inscription.setOnClickListener(inscription_listener);
                break;
        }

        return view;
    }

    private View.OnClickListener inscription_listener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            startActivity(new Intent(getActivity(), InscriptionActivity.class));
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
            if (flag) connexion();
        }
    };

    private void connexion() {
        // On verifie que le compte existe
        String data ="", utf = "UTF-8";
        try {
            data = "email=" + URLEncoder.encode(email.getText().toString(), utf) +
                    "&" + "mdp=" + URLEncoder.encode(String.valueOf(mdp.getText().toString().hashCode()), utf);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        loading.show();
        new SendToServerAsc(this, MainActivity.PREF_URL_VALUE + "connexion.php", FOA_CONNEXION).execute(data);
    }

    @Override
    public void fetchOnlineResult(Object result, String code) {
        loading.dismiss();
        if (getActivity() == null) return;
        if (code.equals(FOA_CONNEXION)) {
            if (result == null || result.toString().isEmpty())
                new SimpleMessageDialog().putArguments("Echec de la connexion.\n Veuillez réessayer plus tard")
                        .show(getActivity().getSupportFragmentManager(), "echec_conn");
            else {
                if (result.toString().startsWith("1")) {
                    // recuperation reussie
                    try {
                        JSONObject json = new JSONObject(result.toString().substring(1));
                        // on enregistre les données dans les preferences
                        MainActivity.preferences.edit()
                                .putString(MainActivity.PREF_EMAIL, json.getString("email"))
                                .putString(MainActivity.PREF_NAME, json.getString("nomEtudiant"))
                                .putBoolean(MainActivity.PREF_IS_ADMIN, json.getInt("isAdmin") == 1)
                                .putString(MainActivity.PREF_CLASSE, json.getString("nomClasse"))
                                .putString(MainActivity.PREF_TOKEN, FirebaseInstanceId.getInstance().getToken())
                                .apply();

                        // on actualise le token
                        new ResetFirebaseToken().execute();

                        ActivityCompat.finishAffinity(getActivity());
                        startActivity(new Intent(getActivity(), HomeActivity.class));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } // erreur inattendue
                else new SimpleMessageDialog().putArguments(result.toString().substring(1))
                        .show(getActivity().getSupportFragmentManager(), "erreur conn");
            }
        }
    }
}
