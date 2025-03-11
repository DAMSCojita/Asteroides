package com.stefancojita.asteroides;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public class ScoreStorageSQLite extends SQLiteOpenHelper implements ScoreStorage {

    public ScoreStorageSQLite(Context context) {
        super(context, "scores", null, 1);
    }

    @Override public void onCreate(SQLiteDatabase db) {
        // Cream una query que creará una taula de puntuacions amb els camps _id, score, name i date.
        db.execSQL("CREATE TABLE scores ("+ "_id INTEGER PRIMARY KEY AUTOINCREMENT, " + "score INTEGER, name TEXT, date BIGINT)");
    }

    // Mètode per actualitzar la base de dades. No necessitarem res dins de moment.
    @Override public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {}

    // Cream un mètode per emmagatzemar una puntuació.
    public void storeScore(int score, String name, long date) {
        SQLiteDatabase db = getWritableDatabase(); // Obtenim una referència a la base de dades.
        db.execSQL("INSERT INTO scores VALUES (null, "+score+", '"+name+"', "+date+")"); // Executam una query SQL per inserir una nova puntuació.
        db.close(); // Tanquem la base de dades.
    }

    // Cream un mètode per obtenir una llista de puntuacions.
    public ArrayList<String> getScoreList(int maxNo) {
        ArrayList<String> result = new ArrayList<String>(); // Cream un ArrayList per emmagatzemar les puntuacions.
        SQLiteDatabase db = getReadableDatabase(); // Obtenim una referència a la base de dades.
        String[] FIELDS = {"score", "name"}; // Cream un array amb els camps que volem obtenir.
        // Executam una query SQL per obtenir les puntuacions ordenades per puntuació de forma descendent. Ho feim amb es cursor.
        Cursor cursor = db.query("scores", FIELDS, null, null, null, null, "score DESC", Integer.toString(maxNo));
        // Recorrem el cursor per obtenir les puntuacions.
        while (cursor.moveToNext()){
            result.add(cursor.getInt(0)+ " " + cursor.getString(1)); // Afegim la puntuació al ArrayList.
        }
        cursor.close(); // Tanquem el cursor.
        db.close(); // Tanquem la base de dades.
        return result; // Retornam el ArrayList amb les puntuacions.
    }
}
