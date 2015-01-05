package com.mayer.framework.web.utils;

import com.mayer.framework.web.Logger.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PushbackInputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Arrays;

/**
 * @author Ivan v. Rikhmayer
 *         This class is intended to help with strings..
 */
public class StringUtils {

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
                Logger.e("Failed to unread pushback stream", e);
            }

        }
        return result;
    }

    public static String getString(PushbackInputStream is, int buffSize) {
        byte[] buff = null;
        try {
            buff = new byte[buffSize];
            int i = 0;
            byte b;
            while (i < buffSize && (b = (byte) is.read()) != -1) {
                buff[i++] = b;
            }
            if (i == 0) {
                buff = null;
                return null;
            }
            if (i < buffSize) {
                buff = Arrays.copyOf(buff, i);
            }
        } catch (IOException e) {
            Logger.e("Failed to read string", e);
            buff = null;
        } finally {
            try {
                if (buff != null)
                    is.unread(buff);
            } catch (IOException e) {
                Logger.e("Failed to unread pushback stream", e);
            }
        }
        if (buff == null)
            return null;

        String result = null;
        try {
            result = new String(buff, "UTF-8");
        } catch (UnsupportedEncodingException ignore) {}
        return result;
    }
}
