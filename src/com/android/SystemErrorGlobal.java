package com.android;

import java.io.File;
import android.util.Log;

public class SystemErrorGlobal {
    private static final String  TAG   = SystemErrorGlobal.class.getName();
    private static final boolean DEBUG = true;

    private static SystemErrorGlobal gGlobal = null;

    private SystemErrorInfo   mSystemErrorInfo   = null;
    private SystemErrorParser mSystemErrorParser = null;

    static  {
        gGlobal = new SystemErrorGlobal();
    }
    
    private SystemErrorGlobal () {
        /* do nothing */
    }

    public static final SystemErrorGlobal getInstance () {
        return gGlobal;
    }

    public void loadData (CharSequence path) {
        if (path != null)
            loadData(new File(path.toString()));
        else
            Log.e(TAG, "loadData NullPointer");
    }

    SystemErrorParser obtainErrorParser () {
        return new SystemErrorParser();
    }

    public void loadData (File file) {
        if (mSystemErrorParser == null)
            mSystemErrorParser = obtainErrorParser();

        if (file != null)
            mSystemErrorInfo = mSystemErrorParser.parseErrorFile(file);
    }

    public SystemErrorInfo getSystemErrorInfo () {
        return mSystemErrorInfo;
    }

    public void clear () {
        mSystemErrorParser = null;
        mSystemErrorInfo = null;
    }
}
