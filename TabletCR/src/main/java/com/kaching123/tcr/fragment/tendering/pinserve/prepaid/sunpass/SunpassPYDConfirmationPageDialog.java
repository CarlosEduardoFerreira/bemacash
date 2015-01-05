package com.kaching123.tcr.fragment.tendering.pinserve.prepaid.sunpass;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Gravity;
import android.view.View;
import android.widget.ListView;

import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.EFragment;
import com.googlecode.androidannotations.annotations.FragmentArg;
import com.googlecode.androidannotations.annotations.ViewById;
import com.googlecode.androidannotations.annotations.res.ColorRes;
import com.kaching123.tcr.R;
import com.kaching123.tcr.adapter.PrepaidReceiptAdapter;
import com.kaching123.tcr.fragment.dialog.DialogUtil;
import com.kaching123.tcr.fragment.dialog.StyledDialogFragment;
import com.kaching123.tcr.fragment.tendering.pinserve.prepaid.wireless.PrepaidPaymentCompleteReceiptListViewModel;
import com.kaching123.tcr.model.payment.blackstone.prepaid.PrepaidUser;
import com.kaching123.tcr.model.payment.blackstone.prepaid.sunpass.request.SunPassDocumentPaymentRequest;
import com.kaching123.tcr.model.payment.blackstone.prepaid.wireless.SunpassType;
import com.kaching123.tcr.websvc.api.prepaid.DocumentInquiryResponse;

import java.math.BigDecimal;

/**
 * @author Ivan v. Rikhmayer
 *         This class is intended to
 */
@EFragment
public class SunpassPYDConfirmationPageDialog extends StyledDialogFragment {

    private static final String DIALOG_NAME = "SunpassAmountFragmentDialog";

    private final static BigDecimal FEE_AMOUNT = new BigDecimal(1.5);
    private final String DollarAmpsand = "$";

    protected SunpassPYDConfirmationPageDialogCallback callback;
    @FragmentArg
    protected String transactionMode;
    @FragmentArg
    protected String cashierId;
    @FragmentArg
    protected PrepaidUser user;
    @FragmentArg
    protected SunPassDocumentPaymentRequest request;
    @FragmentArg
    protected String accountNumber;
    @FragmentArg
    protected SunpassType type;
    @FragmentArg
    protected DocumentInquiryResponse response;

    @ViewById
    protected ListView listView;

    @ColorRes(R.color.prepaid_dialog_white)
    protected int colorOk;
    @ColorRes(R.color.gray_dark)
    protected int colorDisabled;

    private PrepaidReceiptAdapter adapter;
    static private FragmentActivity mContext;

    public void setCallback(SunpassPYDConfirmationPageDialogCallback callback) {
        this.callback = callback;
    }


    @AfterViews
    void afterViewInitList()
    {
        switch (type) {
            case SUNPASS_PAY_YOUR_DOCUMENT:
                listView.setVisibility(View.VISIBLE);
                String[] names = PrepaidPaymentCompleteReceiptListViewModel.getListForConfirmationItemNames();
                String[] contents = PrepaidPaymentCompleteReceiptListViewModel.getPYDConfiramtionListForContents(request, response, FEE_AMOUNT.toString());
                PrepaidPaymentCompleteReceiptListViewModel.trace(names, contents);
                adapter = new PrepaidReceiptAdapter(getActivity(),names, contents);
                listView.setAdapter(adapter);
                listView.setDivider(null);
                listView.setDividerHeight(0);
                break;
            case SUNPASS_TRANSPONDER:

        }

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
        return R.drawable.icon_sun_pass;
    }
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
        return R.layout.sunpass_confirmation_page;
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
        callback.onComplete(request);
        return true;
    }


    public static void show(FragmentActivity context, String accountNumber, String transactionMode,
                            String cashierId, PrepaidUser user,SunPassDocumentPaymentRequest request,  DocumentInquiryResponse response, SunpassType type, SunpassPYDConfirmationPageDialogCallback listener) {
        SunpassPYDConfirmationPageDialog dialog = SunpassPYDConfirmationPageDialog_.builder()
                .transactionMode(transactionMode)
                .cashierId(cashierId)
                .user(user)
                .request(request)
                .response(response)
                .accountNumber(accountNumber)
                .type(type)
                .build();
        dialog.setCallback(listener);
        mContext = context;
        DialogUtil.show(context,
                DIALOG_NAME,
                dialog);
    }

    public static void hide(FragmentActivity activity) {
        DialogUtil.hide(activity, DIALOG_NAME);
    }


    public interface SunpassPYDConfirmationPageDialogCallback {

        public abstract void onCancel();

        public abstract void onError(String message);

        public abstract void onComplete(SunPassDocumentPaymentRequest request);
    }

}
