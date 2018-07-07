package com.venus.app.Base;

/**
 * Created by arnold on 22/07/17.
 */
public enum TypeCours {
    CM("Cours Magistral", "CM"),
    TD("Travaux Dirigés", "TD"),
    NULL("", "");

    private String name;
    private String abbr;

    TypeCours(String name, String abbr) {
        this.name= name;
        this.abbr = abbr;
    }


    @Override
    public String toString() {
        return name;
    }

    public String abbr() {
        return abbr;
    }

    public static TypeCours buildFromAbbr(String abbr) {
        switch (abbr) {
            case "CM": return TypeCours.CM;
            case "TD": return TypeCours.TD;
            default: return TypeCours.NULL;
        }
    }

    public static TypeCours[] getValues() {
        return new TypeCours[]{CM, TD};
    }
}
