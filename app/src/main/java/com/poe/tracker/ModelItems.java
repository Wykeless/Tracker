package com.poe.tracker;

public class ModelItems {

    String item_id, item_name, category_id, item_desc, item_acquisition_date, url;
    long timestamp;

    //empty constructor for firebase
    public ModelItems() {

    }

    public ModelItems(String item_id, String item_name, String category_id, String item_desc, String item_acquisition_date, String url, long timestamp) {
        this.item_id = item_id;
        this.item_name = item_name;
        this.category_id = category_id;
        this.item_desc = item_desc;
        this.url = url;
        this.timestamp = timestamp;
        this.item_acquisition_date = item_acquisition_date;
    }

    public String getItem_id() {
        return item_id;
    }

    public void setItem_id(String item_id) {
        this.item_id = item_id;
    }

    public String getItem_name() {
        return item_name;
    }

    public void setItem_name(String item_name) {
        this.item_name = item_name;
    }

    public String getCategory_id() {
        return category_id;
    }

    public void setCategory_id(String category_id) {
        this.category_id = category_id;
    }

    public String getItem_desc() {
        return item_desc;
    }

    public void setItem_desc(String item_desc) {
        this.item_desc = item_desc;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getItem_acquisition_date() {
        return item_acquisition_date;
    }

    public void setItem_acquisition_date(String item_acquisition_date) {
        this.item_acquisition_date = item_acquisition_date;
    }
}
