package com.example.leah.myapplication;

/**
 * Created by wduello on 2/24/2016.
 */
public class Cow {
    // information that will be set
    private long _cowID;
    private Boolean _isWaiting;
    private Boolean _isTodo;

    // constructor
    public Cow(long cowID, Boolean isWaiting, Boolean isTodo) {
        _cowID = cowID;
        _isWaiting = isWaiting;
        _isTodo = isTodo;
    }

    // getters
    public long getCowID(){
        return _cowID;
    }

    public Boolean getWaitingStatus(){
        return _isWaiting;
    }

    public Boolean getTodoStatus(){
        return _isTodo;
    }

}
