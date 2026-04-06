package com.hardword.model;

public class GameSession {
    private String targetWord;
    private int attempts;

    public GameSession(String targetWord) {
        this.targetWord = targetWord;
        this.attempts = 0;
    }

    public String getTargetWord() { return targetWord; }
    public void setTargetWord(String targetWord) { this.targetWord = targetWord; }
    public int getAttempts() { return attempts; }
    public void setAttempts(int attempts) { this.attempts = attempts; }
}
