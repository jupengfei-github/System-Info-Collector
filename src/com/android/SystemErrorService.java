package com.android;

import java.io.File;

import android.util.Log;
import android.app.Service;
import android.content.Intent;
import android.os.FileObserver;
import android.os.IBinder;

import com.android.content.SystemErrorGlobal;

public class SystemErrorService extends Service {
    private static final String  TAG   = SystemErrorService.class.getName();
    private static final boolean DEBUG = true;

    private static final String ERROR_FILE_NAME = "system_errors.xml";
    private static final String ERROR_FILE_DIR  = "data/bbkcore";

    private SystemErrorGlobal gSystemError  = null;
    private FileObserver      mFileObserver = null;

    @Override
    public IBinder onBind (Intent intent) {
        return null;
    }

    @Override
    public void onCreate () {
        super.onCreate();

        gSystemError = SystemErrorGlobal.getInstance();

        File file = new File(ERROR_FILE_DIR + File.separator + ERROR_FILE_NAME);
        gSystemError.loadData(file);

        mFileObserver = new ErrorFileObserver(file.getAbsolutePath(), FileObserver.MODIFY);
        mFileObserver.startWatching();
    }

    @Override
    public void onDestroy () {
        super.onDestroy();

        mFileObserver.stopWatching();
        gSystemError.clear();
    }

    private class ErrorFileObserver extends FileObserver {
        private String mFilePath = null;

        ErrorFileObserver (String path, int mask) {
            super(path, mask);
            mFilePath = path;
        }

        @Override
        public void onEvent (int event, String path) {
            if (event == FileObserver.MODIFY && mFilePath.equals(path)) {
                Log.d(TAG, "reLoad data from file : " + mFilePath);
                gSystemError.loadData(new File(path));
            }
        }
    }
}
