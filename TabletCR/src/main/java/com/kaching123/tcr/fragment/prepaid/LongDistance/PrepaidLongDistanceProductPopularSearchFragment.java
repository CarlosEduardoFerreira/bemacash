package com.kaching123.tcr.fragment.prepaid.LongDistance;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.Click;
import com.googlecode.androidannotations.annotations.EFragment;
import com.googlecode.androidannotations.annotations.FragmentArg;
import com.googlecode.androidannotations.annotations.UiThread;
import com.googlecode.androidannotations.annotations.ViewById;
import com.kaching123.tcr.Logger;
import com.kaching123.tcr.R;
import com.kaching123.tcr.TcrApplication;
import com.kaching123.tcr.activity.PrepaidActivity.PrepaidLongDistanceActivity;
import com.kaching123.tcr.commands.payment.blackstone.prepaid.bill.GetBillerCategoriesCommand;
import com.kaching123.tcr.commands.payment.blackstone.prepaid.bill.GetMasterBillersByCategoryCommand;
import com.kaching123.tcr.commands.payment.blackstone.prepaid.update.UpdateBillPaymentCommand;
import com.kaching123.tcr.fragment.dialog.WaitDialogFragment;
import com.kaching123.tcr.fragment.prepaid.PrepaidHomeFragment;
import com.kaching123.tcr.model.payment.blackstone.prepaid.PrepaidUser;
import com.kaching123.tcr.model.payment.blackstone.prepaid.pinserve.request.GetBillerCategoriesRequest;
import com.kaching123.tcr.model.payment.blackstone.prepaid.pinserve.request.GetMasterBillersByCategoryRequest;
import com.kaching123.tcr.model.payment.blackstone.prepaid.wireless.WirelessItem;
import com.kaching123.tcr.model.payment.blackstone.prepaid.wireless.request.BillPaymentItem;
import com.kaching123.tcr.processor.PrepaidProcessor;
import com.kaching123.tcr.websvc.api.prepaid.BillerCategoriesResponse;
import com.kaching123.tcr.websvc.api.prepaid.Category;
import com.kaching123.tcr.websvc.api.prepaid.MasterBiller;
import com.kaching123.tcr.websvc.api.prepaid.MasterBillersByCategoryResponse;
import com.kaching123.tcr.websvc.api.prepaid.VectorCategory;
import com.telly.groundy.annotations.OnFailure;
import com.telly.groundy.annotations.OnSuccess;
import com.telly.groundy.annotations.Param;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by teli.yin on 10/29/2014.
 */
@EFragment
public class PrepaidLongDistanceProductPopularSearchFragment extends PrepaidLongDistanceBaseBodyFragment implements PrepaidLongDistanceActivity.PrepaidDistanceBackInterface {

    @ViewById
    protected TextView countrySearchButton;

    @ViewById
    protected TextView viewAllButton;

    @FragmentArg
    protected int prepaidMode;
    @FragmentArg
    protected String transactionMode;
    @FragmentArg
    protected String cashierId;
    @FragmentArg
    protected PrepaidUser user;

    private List<Category> categories;

    private Category currentCategory;
    private ArrayList<BillPaymentItem> billPaymentItems;

    private PrepaidLongDistancePopularGridViewFragment popularGridView;

    PrepaidLongDistanceProductAllSearchFragment.LongDistanceSearchCallback callback;

    private final ProductGridViewCallback productGridViewCallback = new ProductGridViewCallback();

    private int categoryCount;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.prepaid_long_distance_product_popular_search_fragment, container, false);
    }

    @AfterViews
    public void init() {
        updateUI();
        billPaymentItems = new ArrayList<BillPaymentItem>();
        if (prepaidMode != PrepaidHomeFragment.BILLPAYMENT) {
            popularGridView = PrepaidLongDistancePopularGridViewFragment_.builder().prepaidMode(prepaidMode).build();
            popularGridView.setCallback(productGridViewCallback);
            getChildFragmentManager().beginTransaction().add(R.id.popular_grid_view, popularGridView).commit();
        } else {
            if (TcrApplication.get().getNeedBillPaymentUpdated()) {
                WaitDialogFragment.show(getActivity(), getString(R.string.loading_message));
                getBillerCategories(getActivity());
            } else
                billPayment2PopularGridFragment();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (prepaidMode == PrepaidHomeFragment.BILLPAYMENT)
            callback.headMessage(PrepaidLongDistanceHeadFragment.SELECT_CATEGORY_OR_MOST_POPULAR_BILLER);

    }

    private void updateUI() {
        if (prepaidMode == PrepaidHomeFragment.LONGDISTANCE)
            countrySearchButton.setText(getString(R.string.country_search));
        if (prepaidMode == PrepaidHomeFragment.PINLESS)
            countrySearchButton.setText(getString(R.string.carrier_search));
        if (prepaidMode == PrepaidHomeFragment.WIRELESS)
            countrySearchButton.setText(getString(R.string.carrier_search));
        if (prepaidMode == PrepaidHomeFragment.BILLPAYMENT)
            countrySearchButton.setText(getString(R.string.category_search));
    }

    public void setCallback(PrepaidLongDistanceProductAllSearchFragment.LongDistanceSearchCallback callback) {
        this.callback = callback;
    }

    @Click
    void countrySearchButton() {
        callback.searchModeSelected(PrepaidLongDistanceActivity.COUNTRY_SEARCH);
    }

    @Click
    void viewAllButton() {
        callback.searchModeSelected(PrepaidLongDistanceActivity.ALL_SEARCH);
    }

    @Override
    public void onBackPressed() {
        callback.popUpFragment();
    }


    class ProductGridViewCallback implements PrepaidLongDistanceProductGridViewFragment.ProductFridViewInterface {

        @Override
        public void productSelected(WirelessItem item, int searchMode) {
            callback.productSelected(item, searchMode);
        }

        @Override
        public void billPaymentItemSelected(BillPaymentItem billPaymentItem, int searchMode) {
            callback.onBillPaymentItemSelected(billPaymentItem, searchMode);
        }
    }

    public void getBillerCategories(final FragmentActivity context) {
        GetBillerCategoriesRequest request = new GetBillerCategoriesRequest();
        request.amount = BigDecimal.ZERO;
        request.transactionId = PrepaidProcessor.generateId();
        request.TransactionMode = transactionMode;
        request.Cashier = cashierId;
        request.MID = String.valueOf(user.getMid());
        request.TID = String.valueOf(user.getTid());
        request.Password = String.valueOf(user.getPassword());
        GetBillerCategoriesCommand.start(context, this, request);
    }

    protected void startUpdateBillPaymentCommand() {
        UpdateBillPaymentCommand.start(getActivity(), this, billPaymentItems);
    }

    protected void billPayment2PopularGridFragment() {
        popularGridView = PrepaidLongDistancePopularGridViewFragment_.builder().prepaidMode(prepaidMode).build();
        popularGridView.setCallback(productGridViewCallback);
        getChildFragmentManager().beginTransaction().add(R.id.popular_grid_view, popularGridView).commit();

    }

    @UiThread
    protected void doGetMasterBillers(FragmentActivity context, String caretodyId) {
        getMasterBillers(context, caretodyId);
    }

    @OnSuccess(GetBillerCategoriesCommand.class)
    public void onGetBillerCategoriesCommandSuccess(@Param(GetBillerCategoriesCommand.ARG_RESULT) BillerCategoriesResponse result) {
        VectorCategory temp = result.categories;
        categories = new ArrayList<Category>(temp.size());
        categories.addAll(temp);
        assert categories != null && !categories.isEmpty();
        doGetMasterBillers(getActivity(), categories.get(categoryCount).id);

    }


    @OnFailure(GetBillerCategoriesCommand.class)
    public void onGetBillerCategoriesCommandFail(@Param(GetBillerCategoriesCommand.ARG_RESULT) BillerCategoriesResponse result) {
        WaitDialogFragment.hide(getActivity());
        callback.popUpFragment();
        Toast.makeText(getActivity(), "Cannot get Billpayment categories", Toast.LENGTH_LONG);
    }

    public void getMasterBillers(final FragmentActivity context, String caretodyId) {
        GetMasterBillersByCategoryRequest request = new GetMasterBillersByCategoryRequest();
        request.amount = BigDecimal.ZERO;
        request.transactionId = PrepaidProcessor.generateId();
        request.TransactionMode = transactionMode;
        request.Cashier = cashierId;
        request.CaregoryId = caretodyId;
        request.MID = String.valueOf(user.getMid());
        request.TID = String.valueOf(user.getTid());
        request.Password = String.valueOf(user.getPassword());
        currentCategory = categories.get(categoryCount);
        GetMasterBillersByCategoryCommand.start(context, new GetMasterBillersByCategoryCommand.MasterBillerCategoryCommand() {
            @Override
            protected void onSuccess(MasterBillersByCategoryResponse result) {
                ArrayList<MasterBiller> currentInputs = new ArrayList<MasterBiller>();
                currentInputs.addAll(result.masterBillers);
                for (MasterBiller masterBiller : currentInputs) {
                    billPaymentItems.add(new BillPaymentItem(currentCategory.id, currentCategory.description, masterBiller.id, masterBiller.description));
                }

                if (++categoryCount < categories.size()) {
                    doGetMasterBillers(getActivity(), categories.get(categoryCount).id);
                } else {
                    WaitDialogFragment.hide(getActivity());
                    startUpdateBillPaymentCommand();
                }
            }

            @Override
            protected void onFailure(MasterBillersByCategoryResponse result) {
                WaitDialogFragment.hide(getActivity());
                callback.error(result.resultDescription.toString());
            }
        }, request);
    }


//    @OnSuccess(GetMasterBillersByCategoryCommand.class)
//    public void onGetMasterBillersByCategoryCommandSuccess(@Param(GetMasterBillersByCategoryCommand.ARG_RESULT) MasterBillersByCategoryResponse result) {
//
//        Logger.d("trace billpayment--- GetMasterBillersByCategoryCommand success " + result.resultDescription + " " + result.masterBillers.size());
//        ArrayList<MasterBiller> currentInputs = new ArrayList<MasterBiller>();
//        currentInputs.addAll(result.masterBillers);
//        for (MasterBiller masterBiller : currentInputs) {
//            billPaymentItems.add(new BillPaymentItem(currentCategory.id, currentCategory.description, masterBiller.id, masterBiller.description));
//        }
//
//        if (++categoryCount < categories.size()) {
//            doGetMasterBillers(getActivity(), categories.get(categoryCount).id);
//        } else {
//            WaitDialogFragment.hide(getActivity());
//            startUpdateBillPaymentCommand();
//        }
//    }
//
//    @OnFailure(GetMasterBillersByCategoryCommand.class)
//    public void onGetMasterBillersByCategoryCommandFail(@Param(GetMasterBillersByCategoryCommand.ARG_RESULT) MasterBillersByCategoryResponse result) {
//        WaitDialogFragment.hide(getActivity());
//        callback.error(result.resultDescription.toString());
//    }


    @OnSuccess(UpdateBillPaymentCommand.class)
    public void onUpdateBillPaymentCommandSuccess() {
        WaitDialogFragment.hide(getActivity());
        billPayment2PopularGridFragment();
        TcrApplication.get().setNeedBillPaymentUpdated(false);
    }


    @OnFailure(UpdateBillPaymentCommand.class)
    public void onUpdateBillPaymentCommandFail() {
        WaitDialogFragment.hide(getActivity());
    }
}
