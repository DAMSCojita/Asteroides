package com.stefancojita.asteroides;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class ScoreStorageApplicationFile implements ScoreStorage {

    // Declaració de variables.
    private static String FILE = "/data/data/com.stefancojita.asteroides/Application/scores.txt";
    private Context context;

    public ScoreStorageApplicationFile(Context context) {
        this.context = context;
    }

    // Cream un mètode per emmagatzemar la puntuació.
    public void storeScore(int score, String name, long date) {
        // Guardam la puntuació en un fitxer intern.
        try {
            FileOutputStream f = new FileOutputStream(FILE, true);
            String text = score + " " + name + "\n"; // Creem el text a guardar.
            f.write(text.getBytes());
            f.close();
        } catch (Exception e) {
            Toast.makeText(context, "Error al guardar la puntuació", Toast.LENGTH_SHORT).show();
        }
    }

    // Cream un mètode per obtenir la llista de puntuacions.
    public List<String> getScoreList(int maxNo) {
        List<String> result = new ArrayList<String>();
        // Obtenim la llista de puntuacions.
        try {
            FileInputStream f = new FileInputStream(FILE);
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
