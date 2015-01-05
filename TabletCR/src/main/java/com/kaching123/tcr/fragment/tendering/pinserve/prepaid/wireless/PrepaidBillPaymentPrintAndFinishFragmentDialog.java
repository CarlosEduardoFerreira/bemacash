package com.kaching123.tcr.fragment.tendering.pinserve.prepaid.wireless;

import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.getbase.android.db.provider.ProviderAction;
import com.getbase.android.db.provider.Query;
import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.EFragment;
import com.googlecode.androidannotations.annotations.FragmentArg;
import com.googlecode.androidannotations.annotations.ViewById;
import com.kaching123.tcr.R;
import com.kaching123.tcr.commands.print.pos.PrintPrepaidOrderCommand;
import com.kaching123.tcr.fragment.UiHelper;
import com.kaching123.tcr.fragment.dialog.DialogUtil;
import com.kaching123.tcr.fragment.dialog.WaitDialogFragment;
import com.kaching123.tcr.fragment.tendering.PrepaidChooseCustomerDialog;
import com.kaching123.tcr.fragment.tendering.payment.PayPrintAndFinishFragmentDialog;
import com.kaching123.tcr.model.PaymentTransactionModel;
import com.kaching123.tcr.model.payment.blackstone.prepaid.IPrePaidInfo;
import com.kaching123.tcr.model.payment.blackstone.prepaid.pinserve.request.BillPaymentRequest;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopSchema2;
import com.kaching123.tcr.store.ShopStore;
import com.kaching123.tcr.websvc.api.prepaid.Category;

import java.math.BigDecimal;
import java.util.ArrayList;


/**
 * @author Ivan v. Rikhmayer
 */
@EFragment
public class PrepaidBillPaymentPrintAndFinishFragmentDialog extends PayPrintAndFinishFragmentDialog {
    private static final Uri URI_ORDER = ShopProvider.getContentUri(ShopStore.SaleOrderView.URI_CONTENT);
    private static final String DIALOG_NAME = PrepaidBillPaymentPrintAndFinishFragmentDialog.class.getSimpleName();
    private final String DollarAmpsand = "$";
    @ViewById
    protected TextView productOrdernum, productNameDisplay, accountNumber_Display, amount, fee, total;
    @ViewById
    protected ViewGroup changeBlock;
    @FragmentArg
    protected IPrePaidInfo info;
    @FragmentArg
    protected BigDecimal mAmount;
    @FragmentArg
    protected Category chosenCategory;
    @FragmentArg
    protected BigDecimal transactionFee;
    @FragmentArg
    protected BillPaymentRequest request;
    @FragmentArg
    protected String mOrderNum;
    @FragmentArg
    protected String mTotal;
    static private FragmentActivity mContext;

    @Override
    protected int getDialogTitle() {
        return R.string.prepaid_dialog_wireless_bill_payment_title;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getDialog().getWindow().setLayout(getResources().getDimensionPixelOffset(R.dimen.prepaid_supass_choice_dlg_width), getResources().getDimensionPixelOffset(R.dimen.prepaid_wireless_payment_complete_heigth));
        UiHelper.showPrice(change, changeAmount);
        setCancelable(false);
    }

    @AfterViews
    void afterViewInitList() {
        productOrdernum.setText(mOrderNum);
        productNameDisplay.setText(chosenCategory.id);
        accountNumber_Display.setText(request.accountNumber);
        amount.setText(DollarAmpsand + mAmount.toString());
        fee.setText(String.valueOf(DollarAmpsand + request.feeAmount));
        total.setText(DollarAmpsand + mTotal);
        changeBlock.setVisibility(changeAmount != null && changeAmount.compareTo(BigDecimal.ZERO) == 1 ? View.VISIBLE : View.GONE);
    }


    @Override
    protected int getDialogContentLayout() {
        return R.layout.bill_payment_complete;
    }

    @Override
    protected int getTitleGravity() {
        return Gravity.LEFT;
    }

    @Override
    protected int getTitleTextColor() {
        return Color.WHITE;
    }

    @AfterViews
    protected void initViews() {
        printBox.setChecked(true);
    }

    @Override
    protected int getTitleIcon() {
        return R.drawable.icon_bill_payment;
    }

    @Override
    protected void printOrder(boolean skipPaperWarning, boolean searchByMac) {
        WaitDialogFragment.show(getActivity(), getString(R.string.wait_printing));
        PrintPrepaidOrderCommand.start(getActivity(), false, skipPaperWarning, searchByMac, orderGuid, info, printOrderCallback);
    }

    @Override
    protected void sendDigitalOrder() {
        PrepaidChooseCustomerDialog.show(getActivity(), orderGuid, info);
    }

    public static void show(FragmentActivity context,
                            String orderGuid,
                            IFinishConfirmListener listener,
                            ArrayList<PaymentTransactionModel> transactions,
                            IPrePaidInfo info,
                            Category chosenCategory,
                            BillPaymentRequest request,
                            BigDecimal mAmount,
                            BigDecimal changeAmount,
                            BigDecimal transactionFee,
                            String orderNum,
                            String total) {
        DialogUtil.show(context,
                DIALOG_NAME,
                PrepaidBillPaymentPrintAndFinishFragmentDialog_.builder().info(info).transactions(transactions).request(request).mAmount(mAmount).orderGuid(orderGuid).chosenCategory(chosenCategory).changeAmount(changeAmount).transactionFee(transactionFee).mOrderNum(orderNum).mTotal(total).build()).setListener(listener);
    }

    public static void hide(FragmentActivity activity) {
        DialogUtil.hide(activity, DIALOG_NAME);
    }

}