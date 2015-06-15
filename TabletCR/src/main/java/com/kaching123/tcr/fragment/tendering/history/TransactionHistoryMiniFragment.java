package com.kaching123.tcr.fragment.tendering.history;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import com.kaching123.tcr.Logger;
import com.kaching123.tcr.R;
import com.kaching123.tcr.function.ReadPaymentTransactionsFunction;
import com.kaching123.tcr.model.PaymentTransactionModel;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Ivan v. Rikhmayer
 */
@EFragment
public class TransactionHistoryMiniFragment extends ListFragment implements LoaderCallbacks<ArrayList<PaymentTransactionModel>> {

//    private static final long TIMESPAN = 14 * 24 * 60 * 60 * 1000;

    private List<ITransactionHistoryMiniFragmentLoader> loaderCallback = new ArrayList<ITransactionHistoryMiniFragmentLoader>();

    @Bean
    protected TransactionHistoryMiniAdapter adapter;
    private String guid;
    protected ArrayList<PaymentTransactionModel> fakeTransactions = new ArrayList<PaymentTransactionModel>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.tendering_history_payment_transaction_mini_fragment, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setListAdapter(adapter);
        getLoaderManager().restartLoader(0, null, this);
    }

    @Override
    public Loader<ArrayList<PaymentTransactionModel>> onCreateLoader(int loaderId, Bundle args) {
        Logger.d("onCreateLoader");
        return ReadPaymentTransactionsFunction.createLoaderOnlySaleOrderByAmount(getActivity(), guid);
    }

    @Override
    public void onLoadFinished(Loader<ArrayList<PaymentTransactionModel>> listLoader, ArrayList<PaymentTransactionModel> saleOrderModels) {
        saleOrderModels.addAll(fakeTransactions);
        adapter.changeCursor(saleOrderModels);
        for (ITransactionHistoryMiniFragmentLoader loader : loaderCallback) {
            loader.onLoadComplete(saleOrderModels);
        }
        for (PaymentTransactionModel model : saleOrderModels) {
            Logger.d(model.toDebugString());
        }
    }

    @Override
    public void onLoaderReset(Loader<ArrayList<PaymentTransactionModel>> listLoader) {
        Logger.d("onLoaderReset");
        if (getActivity() == null)
            return;
        adapter.changeCursor(null);
    }

    public TransactionHistoryMiniFragment init(String guid) {
        this.guid = guid;
        if (guid == null) {
            adapter.changeCursor(null);
            if (getActivity() != null) {
                getLoaderManager().destroyLoader(0);
            }
            return this;
        }
        return this;
    }

    public TransactionHistoryMiniFragment addListener(ITransactionHistoryMiniFragmentLoader callback) {
        loaderCallback.add(callback);
        return this;
    }

    public TransactionHistoryMiniFragment injectFakeTransactions(ArrayList<PaymentTransactionModel> transactions) {
        if(transactions != null) {
            fakeTransactions.addAll(transactions);
        }
        return this;
    }

    public interface ITransactionHistoryMiniFragmentLoader {

        void onLoadComplete(List<PaymentTransactionModel> saleOrderModels);
    }
}
