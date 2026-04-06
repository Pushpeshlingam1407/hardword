package com.hardword.model;

public class GuessRequest {
    private String sessionId;
    private String guess;

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    public String getGuess() { return guess; }
    public void setGuess(String guess) { this.guess = guess; }
}
