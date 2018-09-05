package com.android.content;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;

import android.util.Log;
import android.util.Xml;
import android.util.ArrayMap;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class SystemErrorParser {
    private static final String  TAG   = "SystemErrorParser";
    private static final boolean DEBUG = true;

    private static final int TARGET_TAG_DEPTH = 1;

    private static final String ERROR_FILE_NAME = "system_errors.xml";
    private static final String ERROR_FILE_DIR  = "data/bbkcore";

    private static final String XML_NS = null;
    private static final String TAG_SYSTEM_ERROR   = "SystemError";
    private static final String TAG_ITEM_ERROR     = "Error";
    private static final String TAG_LAUNGUAGE      = "Language";
    private static final String TAG_EFFICIENT_MODE = "EfficientCondition";
    private static final String TAG_LANGUAGE_VALUE = "Value";

    private SystemErrorInfo mSystemErrorInfo = null;

    public final SystemErrorInfo parseErrorFile () {
       return parseErrorFile(new File(ERROR_FILE_DIR + File.separator + ERROR_FILE_NAME));
    }

    public final SystemErrorInfo parseErrorFile (CharSequence path) {
        if (path != null)
            return parseErrorFile(new File(path.toString()));
        else
            return null;
    }

    private int ensureTagDepth (XmlPullParser parser) throws IOException, XmlPullParserException {
        while (parser.getDepth() > TARGET_TAG_DEPTH && parser.next() != XmlPullParser.END_DOCUMENT);
        return parser.getEventType();
    }

    public final SystemErrorInfo parseErrorFile (File file) {
        XmlPullParser pullParser = null;
        String nodeName = null, lastTagName = null;
        int depth = 0;
        int eventType = 0;

        if (!file.exists() || !file.isFile()) {
            Log.e(TAG, "File [" + file.getAbsolutePath() + "] does not exists");
            return null;
        }

        if (file.canWrite() || file.canExecute()) {
            Log.e(TAG, "request [" + file.getAbsolutePath() + "] read-only");
            return null;
        }

        try {
            pullParser = Xml.newPullParser();
            pullParser.setInput(new FileReader(file));

            while ((eventType = pullParser.getEventType()) != XmlPullParser.END_DOCUMENT) {
                lastTagName = pullParser.getName();

                switch (eventType) {
                    case XmlPullParser.START_DOCUMENT:
                        mSystemErrorInfo = onCreateSystemErrorInfo();
                        break;
                    case XmlPullParser.START_TAG:
                        nodeName = pullParser.getName();

                        if (DEBUG)
                            Log.d(TAG, "Parsing TAG : " + nodeName);

                        if (TAG_SYSTEM_ERROR.equals(nodeName))
                            parseSystemError(pullParser);
                        else if (TAG_EFFICIENT_MODE.equals(nodeName))
                            parseEfficientCondition(pullParser);
                        else if (TAG_ITEM_ERROR.equals(nodeName))
                            parseItemError(pullParser);
                        else if (TAG_LAUNGUAGE.equals(nodeName))
                            parseLanguage(pullParser);
                        else
                            parseOther(pullParser);

                        if (!TAG_SYSTEM_ERROR.equals(nodeName))
                            eventType = ensureTagDepth(pullParser);
                        break;
                    default:
                        Log.w(TAG, "invalidate XmlPullParser TAG");
                        break;
                }

                if (lastTagName == null || lastTagName.equals(pullParser.getName()))
                    pullParser.next();
            }
        }
        catch (IOException | XmlPullParserException e) {
            Log.e(TAG, "Xml parsing failed for SystemError TAG [" + nodeName + "]");
            //handlePaserException(e);
        }

        return mSystemErrorInfo;
    }

    protected SystemErrorInfo onCreateSystemErrorInfo () {
        return new SystemErrorInfo();
    }

    private SystemErrorInfo parseSystemError (XmlPullParser parser) throws IOException, XmlPullParserException {
        String versionCode = parser.getAttributeValue(XML_NS, "versionCode");

        if (versionCode != null && versionCode.matches("\\d+\\.\\d+\\.\\d+"))
            mSystemErrorInfo.setVersion(versionCode);

        mSystemErrorInfo.setPrompt(parser.getAttributeValue(XML_NS, "prompt"));
        return mSystemErrorInfo;
    }

    private void parseEfficientCondition (XmlPullParser parser) throws IOException, XmlPullParserException {
        SystemErrorInfo.EfficientInfo efficient = mSystemErrorInfo.mEfficientInfo;
        efficient.parseFromXml(parser);
    }

    protected SystemErrorInfo.ItemErrorInfo onCreateItemErrorInfo () {
        return mSystemErrorInfo.new ItemErrorInfo();
    }

    private void parseItemError (XmlPullParser parser) throws IOException, XmlPullParserException {
        SystemErrorInfo.ItemErrorInfo errorInfo = onCreateItemErrorInfo();

        errorInfo.setType(parser.getAttributeValue(XML_NS, "type"));
        errorInfo.setEnable(parser.getAttributeValue(XML_NS, "enable"));
        errorInfo.parseFromXml(parser);

        if (errorInfo.isValide())
            mSystemErrorInfo.addItemErrorInfo(errorInfo);
    }

    private void parseLanguage (XmlPullParser parser) throws IOException, XmlPullParserException {
        Map<String, String> language = new ArrayMap<String, String>();
        String local = null;
        boolean defLanguage = false;

        String def = parser.getAttributeValue(XML_NS, "default");
        if (def != null)
            defLanguage = def.equals("true");

        local = parser.getAttributeValue(XML_NS, "local");
        if (local == null) {
            Log.e(TAG, "parseLanguage can't find effictive local, skip");
            return;
        }

        int depth = parser.getDepth();
        while (parser.next() != XmlPullParser.END_DOCUMENT && parser.getDepth() >= depth) {
            String name = parser.getName();
            if (parser.getEventType() != XmlPullParser.START_TAG)
                continue;

            if (TAG_LANGUAGE_VALUE.equals(name)) {
                String label = parser.getAttributeValue(XML_NS, "name");

                parser.next();
                language.put(label, parser.getText()); 
            }
            else
                Log.w(TAG, "parseLanguage skip effictive tag : " + name);
        }

        mSystemErrorInfo.addLanguage(local, language, defLanguage);
    }

    protected void parseOther (XmlPullParser parser) throws IOException, XmlPullParserException {
        Log.w(TAG, "ParseOther Element [" + parser.getName() + "] In " + parser);
    }
}
