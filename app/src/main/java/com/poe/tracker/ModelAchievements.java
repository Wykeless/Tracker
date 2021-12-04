package com.poe.tracker;

public class ModelAchievements {

    String name, completed;

    // Constructor for firebase
    public ModelAchievements() {
    }

    // Constructor
    public ModelAchievements(String name, String completed) {
        this.name = name;
        this.completed = completed;
    }

    public String getName() {
        return name;
    }

    public void setName(String achievementName) {
        this.name = achievementName;
    }

    public String getCompleted() {
        return completed;
    }

    public void setCompleted(String completed) {
        this.completed = completed;
    }

}
