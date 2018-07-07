package com.venus.app.DAO;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import com.venus.app.Base.Cours;
import com.venus.app.Base.TypeCours;

/**
 * Created by arnold on 22/07/17.
 */
public class DAOCours extends DAO {
    public static final String COURS_ID = "idCours";
    public static final String COURS_NOM = "nomCours";
    public static final String COURS_NPROF = "nomProf";
    public static final String COURS_HD = "heureD";
    public static final String COURS_HF = "heureF";
    public static final String COURS_TYPE = "type";
    public static final String COURS_JOUR = "jour";

    private static String TABLE_CREATE = "CREATE TABLE IF NOT EXISTS " + S_TABLE_NAME() + " (" +
            COURS_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COURS_NOM + " TEXT, " +
            COURS_NPROF + " TEXT, "+
            COURS_HD + " TEXT, " +
            COURS_HF + " TEXT, " +
            COURS_TYPE + " TEXT, " +
            COURS_JOUR + " INTEGER" + ");";

    public DAOCours(Context pContext) {
        super(pContext);
    }

    @Override
    public String TABLE_NAME() {
        return "Cours";
    }

    public static String S_TABLE_NAME() { return "Cours"; }

    @Override
    public String TABLE_DROP() {
        return "DROP TABLE IF EXISTS " + TABLE_NAME() + ";";
    }

    @Override
    public String TABLE_CREATE() {
        return DAOCours.TABLE_CREATE;
    }

    public void addCours(Cours cours) {
        ContentValues values = new ContentValues();
        values.put(COURS_ID, cours.getIdCours());
        values.put(COURS_NOM, cours.getNom());
        values.put(COURS_NPROF, cours.getNomProf());
        values.put(COURS_HD, cours.getHeureD());
        values.put(COURS_HF, cours.getHeureF());
        values.put(COURS_TYPE, cours.getTypeCours().abbr());
        values.put(COURS_JOUR, cours.getJour());
        mDb.insert(TABLE_NAME(), null, values);
    }

    public Cours getCours(int id) {
        Cursor cursor = mDb.rawQuery("select * from " + TABLE_NAME() + " where " + COURS_ID + " = ?",
                new String[]{String.valueOf(id)});
        cursor.moveToFirst();
        Cours cours = new Cours(cursor.getInt(0), cursor.getString(1), cursor.getString(2), cursor.getString(3),
                cursor.getString(4), TypeCours.buildFromAbbr(cursor.getString(5)), cursor.getInt(6));
        cursor.close();
        return cours;
    }

    public boolean removeCours(int id) {
        return mDb.delete(TABLE_NAME(), COURS_ID + " = ?", new String[]{String.valueOf(id)}) > 0;
    }

    public Cours[] getAllCours() {
        Cours[] cours = new Cours[length()];
        Cursor cursor = mDb.rawQuery("select " + COURS_ID + " from " + TABLE_NAME() + " order by " + COURS_ID + " desc",
                new String[]{});
        int i = 0;
        while (cursor.moveToNext()) {
            cours[i] = getCours(cursor.getInt(0));
            i++;
        }
        cursor.close();

        return cours;
    }

    public static void asyncSave(Context context, Cours[] cours) {
        new SaveThread(context, cours).run();
    }

    private static class SaveThread extends Thread {
        private Context context;
        private Cours[] cours;

        SaveThread(Context context, Cours[] cours) {
            this.context = context;
            this.cours = cours;
        }

        @Override
        public void run() {
            DAOCours dao = new DAOCours(context);
            dao.open();
            dao.drop();
            for (Cours c : cours)
                dao.addCours(c);
            dao.close();
        }
    }
}
