package com.kaching123.pos.printer;

import com.kaching123.pos.ActionWithAnswer;
import com.kaching123.pos.PosPrinter;
import com.kaching123.pos.data.PrinterStatusEx;
import com.kaching123.pos.data.PrinterStatusEx.ErrorStatusInfo;
import com.kaching123.pos.data.PrinterStatusEx.OfflineStatusInfo;
import com.kaching123.pos.data.PrinterStatusEx.PrinterHeadInfo;
import com.kaching123.pos.data.PrinterStatusEx.PrinterStatusInfo;
import static com.kaching123.pos.util.ByteUtil._bit;

import java.io.IOException;

/**
 *
 * [Function] Printer  status enquiry --> USB / Centronics emulations
 *
 * [Description] Issuing this command makes the printer to return five status bytes.
 *
 * Created by rottmann on 1/28/15.
 */
public class GetPrinterBasicStatusAction extends ActionWithAnswer<PrinterStatusEx> {

    public GetPrinterBasicStatusAction() {
        super(0);
    }

    @Override
    public PrinterStatusEx execute(PosPrinter printer) throws IOException {
        byte [] ret = new byte[1];
        ret[0] = printer.getBasicStatus();

        return parseAnswer(ret);

    }

    @Override
    protected byte[] getCommand() {
        return null;
    }

    @Override
    public PrinterStatusEx parseAnswer(byte[] bytes)
    {
        byte ret = bytes[0];
        PrinterStatusInfo info = new PrinterStatusInfo();

        info.isOverrunError = _bit(ret,3) ==0 ;
        info.printerIsOffline = _bit(ret,4) == 0;
        info.isBusy = info.printerIsOffline;

        OfflineStatusInfo offInfo = new OfflineStatusInfo();

        offInfo.coverIsClosed = true;
        offInfo.drawerIsClosed = true;
        offInfo.isError = info.isOverrunError;
        offInfo.paperIsNearEnd = false;
        offInfo.noPaper = _bit(ret,5) == 1;
        if ( offInfo.noPaper) {
            info.printerIsOffline = true;
        }


        ErrorStatusInfo errInfo = new ErrorStatusInfo();
        errInfo.cutterErrorIsDetected = false;
        errInfo.cutterIsAbsent = false;
        errInfo.isNRE = false;
        errInfo.isREConditionPresent = true;

        PrinterHeadInfo headInfo = new PrinterHeadInfo();

        headInfo.headIsOverhead = false;

        PrinterStatusEx status = new PrinterStatusEx(info,offInfo,errInfo,headInfo);
        return status;
    }

}
