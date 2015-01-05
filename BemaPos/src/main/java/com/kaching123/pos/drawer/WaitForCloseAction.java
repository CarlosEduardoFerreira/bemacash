package com.kaching123.pos.drawer;

import com.kaching123.pos.BaseAction;
import com.kaching123.pos.PosPrinter;

import java.io.IOException;

import static com.kaching123.pos.util.ByteUtil._bit;

/**
 * [Function] Printer extended status enquiry.
 * [Format] ASCII GS F8h 1
 * Hexadecimal 1D F8 31
 * Decimal 29 248 49
 * [Description] Issuing this command makes the printer to return five status bytes.
 * <p/>
 * Created by gdubina on 04/12/13.
 */
public class WaitForCloseAction extends BaseAction<Boolean> {

    private static int ANSWER_LEN = 5;
    private static byte[] COMMAND = new byte[]{GS, F8, 0x31};
    private static int RESPONSE_DRAWER_STATUS_INDEX = 1;//the second byte
    private static int RESPONSE_DRAWER_STATUS_BIT = 4;//range from 0-7

    private boolean canceled = false;
    private final long timeout;
    private final int closedValue;

    public WaitForCloseAction(int closedValue, long timeout) {
        this.timeout = timeout;
        this.closedValue = closedValue;
    }

    @Override
    protected byte[] getCommand() {
        return COMMAND;
    }

    @Override
    public Boolean execute(PosPrinter printer) throws IOException {
        System.out.println("WaitForCloseAction start");
        long startTime = System.currentTimeMillis();
        boolean isClosed = false;
        while (!isClosed && !canceled) {
            if (System.currentTimeMillis() - startTime >= timeout) {
                System.out.println("WaitForCloseAction TIMEOUT");
                break;
            }
            System.out.println("WaitForCloseAction step");
            printer.write(getCommand());
            System.out.println("WaitForCloseAction after write");
            byte[] bytes = printer.read(ANSWER_LEN);
            isClosed = _bit(bytes[RESPONSE_DRAWER_STATUS_INDEX], RESPONSE_DRAWER_STATUS_BIT) == closedValue;
            System.out.println("WaitForCloseAction isClosed = " + isClosed);
            if (!isClosed && !canceled) {
                try {
                    System.out.println("WaitForCloseAction SLEEP");
                    Thread.sleep(1000);
                } catch (InterruptedException ignore) {
                }
            }
        }
        System.out.println("WaitForCloseAction RETURN");
        return isClosed;
    }

    public void cancel() {
        System.out.println("WaitForCloseAction CANCEL");
        canceled = true;
    }
}
