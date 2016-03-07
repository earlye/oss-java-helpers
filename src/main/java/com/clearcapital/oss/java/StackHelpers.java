package com.clearcapital.oss.java;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StackHelpers {

    private static Logger log = LoggerFactory.getLogger(StackHelpers.class);

    public final static String UNKNOWN_LOCATION = "Unknown Location";

    /**
     * Determines the name of the specified position in the call stack.
     * 
     * @param position
     * @return Depends on position and the depth of the stack. If position > stack's depth, returns UNKNOWN_LOCATION.
     *         Position 0 is always java.lang.Thread.getStackTrace(). Position 1 is always **this method**. Position 2
     *         is always **this method's caller**
     * 
     */
    public static String getStackLocation(int position) {
        StackTraceElement[] stack = null;
        try {
            stack = Thread.currentThread().getStackTrace();
        } catch (SecurityException se) {
            log.warn("Couldn't get location.", se);
            return UNKNOWN_LOCATION;
        }
        if (stack == null) {
            log.warn("Couldn't get location because stack is null.");
            return UNKNOWN_LOCATION;
        }
        if (stack.length >= position + 1) {
            return stack[position].toString();
        } else {
            log.warn("Couldn't get location because stack.length was insufficient.");
            return UNKNOWN_LOCATION;
        }
    }

    /**
     * Similar to getStackLocation, but offset so that position "0" is the caller's position.
     * 
     * @param position
     * @return
     */
    public static String getRelativeStackLocation(int position) {
        // 0 - j.l.T.getStackTrace()
        // 1 - thisClass.getStackLocation()
        // 2 - thisMethod
        // 3 - caller
        return getStackLocation(position + 3);
    }

    /**
     * Returns the stack location of the caller.
     * 
     * @param position
     * @return
     */
    public static String getRelativeStackLocation() {
        return getRelativeStackLocation(1);
    }

}
