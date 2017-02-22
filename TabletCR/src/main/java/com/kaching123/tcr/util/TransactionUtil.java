package com.kaching123.tcr.util;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;

import java.math.BigDecimal;
import java.util.ArrayList;

import com.kaching123.tcr.R;
import com.kaching123.tcr.TcrApplication;
import com.kaching123.tcr.activity.SuperBaseActivity;
import com.kaching123.tcr.fragment.dialog.AlertDialogFragment;
import com.kaching123.tcr.fragment.dialog.StyledDialogFragment;
import com.kaching123.tcr.model.ContentValuesUtil;
import com.kaching123.tcr.model.PaymentTransactionModel;
import com.kaching123.tcr.model.StateModel;

/**
 * Created by irikhmayer on 03.10.2015.
 */
public class TransactionUtil {

    protected final static BigDecimal BREAKPOINT_6 = new BigDecimal("100");


    public static boolean refundPossible(final ArrayList<PaymentTransactionModel> transactions) {

        if (TcrApplication.get().getShopInfo().useCreditReceipt) {
            return true;
        }

        for (PaymentTransactionModel transaction : transactions) {
            if (!transaction.gateway.gateway().enabled()) {
                return false;
            }
        }
        return true;
    }

    public static void processPayment(BigDecimal amount, Context context) {
        /*
        BigDecimal currentValue = ContentValuesUtil._decimal(TcrApplication.get().getShopPref().storeNofiscalMaxValue().getOr(null));
        if (currentValue == null) {
            currentValue = amount;
        } else {
            currentValue = currentValue.add(amount);
        }
        final StateModel state = StateModel.getStateById(context, (int) (long) TcrApplication.get().getShopInfo().stateId);

        final BigDecimal storeMaxAmount = state.maxAmount;

        ResourceUtil.setNewValues(currentValue.subtract(storeMaxAmount), context);

        TcrApplication.get().getShopPref().storeNofiscalMaxValue().put(ContentValuesUtil._decimal(currentValue));
        /**/
    }

    public static void processPaymentCheckForAlert(BigDecimal amount, Context context, PercentageAlertInterface callback) {
        /*
        if (NFCeProcessor.isFiscalShop() && TcrApplication.get().getShopInfo().isFMEnabled) {
            callback.onConditionsMaintained();
            return;
        }
        BigDecimal currentValue = ContentValuesUtil._decimal(TcrApplication.get().getShopPref().storeNofiscalMaxValue().getOr(null));
        if (currentValue == null) {
            currentValue = BigDecimal.ZERO;
        }
        final StateModel state = StateModel.getStateById(context, (int) (long) TcrApplication.get().getShopInfo().stateId);

        final BigDecimal storeMaxAmount = state.maxAmount;

        if (storeMaxAmount == null || storeMaxAmount.compareTo(BigDecimal.ZERO) == 0) {
            callback.onConditionsBreakpointBroken(BREAKPOINT_6);
            return;
        }

        if (currentValue.subtract(storeMaxAmount).compareTo(BigDecimal.ZERO) > 0) {
            callback.onConditionsAlreadyBroken();
            return;
        } else if (currentValue.add(amount).subtract(storeMaxAmount).compareTo(BigDecimal.ZERO) > 0) {
            callback.onConditionsToBeBroken();
            return;
        }

        callback.onConditionsMaintained();
        /**/
    }

    public interface PercentageAlertInterface {
        void onConditionsBreakpointBroken(BigDecimal point);
        void onConditionsToBeBroken();
        void onConditionsAlreadyBroken();

        void onConditionsMaintained();

    }

    public abstract static class PercentageAlertInterfaceImpl implements PercentageAlertInterface {

        public PercentageAlertInterfaceImpl() {

        }

        public abstract FragmentActivity getContext();

        public abstract void onAction();

        @Override
        public void onConditionsBreakpointBroken(final BigDecimal point) {
            AlertDialogFragment.showConfirmation(
                    getContext(),
                    R.string.item_activity_sales_permitted_title,
                    getContext().getString(R.string.item_activity_sales_permitted_near_end, point.toString()),
                    new StyledDialogFragment.OnDialogClickListener() {

                        @Override
                        public boolean onClick() {
                            onAction();
                            return true;
                        }
                    });
        }

        @Override
        public void onConditionsToBeBroken() {

            AlertDialogFragment.showConfirmation(
                    getContext(),
                    R.string.item_activity_sales_permitted_title,
                    getContext().getString(R.string.item_activity_sales_permitted_near_end, "100"),
                    new StyledDialogFragment.OnDialogClickListener() {

                        @Override
                        public boolean onClick() {
                            onAction();
                            return true;
                        }
                    });
        }

        @Override
        public void onConditionsAlreadyBroken() {
            //final StateModel state = StateModel.getStateById(getContext(), (int) (long) TcrApplication.get().getShopInfo().stateId);
            onAction();
        }

        @Override
        public void onConditionsMaintained() {
            onAction();
        }
    }

    private static class ResourceUtil {

        private static void setNewValues(BigDecimal value, Context context) {
            int color;
            String text;
            if (value.compareTo(BigDecimal.ZERO) > 0) {
                text = context.getString(R.string.warning_new_msg);
                color = 2;                // red
            }
            else {
                text = null;
                color = 0;
            }
            //TcrApplication.get().getShopPref().appwideColorEnumValue().put(color);
            //TcrApplication.get().getShopPref().appwideWarningTextMessage().put(text);

            LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(SuperBaseActivity.LOCAL_BROADCAST_WARNING));
        }

    }
}
