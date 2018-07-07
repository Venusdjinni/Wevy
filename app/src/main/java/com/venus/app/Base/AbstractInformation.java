package com.venus.app.Base;

import android.os.Parcelable;

/**
 * Created by arnold on 12/09/17.
 */
public abstract class AbstractInformation implements Parcelable {
    protected String titre;
    protected String dateEnreg;
    protected String echeance;
    protected String description;

    public AbstractInformation() {}

    public AbstractInformation(String titre, String dateEnreg, String echeance, String description) {
        this.titre = titre;
        this.dateEnreg = dateEnreg;
        this.echeance = echeance;
        this.description = description;
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

}
