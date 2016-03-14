package com.kaching123.tcr.commands.prepaid;

import android.content.ContentProviderOperation;
import android.content.Context;

import com.kaching123.tcr.commands.store.AsyncCommand;
import com.kaching123.tcr.model.SaleOrderModel;
import com.kaching123.tcr.service.ISqlCommand;
import com.telly.groundy.TaskResult;
import com.telly.groundy.annotations.OnFailure;
import com.telly.groundy.annotations.OnSuccess;
import com.telly.groundy.annotations.Param;

import java.math.BigDecimal;
import java.util.ArrayList;

/**
 * Created by yinteli on 16/3/7.
 */
public class AddPrepaidItemCommand extends AsyncCommand {
    public static final String ARG_ACTION = "arg_action";
    public static final String ARG_ERROR = "arg_error";
    public static final String ARG_ERROR_MSG = "arg_error_msg";
    public static final String ARG_TRANSACTION_ID = "arg_transaction_id";
    public static final String ARG_ITEM_NAME = "arg_item_nameitem_qty";
    public static final String ARG_ITEM_DETAILS = "arg_item_details";
    public static final String ARG_ITEM_QTY = "arg_item_qty";
    public static final String ARG_ITEM_PRICE = "arg_item_price";
    public static final String ARG_ITEM_TAXABLE = "arg_item_taxable";

    @Override
    protected TaskResult doCommand() {
        String action = getArgs().getString(ARG_ACTION);
        String error = getArgs().getString(ARG_ERROR);
        String errorMsg = getArgs().getString(ARG_ERROR_MSG);
        String transactionId = getArgs().getString(ARG_TRANSACTION_ID);
        String itemName = getArgs().getString(ARG_ITEM_NAME);
        String itemDetails = getArgs().getString(ARG_ITEM_DETAILS);
        BigDecimal itemQty = new BigDecimal(getArgs().getInt(ARG_ITEM_QTY));
        BigDecimal itemPrice = new BigDecimal(getArgs().getInt(ARG_ITEM_QTY));
        boolean itemTaxable = getArgs().getBoolean(ARG_ITEM_TAXABLE);
        return succeeded();
    }

    @Override
    protected ISqlCommand createSqlCommand() {
        return null;
    }

    @Override
    protected ArrayList<ContentProviderOperation> createDbOperations() {
        return null;
    }

    public static void start(Context context, String action, String error, String errorMsg, String transactionId, String itemName, String itemDetails, int itemQty, int itemPrice,boolean itemTaxable,  BasePrepaidItemCallback callback) {
        create(AddPrepaidItemCommand.class).arg(ARG_ACTION, action).arg(ARG_ERROR, error).arg(ARG_ERROR_MSG, errorMsg).arg(ARG_TRANSACTION_ID, transactionId).arg(ARG_ITEM_NAME, itemName).arg(ARG_ACTION, action).arg(ARG_ITEM_DETAILS, itemDetails).arg(ARG_ITEM_QTY, itemQty).arg(ARG_ITEM_PRICE, itemPrice).arg(ARG_ITEM_TAXABLE, itemTaxable).callback(callback).queueUsing(context);
    }

    public static abstract class BasePrepaidItemCallback {

        @OnSuccess(AddPrepaidItemCommand.class)
        public void handleSuccess() {
            onSuccess();
        }

        @OnFailure(AddPrepaidItemCommand.class)
        public void handleFailure() {
            onFailure();
        }

        protected abstract void onSuccess();

        protected abstract void onFailure();
    }
}
