package com.kaching123.tcr.util;

import android.database.Cursor;

import com.google.common.base.Function;

/**
 * Created by gdubina on 06/03/14.
 */
public final class CursorUtil {

    private CursorUtil() {
    }

    public static <T> T _wrap(Cursor c, Function<Cursor, T> function) {
        try {
            return function.apply(c);
        } finally {
            c.close();
        }
    }

    public static <T> T _wrapOrNull(Cursor c, Function<Cursor, T> function) {
        if(!c.moveToFirst()){
            c.close();
            return null;
        }
        try {
            return function.apply(c);
        } finally {
            c.close();
        }
    }

    public static String[] _selectionArgs(Object... args){
        String[] selectionArgs = new String[args.length];
        for (int i = 0; i < args.length; i++) {
            selectionArgs[i] = args[i].toString();
        }

        return selectionArgs;
    }
}
