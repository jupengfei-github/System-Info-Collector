package com.android.content;

import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Locale;

import java.io.IOException;

import android.util.ArrayMap;
import android.util.Log;
import android.os.SystemProperties;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class SystemErrorInfo {
    private static final String  TAG = SystemErrorInfo.class.getName();
    private static final boolean DEBUG   = true;

    private static final String DUMP_PREFIX = "  ";
    private static final String XML_NS = null;

    /* common ItemError information */
    private Version mVersion = new Version();
    private String  mPrompt  = null;

    /* effective matches */
    public EfficientInfo mEfficientInfo = new EfficientInfo();

    /* language */
    private Map<String, Map<String, String>> mLanguage = new ArrayMap<String, Map<String, String>>();
    private String mDefaultLanguage = null;

    /* error */
    private List<ItemErrorInfo> mItemErrorInfo = new ArrayList<ItemErrorInfo>();
    private Map<String, ItemErrorInfo> mPackageItemErrorInfo = new ArrayMap<String, ItemErrorInfo>();

    void setVersion (String version) {
        if (version != null)
            mVersion.parseFromString(version);
        else
            Log.e(TAG, "setVersion ignore NullPointer, reset back version : " + mVersion);
    }

    void setPrompt (String prompt) {
        mPrompt = prompt;
    }

    void addItemErrorInfo (ItemErrorInfo info) {
        mItemErrorInfo.add(info);
        mPackageItemErrorInfo.put(info.name, info);
    }

    void addLanguage (String locale, Map<String, String> language, boolean def) {
        mLanguage.put(locale, language);

        if (mDefaultLanguage == null || def)
            mDefaultLanguage = locale;
    }

    public ResolveItemInfo resolvePackageErrorInfo (CharSequence packageName) {
        ItemErrorInfo   error = mPackageItemErrorInfo.get(packageName);
        ResolveItemInfo info  = new ResolveItemInfo();

        if (error == null)
            return null;

        info.name = error.name;
        info.version = error.version.toString();

        if (error.newVersion != null)
            info.newVersion = error.newVersion.toString();

        Locale curLocale = Locale.getDefault();
        String locale = curLocale.getLanguage() + "-" + curLocale.getCountry();

        if (mLanguage.get(locale) == null)
            locale = mDefaultLanguage;

        Map<String,String> language = mLanguage.get(locale);
        if (error.prompt != null)
            info.promptMsg = language.get(error.prompt);
        else if (mPrompt != null)
            info.promptMsg = language.get(mPrompt); 
        return info;
    }

    public boolean match () {
        return mEfficientInfo.match();
    }

    @Override
    public String toString () {
        StringBuilder builder = new StringBuilder();
        final String newline = "\n";

        builder.append("SystemErrorInfo : Version=" + mVersion + "  mPrompt=" + mPrompt + newline);
        builder.append(mEfficientInfo);

        builder.append("ItemErrorInfo : " + newline);
        for (ItemErrorInfo info : mItemErrorInfo)
            builder.append(DUMP_PREFIX + info + newline);

        builder.append("Launguage Default Locale : " + mDefaultLanguage + newline);
        for (String locale : mLanguage.keySet()) {
            builder.append("Launguage [" + locale + "]" + newline);

            Map<String,String> lg = mLanguage.get(locale);
            for (String id : lg.keySet())
                builder.append(DUMP_PREFIX + id + " : " + lg.get(id) + newline);
        }

        return builder.toString();
    }

    private class Version {
        int major;
        int mirror;
        int modify;

        void parseFromString (String version) {
            String[] versions = version.split("\\.");

            major  = Integer.parseInt(versions.length > 0? versions[0] : "0");
            mirror = Integer.parseInt(versions.length > 1? versions[1] : "0");
            modify = Integer.parseInt(versions.length > 2? versions[2] : "0");
        }

        @Override
        public String toString () {
            return major + "." + mirror + "." + modify;
        }

        int compare (Version version) {
            /* major */
            if (major > version.major) 
                return  1;
            else if (major < version.major)
                return -1;
            
            /* mirror */
            if (mirror > version.mirror)
                return  1;
            else if (mirror < version.mirror)
                return -1;

            /* modify */
            if (modify > version.modify)
                return  1;
            else if (modify < version.modify)
                return -1;
            else
                return 0;
        }
    }

    class ItemErrorInfo { 
        private static final String TAG_PACKAGE = "Package";
        private static final String TAG_PROMPT  = "Prompt";

        private static final int TYPE_PACKAGE = 0x01;

        String  name;
        Version version;
        String  prompt;
        Version newVersion;

        int type;
        boolean enable;

        void parseFromXml (XmlPullParser parser) throws XmlPullParserException, IOException  {
            int depth = parser.getDepth();

            while (parser.next() != XmlPullParser.END_DOCUMENT && parser.getDepth() >= depth) {
                String nodeName = parser.getName();
                if (parser.getEventType() != XmlPullParser.START_TAG)
                    continue;

                if (TAG_PACKAGE.equals(nodeName)) {
                    name = parser.getAttributeValue(XML_NS, "name");
                    
                    String versionStr = parser.getAttributeValue(XML_NS, "version");
                    if (versionStr != null) {
                        version = new Version();
                        version.parseFromString(versionStr);
                    }

                    String newVersionStr = parser.getAttributeValue(XML_NS, "newVersion");
                    if (newVersionStr != null) {
                        newVersion = new Version();
                        newVersion.parseFromString(newVersionStr);
                    }
                }
                else if (TAG_PROMPT.equals(nodeName)) {
                    prompt = parser.getAttributeValue(XML_NS, "name");
                }
                else {
                    Log.w(TAG, "Ignore parse invalidate TAG : " + nodeName);
                    break;
                }
            }
        }

        void setEnable (String en) {
            enable = (en == null || en.equals("true"))? true : false;
        }

        void setType (String t) {
            type = TYPE_PACKAGE;
            if (t == null || t.equals("package"))
                return;
        }

        boolean isValide () {
            return name != null;
        }

        @Override
        public String toString () {
            StringBuilder  builder = new StringBuilder();

            builder.append("Name=" + name + " Version=" + version + " Prompt=" + prompt 
                + " Type=" + type + " enable=" + enable
                + " newVersion=" + newVersion);

            return builder.toString();
        }
    }

    private enum Condition{
        LT, LE, GT, GE, EQ
    }

    abstract class AbsEfficientInfo {
        private Condition condition;
        private boolean   must;
        protected Version version;

        boolean must () {
            return must;
        }

        private Condition translateCondition (String condition) {
            Condition ret = Condition.EQ;
            if (condition == null)
                return ret;

            if (condition.equals("lt"))
                ret = Condition.LT;
            else if (condition.equals("le"))
                ret = Condition.LE;
            else if (condition.equals("gt"))
                ret = Condition.GT;
            else if (condition.equals("ge"))
                ret = Condition.GE;
            /*else
                default Condtion.EQ */
            
            return ret;
        }
 
        void parseFromXml (XmlPullParser parser) throws IOException, XmlPullParserException {
            /* parse version [xx.xx.xx] */
            String vs = parser.getAttributeValue(XML_NS, "version");
            if (vs != null) {
                version = new Version();
                version.parseFromString(vs);
            }

            /* parse condition [ >, >=, <, <=, = ] */
            condition = translateCondition(parser.getAttributeValue(XML_NS, "condition"));
            
            /* necessary */
            String isMust = parser.getAttributeValue(XML_NS, "must");
            if (isMust == null || isMust.equals("true"))
                must = true;
            else
                must = false;
        }

        boolean match (Version tmpVer) {
            switch (condition) {
                case LT:
                    return version.compare(tmpVer) < 0;
                case LE:
                    return version.compare(tmpVer) <= 0;
                case GT:
                    return version.compare(tmpVer) > 0;
                case GE:
                    return version.compare(tmpVer) >= 0;
                case EQ:
                    return version.compare(tmpVer) == 0;
                default:
                    return false;
            }
        }

        @Override
        public String toString () {
            StringBuilder builder = new StringBuilder();

            builder.append("EfficientInfo : Version=" + version + "  Condition=" + condition + "  Must=" + must);
            return builder.toString();
        }

        abstract boolean match ();
    }

    public class EfficientInfo {
        private static final String TAG_PROJECT = "Project";
        private static final String TAG_ROM     = "Rom";
        private static final String TAG_OS      = "Os";

        private List<AbsEfficientInfo> mListItems = new ArrayList<AbsEfficientInfo>();        

        public void parseFromXml (XmlPullParser parser) throws IOException, XmlPullParserException {
            AbsEfficientInfo info = null;
            int depth = parser.getDepth();

            while (parser.next() != XmlPullParser.END_DOCUMENT && parser.getDepth() >= depth) {
                String name = parser.getName();

                if (parser.getEventType() != XmlPullParser.START_TAG)
                    continue;

                info = null;
                if (TAG_PROJECT.equals(name))
                    info = new EfficientProjectInfo();
                else if (TAG_ROM.equals(name))
                    info = new EfficientRomInfo();
                else if (TAG_OS.equals(name))
                    info = new EfficientOsInfo();

                if (info != null) {
                    info.parseFromXml(parser);
                    mListItems.add(info);
                }
                else {
                    Log.e(TAG, "Efficient Parse Invalidate TAG : " + name);
                    break;
                }
            }
        }

        public boolean match () {
            boolean lastMatch = false;

            for (AbsEfficientInfo info : mListItems) {
                lastMatch = info.match();

                if (!lastMatch && info.must())
                    return false;
                else if(lastMatch && !info.must())
                    return true;
            }

            return lastMatch;
        }

        @Override
        public String toString () {
            final String newline = "\n";
            StringBuilder builder = new StringBuilder();

            for (AbsEfficientInfo info : mListItems)
                builder.append(DUMP_PREFIX + info + newline);

            return builder.toString();
        }
    }

    private class EfficientProjectInfo extends AbsEfficientInfo {
        private static final String PROP_PRODUCT_VERSION = "ro.vivo.product.version";
        private static final String PROP_PRODUCT_NAME    = "ro.product.name";

        private String  name;

        @Override
        public void parseFromXml (XmlPullParser parser) throws IOException, XmlPullParserException {
            super.parseFromXml(parser);
            name = parser.getAttributeValue(XML_NS, "name");
        }

        @Override
        public boolean match () {
            String project = SystemProperties.get(PROP_PRODUCT_NAME);
            if (project == null || name == null || project.equals(name))
                return true;

            String vs = SystemProperties.get(PROP_PRODUCT_VERSION);
            if (vs == null)
                return true;

            vs = vs.replaceAll(project, "");
            vs = vs.replaceAll("^[0-9.]", "");

            Version tmpVer = new Version();
            tmpVer.parseFromString(vs);
            return match(tmpVer); 
        }

        @Override
        public String toString () {
            return super.toString() + " Name=" + name;
        }
    }

    private class EfficientRomInfo extends AbsEfficientInfo {
        private static final String PROP_ROM_VERSION = "ro.vivo.os.version";

        @Override
        public boolean match () {
            String  tmpVerStr = SystemProperties.get(PROP_ROM_VERSION);    
            Version tmpVer = new Version();

            if (tmpVerStr != null)
                tmpVer.parseFromString(tmpVerStr);    

            return match(tmpVer);
        }
    }

    private class EfficientOsInfo extends AbsEfficientInfo {
        private static final String PROP_OS_VERSION = "ro.build.version.release";

        @Override
        public boolean match () {
            String tmpVerStr = SystemProperties.get(PROP_OS_VERSION);
            Version tmpVer = new Version();

            if (tmpVerStr != null)
                tmpVer.parseFromString(tmpVerStr);

            return match(tmpVer);
        }
    }
}
