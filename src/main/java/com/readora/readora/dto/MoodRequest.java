package com.readora.readora.dto;

public class MoodRequest {

    private String mood;

    public MoodRequest() {
    }

    public MoodRequest(String mood) {
        this.mood = mood;
    }

    public String getMood() {
        return mood;
    }

    public void setMood(String mood) {
        this.mood = mood;
    }
}
