package com.cv.utils;

import android.content.Context;
import android.graphics.Point;
import android.util.DisplayMetrics;
import android.view.ViewGroup;
import android.view.WindowManager;
public class GetWindowSizeUtil {

    //针对ViewGroup
    public static Point getWindowSize(ViewGroup viewGroup){
        WindowManager windowManager;
        int xOffset;
        int yOffset;
        Point point = new Point();
        DisplayMetrics displaymetrics = new DisplayMetrics();
        windowManager =(WindowManager)viewGroup.getContext().getSystemService(Context.WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getMetrics(displaymetrics);
        xOffset = displaymetrics.widthPixels;
        yOffset = displaymetrics.heightPixels;
        point.x = xOffset;
        point.y = yOffset;
        return point;
    }


}
