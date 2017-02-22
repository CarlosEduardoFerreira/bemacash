package com.kaching123.tcr.commands.rest.sync;

import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by Rodrigo Busata on 9/8/2016.
 */
public class DownloadSyncFile {

    public String md5;
    public String path;

    public DownloadSyncFile(String md5, String path) {
        this.md5 = md5;
        this.path = path;
    }

    public static String calculateMD5(File file) {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            return null;
        }

        InputStream is;
        try {
            is = new FileInputStream(file);

        } catch (FileNotFoundException e) {
            Log.e("calculateMD5", "Exception while getting FileInputStream", e);
            return null;
        }

        byte[] buffer = new byte[8192];
        int read;
        try {
            while ((read = is.read(buffer)) > 0) {
                digest.update(buffer, 0, read);
            }
            byte[] md5sum = digest.digest();
            BigInteger bigInt = new BigInteger(1, md5sum);
            String output = bigInt.toString(16);

            // Fill to 32 chars
            output = String.format("%32s", output).replace(' ', '0');
            return output;

        } catch (IOException e) {
            throw new RuntimeException("Unable to process file for MD5", e);
        }
    }
}
