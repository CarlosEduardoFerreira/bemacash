package com.kaching123.pos.data;

import java.io.Serializable;

import static com.kaching123.pos.util.ByteUtil._bit;

/**
 * Created by gdubina on 16.12.13.
 */
public class PrinterStatusEx implements Serializable{

    public PrinterStatusInfo printerStatus;
    public OfflineStatusInfo offlineStatus;
    public ErrorStatusInfo errorStatus;
    public PrinterHeadInfo printerHead;

    public PrinterStatusEx(PrinterStatusInfo printerStatus, OfflineStatusInfo offlineStatus, ErrorStatusInfo errorStatus, PrinterHeadInfo printerHead) {
        this.printerStatus = printerStatus;
        this.offlineStatus = offlineStatus;
        this.errorStatus = errorStatus;
        this.printerHead = printerHead;
    }

    public static class PrinterStatusInfo implements Serializable{

        public static enum BufferState{EMPTY, UNDER_1_3, ABOVE_1_3, BEYOND_3_4}

        /**
         * Bit 2: OVR (Overrun Error)
         * 0 – Printer is ready to receive data.
         * 1 – Printer is in overrun condition. If more data is received, it will be lost.
         */
        public boolean isOverrunError;

        /**
         * Bit 3: Offline.
         * 0 – Printer is on-line.
         * 1 – Printer is off-line
         */
        public boolean printerIsOffline;

        /**
         * Bit 4: Wait.
         * 0 – Printer is printing (busy condition).
         * 1 – Printer buffer is empty, waiting for more data or commands.
         */
        public boolean isBusy;

        /**
         * Bit 6 & 5: BufStat - Buffer status.
         * 00 – Printer buffer empty.
         * 01 – Printer buffer is under 1/3 of its capacity.
         * 10 – Printer buffer is above 1/3 of its capacity.
         * 11 – Printer buffer is beyond ¾ of its capacity.
         */
        public BufferState bufferState;

        public PrinterStatusInfo(byte b){
            isOverrunError = _bit(b, 2) == 1;
            printerIsOffline = _bit(b, 3) == 1;
            isBusy = _bit(b, 6) == 1;

            int bufState = _bit(b, 5) +  _bit(b, 6);
            bufferState = BufferState.values()[bufState];
        }

        private PrinterStatusInfo(){}

        public static PrinterStatusInfo emulate(){
            PrinterStatusInfo info = new PrinterStatusInfo();
            info.bufferState = BufferState.EMPTY;
            return info;
        }
    }

    public static class OfflineStatusInfo implements Serializable{
        /**
         * Bit 1: PNES – Paper Near-end Sensor
         * 0 – Paper is not near the end of roll.
         * 1 – Paper is near the end of roll
         */
        public boolean paperIsNearEnd;

        /**
         * Bit 2: PS – Paper sensor
         * 0 – Printer has paper.
         * 1 – Printer has no paper at all.
         */
        public boolean noPaper;

        /**
         * Bit 4: Drawer
         * 0 – Drawer sensor is in low level (logical 0).
         * 1 – Drawer sensor is in high level (logical 1).
         */
        public boolean drawerIsClosed;

        /**
         * Bit 6: Error
         * 0 – No error condition exist in the printer.
         * 1 – At least one error condition is being reported by the printer.
         */
        public boolean isError;

        /**
         * Bit 7: Cover
         * 0 – Printer cover is opened.
         * 1 – Printer cover is closed.
         */
        public boolean coverIsClosed;

        public OfflineStatusInfo(byte b, int drawerClosedValue) {
            paperIsNearEnd = _bit(b, 1) == 1;
            noPaper = _bit(b, 2) == 1;
            drawerIsClosed = _bit(b, 4) == drawerClosedValue;
            isError = _bit(b, 6) == 1;
            coverIsClosed = _bit(b, 7) == 1;
        }

        private OfflineStatusInfo(){

        }

        public static OfflineStatusInfo emulate(){
            OfflineStatusInfo info = new OfflineStatusInfo();
            info.drawerIsClosed = true;
            info.coverIsClosed = true;
            return info;
        }
    }

    public static class ErrorStatusInfo implements Serializable{
        /**
         * Bit 2: CA – Cutter Absence
         * 0 – Cutter present.
         * 1 – Cutter absent.
         */
        public boolean cutterIsAbsent;

        /**
         * Bit 3: CE – Cutter Error
         * 0 – No error condition detected in the cutter.
         * 1 – Cutter error condition detected
         */
        public boolean cutterErrorIsDetected;

        /**
         * Bit 5: NRE – Non-recoverable Error
         * 0 – NRE condition not detected.
         * 1 – NRE condition detected.
         */
        public boolean isNRE;

        /**
         * Bit 6: RE – Recoverable Error
         * 0 – RE condition not present.
         * 1 – RE condition present
         */
        public boolean isREConditionPresent;

        public ErrorStatusInfo(byte b) {
            cutterIsAbsent = _bit(b, 2) == 1;
            cutterErrorIsDetected = _bit(b, 3) == 1;
            isNRE = _bit(b, 5) == 1;
            isREConditionPresent = _bit(b, 6) == 1;
        }

        private ErrorStatusInfo(){}

        public static ErrorStatusInfo emulate(){
            return new ErrorStatusInfo();
        }
    }

    public static class PrinterHeadInfo implements Serializable{
        /**
         * Bit 2: HOH – Head Overheat
         * 0 – Print head has normal temperature.
         * 1 – Print head is overheated.
         */
        public boolean headIsOverhead;

        public PrinterHeadInfo(byte b) {
            headIsOverhead = _bit(b, 2) == 1;
        }

        private PrinterHeadInfo(){}

        public static PrinterHeadInfo emulate(){
            return new PrinterHeadInfo();
        }
    }
}
