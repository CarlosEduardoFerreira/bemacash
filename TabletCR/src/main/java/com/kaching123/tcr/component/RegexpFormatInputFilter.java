package com.kaching123.tcr.component;

import android.text.InputFilter;
import android.text.Spanned;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by gdubina on 11.12.13.
 */
public class RegexpFormatInputFilter implements InputFilter {

    private Pattern pattern;

    protected RegexpFormatInputFilter(String regexp){
        pattern = Pattern.compile(regexp);
    }

    protected RegexpFormatInputFilter(Pattern regexp){
        pattern = regexp;
    }

    @Override
    public CharSequence filter(
            CharSequence source,
            int start,
            int end,
            Spanned dest,
            int dstart,
            int dend) {

        source = source.subSequence(start, end);
        String result = dest.subSequence(0, dstart) + source.toString() + dest.subSequence(dend, dest.length());
        Matcher matcher = pattern.matcher(result);
        if (!matcher.matches()){
            CharSequence ret = dest.subSequence(dstart, dend);
            return ret;
        }
        return null;
    }
}