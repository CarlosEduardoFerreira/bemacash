package com.kaching123.tcr.fragment.tendering.pinserve.prepaid.wireless;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.ListView;

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
import com.kaching123.tcr.model.payment.blackstone.prepaid.sunpass.request.SunPassDocumentPaymentRequest;
import com.kaching123.tcr.model.payment.blackstone.prepaid.sunpass.request.SunReplenishmentRequest;
import com.kaching123.tcr.model.payment.blackstone.prepaid.wireless.SunpassType;
import com.kaching123.tcr.websvc.api.prepaid.BalanceResponse;
import com.kaching123.tcr.websvc.api.prepaid.DocumentInquiryResponse;

import java.math.BigDecimal;
import java.util.ArrayList;


/**
 * @author Ivan v. Rikhmayer
 */
@EFragment
public class PrepaidSunPassPrintAndFinishFragmentDialog extends PayPrintAndFinishFragmentDialog {

    private static final String DIALOG_NAME = PrepaidSunPassPrintAndFinishFragmentDialog.class.getSimpleName();
    @ViewById
    protected ListView listView;
    @FragmentArg
    protected SunReplenishmentRequest request;
    @FragmentArg
    protected SunPassDocumentPaymentRequest dRequest;
    @FragmentArg
    protected DocumentInquiryResponse dResponse;
    @FragmentArg
    protected IPrePaidInfo info;
    @FragmentArg
    protected SunpassType type;

    @FragmentArg
    BigDecimal transactionFee;
    @FragmentArg
    String orderNum;
    @FragmentArg
    String total;

    @FragmentArg
    protected BalanceResponse response;
    private PrepaidReceiptAdapter adapter;
    static private FragmentActivity mContext;

    @Override
    protected int getDialogTitle() {

        switch (type) {
            case SUNPASS_PAY_YOUR_DOCUMENT:
                return R.string.prepaid_dialog_sunpass_pay_your_document_title;
            case SUNPASS_TRANSPONDER:
                return R.string.prepaid_dialog_sunpass_transponder_title;
        }
        return R.string.blackstone_pay_confirm_title;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getDialog().getWindow().setLayout(getResources().getDimensionPixelOffset(R.dimen.prepaid_supass_choice_dlg_width), getResources().getDimensionPixelOffset(R.dimen.prepaid_wireless_payment_complete_heigth));
        setCancelable(false);
        UiHelper.showPrice(change, changeAmount);
    }

    @AfterViews
    void afterViewInitList() {
        switch (type) {
            case SUNPASS_TRANSPONDER:
                listView.setVisibility(View.VISIBLE);
                String[] names = PrepaidPaymentCompleteReceiptListViewModel.getListForNames();
                String[] contents = PrepaidPaymentCompleteReceiptListViewModel.getListForContents(request, response, orderNum, total);
                PrepaidPaymentCompleteReceiptListViewModel.trace(names, contents);
                adapter = new PrepaidReceiptAdapter(getActivity(), names, contents);
                listView.setAdapter(adapter);
                listView.setDivider(null);
                listView.setDividerHeight(0);
                break;
            case SUNPASS_PAY_YOUR_DOCUMENT:
                listView.setVisibility(View.VISIBLE);
                String[] names2 = PrepaidPaymentCompleteReceiptListViewModel.getListForPYDConfirmationItemNames();
                String[] contents2 = PrepaidPaymentCompleteReceiptListViewModel.getPYDConfiramtionListForContents(dRequest, dResponse, String.valueOf(dRequest.feeAmount));
                adapter = new PrepaidReceiptAdapter(getActivity(), names2, contents2);
                listView.setAdapter(adapter);
                listView.setDivider(null);
                listView.setDividerHeight(0);
                break;

        }
    }

    @Override
    protected int getDialogContentLayout() {
        return R.layout.sunpass_payment_complete;
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
        return R.drawable.icon_sun_pass;
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
                            SunpassType type,
                            SunReplenishmentRequest request,
                            BalanceResponse response, BigDecimal changeAmount,
                            BigDecimal transactionFee, String orderNum, String total,
                            SunPassDocumentPaymentRequest dRequest,
                            DocumentInquiryResponse dResponse
    ) {
        DialogUtil.show(context,
                DIALOG_NAME,
                PrepaidSunPassPrintAndFinishFragmentDialog_.builder().info(info).transactions(transactions).dRequest(dRequest).dResponse(dResponse).orderGuid(orderGuid).type(type).request(request).response(response).changeAmount(changeAmount).transactionFee(transactionFee).orderNum(orderNum).total(total).build()).setListener(listener);
    }

    public static void hide(FragmentActivity activity) {
        DialogUtil.hide(activity, DIALOG_NAME);
    }

}