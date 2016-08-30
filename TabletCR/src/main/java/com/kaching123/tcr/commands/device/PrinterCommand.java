package com.kaching123.tcr.commands.device;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.hardware.usb.UsbManager;
import android.net.Uri;

import com.getbase.android.db.provider.ProviderAction;
import com.kaching123.pos.PosPrinter;
import com.kaching123.pos.SocketPrinter;
import com.kaching123.pos.USBPrinter;
import com.kaching123.pos.data.PrinterStatusEx;
import com.kaching123.pos.drawer.ConfigurePaperSensorAction;
import com.kaching123.pos.printer.GetMP200ErrorStatusExAction;
import com.kaching123.pos.printer.GetMP200OffLineStatusExAction;
import com.kaching123.pos.printer.GetMP200PaperRollSensorStatusExAction;
import com.kaching123.pos.printer.GetMP200PrinterStatusExAction;
import com.kaching123.pos.printer.GetPrinterBasicStatusAction;
import com.kaching123.pos.printer.GetPrinterStatusExAction;
import com.kaching123.pos.printer.SelectPOSAction;
import com.kaching123.tcr.BuildConfig;
import com.kaching123.tcr.Logger;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore.PrinterTable;
import com.telly.groundy.PublicGroundyTask;
import com.telly.groundy.TaskResult;
import com.telly.groundy.annotations.OnFailure;
import com.telly.groundy.annotations.OnSuccess;
import com.telly.groundy.annotations.Param;

import java.io.IOException;

/**
 * Created by gdubina on 04.12.13.
 */
public abstract class PrinterCommand extends PublicGroundyTask {

    public static enum PrinterError {NOT_CONFIGURED, DISCONNECTED, BUSY, NO_PAPER, PAPER_IS_NEAR_END, HEAD_OVERHEATED, COVER_IS_OPENED, CUTTER_ERROR, OFFLINE, IP_NOT_FOUND}

    public static final String ARG_SEARCH_BY_MAC = "ARG_SEARCH_BY_MAC";

    public static final String ARG_PREPAID_RECEIPTS= "ARG_PREPAID_RECEIPTS";

    public static final String ARG_NEED_SYNC = "ARG_NEED_SYNC";

    public static final String EXTRA_ERROR_PRINTER = "EXTRA_ERROR_PRINTER";
    public static final String EXTRA_PRINTER_INFO = "EXTRA_PRINTER_INFO";
    public static final String ACTION_USB_PERMISSION = "com.test.action.USB_PERMISSION";

    public static final String EXTRA_NEED_SYNC = "EXTRA_NEED_SYNC";

    protected static final Uri URI_PRINTER = ShopProvider.getContentWithLimitUri(PrinterTable.URI_CONTENT, 1);

    protected PrinterInfo getPrinter() {
        Cursor c = ProviderAction.query(URI_PRINTER)
                .projection(
                        PrinterTable.IP,
                        PrinterTable.PORT,
                        PrinterTable.MAC,
                        PrinterTable.SUBNET,
                        PrinterTable.GATEWAY,
                        PrinterTable.DHCP,
                        PrinterTable.PRINTER_TYPE
                )
                .where(PrinterTable.ALIAS_GUID + " IS NULL")
                .perform(getContext());
        PrinterInfo printerInfo = null;
        if (c.moveToFirst()) {
            printerInfo = new PrinterInfo(
                    c.getString(0),
                    c.getInt(1),
                    c.getString(2),
                    c.getString(3),
                    c.getString(4),
                    c.getInt(5) == 1,
                    c.getString(6) == null ? "Thermal" : c.getString(6)
            );
        }
        c.close();
        return printerInfo;
    }

    private PosPrinter createPrinter( PrinterInfo info) throws IOException
    {

        PosPrinter printer = null;
        if ( info.ip.compareTo(USBPrinter.USB_DESC) == 0) {
            UsbManager manager = (UsbManager) getContext().getSystemService(Context.USB_SERVICE);
            PendingIntent mPermissionIntent;

            mPermissionIntent = PendingIntent.getBroadcast(getContext(), 0, new Intent(ACTION_USB_PERMISSION), 0);

            // IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
            printer = (PosPrinter) new USBPrinter(USBPrinter.LR2000_PID, USBPrinter.LR2000_VID, manager,mPermissionIntent);
        }
        else
            printer = (PosPrinter) new SocketPrinter(info.ip, info.port);

        return printer;
    }
    @Override
    protected TaskResult doInBackground() {
        if (isEmulate()) {
            Logger.d("PrinterCommand: !!!Build doesn't support printer!!! run emulate mode");
            try {
                return execute(null);
            } catch (IOException e) {
                Logger.e("PrinterCommand execute error: ", e);
                return failed();
            }
        }

        boolean searchByMac = getBooleanArg(ARG_SEARCH_BY_MAC, false);

        PrinterInfo info = getPrinter();
        if (info == null) {
            Logger.e("PrinterCommand: printer doesn't configured");
            return failed().add(EXTRA_ERROR_PRINTER, PrinterError.NOT_CONFIGURED);
        }

        PosPrinter printer = null;
        if ( info.ip.compareTo(USBPrinter.USB_DESC) != 0) {
            if (searchByMac){
                info = FindPrinterByMacCommand.find(getContext(), info.macAddress, getAppCommandContext());
                if (info == null)
                    return failed().add(EXTRA_ERROR_PRINTER, PrinterError.DISCONNECTED);

                updatePrinterInDB(info);
            }
        }

        try {
            Logger.d("PrinterCommand: before connect to printer");
            printer = createPrinter(info);
            Logger.d("PrinterCommand: connection is opened");

        } catch (IOException e) {
            Logger.e("PrinterCommand connection error: ", e);
            return failed().add(EXTRA_ERROR_PRINTER, PrinterError.IP_NOT_FOUND);
        }

        try {
            internalConfigure(printer);
            TaskResult result = validatePrinterState(printer);
            if (result != null) {
                Logger.e("PrinterCommand: validate printer state response " + result);
                return result;
            }
            return execute(printer);
        } catch (Exception e) {
            Logger.e("PrinterCommand execute error: ", e);
            return failed();
        } finally {
            if (printer != null) {
                try {
                    printer.close();
                } catch (IOException e) {
                    Logger.e("PrinterCommand close error: ", e);
                }
            }
        }
    }

    private void updatePrinterInDB(PrinterInfo info) {
        ProviderAction.update(ShopProvider.getContentUri(PrinterTable.URI_CONTENT))
                .where(PrinterTable.MAC + " = ?", info.macAddress)
                .value(PrinterTable.IP, info.ip)
                .perform(getContext());
    }

    protected TaskResult validatePrinterState(PosPrinter printer) {
        if (isEmulate()) {
            return null;
        }
        TaskResult validateStateExtResult = null;

        PrinterStatusEx status;


        try {
            new ConfigurePaperSensorAction().execute(printer);
              
            if (printer.supportExtendedStatus()) {
                if (getPrinter().printerType.equalsIgnoreCase("Thermal"))
                status = new GetPrinterStatusExAction(getApp().getDrawerClosedValue()).execute(printer);
            else {
                PrinterStatusEx.PrinterStatusInfo printerStatus = new GetMP200PrinterStatusExAction(getApp().getDrawerClosedValue()).execute(printer).printerStatus;
                PrinterStatusEx.OfflineStatusInfo offlineStatusInfo = new GetMP200OffLineStatusExAction(getApp().getDrawerClosedValue(), !printerStatus.printerIsOffline).execute(printer).offlineStatus;
                PrinterStatusEx.ErrorStatusInfo errorStatusInfo = new GetMP200ErrorStatusExAction(getApp().getDrawerClosedValue()).execute(printer).errorStatus;
                PrinterStatusEx.PrinterHeadInfo printerHeadInfo = new GetMP200PaperRollSensorStatusExAction(getApp().getDrawerClosedValue()).execute(printer).printerHead;

                status = new PrinterStatusEx(printerStatus, offlineStatusInfo, errorStatusInfo, printerHeadInfo);
            }
            }
            else {
                status = new GetPrinterBasicStatusAction().execute(printer);
            }

        } catch (IOException e) {
            Logger.e("PrinterCommand validate statues execute: ", e);
            return failed().add(EXTRA_ERROR_PRINTER, PrinterError.DISCONNECTED);
        }


        if (status == null || status.printerStatus.isOverrunError || status.printerStatus.isBusy) {
            if (status == null)
                Logger.e("PrinterCommand validate statues execute: status is NULL!");
            else
                Logger.e("PrinterCommand validate statues execute: status: isOverrunError = " + status.printerStatus.isOverrunError + " isBusy: " + status.printerStatus.isBusy);
            return failed().add(EXTRA_ERROR_PRINTER, PrinterError.BUSY);
        }

        validateStateExtResult = validatePrinterStateExt(status);
        if (validateStateExtResult == null && status.printerStatus.printerIsOffline) {
            Logger.e("PrinterCommand validate statues execute: status: printerIsOffline = " + status.printerStatus.printerIsOffline);
            return failed().add(EXTRA_ERROR_PRINTER, PrinterError.OFFLINE);
        }

        return validateStateExtResult;
    }

    private void internalConfigure(PosPrinter printer) throws IOException {
        printer.write(new SelectPOSAction().getCommand());
    }

    protected TaskResult validatePrinterStateExt(PrinterStatusEx status) {
        return null;
    }

    protected boolean isEmulate() {
        return !BuildConfig.SUPPORT_PRINTER;
    }

    protected abstract TaskResult execute(PosPrinter printer) throws IOException;

    public static abstract class SuperBasePrintReportsCallback {

        @OnSuccess(PrinterCommand.class)
        public void handleSuccess() {
            onPrintSuccess();
        }

        @OnFailure(PrinterCommand.class)
        public void handleFailure(
                @Param(EXTRA_ERROR_PRINTER)
                PrinterError printerError) {

            if (printerError != null && printerError == PrinterError.DISCONNECTED) {
                onPrinterDisconnected();
                return;
            }
            if (printerError != null && printerError == PrinterError.NOT_CONFIGURED) {
                onPrinterNotConfigured();
                return;
            }
            if (printerError != null && printerError == PrinterError.PAPER_IS_NEAR_END) {
                onPrinterPaperNearTheEnd();
                return;
            }
            onPrintError(printerError);
        }

        protected abstract void onPrintSuccess();

        protected abstract void onPrintError(PrinterError error);

        protected abstract void onPrinterNotConfigured();

        protected abstract void onPrinterDisconnected();

        protected abstract void onPrinterPaperNearTheEnd();
    }

}
