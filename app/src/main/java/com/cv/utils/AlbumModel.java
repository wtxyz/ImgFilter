package com.cv.utils;


public class AlbumModel {

    private String mImgUrlId;
    private String mImgPathName;

    public AlbumModel(String mImgPathName, String mImgUrlId){
        this.mImgPathName = mImgPathName;
        this.mImgUrlId = mImgUrlId;
    }

    public String getmImgUrlId() {
        return mImgUrlId;
    }

    public void setmImgUrlId(String mImgUrlId) {
        this.mImgUrlId = mImgUrlId;
    }

    public String getmImgPathName() {
        return mImgPathName;
    }

    public void setmImgPathName(String mImgPathName) {
        this.mImgPathName = mImgPathName;
    }
}
