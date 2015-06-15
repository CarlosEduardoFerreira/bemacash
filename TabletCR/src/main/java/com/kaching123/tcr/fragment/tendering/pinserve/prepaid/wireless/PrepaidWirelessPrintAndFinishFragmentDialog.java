package com.kaching123.tcr.fragment.tendering.pinserve.prepaid.wireless;

import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.getbase.android.db.provider.ProviderAction;
import com.getbase.android.db.provider.Query;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;
import com.kaching123.tcr.R;
import com.kaching123.tcr.adapter.PrepaidReceiptAdapter;
import com.kaching123.tcr.commands.print.pos.PrintPrepaidOrderCommand;
import com.kaching123.tcr.fragment.UiHelper;
import com.kaching123.tcr.fragment.dialog.DialogUtil;
import com.kaching123.tcr.fragment.dialog.WaitDialogFragment;
import com.kaching123.tcr.fragment.tendering.ChooseCustomerBaseDialog;
import com.kaching123.tcr.fragment.tendering.PrepaidChooseCustomerDialog;
import com.kaching123.tcr.fragment.tendering.payment.PayPrintAndFinishFragmentDialog;
import com.kaching123.tcr.model.PaymentTransactionModel;
import com.kaching123.tcr.model.payment.blackstone.prepaid.IPrePaidInfo;
import com.kaching123.tcr.model.payment.blackstone.prepaid.wireless.WirelessItem;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopSchema2;
import com.kaching123.tcr.store.ShopStore;
import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;

import java.math.BigDecimal;
import java.util.ArrayList;


/**
 * @author Ivan v. Rikhmayer
 */
@EFragment
public class PrepaidWirelessPrintAndFinishFragmentDialog extends PayPrintAndFinishFragmentDialog {

    private static final String DIALOG_NAME = PrepaidWirelessPrintAndFinishFragmentDialog.class.getSimpleName();
    @ViewById
    protected ImageView productImageview;
    @ViewById
    protected TextView productName, productOrdernum, productNameDisplay, total;
    @ViewById
    protected ViewGroup changeBlock;
    @FragmentArg
    protected IPrePaidInfo info;
    @FragmentArg
    protected BigDecimal transactionFee;
    @FragmentArg
    protected String orderNum;
    @FragmentArg
    protected String mTotal;
    @FragmentArg
    protected WirelessItem chosenCategory;
    private PrepaidReceiptAdapter adapter;

    @Override
    protected int getDialogTitle() {

        switch (chosenCategory.type) {
            case NATIONAL_TOP_UP:
                return R.string.prepaid_dialog_wireless_recharge_title;
            case INTERNATIONAL_WIRELESS_PIN:
                return R.string.prepaid_dialog_wireless_recharge_title;
            case PINLESS:
                return R.string.prepaid_dialog_pinless_recharge_title;
            case INTERNATIONAL_TOP_UP:
                return R.string.prepaid_dialog_wireless_recharge_international_title;
            case LONG_DISTANCE_PIN:
                return R.string.prepaid_dialog_long_distance_title;
            case NATIONAL_WIRELESS_PIN:
                return R.string.prepaid_dialog_wireless_recharge_title;
        }
        return R.string.blackstone_pay_confirm_title;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getDialog().getWindow().setLayout(getResources().getDimensionPixelOffset(R.dimen.prepaid_supass_choice_dlg_width),getResources().getDimensionPixelOffset(R.dimen.prepaid_wireless_payment_complete_heigth));
        setCancelable(false);
        UiHelper.showPrice(change, changeAmount);
    }

    @AfterViews
    void afterViewInitList() {
        productName.setText(chosenCategory.name);
        UrlImageViewHelper.setUrlDrawable(productImageview, chosenCategory.iconUrl, R.drawable.operator_default_icon, 60000);
        productOrdernum.setText(orderNum);
        productNameDisplay.setText(chosenCategory.name);
        total.setText(mTotal);
        changeBlock.setVisibility(changeAmount != null && changeAmount.compareTo(BigDecimal.ZERO) == 1 ? View.VISIBLE : View.GONE);
    }



    @Override
    protected int getDialogContentLayout() {
        return R.layout.wireless_payment_complete;
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
        switch (chosenCategory.type) {
            case NATIONAL_TOP_UP:
                return R.drawable.icon_wireless_recharge;
            case INTERNATIONAL_WIRELESS_PIN:
                return R.drawable.icon_wireless_recharge;
            case PINLESS:
                return R.drawable.icon_pinless;
            case INTERNATIONAL_TOP_UP:
                return R.drawable.icon_international_topup;
            case LONG_DISTANCE_PIN:
                return R.drawable.icon_long_distance;
            case NATIONAL_WIRELESS_PIN:
                return R.drawable.icon_wireless_recharge;
        }
        return 0;
    }

    @Override
    protected void printOrder(boolean skipPaperWarning, boolean searchByMac) {
        WaitDialogFragment.show(getActivity(), getString(R.string.wait_printing));
        PrintPrepaidOrderCommand.start(getActivity(), false, skipPaperWarning, searchByMac, orderGuid, info, printOrderCallback);
    }

    @Override
    protected void sendDigitalOrder() {
        PrepaidChooseCustomerDialog.show(getActivity(), orderGuid, info, new ChooseCustomerBaseDialog.emailSenderListener() {
            @Override
            public void onComplete() {
                listener.onConfirmed();
            }
        });
    }

    public static void show(FragmentActivity context,
                            String orderGuid,
                            IFinishConfirmListener listener,
                            ArrayList<PaymentTransactionModel> transactions,
                            IPrePaidInfo info,
                            WirelessItem chosenCategory,
                            BigDecimal changeAmount,
                            BigDecimal transactionFee,
                            String orderNum,
                            String total) {
        DialogUtil.show(context,
                DIALOG_NAME,
                PrepaidWirelessPrintAndFinishFragmentDialog_.builder().info(info).transactions(transactions).orderGuid(orderGuid).chosenCategory(chosenCategory).changeAmount(changeAmount).transactionFee(transactionFee).orderNum(orderNum).mTotal(total).build()).setListener(listener);
    }

    public static void hide(FragmentActivity activity) {
        DialogUtil.hide(activity, DIALOG_NAME);
    }

}
