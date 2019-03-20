package com.sparklit.adbutler;


/**
 * The size of an ad request.
 */
public class AdSize {
    private int mWidth;
    private int mHeight;

    public AdSize(int width, int height) {
        this.mWidth = width;
        this.mHeight = height;
    }

    public int getWidth() {
        return mWidth;
    }

    public int getHeight() {
        return mHeight;
    }
}
