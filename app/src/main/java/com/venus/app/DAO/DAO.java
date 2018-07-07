package com.venus.app.DAO;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by arnold on 14/10/16.
 */
public abstract class DAO {
    // Nous sommes à la première version de la base
    // Si je décide de la mettre à jour, il faudra changer cet attribut
    protected final static int VERSION = 1;
    // Le nom du fichier qui représente ma base
    public static final String NOM = "database.db";
    public abstract String TABLE_NAME();
    public abstract String TABLE_DROP();
    public abstract String TABLE_CREATE();
    protected SQLiteDatabase mDb = null;
    protected DataBaseHandler mHandler = null;

    class DataBaseHandler extends SQLiteOpenHelper {

        public DataBaseHandler(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        @Override
        public void onCreate(SQLiteDatabase sqLiteDatabase) {
            sqLiteDatabase.execSQL(TABLE_CREATE());
        }

        @Override
        // met a jour la bd en la detruisant,puis la recreant
        public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
            mDb.delete(TABLE_NAME(), "1", null);
            onCreate(sqLiteDatabase);
        }
    }

    public DAO(Context pContext) {
        this.mHandler = new DataBaseHandler(pContext, NOM, null, VERSION);
    }

    public SQLiteDatabase open() {
        // Pas besoin de fermer la dernière base puisque getWritableDataBase s'en charge
        mDb = mHandler.getWritableDatabase();
        mDb.execSQL(TABLE_CREATE());
        return mDb;
    }

    public void close() {
        mDb.close();
    }

    public SQLiteDatabase getDb() {
        return mDb;
    }

    public void drop(){
        mDb.delete(TABLE_NAME(), "1", null);
    }

    public int length(){
        Cursor cursor = mDb.rawQuery("select * from " + TABLE_NAME(), null);
        int rep = cursor.getCount();
        cursor.close();
        return rep;
    }

    public Cursor rawQuery(String sql, String[] selectionArgs){
        return mDb.rawQuery(sql, selectionArgs);
    }
}
