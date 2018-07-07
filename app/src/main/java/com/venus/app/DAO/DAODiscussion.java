package com.venus.app.DAO;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Parcel;
import android.os.Parcelable;
import com.venus.app.Base.Discussion;
import com.venus.app.Base.Information;
import com.venus.app.BaseView.VInformation;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by arnold on 28/11/17.
 */
public class DAODiscussion extends DAO {
    public static final String DISC_ID = "idDiscussion";
    public static final String DISC_NONLUS = "nonLus";
    public static final String DISC_LASTID = "lastID";

    private static String TABLE_CREATE = "CREATE TABLE IF NOT EXISTS " + S_TABLE_NAME() + " (" +
            DISC_ID + " TEXT PRIMARY KEY, " +
            DISC_NONLUS + " INTEGER, " +
            DISC_LASTID + " TEXT" + ");";

    public DAODiscussion(Context pContext) {
        super(pContext);
    }

    @Override
    public String TABLE_NAME() {
        return "Discussion";
    }

    public static String S_TABLE_NAME() { return "Discussion"; }

    @Override
    public String TABLE_DROP() {
        return "DROP TABLE IF EXISTS " + TABLE_NAME() + ";";
    }

    @Override
    public String TABLE_CREATE() {
        return DAODiscussion.TABLE_CREATE;
    }

    public void addDiscussion(String id, int nl, String lID) {
        ContentValues values = new ContentValues();
        values.put(DISC_ID, id);
        values.put(DISC_NONLUS, nl);
        values.put(DISC_LASTID, lID);
        mDb.insertWithOnConflict(TABLE_NAME(),null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public DiscItem getDiscussion(String id) {
        Cursor cursor = mDb.rawQuery("select * from " + TABLE_NAME() + " where " + DISC_ID + " = ?",
                new String[]{id});
        cursor.moveToFirst();
        DiscItem d = new DiscItem(cursor.getString(0), cursor.getInt(1), cursor.getString(2));
        cursor.close();
        return d;
    }

    public boolean removeDisc(String id) {
        return mDb.delete(TABLE_NAME(), DISC_ID + " = ?", new String[]{id}) > 0;
    }

    public void removeAllDiscs(int idInfo) {
        for (DiscItem d : getAllDiscs()) {
            if (d.getIdInfo() == idInfo)
                removeDisc(d.getNode());
        }
    }

    public DiscItem[] getAllDiscs() {
        DiscItem[] discs = new DiscItem[this.length()];
        Cursor cursor = mDb.rawQuery("select " + DISC_ID + " from " + TABLE_NAME(),
                new String[]{});
        int i = 0;
        while (cursor.moveToNext()) {
            discs[i] = getDiscussion(cursor.getString(0));
            i++;
        }
        cursor.close();

        return discs;
    }

    public static DiscItem[] setExtraDatas(Context context, Discussion[] discussions){
        DAODiscussion dao = new DAODiscussion(context);
        dao.open();
        ArrayList<DiscItem> a = new ArrayList<>();
        for (Discussion d : discussions)
            a.add(new DiscItem(d));
        for (DiscItem d : dao.getAllDiscs())
            for (DiscItem da : a)
                if (d.getNode().equals(da.getNode())) {
                    da.setNonLus(d.getNonLus());
                    da.setLastMessageID(d.getLastMessageID());
                    break;
                }
        dao.close();
        return a.toArray(new DiscItem[]{});
    }

    public static VInformation[] setExtraDatas(Context context, Information[] infos){
        DAODiscussion dao = new DAODiscussion(context);
        dao.open();
        ArrayList<VInformation> a = new ArrayList<>();
        for (Information i : infos) a.add(new VInformation(i));
        for (DiscItem d : dao.getAllDiscs())
            for (VInformation i : a)
                if (d.getIdInfo() == i.getIdInformation()) {
                    i.setCount(i.getCount() + d.getNonLus());
                    break;
                }
        dao.close();
        return a.toArray(new VInformation[]{});
    }

    public static class DiscItem extends Discussion implements Parcelable {
        private int nonLus;
        private String lastMessageID;
        public boolean notify = false;

        public DiscItem(String node, int nonLus, String lastMessageID) {
            this.idInfo = Integer.parseInt(node.substring(4, node.indexOf('-')));
            this.idDiscussion = Integer.parseInt(node.substring(node.indexOf('-') + 1));
            this.nonLus = nonLus;
            this.lastMessageID = lastMessageID;
        }

        public DiscItem(Discussion from) {
            super(from.getIdDiscussion(), from.getIdInfo(), from.getTitre(), from.getDescription(), from.getMessageCount());
            this.emailAuteur = from.getEmailAuteur();
            this.auteur = from.getAuteur();
        }

        protected DiscItem(Parcel in) {
            super(in);
            nonLus = in.readInt();
            lastMessageID = in.readString();
        }

        public static final Creator<DiscItem> CREATOR = new Creator<DiscItem>() {
            @Override
            public DiscItem createFromParcel(Parcel in) {
                return new DiscItem(in);
            }

            @Override
            public DiscItem[] newArray(int size) {
                return new DiscItem[size];
            }
        };

        @Override
        public void writeToParcel(Parcel parcel, int i) {
            super.writeToParcel(parcel, i);
            parcel.writeInt(nonLus);
            parcel.writeString(lastMessageID);
        }

        public int getNonLus() {
            return nonLus;
        }

        public void setNonLus(int nonLus) {
            this.nonLus = nonLus;
        }

        public void upvoteNonLus() { this.nonLus++; }

        public void resetNonLus() {
            this.nonLus = 0;
            this.notify = false;
        }

        public String getLastMessageID() {
            return lastMessageID;
        }

        public void setLastMessageID(String lastMessageID) {
            this.lastMessageID = lastMessageID;
        }
    }
}
