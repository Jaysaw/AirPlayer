package com.airplayer.model;

import java.io.Serializable;

/**
 * Created by ZiyiTsang on 15/6/10.
 */
public class Artist implements Serializable {

    private String name;
    private String imagePath = "";

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }
}
