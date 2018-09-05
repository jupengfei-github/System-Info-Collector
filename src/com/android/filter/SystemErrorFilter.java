package com.android.filter;

public interface SystemErrorFilter {
    /**
     * filter Error/Exception 
     * @param  appInfo Error Application Information
     * @return 
         true  filter successful
         false filter failed 
    **/
    boolean filterError (ApplicationInfo appInfo);

    /* obtain Error/Exception Tip Message */
    String getErrorTip ();
}
