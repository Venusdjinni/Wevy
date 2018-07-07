package com.venus.app.BaseView;

import com.venus.app.Base.Information;

/**
 * Created by arnold on 05/12/17.
 */
public class VInformation extends Information {
    private int count;

    public VInformation(Information base) {
        super(base.getTitre(), base.getDateEnreg(), base.getEcheance(), base.getDescription(), base.getIdInformation(), base.getValide(), base.getAuteur(), base.getTypeInformation());
        count = 0;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

}
