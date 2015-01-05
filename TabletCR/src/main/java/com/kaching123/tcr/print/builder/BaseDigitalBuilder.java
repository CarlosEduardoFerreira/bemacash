package com.kaching123.tcr.print.builder;

import android.text.Html;

import com.kaching123.pos.PosPrinter;
import com.kaching123.pos.util.IPrinter;
import com.kaching123.tcr.Logger;

import java.io.IOException;

/**
 * Created by vkompaniets on 23.01.14.
 */
public class BaseDigitalBuilder implements IPrinter {

    protected static final String BODY_STYLE = "font-size:0.9em;width:%dpx;padding:10px;";//font-family:'Comic Sans MS', cursive, sans-serif;
    protected static final String HR_STYLE = "height: 1px;border: 0;background-color:#C0C0C0;";
    protected static final String TABLE_STYLE = "width:100%;font-size:0.9em;";
    protected static final String TABLE_FULL_STYLE = "width:100%;";
    protected static final String RIGHT_STYLE = "text-align:right;";
    protected static final String CENTER_STYLE = "text-align:center;";
    protected static final String BOLD_STYLE = "font-weight:bold;";
    protected static final String CROP_STYLE = "overflow:hidden;white-space:nowrap;";


    private static final int BODY_WIDTH = 350;

    protected static String _styled(String tag, String...styles){
        StringBuilder builder = new StringBuilder(128);
        builder.append('<');
        builder.append(tag);
        builder.append(" style=\"");
        for(String s : styles){
            builder.append(s);
        }
        builder.append("\"");
        builder.append(">");
        return builder.toString();
    }

    protected static final String HEADER =
            "<!DOCTYPE html>\n" +
                    "<html>\n" +
                    "<head>\n" +
                    "</head>\n";

    protected static final String FOOTER =
            "</body>\n" +
                    "</html>";

    protected StringBuilder stringBuilder = createBuilder();

    protected StringBuilder createBuilder(){
        return new StringBuilder(getDocumentHeader());
    }

    private String getDocumentHeader() {
        return HEADER + _styled("body", getBodyStyle());
    }

    private String getBodyStyle() {
        return String.format(BODY_STYLE, getBodyWidth());
    }

    protected int getBodyWidth() {
        return BODY_WIDTH;
    }

    public String build() {
        stringBuilder.append(FOOTER);
        Logger.d("\n" + Html.fromHtml(stringBuilder.toString()).toString());
        return stringBuilder.toString();
    }

    @Override
    public String toString() {
        return stringBuilder.toString();
    }

    @Override
    public void emptyLine() {
        emptyLine(1);
    }

    @Override
    public void emptyLine(int c) {
        for (int i = 0; i < c; i++)
            stringBuilder.append("<br>");
    }

    public void drawLine() {
        stringBuilder.append(_styled("hr", HR_STYLE));
    }

    @Override
    public void drawDoubleLine() {

    }

    @Override
    public void barcode(String text) {

    }

    @Override
    public void logo() {

    }

    @Override
    public void print(PosPrinter posPrinter) throws IOException {

    }
}
