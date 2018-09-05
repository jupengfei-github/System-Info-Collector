package com.android.filter;

public class HeaderErrorFilter implements SystemErrorFitler {
    private static final String  TAG   = "HeaderErrorFilter";
    private static final boolean DEBUG = true;

    @Override
    public boolean filterError (ApplicationInfo appInfo) {
        if (appInfo.flags & ApplicationInfo.FLAG_SYSTEM != 0)
            return true;
        else
            return false;
    }

    @Override
    public String getErrorTip () {
        return null;
    }
}
