package com.android.filter;

public class FileErrorFilter implements SystemErrorFilter {
    private static final String  TAG   = "FileErrorFilter";
    private static final boolean DEBUG = true;

    private Context mContext = null;
    private SystemErrorGlobal gGlobal   = null;
    private ResolveItemInfo   mItemInfo = null;
    private PackageManager    mPm = null;

    public FileErrorFilter (Context context) {
        mContext = context;
    }
    
    @Override
    public boolean filterError (Application appInfo) {
        if (gGlobal == null)
            gGlobal = SystemErrorGlobal.getInstance();

        String pkgName = appInfo.packageName;
        mItemInfo = gGlobal.resolvePackageErrorInfo(pkgName);
        if (mItemInfo == null) {
            Log.d(TAG, "Can't find ErrorItem for App : " + pkgName);
            return false;
        }

        if (filterErrorByVersion(appInfo)) {
            Log.d(TAG, "Filter Version for App : " + pkgName);
            return true;
        }

        return false;
    }

    private boolean filterErrorByVersion (ApplicationInfo appInfo) {
        if (mPm == null)
            mPm = mContxt.getPackageManager();

        PackageInfo info = mPm.getPackageInfo(appInfo.packageName, 0);

        String versionName = info.versionName;
        if (versionName == null)
            return false;

        versionName = versionName.replaceAll("^[0-9.]", "");

        Version version = new Version();
        version.parseFromString(versionName);

        if (mItemInfo.matchVersion(version) == 0 && mItemInfo.newVersion != null)
            return true;
        else
            return false;
    }

    @Override
    public String getErrorTip () {
        if (mItemInfo != null)
            return mItemInfo.getPromptMessage();
        else
            return null;
    }
}
