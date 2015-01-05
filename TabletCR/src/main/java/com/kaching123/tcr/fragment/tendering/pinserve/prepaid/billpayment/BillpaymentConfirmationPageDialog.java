package com.kaching123.tcr.fragment.tendering.pinserve.prepaid.billpayment;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Gravity;
import android.widget.ListView;
import android.widget.TextView;

import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.EFragment;
import com.googlecode.androidannotations.annotations.FragmentArg;
import com.googlecode.androidannotations.annotations.ViewById;
import com.googlecode.androidannotations.annotations.res.ColorRes;
import com.kaching123.tcr.R;
import com.kaching123.tcr.adapter.PrepaidReceiptAdapter;
import com.kaching123.tcr.fragment.dialog.DialogUtil;
import com.kaching123.tcr.fragment.dialog.StyledDialogFragment;
import com.kaching123.tcr.model.payment.blackstone.prepaid.pinserve.request.BillPaymentRequest;
import com.kaching123.tcr.websvc.api.prepaid.BillerLoadRecord;
import com.kaching123.tcr.websvc.api.prepaid.Category;
import com.kaching123.tcr.websvc.api.prepaid.MasterBiller;
import com.kaching123.tcr.websvc.api.prepaid.PaymentOption;

import java.math.BigDecimal;

/**
 * @author Ivan v. Rikhmayer
 *         This class is intended to
 */
@EFragment
public class BillpaymentConfirmationPageDialog extends StyledDialogFragment {

    private static final String DIALOG_NAME = "SunpassAmountFragmentDialog";

    private final static BigDecimal FEE_AMOUNT = new BigDecimal(1.5);
    private final String DollarAmpsand = "$";

    protected BillpaymentConfirmationPageDialogCallback callback;

    @FragmentArg
    protected Category chosenCategory;
    @FragmentArg
    protected MasterBiller chosenBiller;
    @FragmentArg
    protected PaymentOption chosenOption;
    @FragmentArg
    protected BigDecimal mAmount;
    @FragmentArg
    protected BillerLoadRecord billerData;
    @FragmentArg
    protected String accountNumber;
    @FragmentArg
    protected BillPaymentRequest formedRequest;
    @FragmentArg
    protected BigDecimal mTotal;

    @ViewById
    protected ListView listView;
    @ViewById
    protected TextView  productNameDisplay, accountNumber_Display, amount, fee, total;

    @ColorRes(R.color.prepaid_dialog_white)
    protected int colorOk;
    @ColorRes(R.color.gray_dark)
    protected int colorDisabled;


    public void setCallback(BillpaymentConfirmationPageDialogCallback callback) {
        this.callback = callback;
    }


    @AfterViews
    void afterViewInitList()
    {

        productNameDisplay.setText(chosenCategory.id);
        accountNumber_Display.setText(accountNumber);
        amount.setText(DollarAmpsand + mAmount.toString());
        fee.setText(String.valueOf(DollarAmpsand + chosenOption.feeAmount));
        total.setText(DollarAmpsand + mTotal + "");
//        total.setText((mAmount.doubleValue() + chosenOption.feeAmount) + "");


    }







    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getNegativeButton().setTextColor(Color.WHITE);
        getPositiveButton().setTextColor(colorOk);
        getNegativeButton().setTextSize(25);
        getPositiveButton().setTextSize(25);
        getDialog().getWindow().setLayout(
                getResources().getDimensionPixelOffset(R.dimen.prepaid_supass_choice_dlg_width),
                getResources().getDimensionPixelOffset(R.dimen.default_dlg_heigth));
    }
    @Override protected  int getTitleGravity(){return Gravity.LEFT;};

    @Override protected  int getSeparatorColor(){return Color.WHITE;}

    @Override protected  int getTitleTextColor(){return Color.WHITE;}

    @Override protected  int getTitleViewBackgroundColor(){return getResources().getColor(R.color.prepaid_dialog_title_background_color); }

    @Override protected  int getButtonsBackgroundColor(){return getResources().getColor(R.color.prepaid_dialog_buttons_background_color); }

    @Override
    protected int getTitleIcon() {
        return R.drawable.icon_bill_payment;
    }
    @Override
    protected int getDialogTitle() {
        return R.string.prepaid_dialog_wireless_bill_payment_title;
    }


    @Override
    protected int getNegativeButtonTitle() {
        return R.string.btn_back;
    }

    @Override
    protected int getPositiveButtonTitle() {
        return R.string.btn_confirm;
    }

    @Override
    protected boolean hasNegativeButton() {
        return true;
    }

    @Override
    protected boolean hasPositiveButton() {
        return true;
    }

    @Override
    protected int getDialogContentLayout() {
        return R.layout.bill_payment_confirmation;
    }

    @Override
    protected OnDialogClickListener getPositiveButtonListener() {
        return new OnDialogClickListener() {
            @Override
            public boolean onClick() {
                complete();
                return false;
            }
        };
    }

    @Override
    protected OnDialogClickListener getNegativeButtonListener() {
        return new OnDialogClickListener() {
            @Override
            public boolean onClick() {
                callback.onCancel();
                return false;
            }
        };
    }

    protected boolean complete() {
        callback.onComplete();
        return true;
    }


    public static void show(final FragmentActivity context,
                            final Category chosenCategory,
                            final MasterBiller chosenBiller,
                            final PaymentOption chosenOption,
                            final BigDecimal mAmount,
                            final BillerLoadRecord billerData,
                            final String accountNumber,
                            final BillPaymentRequest formedRequest,
                            final BigDecimal mTotal,
                            BillpaymentConfirmationPageDialogCallback listener) {
        BillpaymentConfirmationPageDialog dialog = BillpaymentConfirmationPageDialog_.builder()
                .chosenCategory(chosenCategory)
                .chosenBiller(chosenBiller)
                .chosenOption(chosenOption)
                .mAmount(mAmount)
                .accountNumber(accountNumber)
                .billerData(billerData)
                .formedRequest(formedRequest)
                .mTotal(mTotal)
                .build();
        dialog.setCallback(listener);
        DialogUtil.show(context,
                DIALOG_NAME,
                dialog);
    }

    public static void hide(FragmentActivity activity) {
        DialogUtil.hide(activity, DIALOG_NAME);
    }


    public interface BillpaymentConfirmationPageDialogCallback {

        public abstract void onCancel();

        public abstract void onError(String message);

        public abstract void onComplete();
    }

}
