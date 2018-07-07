package com.venus.app.wevy;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import com.venus.app.Base.AbstractInformation;
import com.venus.app.Base.Information;
import com.venus.app.Base.Note;
import com.venus.app.Base.TypeInformation;
import com.venus.app.DAO.DAO;
import com.venus.app.DAO.DAOCours;
import com.venus.app.DAO.DAOInformation;
import com.venus.app.DAO.DAONote;
import com.venus.app.IO.Asyncable;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by arnold on 18/10/16.
 */
public class LoadFromDbAsc extends AsyncTask<String, Integer, Object[]> {

    /*
        Prend en parametre un String qui appartient à une liste de choix possibles
        La progression est gérée par un Integer, mais on s'en fout;
        Le resultat est un Object[], la liste récupérée depuis la bd
     */

    public static final String PARAM_INFO = "Information";
    public static final String PARAM_NOTE = "Note";
    public static final String PARAM_ABSINFO = "AbsInformation";
    public static final String PARAM_ABSINFO_WEEK = "AbsInformation week";
    public static final String PARAM_COURS = "Cours";
    private Asyncable mSource;
    private String code;
    private ArrayList<Object> donnees;

    public LoadFromDbAsc(Asyncable source, String code){
        mSource = source;
        this.code = code;
    }

    @Override
    protected Object[] doInBackground(String... strings) {
        Context context = mSource instanceof Activity ? (Context) mSource : ((Fragment) mSource).getActivity();
        if(!isCancelled()){
            donnees = new ArrayList<>();
            DAO dao = null;
            switch (strings[0]) {
                case PARAM_INFO:
                    dao = new DAOInformation(context);
                    dao.open();
                    Collections.addAll(donnees, ((DAOInformation) dao).getAllInfos());
                    break;
                case PARAM_NOTE:
                    dao = new DAONote(context);
                    dao.open();
                    Collections.addAll(donnees, ((DAONote) dao).getAllNotes());
                    break;
                case PARAM_ABSINFO:
                    ArrayList[] res = new ArrayList[2];
                    res[0] = new ArrayList();
                    res[1] = new ArrayList();
                    dao = new DAOInformation(context);
                    dao.open();
                    Collections.addAll(res[0], ((DAOInformation) dao).getAllInfos());
                    //Collections.addAll(donnees, ((DAOInformation) dao).getAllInfos());
                    dao.close();
                    dao = new DAONote(context);
                    dao.open();
                    Collections.addAll(res[1], ((DAONote) dao).getAllNotes());
                    return res;
                    //Collections.addAll(donnees, ((DAONote) dao).getAllNotes());
                    //break;
                case PARAM_ABSINFO_WEEK:
                    dao = new DAOInformation(context);
                    dao.open();
                    Cursor cursor = dao.rawQuery("select * from " + DAOInformation.S_TABLE_NAME() + " where " + DAOInformation.INFO_ECHEANCE + " between ? and ?",
                            new String[]{strings[1], strings[2]});
                    AbstractInformation[] tab = new AbstractInformation[cursor.getCount()];
                    int i = 0;
                    while (cursor.moveToNext()) {
                        tab[i] = new Information(cursor.getString(1), cursor.getString(2), cursor.getString(5), cursor.getString(4),
                                cursor.getInt(0), cursor.getInt(3) == 1, cursor.getString(6), TypeInformation.buildFromNom(cursor.getString(7)));
                        i++;
                    }
                    cursor.close();
                    Collections.addAll(donnees, tab);
                    dao.close();
                    dao = new DAONote(context);
                    dao.open();
                    cursor = dao.rawQuery("select * from " + DAONote.S_TABLE_NAME() + " where " + DAONote.NOTE_ECHEANCE + " between ? and ?",
                            new String[]{strings[1], strings[2]});
                    tab = new AbstractInformation[cursor.getCount()];
                    i = 0;
                    while (cursor.moveToNext()) {
                        tab[i] = new Note(cursor.getInt(0), cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4));
                        i++;
                    }
                    cursor.close();
                    Collections.addAll(donnees, tab);
                    break;
                case PARAM_COURS:
                    dao = new DAOCours(context);
                    dao.open();
                    Collections.addAll(donnees, ((DAOCours) dao).getAllCours());
                    break;
            }
            dao.close();
        }

        return donnees.toArray();
    }

    @Override
    protected void onPostExecute(Object[] result){
        mSource.fetchOnlineResult(result, code);
    }

}
