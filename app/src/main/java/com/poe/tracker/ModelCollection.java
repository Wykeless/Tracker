package com.poe.tracker;


public class ModelCollection {

    String collection_id, collection_name, user_id, collection_desc, url;
    int goal;
    long timestamp;

    //Empty Constructor required for firebase
    public ModelCollection() {

    }

    public ModelCollection(String collection_id, String collection_name, String user_id, String collection_desc, String  url, int goal, long timestamp) {
        this.collection_id = collection_id;
        this.collection_name = collection_name;
        this.user_id = user_id;
        this.collection_desc = collection_desc;
        this.url = url;
        this.goal = goal;
        this.timestamp = timestamp;
    }

    public String getCollection_id() {
        return collection_id;
    }

    public void setCollection_id(String collection_id) {
        this.collection_id = collection_id;
    }

    public String getCollection_name() {
        return collection_name;
    }

    public void setCollection_name(String collection_name) {
        this.collection_name = collection_name;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getCollection_desc() {
        return collection_desc;
    }

    public void setCollection_desc(String collection_desc) {
        this.collection_desc = collection_desc;
    }

    public String  getUrl() {
        return url;
    }

    public void setUrl(String  url) {
        this.url = url;
    }

    public int getGoal() {
        return goal;
    }

    public void setGoal(int goal) {
        this.goal = goal;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}




