package net.softm.data.kata.common;

import android.view.accessibility.AccessibilityNodeInfo;

import java.util.List;

public abstract class RunnableArg implements Runnable {

//    Object[] m_args;

    public RunnableArg() {
    }

//    public void setArgs(Object... args) {
//        m_args = args;
//    }

//    public int getArgCount() {
//        return m_args == null ? 0 : m_args.length;
//    }

//    public Object[] getArgs() {
//        return m_args;
//    }



    CharSequence packageName;
    List<CharSequence> text;
    CharSequence contentDescription;
    AccessibilityNodeInfo source;
    int eventType;

    public CharSequence getPackageName() {
        return packageName;
    }

    public void setPackageName(CharSequence packageName) {
        this.packageName = packageName;
    }

    public List<CharSequence> getText() {
        return text;
    }

    public void setText(List<CharSequence> text) {
        this.text = text;
    }

    public CharSequence getContentDescription() {
        return contentDescription;
    }

    public void setContentDescription(CharSequence contentDescription) {
        this.contentDescription = contentDescription;
    }

    public AccessibilityNodeInfo getSource() {
        return source;
    }

    public void setSource(AccessibilityNodeInfo source) {
        this.source = source;
    }

    public int getEventType() {
        return eventType;
    }

    public void setEventType(int eventType) {
        this.eventType = eventType;
    }
}
