package com.stefancojita.asteroides;

import java.util.List;

public interface ScoreStorage {
    public void storeScore(int score, String name, long date);
    public List<String> getScoreList(int maxNo);
}
