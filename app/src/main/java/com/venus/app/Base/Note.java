package com.venus.app.Base;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by arnold on 22/07/17.
 */
public class Note extends AbstractInformation implements Parcelable {
    private int idNote;

    public Note() {}

    public Note(int idNote, String titre, String dateEnreg, String echeance, String description) {
        this.idNote = idNote;
        this.titre = titre;
        this.dateEnreg = dateEnreg;
        this.echeance = echeance;
        this.description = description;
    }

    protected Note(Parcel in) {
        idNote = in.readInt();
        titre = in.readString();
        dateEnreg = in.readString();
        echeance = in.readString();
        description = in.readString();
    }

    public static final Creator<Note> CREATOR = new Creator<Note>() {
        @Override
        public Note createFromParcel(Parcel in) {
            return new Note(in);
        }

        @Override
        public Note[] newArray(int size) {
            return new Note[size];
        }
    };

    public int getIdNote() {
        return idNote;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public String getDateEnreg() {
        return dateEnreg;
    }

    public void setDateEnreg(String dateEnreg) {
        this.dateEnreg = dateEnreg;
    }

    public String getEcheance() {
        return echeance;
    }

    public void setEcheance(String echeance) {
        this.echeance = echeance;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(idNote);
        parcel.writeString(titre);
        parcel.writeString(dateEnreg);
        parcel.writeString(echeance);
        parcel.writeString(description);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Note && this.idNote == ((Note) obj).getIdNote();
    }

    @Override
    public int hashCode() {
        return "note".hashCode() + idNote;
    }
}
