package com.deekshant.browser;

import android.graphics.Bitmap;

public class TabListItem {
    String Title;
    Bitmap Icon;

    public TabListItem(String title, Bitmap icon) {
        Title = title;
        Icon = icon;
    }

    public String getTitle() {
        return Title;
    }

    public Bitmap getIcon() {
        return Icon;
    }
}
