package com.stefancojita.asteroides;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class ScoreStorageInternalFile implements ScoreStorage {

    // Declaració de variables.
    private static String FILE = "scores.txt"; // Emprem un fitxer per emmagatzemar les puntuacions.
    private Context context;

    public ScoreStorageInternalFile(Context context) {
        this.context = context;
    }

    // Cream un mètode per emmagatzemar la puntuació.
    public void storeScore(int score, String name, long date) {
        // Guardam la puntuació en un fitxer intern.
        try {
            FileOutputStream f = context.openFileOutput(FILE, Context.MODE_APPEND); // Obrim el fitxer en mode afegir.
            String text = score + " " + name + "\n"; // Creem el text a guardar.
            f.write(text.getBytes());
            f.close();
        } catch (Exception e) {
            Log.e("Asteroids", e.getMessage(), e);
        }
    }

    // Cream un mètode per obtenir la llista de puntuacions.
    public List<String> getScoreList(int maxNo) {
        List<String> result = new ArrayList<String>();
        // Obtenim la llista de puntuacions.
        try {
            FileInputStream f = context.openFileInput(FILE); // Obrim el fitxer en mode lectura.
            BufferedReader inReader = new BufferedReader(new InputStreamReader(f)); // Creem un buffer de lectura.
            // Recorrem el fitxer per obtenir les puntuacions.
            int n = 0; // Comptador de puntuacions.
            String line; // Línia llegida.
            do {
                line = inReader.readLine();
                if (line != null) {
                    result.add(line);
                    n++;
                }
            } while (n < maxNo && line != null);
            f.close();
        } catch (Exception e) {
            Log.e("Asteroids", e.getMessage(), e); // Mostrem l'error en el log (cas que ocorri).
        }
        return result;
    }
}
