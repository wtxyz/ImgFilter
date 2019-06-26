package com.cv.Entity;

import android.graphics.Bitmap;

import ja.burhanrashid52.photoeditor.PhotoFilter;

public class FilterModel {
    private PhotoFilter photoFilter;
    private Bitmap filterSampleImage;
    private String filterName;
    private String filterID;


    public FilterModel(PhotoFilter photoFilter, Bitmap bitmap, String filterName, String filterID){
        this.photoFilter = photoFilter;
        this.filterSampleImage = bitmap;
        this.filterName = filterName;
        this.filterID = filterID;
    }

    public PhotoFilter getPhotoFilter() {
        return photoFilter;
    }

    public void setPhotoFilter(PhotoFilter photoFilter) {
        this.photoFilter = photoFilter;
    }

    public Bitmap getFilterSampleImage() {
        return filterSampleImage;
    }

    public void setFilterSampleImage(Bitmap filterSampleImage) {
        this.filterSampleImage = filterSampleImage;
    }

    public String getFilterName() {
        return filterName;
    }

    public void setFilterName(String filterName) {
        this.filterName = filterName;
    }

    public String getFilterID() {
        return filterID;
    }

    public void setFilterID(String filterID) {
        this.filterID = filterID;
    }
}
