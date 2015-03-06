package com.kaching123.tcr.fragment.prepaid.SunPass;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;
import com.kaching123.tcr.R;
import com.kaching123.tcr.activity.PrepaidActivity.PrepaidSunPassActivity;
import com.kaching123.tcr.adapter.SunPassTransponderConfirmationAdapter;
import com.kaching123.tcr.fragment.tendering.pinserve.prepaid.wireless.PrepaidPaymentCompleteReceiptListViewModel;
import com.kaching123.tcr.model.payment.blackstone.prepaid.PrepaidUser;
import com.kaching123.tcr.model.payment.blackstone.prepaid.sunpass.request.SunReplenishmentRequest;
import com.kaching123.tcr.websvc.api.prepaid.BalanceResponse;

import java.math.BigDecimal;

/**
 * Created by teli.yin on 12/5/2014.
 */
@EFragment
public class SunPassTransponderConfirmationFragment extends Fragment implements PrepaidSunPassActivity.PrepaidSunPassInterface {
    @FragmentArg
    protected String transactionMode;
    @FragmentArg
    protected String cashierId;
    @FragmentArg
    protected PrepaidUser user;
    @FragmentArg
    protected BalanceResponse response;
    @FragmentArg
    protected String accountNumber;
    @FragmentArg
    protected BigDecimal amount;
    @FragmentArg
    protected BigDecimal FEE_AMOUNT;

    @ViewById
    protected TextView check;

    @ViewById
    protected ListView listView;
    private SunPassTransponderConfirmationAdapter adapter;
    private SunPassTransponderConfirmationCallBack callBack;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.sunpass_confirmation_page_new, container, false);
    }
    @Override
    public void onResume() {
        super.onResume();
        callBack.headMeassage(PrepaidSunPassHeadFragment.PURCHASE_SUMMARY);
    }
    @AfterViews
    protected void init() {
        callBack.headMeassage(PrepaidSunPassHeadFragment.PURCHASE_SUMMARY);
        listView.setVisibility(View.VISIBLE);
        String[] names = PrepaidPaymentCompleteReceiptListViewModel.getListForConfirmationItemNames();
        String[] contents = PrepaidPaymentCompleteReceiptListViewModel.getConfiramtionListForContents(accountNumber, response, amount.toString(), FEE_AMOUNT.toString());
        PrepaidPaymentCompleteReceiptListViewModel.trace(names, contents);
        adapter = new SunPassTransponderConfirmationAdapter(getActivity(), names, contents);
        listView.setAdapter(adapter);
        listView.setDivider(null);
        listView.setDividerHeight(0);

    }

    @Click
    protected void check() {
        complete();
    }

    protected boolean complete() {
        SunReplenishmentRequest request = new SunReplenishmentRequest();
        request.mID = String.valueOf(this.user.getMid());
        request.tID = String.valueOf(this.user.getTid());
        request.password = this.user.getPassword();
        request.cashier = this.cashierId;
        request.accountNumber = this.accountNumber;
        request.purchaseId = request.accountNumber;
        request.amount = amount;
        request.feeAmount = FEE_AMOUNT.doubleValue();
        request.purchaseId = response == null ? "0" : this.response.purchaseId;
//        request.purchaseId = this.accountNumber;
        request.transactionMode = this.transactionMode;
        callBack.onComplete(request, response);
        return true;
    }

    public void setCallBack(SunPassTransponderConfirmationCallBack callBack) {
        this.callBack = callBack;
    }

    @Override
    public void onBackPressed() {
        callBack.onBackButtonPressed();
        callBack.headMeassage(PrepaidSunPassHeadFragment.ENTER_DOCUMENT_ID_AND_LICENSE_PLATE_NUMBER);
    }

    public interface SunPassTransponderConfirmationCallBack {
        void onComplete(SunReplenishmentRequest request, BalanceResponse response);

        void headMeassage(int mode);

        void onBackButtonPressed();
    }

}
