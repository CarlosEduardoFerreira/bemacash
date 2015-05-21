package com.kaching123.tcr.commands.payment;

import com.kaching123.tcr.Logger;
import com.kaching123.tcr.commands.store.AsyncCommand;
import com.telly.groundy.TaskResult;

/**
 * @author Ivan v. Rikhmayer
 *         This class is intended to be parent to all web commands
 *         // TODO proly there's a way to merge with AsynCommand.class, I see no profit doing so ATM tho
 */
public abstract class WebCommand extends AsyncCommand {

    public static final String RESULT_REASON = "RESULT_REASON";
    public static final String RESULT_DATA = "RESULT_DATA";
    // These should stay here considering they are common for all blackstone requests
    public static final String ARG_DATA = "ARG_data";

    public enum ErrorReason {
        UNKNOWN("Reason is undefined"),
        DUE_TO_SERVICE_DESTROYED("Service was destroyed"),
        DUE_TO_CANCEL_BY_GROUP("Cancelled by group"),
        DUE_TO_OFFLINE("Can't launch a transaction while offline"),
        DUE_TO_USER_CANCELLED("Cancelled by user"),
        DUE_TO_MALFUNCTION("Failed due to gateway malfunction"),
        DUE_TO_PAX_IP_CHANGED("ignore error");

        String description;

        ErrorReason (String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }

        public static ErrorReason valueOf(int i) {
            switch (i) {
                case SERVICE_DESTROYED : return DUE_TO_SERVICE_DESTROYED;
                case CANCEL_BY_GROUP : return DUE_TO_CANCEL_BY_GROUP;
                case CANCEL_ALL : return DUE_TO_OFFLINE;
                default :
                    if (i >= 0 && i < ErrorReason.values().length) { // local values
                        return ErrorReason.values()[i];
                    }
            }
            return UNKNOWN;
        }
    }

    protected abstract boolean performAction();

    protected abstract TaskResult afterAction();

    @Override
    protected TaskResult doCommand() {
        Logger.d("Welcome to webservice BlackSaleCommand task. Execution starts.");

        boolean success;
        if (isQuitting()) {
            return cancelled().add(RESULT_REASON, ErrorReason.valueOf(getQuittingReason()));
        } else if (!isOnline()) {
            return failed().add(RESULT_REASON, ErrorReason.DUE_TO_OFFLINE);
        } else {
            success = performAction();
        }
        Logger.d("BlackSaleCommand task is about to finish.");
        if (isQuitting()) {
            return cancelled().add(RESULT_REASON, ErrorReason.valueOf(getQuittingReason()));
        } else if (!success) {
            return failed().add(RESULT_REASON, ErrorReason.valueOf(getQuittingReason()));
        } else {
            return afterAction();
        }
    }

}
