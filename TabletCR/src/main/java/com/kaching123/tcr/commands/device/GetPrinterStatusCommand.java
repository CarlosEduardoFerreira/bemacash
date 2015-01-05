package com.kaching123.tcr.commands.device;

import android.content.Context;

import com.kaching123.pos.PosPrinter;
import com.kaching123.pos.data.PrinterStatusEx;
import com.kaching123.pos.data.PrinterStatusEx.ErrorStatusInfo;
import com.kaching123.pos.data.PrinterStatusEx.OfflineStatusInfo;
import com.kaching123.pos.data.PrinterStatusEx.PrinterHeadInfo;
import com.kaching123.pos.data.PrinterStatusEx.PrinterStatusInfo;
import com.kaching123.pos.printer.GetPrinterStatusExAction;
import com.kaching123.tcr.Logger;
import com.telly.groundy.TaskResult;
import com.telly.groundy.annotations.OnFailure;
import com.telly.groundy.annotations.OnSuccess;
import com.telly.groundy.annotations.Param;

import java.io.IOException;

/**
 * Created by gdubina on 16.12.13.
 */
public class GetPrinterStatusCommand extends PrinterCommand {

    public static final String EXTRA_PRINTER_STATUS = "EXTRA_PRINTER_STATUS";

    public static final String ARG_PRINTER_IP = "ARG_PRINTER_IP";
    public static final String ARG_PRINTER_PORT = "ARG_PRINTER_PORT";
    public static final String ARG_PRINTER_MAC = "ARG_PRINTER_MAC";

    private PrinterStatusEx status;

    @Override
    protected PrinterInfo getPrinter() {
        String printerIp = getStringArg(ARG_PRINTER_IP);
        int printerPort = getIntArg(ARG_PRINTER_PORT);
        String printerMac = getStringArg(ARG_PRINTER_MAC);

        if (printerIp == null)
            return super.getPrinter();

        return new PrinterInfo(
                printerIp,
                printerPort,
                printerMac,
                null,
                null,
                false);
    }

    @Override
    protected TaskResult execute(PosPrinter printer) throws IOException {
        Logger.d("PrinterCommand: try to get status");
        if (isEmulate()) {
            Logger.d("PrinterCommand: try to get status emulate mode!!");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignore) {
            }
            return succeeded().add(EXTRA_PRINTER_STATUS, new PrinterStatusEx(
                    PrinterStatusInfo.emulate(),
                    OfflineStatusInfo.emulate(),
                    ErrorStatusInfo.emulate(),
                    PrinterHeadInfo.emulate()));
        }
        Logger.d("PrinterCommand: info returned");
        return status == null ? failed() : succeeded().add(EXTRA_PRINTER_STATUS, status);
    }

    @Override
    protected TaskResult validatePrinterState(PosPrinter printer){
        if (isEmulate()) {
            return null;
        }

        try {
            status = new GetPrinterStatusExAction(getApp().getDrawerClosedValue()).execute(printer);
        } catch (IOException e) {
            Logger.e("PrinterCommand validate statues execute: ", e);
            return failed().add(EXTRA_ERROR_PRINTER, PrinterError.DISCONNECTED);
        }

        if (status == null) {
            Logger.e("GetPrinterStatusCommand validate statues execute: status is NULL!");
            return failed().add(EXTRA_ERROR_PRINTER, PrinterError.BUSY);
        }

        return validatePrinterStateExt(status);
    }

    public static void start(Context context, String ip, int port, String mac, boolean searchByMac, BasePrinterStatusCallback callback){
        create(GetPrinterStatusCommand.class)
                .arg(ARG_PRINTER_IP, ip)
                .arg(ARG_PRINTER_PORT, port)
                .arg(ARG_PRINTER_MAC, mac)
                .arg(ARG_SEARCH_BY_MAC, searchByMac)
                .callback(callback).queueUsing(context);
    }

    public static void start(Context context, boolean searchByMac, BasePrinterStatusCallback callback){
        create(GetPrinterStatusCommand.class).callback(callback).arg(ARG_SEARCH_BY_MAC, searchByMac).queueUsing(context);
    }

    public static abstract class BasePrinterStatusCallback {

        @OnSuccess(GetPrinterStatusCommand.class)
        public void onSuccess(@Param(GetPrinterStatusCommand.EXTRA_PRINTER_STATUS) PrinterStatusEx statusInfo) {
            onPrinterStatusSuccess(statusInfo);
        }

        @OnFailure(GetPrinterStatusCommand.class)
        public void onFailure(@Param(PrinterCommand.EXTRA_ERROR_PRINTER) PrinterError error) {
            if (error != null && error == PrinterError.IP_NOT_FOUND){
                onPrinterIpNotFound();
                return;
            }
            onPrinterStatusError(error);
        }

        protected abstract void onPrinterStatusSuccess(PrinterStatusEx statusInfo);
        protected abstract void onPrinterStatusError(PrinterError error);
        protected abstract void onPrinterIpNotFound();
    }
}
