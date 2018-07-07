package com.venus.app.Base;

/**
 * Created by arnold on 22/07/17.
 */
public enum TypeInformation {
    DEVOIR("Devoir"),
    RATTRAPPAGE("Rattrapage"),
    EVALUATION("Evaluation"),
    AUTRE("Autre"),
    NULL("");

    String nom;
    TypeInformation(String nom) {
        this.nom = nom;
    }

    @Override
    public String toString() {
        return nom;
    }

    public static TypeInformation buildFromNom(String nom) {
        switch (nom) {
            case "Devoir": return DEVOIR;
            case "Rattrapage": return RATTRAPPAGE;
            case "Evaluation": return EVALUATION;
            case "Autre": return AUTRE;
            default: return NULL;
        }
    }

    public static TypeInformation[] getValues() {
        return new TypeInformation[] {DEVOIR, RATTRAPPAGE, EVALUATION, AUTRE};
    }
}
