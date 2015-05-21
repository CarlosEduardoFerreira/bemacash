package com.kaching123.pos.printer;

import com.kaching123.pos.ActionWithAnswer;
import com.kaching123.pos.data.PrinterStatusEx;
import com.kaching123.pos.data.PrinterStatusEx.ErrorStatusInfo;
import com.kaching123.pos.data.PrinterStatusEx.OfflineStatusInfo;
import com.kaching123.pos.data.PrinterStatusEx.PrinterHeadInfo;
import com.kaching123.pos.data.PrinterStatusEx.PrinterStatusInfo;

/**
 * [Function] Printer extended status enquiry.
 * [Format] ASCII GS F8h 1
 * Hexadecimal 1D F8 31
 * Decimal 29 248 49
 * [Description] Issuing this command makes the printer to return five status bytes.
 * <p/>
 * Created by gdubina on 16/12/13.
 */
public class GetMP200PrinterStatusExAction extends ActionWithAnswer<PrinterStatusEx> {
    private static int ANSWER_LEN = 5;
    private static byte[] COMMAND = new byte[]{DLE, EOT, N1};

    private final int drawerClosedValue;

    public GetMP200PrinterStatusExAction(int drawerClosedValue) {
        super(ANSWER_LEN);
        this.drawerClosedValue = drawerClosedValue;
    }

    @Override
    public PrinterStatusEx parseAnswer(byte[] bytes) {
        return new PrinterStatusEx(
                new PrinterStatusInfo(bytes[0], true)
        );
    }

    @Override
    protected byte[] getCommand() {
        return COMMAND;
    }
}
