package com.kaching123.tcr.fragment.tendering;

import android.support.v4.app.FragmentActivity;

import com.kaching123.tcr.commands.print.digital.SendDigitalRefundCommand;
import com.kaching123.tcr.fragment.dialog.DialogUtil;
import com.kaching123.tcr.model.CustomerModel;
import com.kaching123.tcr.model.SaleOrderModel;
import com.kaching123.tcr.processor.MoneybackProcessor.RefundSaleItemInfo;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;

import com.kaching123.tcr.commands.print.digital.SendDigitalOrderForGiftCardCommand;
import java.util.ArrayList;

/**
 * Created by gdubina on 03/03/14.
 */
@EFragment
public class VoidChooseCustomerDialog extends ChooseCustomerBaseDialog {

    public static final String DIALOG_NAME = "VOID_CHOOSE_CUSTOMER";

    @FragmentArg
    protected ArrayList<RefundSaleItemInfo> refundItemsInfo;

    @FragmentArg
    protected ArrayList<String> transactionsGuids;

    @FragmentArg
    protected SaleOrderModel childOrderModel;

    protected String getCurrentOrderGuid() {
        return childOrderModel.guid;
    }

    @Override
    protected void sendDigitalOrder(String email) {

    }

    @Override
    protected void onCustomerPicked(CustomerModel customer) {
        SendDigitalRefundCommand.start(getActivity(), orderGuid, refundItemsInfo, transactionsGuids, customer.email, null);
		dismiss();
    }

    @Override
    protected void sendDigitalOrderForGiftCard(String email, String amount) {
        SendDigitalOrderForGiftCardCommand.start(getActivity(),email, amount, null);
        dismiss();
    }

    public static void show(FragmentActivity activity, String orderGuid, ArrayList<RefundSaleItemInfo> refundItemsInfo, ArrayList<String> transactionsGuids, SaleOrderModel childOrderModel) {
        DialogUtil.show(activity,
                DIALOG_NAME,
                VoidChooseCustomerDialog_.builder().orderGuid(orderGuid)
                        .refundItemsInfo(refundItemsInfo)
                        .transactionsGuids(transactionsGuids)
                        .childOrderModel(childOrderModel)
                        .build()
        );
    }
}
