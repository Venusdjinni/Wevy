package com.venus.app.services;

import android.app.IntentService;
import android.content.Intent;
import android.database.Cursor;
import com.venus.app.Base.AbstractInformation;
import com.venus.app.Base.Information;
import com.venus.app.Base.Note;
import com.venus.app.Base.TypeInformation;
import com.venus.app.DAO.DAOInformation;
import com.venus.app.DAO.DAONote;
import com.venus.app.wevy.Utilities;

import java.util.ArrayList;
import java.util.Calendar;


/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions and extra parameters.
 */
public class DailyPinnedInfosService extends IntentService {
    public DailyPinnedInfosService() {
        super("DailyPinnedInfosService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // Epingle les activités de la journée et les affiche dans PinnedInformationsService
        // Désépingle les infos de la veille
        buildList();
    }

    private void buildList() {
        ArrayList<AbstractInformation> toAdd = new ArrayList<>(), toRem = new ArrayList<>();

        DAOInformation daoi = new DAOInformation(this);
        daoi.open();
        // On recupere les infos d'aujourd'hui et d'hier
        Calendar c = Calendar.getInstance();
        c.roll(Calendar.DAY_OF_MONTH, -1);
        Cursor cursor = daoi.rawQuery("select * from " + DAOInformation.S_TABLE_NAME() + " where " + DAOInformation.INFO_ECHEANCE + " = ? or " + DAOInformation.INFO_ECHEANCE + " = ?",
                new String[]{Utilities.parseCalendar(Calendar.getInstance()), Utilities.parseCalendar(c)});
        while (cursor.moveToNext()) {
            Information i = new Information(cursor.getString(1), cursor.getString(2), cursor.getString(5), cursor.getString(4),
                    cursor.getInt(0), cursor.getInt(3) == 1, cursor.getString(6), TypeInformation.buildFromNom(cursor.getString(7)));
            if (!i.getValide()) continue;
            if (i.getEcheance().equals(Utilities.parseCalendar(Calendar.getInstance())))
                toAdd.add(i);
            else toRem.add(i);
        }
        cursor.close();
        daoi.close();
        DAONote daon = new DAONote(this);
        daon.open();
        cursor = daon.rawQuery("select * from " + DAONote.S_TABLE_NAME() + " where " + DAONote.NOTE_ECHEANCE + " = ? or " + DAONote.NOTE_ECHEANCE + " = ?",
                new String[]{Utilities.parseCalendar(Calendar.getInstance()), Utilities.parseCalendar(c)});
        while (cursor.moveToNext()) {
            Note n = new Note(cursor.getInt(0), cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4));
            if (n.getEcheance().equals(Utilities.parseCalendar(Calendar.getInstance())))
                toAdd.add(n);
            else toRem.add(n);
        }
        daon.close();

        PinnedInformationsService.startService(this, toAdd, toRem);
    }
}
