package com.example.yuzelli.carplaterecognition.bean;

import java.io.Serializable;

/**
 * Created by 51644 on 2017/8/9.
 */

public class CarBean implements Serializable {
    private String number;
    private String time;

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }
}
