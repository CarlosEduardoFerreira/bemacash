package com.kaching123.tcr.model.payment.blackstone.prepaid;

import android.util.Base64;

import com.kaching123.tcr.fragment.UiHelper;

import java.math.BigDecimal;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * @author Ivan v. Rikhmayer
 *         This class is intended to
 */
public class SignatureFactory {

    protected final static String HMACSHA = "HmacSHA256";
    protected final static String UTF8 = "UTF-8";

    public static String getSignature(String MID, String TID, String Password, BigDecimal Amount, long OrderID ) throws Exception {
        String amountAsString = Amount == null || BigDecimal.ZERO.compareTo(Amount) >= 0 ? "0" : UiHelper.valueOf(Amount);
        return encode(Password, String.format("%s%s%s%s%d", MID, TID, Password, amountAsString.replace(".", ""), OrderID));
    }

    public static String encode(String key, String data) throws Exception {
        final Mac sha256_HMAC = Mac.getInstance(HMACSHA);
        sha256_HMAC.init(new SecretKeySpec(key.getBytes(UTF8), HMACSHA));
//        return Base64.encodeToString(sha256_HMAC.doFinal(data.getBytes(UTF8)), Base64.DEFAULT).replace("\n","");
        return Base64.encodeToString(sha256_HMAC.doFinal(data.getBytes(UTF8)), Base64.NO_WRAP);
    }
}


