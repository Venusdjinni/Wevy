package com.venus.app.Base;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by arnold on 22/07/17.
 */
public class Cours implements Parcelable {
    private int idCours;
    private String nom;
    private String nomProf;
    private String heureD;
    private String heureF;
    private TypeCours typeCours;
    private int jour;

    public Cours() {
        idCours = 0;
    }

    public Cours(String nom, String nomProf, String heureD, String heureF, TypeCours type) {
        this.nom = nom;
        this.nomProf = nomProf;
        this.heureD = heureD;
        this.heureF = heureF;
        this.typeCours = type;
    }

    public Cours(int idCours, String nom, String nomProf, String heureD, String heureF, TypeCours typeCours, int jour) {
        this.idCours = idCours;
        this.nom = nom;
        this.nomProf = nomProf;
        this.heureD = heureD;
        this.heureF = heureF;
        this.typeCours = typeCours;
        this.jour = jour;
    }

    protected Cours(Parcel in) {
        idCours = in.readInt();
        nom = in.readString();
        nomProf = in.readString();
        heureD = in.readString();
        heureF = in.readString();
        jour = in.readInt();
        typeCours = TypeCours.buildFromAbbr(in.readString());
    }

    public static final Creator<Cours> CREATOR = new Creator<Cours>() {
        @Override
        public Cours createFromParcel(Parcel in) {
            return new Cours(in);
        }

        @Override
        public Cours[] newArray(int size) {
            return new Cours[size];
        }
    };

    public int getIdCours() {
        return idCours;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getNomProf() {
        return nomProf;
    }

    public void setNomProf(String nomProf) {
        this.nomProf = nomProf;
    }

    public String getHeureD() {
        return heureD;
    }

    public void setHeureD(String heureD) {
        this.heureD = heureD;
    }

    public String getHeureF() {
        return heureF;
    }

    public void setHeureF(String heureF) {
        this.heureF = heureF;
    }

    public TypeCours getTypeCours() {
        return typeCours;
    }

    public void setTypeCours(TypeCours typeCours) {
        this.typeCours = typeCours;
    }

    public int getJour() {
        return jour;
    }

    public void setJour(int jour) {
        this.jour = jour;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(idCours);
        parcel.writeString(nom);
        parcel.writeString(nomProf);
        parcel.writeString(heureD);
        parcel.writeString(heureF);
        parcel.writeInt(jour);
        parcel.writeString(typeCours.abbr());
    }
}
