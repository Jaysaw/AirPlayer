package com.airplayer.model;

import android.net.Uri;
import android.os.Environment;

/**
 * Created by ZiyiTsang on 15/6/10.
 */
public class Artist extends AirModel implements PictureGettable {

    private int id;

    private String name;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getArtistPicturePath() {
        if (sSp.getBoolean(name + id, false)) {
            return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                    + "/AirPlayer/"
                    + getSaveName() + ".jpg";
        } else {
            return "";
        }

    }

    public Uri getArtistPictureUri() {
        if (sSp.getBoolean(name + id, false)) {
            return Uri.parse("file://" + Uri.decode(getArtistPicturePath()));
        } else {
            return null;
        }
    }

    @Override
    public String getSearchKeyword() {
        return name;
    }

    @Override
    public String getSaveName() {
        if (name.contains("/")) {
            String[] names = name.split("/");
            StringBuilder builder = new StringBuilder();
            for (String str : names) {
                builder.append(str);
            }
            return builder.toString();
        } else {
            return name;
        }
    }

    @Override
    public void setPictureDownloaded(boolean b) {
        sSp.edit().putBoolean(name + id, b).apply();
    }

    @Override
    public String getPicturePath() {
        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                + "/AirPlayer/"
                + getSaveName() + ".jpg";
    }
}
