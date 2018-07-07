package com.venus.app.Base;

/**
 * Created by arnold on 27/10/17.
 */
public class Message {
    private String auteur;
    private String message;
    private String date;

    public Message() {
        auteur = "";
        message = "";
        date = "";
    }

    public Message(String auteur, String message, String date) {
        this.auteur = auteur;
        this.message = message;
        this.date = date;
    }

    public String getAuteur() {
        return auteur;
    }

    public void setAuteur(String auteur) {
        this.auteur = auteur;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
