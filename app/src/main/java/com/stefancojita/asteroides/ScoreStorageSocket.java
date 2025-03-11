package com.stefancojita.asteroides;

import android.os.StrictMode;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;

public class ScoreStorageSocket implements ScoreStorage {

    // Constructor de la classe ScoreStorageSocket
    public ScoreStorageSocket(MainActivity mainActivity) {
        // Permetem operacions de xarxa en el fil principal
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitNetwork().build());
    }

    // Mètode per emmagatzemar una puntuació
    public void storeScore(int score, String name, long date){
        try {
            // Creem un socket per connectar-nos al servidor
            Socket sk = new Socket("10.0.2.2", 1234);

            BufferedReader in = new BufferedReader(new InputStreamReader(sk.getInputStream())); // Creem un lector per llegir la resposta del servidor
            PrintWriter out = new PrintWriter(new OutputStreamWriter(sk.getOutputStream()),true); // Creem un escriptor per enviar dades al servidor
            // Enviem la puntuació i el nom al servidor
            out.println(score + " " + name);
            // Llegim la resposta del servidor
            String answer = in.readLine();
            // Comprovem si la resposta és "OK"
            if (!answer.equals("OK")) {
                // Si no és "OK", registrem un error
                Log.e("Asteroids", "Error: resposta de servidor incorrecta");
            }
            // Tanquem el socket
            sk.close();
        } catch (Exception e) {
            // Registrem qualsevol excepció que es produeixi
            Log.e("Asteroids", e.toString(), e);
        }
    }

    // Mètode per obtenir la llista de puntuacions
    public ArrayList<String> getScoreList(int maxNo) {
        // Creem una llista per emmagatzemar les puntuacions
        ArrayList<String> result = new ArrayList<String>();
        try {
            // Creem un socket per connectar-nos al servidor
            Socket sk = new Socket("10.0.2.2", 1234);
            // Creem un lector per llegir la resposta del servidor
            BufferedReader in = new BufferedReader(new InputStreamReader(sk.getInputStream()));
            // Creem un escriptor per enviar dades al servidor
            PrintWriter out = new PrintWriter(new OutputStreamWriter(sk.getOutputStream()),true);
            // Enviem la comanda "SCORES" al servidor
            out.println("SCORES");
            int n = 0;
            String answer;
            // Llegim les respostes del servidor fins a maxNo o fins que no hi hagi més respostes
            do {
                answer = in.readLine();
                if (answer != null) {
                    // Afegim la resposta a la llista de resultats
                    result.add(answer);
                    n++;
                }
            } while (n < maxNo && answer != null);
            // Tanquem el socket
            sk.close();
        } catch (Exception e) {
            // Registrem qualsevol excepció que es produeixi
            Log.e("Asteroids", e.toString(), e);
        }
        // Retornem la llista de puntuacions
        return result;
    }
}