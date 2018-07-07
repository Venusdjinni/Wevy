package com.venus.app.Base;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by arnold on 27/07/17.
 */
public class Discussion implements Parcelable {
    protected int idDiscussion;
    protected int idInfo;
    protected String titre;
    protected String description;
    protected int messageCount;
    protected String emailAuteur;
    protected String auteur;

    public Discussion() {}

    public Discussion(int idDiscussion, int idInfo, String titre, String description, int messageCount) {
        this.idDiscussion = idDiscussion;
        this.idInfo = idInfo;
        this.titre = titre;
        this.description = description;
        this.messageCount = messageCount;
    }

    public Discussion(int idInfo, String titre, String description) {
        this.idInfo = idInfo;
        this.titre = titre;
        this.description = description;
    }

    protected Discussion(Parcel in) {
        idDiscussion = in.readInt();
        idInfo = in.readInt();
        titre = in.readString();
        description = in.readString();
        messageCount = in.readInt();
        emailAuteur = in.readString();
        auteur = in.readString();
    }

    public static final Creator<Discussion> CREATOR = new Creator<Discussion>() {
        @Override
        public Discussion createFromParcel(Parcel in) {
            return new Discussion(in);
        }

        @Override
        public Discussion[] newArray(int size) {
            return new Discussion[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(idDiscussion);
        parcel.writeInt(idInfo);
        parcel.writeString(titre);
        parcel.writeString(description);
        parcel.writeInt(messageCount);
        parcel.writeString(emailAuteur);
        parcel.writeString(auteur);
    }

    public int getIdDiscussion() {
        return idDiscussion;
    }

    public int getIdInfo() {
        return idInfo;
    }

    public void setIdInfo(int idInfo) {
        this.idInfo = idInfo;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getMessageCount() {
        return messageCount;
    }

    public void setMessageCount(int messageCount) {
        this.messageCount = messageCount;
    }

    public String getEmailAuteur() {
        return emailAuteur;
    }

    public void setEmailAuteur(String emailAuteur) {
        this.emailAuteur = emailAuteur;
    }

    public String getAuteur() {
        return auteur;
    }

    public void setAuteur(String auteur) {
        this.auteur = auteur;
    }

    public String getNode() { return "disc" + idInfo + "-" + idDiscussion; }
}
