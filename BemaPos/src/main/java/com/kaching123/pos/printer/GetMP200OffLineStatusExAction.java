package com.kaching123.pos.printer;

import com.kaching123.pos.ActionWithAnswer;
import com.kaching123.pos.data.PrinterStatusEx;

/**
 * [Function] Printer extended status enquiry.
 * [Format] ASCII GS F8h 1
 * Hexadecimal 1D F8 31
 * Decimal 29 248 49
 * [Description] Issuing this command makes the printer to return five status bytes.
 * <p/>
 * Created by gdubina on 16/12/13.
 */
public class GetMP200OffLineStatusExAction extends ActionWithAnswer<PrinterStatusEx> {
    private static int ANSWER_LEN = 5;
    private static byte[] COMMAND = new byte[]{DLE, EOT, N2};

    private final int drawerClosedValue;
    private final boolean printerIsOffline;

    public GetMP200OffLineStatusExAction(int drawerClosedValue, boolean printerIsOffline) {
        super(ANSWER_LEN);
        this.drawerClosedValue = drawerClosedValue;
        this.printerIsOffline = printerIsOffline;
    }

    @Override
    public PrinterStatusEx parseAnswer(byte[] bytes) {
        return new PrinterStatusEx(
                new PrinterStatusEx.OfflineStatusInfo(bytes[0], drawerClosedValue, printerIsOffline)
        );
    }

    @Override
    protected byte[] getCommand() {
        return COMMAND;
    }
}
