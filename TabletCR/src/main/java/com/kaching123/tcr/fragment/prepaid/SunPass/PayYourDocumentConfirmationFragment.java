package com.kaching123.tcr.fragment.prepaid.SunPass;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;
import com.kaching123.tcr.R;
import com.kaching123.tcr.activity.PrepaidActivity.PrepaidSunPassActivity;
import com.kaching123.tcr.adapter.SunPassTransponderConfirmationAdapter;
import com.kaching123.tcr.fragment.tendering.pinserve.prepaid.wireless.PrepaidPaymentCompleteReceiptListViewModel;
import com.kaching123.tcr.model.payment.blackstone.prepaid.sunpass.request.SunPassDocumentPaymentRequest;
import com.kaching123.tcr.websvc.api.prepaid.DocumentInquiryResponse;

import java.math.BigDecimal;

/**
 * Created by teli.yin on 12/9/2014.
 */
@EFragment
public class PayYourDocumentConfirmationFragment extends Fragment implements PrepaidSunPassActivity.PrepaidSunPassInterface {

    @FragmentArg
    protected SunPassDocumentPaymentRequest request;
    @FragmentArg
    protected DocumentInquiryResponse response;
    @FragmentArg
    protected BigDecimal Fee;
    private SunPassTransponderConfirmationAdapter adapter;
    @ViewById
    protected ListView listView;
    private PayYourDocumentConfirmationCallBack callBack;

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
        String[] names = PrepaidPaymentCompleteReceiptListViewModel.getListForPYDConfirmationItemNames();
        String[] contents = PrepaidPaymentCompleteReceiptListViewModel.getPYDConfiramtionListForContents(request, response, Fee.toString());
        adapter = new SunPassTransponderConfirmationAdapter(getActivity(), names, contents);
        listView.setAdapter(adapter);
        listView.setDivider(null);
        listView.setDividerHeight(0);
    }

    public void setCallBack(PayYourDocumentConfirmationCallBack callBack) {
        this.callBack = callBack;
    }

    @Click
    protected void check() {
        complete();
    }

    protected boolean complete() {
        callBack.onComplete(request, response);
        return true;
    }

    public interface PayYourDocumentConfirmationCallBack {
        void onComplete(SunPassDocumentPaymentRequest request, DocumentInquiryResponse response);

        void onBackButtonPressed();

        void headMeassage(int mode);
    }

    @Override
    public void onBackPressed() {
        callBack.onBackButtonPressed();
        callBack.headMeassage(PrepaidSunPassHeadFragment.SELECT_TRANSPONDER_NUMBER);
    }
}
