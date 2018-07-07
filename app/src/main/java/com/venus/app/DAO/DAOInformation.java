package com.venus.app.DAO;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.venus.app.Base.Information;
import com.venus.app.Base.TypeInformation;
import com.venus.app.services.DailyPinnedInfosService;

/**
 * Created by arnold on 22/07/17.
 */
public class DAOInformation extends DAO {
    public static final String INFO_ID = "idInfo";
    public static final String INFO_TITRE = "titre";
    public static final String INFO_ENREG = "dateEnreg";
    public static final String INFO_VALIDE = "isValide";
    public static final String INFO_DESC = "description";
    public static final String INFO_ECHEANCE = "echeance";
    public static final String INFO_AUTEUR = "auteur";
    public static final String INFO_TYPE = "type";
    public static final String INFO_PIN = "pin";

    private static String TABLE_CREATE = "CREATE TABLE IF NOT EXISTS " + S_TABLE_NAME() + " (" +
            INFO_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            INFO_TITRE + " TEXT, " +
            INFO_ENREG + " TEXT, " +
            INFO_VALIDE + " INTEGER, "+
            INFO_DESC + " TEXT, " +
            INFO_ECHEANCE + " TEXT, " +
            INFO_AUTEUR + " TEXT, " +
            INFO_TYPE + " TEXT, " +
            INFO_PIN + " INTEGER" + ");";

    public DAOInformation(Context pContext) {
        super(pContext);
    }

    @Override
    public String TABLE_NAME() {
        return "Information";
    }

    public static String S_TABLE_NAME() { return "Information"; }

    @Override
    public String TABLE_DROP() {
        return "DROP TABLE IF EXISTS " + TABLE_NAME() + ";";
    }

    @Override
    public String TABLE_CREATE() {
        return DAOInformation.TABLE_CREATE;
    }

    public void addInfo(Information info) {
        ContentValues values = new ContentValues();
        values.put(INFO_ID, info.getIdInformation());
        values.put(INFO_TITRE, info.getTitre());
        values.put(INFO_ENREG, info.getDateEnreg());
        values.put(INFO_VALIDE, info.getValide());
        values.put(INFO_DESC, info.getDescription());
        values.put(INFO_ECHEANCE, info.getEcheance());
        values.put(INFO_AUTEUR, info.getAuteur());
        values.put(INFO_TYPE, info.getTypeInformation().toString());
        mDb.insertWithOnConflict(TABLE_NAME(),null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public Information getInfo(int id) {
        Cursor cursor = mDb.rawQuery("select * from " + TABLE_NAME() + " where " + INFO_ID + " = ?",
                new String[]{String.valueOf(id)});
        cursor.moveToFirst();
        Information info = new Information(cursor.getString(1), cursor.getString(2), cursor.getString(5), cursor.getString(4),
                cursor.getInt(0), cursor.getInt(3) == 1, cursor.getString(6), TypeInformation.buildFromNom(cursor.getString(7)));
        cursor.close();
        return info;
    }

    public boolean removeInfo(int id) {
        return mDb.delete(TABLE_NAME(), INFO_ID + " = ?", new String[]{String.valueOf(id)}) > 0;
    }

    public Information[] getAllInfos() {
        Information[] infos = new Information[length()];
        Cursor cursor = mDb.rawQuery("select " + INFO_ID + " from " + TABLE_NAME(),
                new String[]{});
        int i = 0;
        while (cursor.moveToNext()) {
            infos[i] = getInfo(cursor.getInt(0));
            i++;
        }
        cursor.close();
        return infos;
    }

    public void setInfoValide(int id) {
        ContentValues cv = new ContentValues();
        cv.put(INFO_VALIDE, 1);
        mDb.update(TABLE_NAME(), cv, INFO_ID + " = ?", new String[]{String.valueOf(id)});
    }

    public static void asyncSave(Context context, Information[] informations) {
        new SaveThread(context, informations).run();
        context.startService(new Intent(context, DailyPinnedInfosService.class));
    }

    private static class SaveThread extends Thread {
        private Context context;
        private Information[] infos;

        SaveThread(Context context, Information[] infos) {
            this.context = context;
            this.infos = infos;
        }

        @Override
        public void run() {
            DAOInformation dao = new DAOInformation(context);
            dao.open();
            for (Information i : infos)
                dao.addInfo(i);
            dao.close();
        }
    }
}
