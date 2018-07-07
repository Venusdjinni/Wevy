package com.venus.app.Base;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.SparseArray;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by arnold on 29/07/17.
 */
public class ListeCours extends SparseArray<ArrayList<Cours>> implements Parcelable {
    public ListeCours() {
        super();
    }

    protected ListeCours(Parcel in) {
    }

    public static final Creator<ListeCours> CREATOR = new Creator<ListeCours>() {
        @Override
        public ListeCours createFromParcel(Parcel in) {
            return new ListeCours(in);
        }

        @Override
        public ListeCours[] newArray(int size) {
            return new ListeCours[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
    }

    public void put(int key, Cours[] cours) {
        ArrayList<Cours> a = new ArrayList<>();
        Collections.addAll(a, cours);
        this.put(key, a);
    }

    public static ListeCours parseArray(Object[] objects) {
        // on transforme en ListeCours
        ListeCours lc = new ListeCours();
        for (Object o : objects) {
            Cours c = (Cours) o;
            if (lc.indexOfKey(c.getJour()) < 0) {
                // le jour n'existe pas encore
                ArrayList<Cours> a = new ArrayList<>();
                a.add(c);
                lc.put(c.getJour(), a);
            } else lc.get(c.getJour()).add(c);
        }

        return lc;
    }
}
