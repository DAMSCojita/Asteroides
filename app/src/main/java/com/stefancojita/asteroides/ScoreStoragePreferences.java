package com.stefancojita.asteroides;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.List;

public class ScoreStoragePreferences implements ScoreStorage {

    // Declaració de variables i constants.
    private static String PREFERENCES = "scores";
    private Context context;

    public ScoreStoragePreferences(Context context) {
        this.context = context;
    }

    // Mètodes de l'interfície ScoreStorage.
    public void storeScore(int score, String name, long date) {
        // Guardam la puntuació en les preferències.
        SharedPreferences prefs = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        // Obtenim la llista de puntuacions per un ArrayList.
        List<String> scores = new ArrayList<>();
        // Recorrem les preferències per obtenir les puntuacions.
        for (int n = 0; n < 9; n++) {
            String s = prefs.getString("score" + n, ""); // Obtenim la puntuació.
            if (!s.isEmpty()) {
                scores.add(s);
            }
        }

        // Insertem la nova puntuació.
        scores.add(0, score + " " + name);

        // Guardem NOMÉS les 10 primeres puntuacions.
        for (int n = 0; n < scores.size() && n < 10; n++) {
            editor.putString("score" + n, scores.get(n));
        }

        editor.apply();
    }

    // Mètode per obtenir la llista de puntuacions.
    public List<String> getScoreList(int maxNo) {
        // Obtenim la llista de puntuacions.
        List<String> result = new ArrayList<String>();
        SharedPreferences prefs = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);

        // Recorrem les preferències per obtenir les puntuacions.
        for (int n = 0; n < 10 && result.size() < maxNo; n++) {
            String s = prefs.getString("score" + n, ""); // Obtenim la puntuació.
            if (!s.isEmpty()) {
                result.add(s);
            }
        }
        return result;
    }
}