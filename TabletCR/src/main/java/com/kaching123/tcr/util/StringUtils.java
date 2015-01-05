package com.kaching123.tcr.util;

import com.kaching123.tcr.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PushbackInputStream;

/**
 * @author Ivan v. Rikhmayer
 *         This class is intended to help with strings..
 */
public class StringUtils {

    public static String getNumericPostfix(int i) {
        switch (i % 10) {
            case 1 : return "st";
            case 2 : return "nd";
            case 3 : return "rd";
            default: return "th";
        }
    }


    /**
     * Read string from IS and pushes back
     */
    public static String getString(PushbackInputStream is) {

        BufferedReader br = null;
        StringBuilder sb = new StringBuilder();
        String result = null;

        String line;
        try {

            br = new BufferedReader(new InputStreamReader(is));
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            result = sb.toString();
        } catch (IOException e) {
            Logger.e("Failed to read string", e);
        } finally {
            try {
                is.unread(result.getBytes());
            } catch (IOException e) {
                Logger.e("Failed to unread pushback stream", e);;
            }

        }
        return result;
//        finally {
//            if (br != null) {
//                try {
//                    br.close();
//                } catch (IOException e) {
//                    Logger.e("Failed to read string", e);
//                }
//            }
//        }


    }
}
