package com.hardword.model;

public class GuessResponse {
    private int green;
    private int yellow;
    private String state;
    private String message;

    public GuessResponse(int green, int yellow, String state, String message) {
        this.green = green;
        this.yellow = yellow;
        this.state = state;
        this.message = message;
    }

    public int getGreen() { return green; }
    public void setGreen(int green) { this.green = green; }
    public int getYellow() { return yellow; }
    public void setYellow(int yellow) { this.yellow = yellow; }
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
