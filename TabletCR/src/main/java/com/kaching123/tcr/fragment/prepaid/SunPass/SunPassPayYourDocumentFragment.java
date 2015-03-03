package com.kaching123.tcr.fragment.prepaid.SunPass;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;
import com.kaching123.tcr.R;
import com.kaching123.tcr.activity.PrepaidActivity.PrepaidSunPassActivity;
import com.kaching123.tcr.adapter.PrepaidSunpassPayYourDocumentOrderViewAdapter;
import com.kaching123.tcr.commands.payment.blackstone.prepaid.sunpass.GetSunPassDocumentInquiryCommand;
import com.kaching123.tcr.fragment.dialog.WaitDialogFragment;
import com.kaching123.tcr.model.payment.blackstone.prepaid.PrepaidUser;
import com.kaching123.tcr.model.payment.blackstone.prepaid.sunpass.request.SunPassDocumentInquiryRequest;
import com.kaching123.tcr.model.payment.blackstone.prepaid.sunpass.request.SunPassDocumentPaymentRequest;
import com.kaching123.tcr.processor.PrepaidProcessor;
import com.kaching123.tcr.websvc.api.prepaid.Document;
import com.kaching123.tcr.websvc.api.prepaid.DocumentInquiryResponse;
import com.kaching123.tcr.websvc.api.prepaid.VectorDocument;
import com.telly.groundy.annotations.OnFailure;
import com.telly.groundy.annotations.OnSuccess;
import com.telly.groundy.annotations.Param;

import java.math.BigDecimal;

import static com.kaching123.tcr.print.FormatterUtil.commaPriceFormat;

/**
 * Created by teli.yin on 12/8/2014.
 */
@EFragment
public class SunPassPayYourDocumentFragment extends Fragment implements PrepaidSunPassActivity.PrepaidSunPassInterface {
    private PayYourDocumentCreditCallBack payYourDocumentCreditCallBack;
    @FragmentArg
    protected PrepaidUser user;
    @FragmentArg
    protected String transactionMode;
    @FragmentArg
    protected String cashierId;
    @FragmentArg
    protected long transactionId;
    @ViewById
    protected TextView check, payCurrentDocument, payAllDocument, accountNumExplain, licensePlateExplain, reviewOrderDetail, documentIdColumn, amountDueColumn, totalLinearAmountContent, totalLinearFeeContent, totalLinearTotalContent;
    private final static BigDecimal FEE_AMOUNT = new BigDecimal("1.5");
    @ViewById
    protected EditText documentId, licensePlate;
    @ViewById
    protected ListView listview;
    @ViewById
    protected LinearLayout cost;
    private DocumentInquiryResponse response;
    private VectorDocument documents;
    private PrepaidSunpassPayYourDocumentOrderViewAdapter adapter;
    private String documentIdStr;
    private boolean currentDocumentPayMethod;
    private BigDecimal mAmount;
    private BigDecimal mTotal;
    private String licensePlateStr;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.sunpass_pay_your_document_fragment_new, container, false);
    }

    @AfterViews
    protected void init() {
        payYourDocumentCreditCallBack.headMessage(PrepaidSunPassHeadFragment.ENTER_DOCUMENT_ID_AND_LICENSE_PLATE_NUMBER);
    }
    @Override
    public void onResume() {
        super.onResume();
        payYourDocumentCreditCallBack.headMessage(PrepaidSunPassHeadFragment.ENTER_DOCUMENT_ID_AND_LICENSE_PLATE_NUMBER);
    }
    private void visibleDocumentInfo(DocumentInquiryResponse result) {
        documentIdStr = documentId.getText().toString().trim();
        licensePlateStr = licensePlate.getText().toString().trim();
        disableInput();
        showPaymentMethod();
        response = result;
    }


    private void hideExplanations() {
        accountNumExplain.setVisibility(View.GONE);
        licensePlateExplain.setVisibility(View.GONE);
    }

    private void showPaymentMethod() {
        payAllDocument.setVisibility(View.VISIBLE);
        payCurrentDocument.setVisibility(View.VISIBLE);
    }

    private void disableInput() {
        documentId.setEnabled(false);
        licensePlate.setEnabled(false);
    }

    public void setCallBack(PayYourDocumentCreditCallBack callBack) {
        this.payYourDocumentCreditCallBack = callBack;
    }

    @Click
    protected void payCurrentDocument() {
        currentDocumentPayMethod = true;
        hideExplanations();
        payAllDocument.setBackgroundResource(R.drawable.amount_button_background_normal);
        payAllDocument.setTextColor(getResources().getColor(R.color.prepaid_blue_divider));
        payAllDocument.setPadding(20, 20, 20, 20);
        payCurrentDocument.setBackgroundResource(R.drawable.amount_button_background_pressed);
        payCurrentDocument.setTextColor(getResources().getColor(R.color.prepaid_dialog_white));
        payCurrentDocument.setPadding(20, 20, 20, 20);
        setListView();
    }

    @Click
    protected void payAllDocument() {
        currentDocumentPayMethod = false;
        hideExplanations();
        payCurrentDocument.setBackgroundResource(R.drawable.amount_button_background_normal);
        payCurrentDocument.setTextColor(getResources().getColor(R.color.prepaid_blue_divider));
        payCurrentDocument.setPadding(20, 20, 20, 20);
        payAllDocument.setBackgroundResource(R.drawable.amount_button_background_pressed);
        payAllDocument.setTextColor(getResources().getColor(R.color.prepaid_dialog_white));
        payAllDocument.setPadding(20, 20, 20, 20);
        setListView();
    }

    private void showDocumentDetails() {
        reviewOrderDetail.setVisibility(View.VISIBLE);
        documentIdColumn.setVisibility(View.VISIBLE);
        amountDueColumn.setVisibility(View.VISIBLE);
        cost.setVisibility(View.VISIBLE);
        listview.setVisibility(View.VISIBLE);
    }

    private void showCosts() {
        mAmount = getAmount();
        mTotal = getTotal();
        totalLinearAmountContent.setText(commaPriceFormat(mAmount));
        totalLinearFeeContent.setText(commaPriceFormat(FEE_AMOUNT));
        totalLinearTotalContent.setText(commaPriceFormat(mTotal));
    }

    private BigDecimal getAmount() {
        double dAmount = 0.0;
        for (int i = 0; i < documents.size(); i++) {
            dAmount += documents.get(i).documentPaymentAmount;
        }
        BigDecimal amount = new BigDecimal(dAmount);
        return amount;
    }


    private BigDecimal getTotal() {
        return getAmount().add(FEE_AMOUNT);
    }

    private boolean setListView() {

        documents = response.unpaidDocumentList;

        if (documents == null || documents.size() == 0) {
            payYourDocumentCreditCallBack.onError("No Documents found");
            return false;
        }



        VectorDocument mDocument = null;
        if (currentDocumentPayMethod) {
            mDocument = new VectorDocument();
            for (int i = 0; i < documents.size(); i++) {
                if (String.valueOf(documents.get(i).documentId).toLowerCase().equalsIgnoreCase(documentIdStr)) {
                    Document temp = documents.get(i);
                    mDocument.add(temp);
                    break;
                }
            }
        }
        documents = mDocument == null ? documents : mDocument;
//        else {
//                documents = response.unpaidDocumentList;
//        }
        showDocumentDetails();
        showCosts();
        adapter = new PrepaidSunpassPayYourDocumentOrderViewAdapter(getActivity(), documents);
        listview.setAdapter(adapter);
        listview.setDivider(null);
        listview.setDividerHeight(0);
        return true;
    }

    @Click
    protected void check() {
        if (payCurrentDocument.getVisibility() == View.VISIBLE)
            getBill();
        else
            getDocumentInfo();
    }

    private void getBill() {
        SunPassDocumentPaymentRequest request = new SunPassDocumentPaymentRequest();
        request.mID = String.valueOf(this.user.getMid());
        request.tID = String.valueOf(this.user.getTid());
        request.password = this.user.getPassword();
        request.cashier = this.cashierId;
        request.accountNumber = documentIdStr;
        request.licensePateleNumber = licensePlateStr;
        request.amount = mAmount;
        request.feeAmount = FEE_AMOUNT.doubleValue();
        request.purchaseId = response == null ? "0" : this.response.purchaseId;
        request.paidDocuments = documents;
//        request.purchaseId = this.accountNumber;
        request.transactionMode = this.transactionMode;
        payYourDocumentCreditCallBack.onComplete(request, response, FEE_AMOUNT);
    }

    public void getDocumentInfo() {
        if (!isCreditInput())
            return;
        WaitDialogFragment.show(getActivity(), "Loading..");
        SunPassDocumentInquiryRequest request = new SunPassDocumentInquiryRequest();
        request.mID = String.valueOf(user.getMid());
        request.tID = String.valueOf(user.getTid());
        request.password = user.getPassword();
        request.cashier = cashierId;
        request.accountNumber = documentId.getText().toString();
        request.transactionId = PrepaidProcessor.generateId();
        request.transactionMode = transactionMode;
        request.licensePlateNumber = licensePlate.getText().toString();
        GetSunPassDocumentInquiryCommand.start(getActivity(), this, request);
    }

    private boolean isCreditInput() {
        if (documentId.getText() == null || documentId.getText().toString().equalsIgnoreCase("")) {
            payYourDocumentCreditCallBack.headMessage(PrepaidSunPassHeadFragment.DOCUMENT_ID_NULL);
            return false;
        }
        if (licensePlate.getText() == null || licensePlate.getText().toString().equalsIgnoreCase("")) {
            payYourDocumentCreditCallBack.headMessage(PrepaidSunPassHeadFragment.LICENSE_PLATE_NUMBER_NULL);
            return false;
        }
        return true;

    }

    @Override
    public void onBackPressed() {
        payYourDocumentCreditCallBack.onBackButtonPressed();
        payYourDocumentCreditCallBack.headMessage(PrepaidSunPassHeadFragment.SELECT_SUNPASS_CATEGORY);
    }

    @OnSuccess(GetSunPassDocumentInquiryCommand.class)
    public void onGetSunPassDocumentInquiryCommandSuccess(@Param(GetSunPassDocumentInquiryCommand.ARG_RESULT) DocumentInquiryResponse result) {
        WaitDialogFragment.hide(getActivity());
        visibleDocumentInfo(result);
    }

    @OnFailure(GetSunPassDocumentInquiryCommand.class)
    public void onGetSunPassDocumentInquiryCommandFail(@Param(GetSunPassDocumentInquiryCommand.ARG_RESULT) DocumentInquiryResponse result) {
        WaitDialogFragment.hide(getActivity());
        payYourDocumentCreditCallBack.onError(result.responseDescription);

    }

    public interface PayYourDocumentCreditCallBack {
        void onComplete(final SunPassDocumentPaymentRequest request, DocumentInquiryResponse result, BigDecimal transcationFee);

        void headMessage(int mode);

        void onBackButtonPressed();

        void onError(String message);
    }
}
