package com.android.content;

import android.util.Log;

public class ResolveItemInfo {
    private static final String  TAG   = ResolveItemInfo.class.getName();
    private static final boolean DEBUG = true;

    private static final String DUMP_PREFIX = "  ";

    public String name = null;
    public String version    = null;
    public String newVersion = null;

    String promptMsg  = null;

    public String getPromptMessage () {
        return promptMsg;
    }

    @Override
    public String toString () {
        StringBuilder builder = new StringBuilder();

        builder.append("ResolveItemInfo : ");
        builder.append(DUMP_PREFIX + "Name=" + name + "  Version=" + version + "  newVersion=" + newVersion);
        builder.append(DUMP_PREFIX + "PromptMessage=" + promptMsg);

        return builder.toString();
    }
}
