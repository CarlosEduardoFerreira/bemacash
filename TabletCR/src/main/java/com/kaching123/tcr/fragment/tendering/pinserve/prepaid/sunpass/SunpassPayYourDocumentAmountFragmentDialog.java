package com.kaching123.tcr.fragment.tendering.pinserve.prepaid.sunpass;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.InputFilter;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.ColorRes;
import com.kaching123.tcr.R;
import com.kaching123.tcr.adapter.PayYourDocumentAmountAdapter;
import com.kaching123.tcr.adapter.PrepaidSunpassPayYourDocumentOrderViewAdapter;
import com.kaching123.tcr.component.CustomEditBox;
import com.kaching123.tcr.component.KeyboardView;
import com.kaching123.tcr.component.TelephoneEditNumberCurrencyFormatInputFilter;
import com.kaching123.tcr.fragment.dialog.DialogUtil;
import com.kaching123.tcr.fragment.dialog.StyledDialogFragment;
import com.kaching123.tcr.model.payment.blackstone.prepaid.PrepaidUser;
import com.kaching123.tcr.model.payment.blackstone.prepaid.sunpass.request.SunPassDocumentPaymentRequest;
import com.kaching123.tcr.model.payment.blackstone.prepaid.sunpass.request.SunReplenishmentRequest;
import com.kaching123.tcr.websvc.api.prepaid.BalanceResponse;
import com.kaching123.tcr.websvc.api.prepaid.Document;
import com.kaching123.tcr.websvc.api.prepaid.DocumentInquiryResponse;
import com.kaching123.tcr.websvc.api.prepaid.VectorDocument;

import java.math.BigDecimal;
import java.util.ArrayList;

/**
 * @author Ivan v. Rikhmayer
 *         This class is intended to
 */
@EFragment
public class SunpassPayYourDocumentAmountFragmentDialog extends StyledDialogFragment {

    private final String SELECT_PAYMENT_METHOD = "Payment method";
    private final String NONE_UNPAID_DOCUMENT = "None unpaid document";
    private final String DOLLAR_AMPERSAND = "$";
    private static final String DIALOG_NAME = "SunpassAmountFragmentDialog";
    private static FragmentActivity mContext;
    private final static BigDecimal FEE_AMOUNT = new BigDecimal("1.5");
    private final String DollarAmpsand = "$";
    private final int DEFAULT_ITEM_POSITION = 2;
    protected SunpassPayYourDocumentAmountFragmentDialogCallback callback;
    @FragmentArg
    protected String transactionMode;
    @FragmentArg
    protected String cashierId;
    @FragmentArg
    protected PrepaidUser user;
    @FragmentArg
    protected DocumentInquiryResponse response;
    @FragmentArg
    protected String accountNumber;

    @FragmentArg
    protected String licensePlateNumber;

    @ViewById
    protected TextView documentNumber;
    @ViewById
    protected TextView plateNumber;
    @ViewById
    protected TextView reviewOrderDetail;
    @ViewById
    protected TextView listviewItemName;
    @ViewById
    protected TextView listviewItemContent;
    @ViewById
    protected ListView listview;
    @ViewById
    protected Spinner biller;
    @ViewById
    protected LinearLayout linearlayout;
    @ViewById
    protected TextView amountTextview;
    @ViewById
    protected TextView feeTextview;
    @ViewById
    protected TextView totalTextview;
    @ViewById
    protected TextView error;
    @ViewById
    protected TextView errorContent;

    private ArrayList<String> list;
    private PayYourDocumentAmountAdapter billerAdapter;
    private String chosenBiller;
    @ColorRes(R.color.prepaid_dialog_white)
    protected int colorOk;
    @ColorRes(R.color.gray_dark)
    protected int colorDisabled;
    private final String SELECT_YOUR_PAYMENT_METHOD = "Selected your Payment Method";
    private final String PAY_ONLY_DOCUMENT_ENTERED = "Pay only the document entered";
    private final String PAY_ALL_OUTSTANDING_DOCUMENT = "Pay all outstanding Documents";
    private VectorDocument documents;
    private String amount;
    private PrepaidSunpassPayYourDocumentOrderViewAdapter adapter;
    private final String FAKE1 = "fake:51111222222";
    private final String FAKE2 = "fake:22488951548";

    public void setCallback(SunpassPayYourDocumentAmountFragmentDialogCallback callback) {
        this.callback = callback;
    }

    @AfterViews
    protected void init() {
        enableFinish(true);
        if (response != null) {
            setSpinner();
            setDocumentNumberView();
            setPlateNumberView();
        }

    }

    private void setLinearlayoutView(String chosenBiller) {
        if(setListView(chosenBiller)) {
            setAmountView();
            setFeeView();
            setTotalView();
        }
    }

    private void setViewVisible(View view)
    {
        view.setVisibility(View.VISIBLE);
    }
    private void setViewGone(View view)
    {
        view.setVisibility(View.GONE);
    }
    private boolean setListView(String chosenBiller) {
        documents = response.unpaidDocumentList;

        if(documents == null || documents.size() == 0)
        {
//            errorContent.setText(NONE_UNPAID_DOCUMENT);
//            setViewVisible(error);
//            setViewVisible(errorContent);
//            biller.setEnabled(false);
            getPositiveButton().setEnabled(false);
            getPositiveButton().setTextColor(colorDisabled);
            return false;
        }

        if (chosenBiller.equalsIgnoreCase(SELECT_YOUR_PAYMENT_METHOD))
            return false;
        // fake data for test purpose
//        documents = new VectorDocument();
//        Document doc = new Document();
//        doc.documentId = accountNumber;
//        doc.documentPaymentAmount = 50.0;
//
//        Document doc3 = new Document();
//        doc3.documentId = FAKE1;
//        doc3.documentPaymentAmount = 10.0;
//
//        Document doc2 = new Document();
//        doc2.documentId = FAKE2;
//        doc2.documentPaymentAmount = 15.0;
//
//        documents.add(doc);
//        documents.add(doc2);
//        documents.add(doc3);


        if (chosenBiller.equalsIgnoreCase(PAY_ONLY_DOCUMENT_ENTERED)) {

            for (int i = 0; i < documents.size(); i++) {
                if (String.valueOf(documents.get(i).documentId).equalsIgnoreCase(accountNumber)) {
                    Document temp = documents.get(i);
                    documents.clear();
                    documents.add(temp);
                    break;
                }
            }
        }
//        else {
//                documents = response.unpaidDocumentList;
//        }


        adapter = new PrepaidSunpassPayYourDocumentOrderViewAdapter(mContext, documents);
        listview.setAdapter(adapter);
        listview.setDivider(null);
        listview.setDividerHeight(0);
        return true;
    }

    private void setAmountView() {
        double dAmount = 0.0;
        for (int i = 0; i < documents.size(); i++) {
            dAmount += documents.get(i).documentPaymentAmount;
        }
        amount = String.valueOf(dAmount);
        amountTextview.setText(DOLLAR_AMPERSAND + amount.toString());
    }

    private void setFeeView() {
        if(isValid())
            feeTextview.setText(DOLLAR_AMPERSAND + (CharSequence) FEE_AMOUNT.toString());
        else
            feeTextview.setText(DOLLAR_AMPERSAND + "0");
    }

    private void setTotalView() {
        if(isValid())
            totalTextview.setText(DOLLAR_AMPERSAND + String.valueOf(Double.parseDouble(amount) + FEE_AMOUNT.doubleValue()));
        else
            totalTextview.setText(DOLLAR_AMPERSAND + "0");
    }

    private void setSpinner() {
        list = new ArrayList<String>();
        list.add(PAY_ONLY_DOCUMENT_ENTERED);
        list.add(PAY_ALL_OUTSTANDING_DOCUMENT);
        list.add(SELECT_YOUR_PAYMENT_METHOD);
        billerAdapter = new PayYourDocumentAmountAdapter(getActivity(), list);
        biller.setAdapter(billerAdapter);
        biller.setSelection(2);
        biller.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                setViewGone(error);
                setViewGone(errorContent);
                chosenBiller = (String) parent.getAdapter().getItem(position);
                if (position == 2)
                    return;
                linearlayout.setVisibility(View.VISIBLE);
                enableFinish(true);
                setLinearlayoutView(chosenBiller);
            }

            @Override
            public void onNothingSelected(AdapterView<?> ignore) {
            }
        });
    }

    private void setDocumentNumberView() {
        documentNumber.setText(this.accountNumber);
        documentNumber.setEnabled(false);
        documentNumber.clearFocus();
        documentNumber.setFocusable(false);
    }

    private void setPlateNumberView() {
        plateNumber.setText(licensePlateNumber);
        plateNumber.setEnabled(false);
        plateNumber.clearFocus();
        plateNumber.setFocusable(false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getNegativeButton().setTextColor(Color.WHITE);
        getNegativeButton().setTextSize(25);
        getNegativeButton().setTypeface(Typeface.DEFAULT_BOLD);
        getPositiveButton().setTypeface(Typeface.DEFAULT_BOLD);
        getPositiveButton().setTextSize(25);
        getDialog().getWindow().setLayout(
                getResources().getDimensionPixelOffset(R.dimen.prepaid_sunpass_pyd_amount_width),
                getResources().getDimensionPixelOffset(R.dimen.prepaid_dlg_heigth));
    }

    @Override
    protected int getTitleGravity() {
        return Gravity.LEFT;
    }

    ;

    @Override
    protected int getSeparatorColor() {
        return Color.WHITE;
    }

    @Override
    protected int getTitleTextColor() {
        return Color.WHITE;
    }

    @Override
    protected int getTitleViewBackgroundColor() {
        return getResources().getColor(R.color.prepaid_dialog_title_background_color);
    }

    @Override
    protected int getButtonsBackgroundColor() {
        return getResources().getColor(R.color.prepaid_dialog_buttons_background_color);
    }

    @Override
    protected int getTitleIcon() {
        return R.drawable.icon_sun_pass;
    }

    @Override
    protected int getNegativeButtonTitle() {
        return R.string.btn_back;
    }

    @Override
    protected int getPositiveButtonTitle() {
        return R.string.btn_sumbit;
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
        return R.layout.sunpass_pay_your_document_amount_fragment;
    }

    @Override
    protected int getDialogTitle() {
        return R.string.prepaid_dialog_sunpass_pay_your_document_title;
    }

    @Override
    protected OnDialogClickListener getPositiveButtonListener() {
        return new OnDialogClickListener() {
            @Override
            public boolean onClick() {
                if(isValid()) {
                    complete();
                }
                else
                {
                    setViewVisible(error);
                    setViewVisible(errorContent);
                }
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
    private boolean isValid(){

        if(chosenBiller.equalsIgnoreCase(SELECT_YOUR_PAYMENT_METHOD)) {
            errorContent.setText(SELECT_PAYMENT_METHOD);
            return false;
        }
        if(documents.size() == 0)
        {
            errorContent.setText(NONE_UNPAID_DOCUMENT);
            setViewVisible(error);
            setViewVisible(errorContent);
            biller.setEnabled(false);
            return false;
        }
        return true;
    }
    protected boolean complete() {
        SunPassDocumentPaymentRequest request = new SunPassDocumentPaymentRequest();
        request.mID = String.valueOf(this.user.getMid());
        request.tID = String.valueOf(this.user.getTid());
        request.password = this.user.getPassword();
        request.cashier = this.cashierId;
        request.accountNumber = this.accountNumber;
        request.purchaseId = request.accountNumber;
        request.amount = new BigDecimal(amount);
        request.feeAmount = FEE_AMOUNT.doubleValue();
        request.purchaseId = response == null ? "0" : this.response.purchaseId;
        request.paidDocuments = documents;
//        request.purchaseId = this.accountNumber;
        request.transactionMode = this.transactionMode;
        request.licensePateleNumber = this.licensePlateNumber;
        callback.onComplete(request, FEE_AMOUNT);
        return true;
    }


    public static void show(FragmentActivity context, String accountNumber, String transactionMode,
                            String cashierId, PrepaidUser user, DocumentInquiryResponse response, String licensePlateNumber, SunpassPayYourDocumentAmountFragmentDialogCallback listener) {
        mContext = context;
        SunpassPayYourDocumentAmountFragmentDialog dialog = SunpassPayYourDocumentAmountFragmentDialog_.builder()
                .transactionMode(transactionMode)
                .cashierId(cashierId)
                .user(user)
                .response(response)
                .accountNumber(accountNumber)
                .licensePlateNumber(licensePlateNumber)
                .build();
        dialog.setCallback(listener);
        DialogUtil.show(context,
                DIALOG_NAME,
                dialog);
    }

    public static void hide(FragmentActivity activity) {
        DialogUtil.hide(activity, DIALOG_NAME);
    }

    protected void enableFinish(Boolean enabled) {
        getPositiveButton().setEnabled(enabled);
        getPositiveButton().setTextColor(colorOk);
//        getPositiveButton().setTextColor(enabled ? colorOk : colorDisabled);
    }

    public interface SunpassPayYourDocumentAmountFragmentDialogCallback {

        public abstract void onCancel();

        public abstract void onError(String message);

        public abstract void onComplete(SunPassDocumentPaymentRequest response, BigDecimal transcationFee);
    }

}
