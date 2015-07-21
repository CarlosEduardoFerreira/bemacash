package com.bematechus.bemaLibrary;

import android.util.Log;

import com.bematechus.bemaUtils.CodePage437Converter;
import com.bematechus.bemaUtils.CodePage850Converter;
import com.bematechus.bemaUtils.CodePage860Converter;
import com.bematechus.bemaUtils.CodePage862Converter;
import com.bematechus.bemaUtils.CodePage863Converter;
import com.bematechus.bemaUtils.CodePage865Converter;
import com.bematechus.bemaUtils.CodePage866Converter;

import java.io.UnsupportedEncodingException;

/**
 * Created by b1107005 on 5/24/2015.
 */
class CodePageCommand extends PrinterCommand {

    private static final String DEFAULT_CHAR_SET = "";
    public CodePageCommand (byte code)
    {
        commandBuffer = new byte[] { 0x1b, 0x74, 0x00};

        commandBuffer[2] = code;
    }
    public byte[] convert (String unicode)
    {
        String charSet = DEFAULT_CHAR_SET;


        try {

            return  unicode.getBytes(charSet);


        }
        catch (UnsupportedEncodingException ex )
        {
            Log.d("CP", ex.getMessage());

        }

        return null;
    }
    public static byte[] convertFromUnicode (BemaPrinter.CodePage codePage, String text)
    {
        byte [] ret = null;

        switch ( codePage) {
            case CP850:
                ret = new CodePage850Converter().convert(text);
                break;
            case CP437:
                ret = new CodePage437Converter().convert(text);
                break;
            case CP860:
                ret = new CodePage860Converter().convert(text);
                break;
            case CP862:
                ret = new CodePage862Converter().convert(text);
                break;
            case CP863:
                ret = new CodePage863Converter().convert(text);
                break;
            case CP865:
                ret = new CodePage865Converter().convert(text);
                break;
            case CP866:
                ret = new CodePage866Converter().convert(text);
                break;

        }


        return ret;
    }
}
