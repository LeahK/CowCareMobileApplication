package com.example.leah.myapplication;

/**
 * Created by zhengliu on 4/23/16.
 */
public class Step {

    String id = null;
    String des = null;
    boolean selected = false;

    public Step(String id, String des, boolean selected) {
        super();
        this.id = id;
        this.des = des;
        this.selected = selected;
    }

    public String getID() {
        return id;
    }
    public void setID(String id) {
        this.id = id;
    }
    public String getDes() {
        return des;
    }
    public void setDes(String des) {
        this.des = des;
    }

    public boolean isSelected() {
        return selected;
    }
    public void setSelected(boolean selected) {
        this.selected = selected;
    }

}
