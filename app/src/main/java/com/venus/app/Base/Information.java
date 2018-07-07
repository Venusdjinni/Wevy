package com.venus.app.Base;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by arnold on 22/07/17.
 */
public class Information extends AbstractInformation implements Parcelable {
    protected int idInformation;
    protected boolean isValide;
    protected String auteur;
    protected TypeInformation typeInformation;

    public Information() {
        idInformation = 0;
        typeInformation = TypeInformation.NULL;
    }

    public Information(String titre, TypeInformation typeInformation, String description) {
        this.titre = titre;
        this.typeInformation = typeInformation;
        this.description = description;
    }

    public Information(String titre, String dateEnreg, String description, String echeance, TypeInformation typeInformation) {
        this.titre = titre;
        this.dateEnreg = dateEnreg;
        this.description = description;
        this.echeance = echeance;
        this.typeInformation = typeInformation;
    }

    public Information(String titre, String dateEnreg, String echeance, String description, int idInformation, boolean isValide, String auteur, TypeInformation typeInformation) {
        super(titre, dateEnreg, echeance, description);
        this.idInformation = idInformation;
        this.isValide = isValide;
        this.auteur = auteur;
        this.typeInformation = typeInformation;
    }

    protected Information(Parcel in) {
        idInformation = in.readInt();
        titre = in.readString();
        dateEnreg = in.readString();
        isValide = in.readByte() != 0;
        description = in.readString();
        echeance = in.readString();
        auteur = in.readString();
        typeInformation = TypeInformation.buildFromNom(in.readString());
    }

    public static final Creator<Information> CREATOR = new Creator<Information>() {
        @Override
        public Information createFromParcel(Parcel in) {
            return new Information(in);
        }

        @Override
        public Information[] newArray(int size) {
            return new Information[size];
        }
    };

    public int getIdInformation() {
        return idInformation;
    }

    public TypeInformation getTypeInformation() {
        return typeInformation;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public void setTypeInformation(TypeInformation typeInformation) {
        this.typeInformation = typeInformation;
    }

    public String getDateEnreg() {
        return dateEnreg;
    }

    public void setDateEnreg(String dateEnreg) {
        this.dateEnreg = dateEnreg;
    }

    public boolean getValide() { return isValide; }

    public void setValide(boolean valide) {
        isValide = valide;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getEcheance() {
        return echeance;
    }

    public void setEcheance(String echeance) {
        this.echeance = echeance;
    }

    public String getAuteur() {
        return auteur;
    }

    public void setAuteur(String auteur) {
        this.auteur = auteur;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(idInformation);
        parcel.writeString(titre);
        parcel.writeString(dateEnreg);
        parcel.writeByte((byte) (isValide ? 1 : 0));
        parcel.writeString(description);
        parcel.writeString(echeance);
        parcel.writeString(auteur);
        parcel.writeString(typeInformation.toString());
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Information && this.idInformation == ((Information) obj).getIdInformation();
    }

    @Override
    public int hashCode() {
        return "information".hashCode() + idInformation;
    }
}
