package com.kaching123.tcr.function;

import android.content.Context;
import android.database.Cursor;

import com.getbase.android.db.provider.ProviderAction;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashSet;

/**
 * Created by vkompaniets on 4/23/2015.
 */
public class NextProductCodeQuery {

    public static int MIN_CODE_LENGTH = 5;
    public static int MAX_CODE_LENGTH = 5;

    public static String getCode(Context context) {
        return chooseCode(getStoredCodes(context));
    }

    public static String[] getCodes(Context context, int count) {
        ArrayList<String> codes = getStoredCodesAsList(context);
        String[] reqCodes = new String[count];
        for (int i = 0; i < count; i++) {
            String s = chooseCode(codes);
            reqCodes[i] = s;
            codes.add(s);
            Collections.sort(codes, comparator);
        }
        return reqCodes;
    }

    private static Comparator<String> comparator = new Comparator<String>() {
        @Override
        public int compare(String lhs, String rhs) {
            int lhsInt = Integer.valueOf(lhs);
            int rhsInt = Integer.valueOf(rhs);
            if (lhsInt > rhsInt) {
                return 1;
            } else if (rhsInt > lhsInt) {
                return -1;
            }
            return 0;
        }
    };

    private static String chooseCode(Collection<String> codes) {
        Iterator<String> iterator = codes.iterator();
        int maxVal = (int) Math.pow(10, MIN_CODE_LENGTH);
        for (int i = 0; i < maxVal; i++) {
            String ii = format(i);
            if (iterator.hasNext()) {
                if (!ii.equals(iterator.next())) {
                    return ii;
                }
            } else {
                return ii;
            }

        }
        return null;
    }

    private static LinkedHashSet<String> getStoredCodes(Context context) {
        Cursor c = ProviderAction.query(ShopProvider.contentUri(ShopStore.ProductCodeView.URI_CONTENT))
                .perform(context);
        LinkedHashSet<String> codes = new LinkedHashSet<String>(c.getCount());
        codes.add("00000");
        while (c.moveToNext()) {
            codes.add(c.getString(0));
        }
        c.close();
        return codes;
    }

    private static ArrayList<String> getStoredCodesAsList(Context context) {
        ArrayList<String> codes = new ArrayList<String>(getStoredCodes(context));
        return codes;
    }


    public static String format(int n) {
        return String.format("%0" + MIN_CODE_LENGTH + "d", n);
    }
}
