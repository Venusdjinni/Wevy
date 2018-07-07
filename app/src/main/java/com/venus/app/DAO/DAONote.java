package com.venus.app.DAO;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import com.venus.app.Base.Note;

/**
 * Created by arnold on 22/07/17.
 */
public class DAONote extends DAO {
    public static final String NOTE_ID = "idNote";
    public static final String NOTE_TITRE = "titre";
    public static final String NOTE_ENREG = "dateEnreg";
    public static final String NOTE_DESC = "description";
    public static final String NOTE_ECHEANCE = "echeance";
    public static final String NOTE_PIN = "pin";

    private static String TABLE_CREATE = "CREATE TABLE IF NOT EXISTS " + S_TABLE_NAME() + " (" +
            NOTE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            NOTE_TITRE + " TEXT, " +
            NOTE_ENREG + " TEXT, " +
            NOTE_ECHEANCE + " TEXT, "+
            NOTE_DESC + " TEXT, " +
            NOTE_PIN + " INTEGER" + ");";

    public DAONote(Context pContext) {
        super(pContext);
    }

    @Override
    public String TABLE_NAME() {
        return "Note";
    }

    public static String S_TABLE_NAME() { return "Note"; }

    @Override
    public String TABLE_DROP() {
        return "DROP TABLE IF EXISTS " + TABLE_NAME() + ";";
    }

    @Override
    public String TABLE_CREATE() {
        return DAONote.TABLE_CREATE;
    }

    public int addNote(Note note) {
        ContentValues values = new ContentValues();
        values.put(NOTE_TITRE, note.getTitre());
        values.put(NOTE_ENREG, note.getDateEnreg());
        values.put(NOTE_ECHEANCE, note.getEcheance());
        values.put(NOTE_DESC, note.getDescription());
        return (int) mDb.insert(TABLE_NAME(),null, values);
    }

    public Note getNote(int id) {
        Cursor cursor = mDb.rawQuery("select * from " + TABLE_NAME() + " where " + NOTE_ID + " = ?",
                new String[]{String.valueOf(id)});
        cursor.moveToFirst();
        Note note = new Note(cursor.getInt(0), cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4));
        cursor.close();
        return note;
    }

    public boolean removeNote(int id) {
        return mDb.delete(TABLE_NAME(), NOTE_ID + " = ?", new String[]{String.valueOf(id)}) > 0;
    }

    public Note[] getAllNotes() {
        Note[] notes = new Note[length()];
        Cursor cursor = mDb.rawQuery("select " + NOTE_ID + " from " + TABLE_NAME() + " order by " + NOTE_ID + " desc",
                new String[]{});
        int i = 0;
        while (cursor.moveToNext()) {
            notes[i] = getNote(cursor.getInt(0));
            i++;
        }
        cursor.close();
        return notes;
    }
}
