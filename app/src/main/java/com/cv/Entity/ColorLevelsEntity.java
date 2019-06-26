package com.cv.Entity;

public class ColorLevelsEntity {
    //储存每个通道的对应的值
    private int colorChannel;//颜色通道
    private int shadowValue;//黑点值
    private int midtonesValus;//灰点值
    private int highlightValue;//白点值
    private int outputShadowValue;//输出黑点值
    private int outputHighlightValue;//输出白点值

    public ColorLevelsEntity(){

    }

    public ColorLevelsEntity(int colorChannel,int shadowValue,int midtones,int highlight,int outputShadow,int outputHighlight){
        this.colorChannel = colorChannel;
        this.shadowValue = shadowValue;
        this.midtonesValus = midtones;
        this.highlightValue = highlight;
        this.outputShadowValue = outputShadow;
        this.outputHighlightValue = outputHighlight;
    }


    public int getColorChannel() {
        return colorChannel;
    }

    public void setColorChannel(int colorChannel) {
        this.colorChannel = colorChannel;
    }

    public int getShadowValue() {
        return shadowValue;
    }

    public void setShadowValue(int shadowValue) {
        this.shadowValue = shadowValue;
    }

    public int getMidtonesValus() {
        return midtonesValus;
    }

    public void setMidtonesValus(int midtonesValus) {
        this.midtonesValus = midtonesValus;
    }

    public int getHighlightValue() {
        return highlightValue;
    }

    public void setHighlightValue(int highlightValue) {
        this.highlightValue = highlightValue;
    }

    public int getOutputShadowValue() {
        return outputShadowValue;
    }

    public void setOutputShadowValue(int outputShadowValue) {
        this.outputShadowValue = outputShadowValue;
    }

    public int getOutputHighlightValue() {
        return outputHighlightValue;
    }

    public void setOutputHighlightValue(int outputHighlightValue) {
        this.outputHighlightValue = outputHighlightValue;
    }
}
