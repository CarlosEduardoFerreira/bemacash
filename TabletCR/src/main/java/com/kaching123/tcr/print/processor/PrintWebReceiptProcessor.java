package com.kaching123.tcr.print.processor;

import android.content.Context;

import com.kaching123.pos.util.ITextPrinter;
import com.kaching123.tcr.R;
import com.kaching123.tcr.TcrApplication;
import com.kaching123.tcr.commands.print.ParseHtmlCommand;
import com.kaching123.tcr.jdbc.converters.ShopInfoViewJdbcConverter;

/**
 * Created by b1107005 on 12/7/2014.
 */
public class PrintWebReceiptProcessor  {


    public PrintWebReceiptProcessor() {

    }

    public void print(Context context, TcrApplication app, ITextPrinter printer, String[] printBuffer){
        if (context == null || printer == null || printBuffer == null) {
            return;
        }
        if (app.isTrainingMode()) {
            printer.subTitle(context.getString(R.string.training_mode_receipt_title));
            printer.emptyLine();
        }

        if (app.printLogo()) {
            printer.logo();
            printer.emptyLine();
        }
        ShopInfoViewJdbcConverter.ShopInfo shopInfo = app.getShopInfo();
        printer.header(shopInfo.name);
        for (String line: printBuffer) {
            if ( line.indexOf( ParseHtmlCommand.BOLD_PATTERN) >=0 ) {
                line = line.replaceAll(ParseHtmlCommand.BOLD_PATTERN,"");
                printer.add(line,true,false);
            }
            else {
                printer.add(line, false, false);
            }
        }
    }

}
