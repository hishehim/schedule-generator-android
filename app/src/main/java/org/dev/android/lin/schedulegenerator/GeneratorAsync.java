package org.dev.android.lin.schedulegenerator;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Mike on 1/22/2016.
 */
public class GeneratorAsync extends AsyncTask<Void, Void, Void> {

    private ClassListDB db;

    private int maxBreak, maxDay, maxClasses;
    private float minCredit, maxCredit;

    public GeneratorAsync(Context context) {

/*        SharedPreferences settings = context.getSharedPreferences(MainActivity.PREFS_NAME, 0);

        maxBreak = settings.getInt(MainActivity.SettingsFragment.KEY_BREAK, 0);
        maxDay = settings.getInt(MainActivity.SettingsFragment.KEY_DAYS, 5);
        maxClasses = settings.getInt(MainActivity.SettingsFragment.KEY_CLASSES, 5);
        minCredit = settings.getFloat(MainActivity.SettingsFragment.KEY_MIN_CREDIT, 12f);
        maxCredit = settings.getFloat(MainActivity.SettingsFragment.KEY_MAX_CREDIT, 17f);

        WeeklySchedule.setRequirement(maxBreak, maxDay, maxClasses, minCredit, maxCredit);
        ClassListDB db = new ClassListDB(context);
        db.open();
        HashMap<String, Course> primary = db.getEssentialCoursesHash();
        HashMap<String, Course> secondary = db.getSecondaryCoursesHash();
        db.close();*/
    }


    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }


    @Override
    protected Void doInBackground(Void... params) {
        return null;
    }


    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
    }
}
