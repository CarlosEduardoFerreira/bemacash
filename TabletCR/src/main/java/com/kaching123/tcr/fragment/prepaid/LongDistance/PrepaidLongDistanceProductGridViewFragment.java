package com.kaching123.tcr.fragment.prepaid.LongDistance;


import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.getbase.android.db.loaders.CursorLoaderBuilder;
import com.google.common.base.Function;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import com.kaching123.tcr.Logger;
import com.kaching123.tcr.R;
import com.kaching123.tcr.activity.PrepaidActivity.PrepaidLongDistanceActivity;
import com.kaching123.tcr.fragment.dialog.WaitDialogFragment;
import com.kaching123.tcr.fragment.prepaid.PrepaidHomeFragment;
import com.kaching123.tcr.fragment.prepaid.utilities.ProductItemsPageAdapter;
import com.kaching123.tcr.model.payment.blackstone.prepaid.wireless.WirelessItem;
import com.kaching123.tcr.model.payment.blackstone.prepaid.wireless.request.BillPaymentItem;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore;
import com.viewpagerindicator.LinePageIndicator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by teli.yin on 11/3/2014.
 */
@EFragment
public class PrepaidLongDistanceProductGridViewFragment extends PrepaidLongDistanceBaseBodyFragment implements LoaderManager.LoaderCallbacks<List<WirelessItem>> {
    private static final int DEFAULT_LOADER = 0;
    private static final Uri URI_ORDER_ITEMS = ShopProvider.getContentUri(ShopStore.WirelessTable.URI_CONTENT);
    private static final Uri URI_BILLPAYMENT_ITEMS = ShopProvider.getContentUri(ShopStore.BillPayment.URI_CONTENT);
    @ViewById
    protected ViewPager viewPager;
    @ViewById
    protected LinePageIndicator viewPagerIndicator;
    @FragmentArg
    protected int prepaidMode;

    private ProductItemsPageAdapter productListViewAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.prepaid_long_distance_product_grid_view_fragment, container, false);
    }

    @AfterViews
    public void init() {
        productListViewAdapter = new ProductItemsPageAdapter(getActivity(), globalItemClickListener, prepaidMode);
        viewPager.setAdapter(productListViewAdapter);
        viewPagerIndicator.setViewPager(viewPager);
        if (prepaidMode == PrepaidHomeFragment.BILLPAYMENT) {
//            ArrayList<MasterBiller> values = new ArrayList<MasterBiller>(vectorMasterBiller.length);
//            for (MasterBiller biller : vectorMasterBiller)
//                values.add(biller);
//            Collections.sort(values, new Comparator<MasterBiller>() {
//
//                @Override
//                public int compare(MasterBiller lhs, MasterBiller rhs) {
//                    return lhs.id.compareToIgnoreCase(rhs.id);
//                }
//            });
//            productListViewAdapter.setList2(values);
            getLoaderManager().restartLoader(DEFAULT_LOADER, null, new BillPaymentItemsLoader());
        } else {

            getLoaderManager().restartLoader(DEFAULT_LOADER, null, this);
        }
    }

    private class BillPaymentItemsLoader implements LoaderManager.LoaderCallbacks<List<BillPaymentItem>> {

        @Override
        public Loader<List<BillPaymentItem>> onCreateLoader(int id, Bundle args) {

            show();
            CursorLoaderBuilder loader = CursorLoaderBuilder.forUri(URI_BILLPAYMENT_ITEMS);
            final ArrayList<BillPaymentItem> billPaymentItems = new ArrayList<BillPaymentItem>();

                return loader.orderBy(ShopStore.BillPayment.CATEGORYDESCRIPTION)
                    .transform(new Function<Cursor, List<BillPaymentItem>>() {
                        @Override
                        public List<BillPaymentItem> apply(Cursor c) {

                            while (c.moveToNext()) {
                                BillPaymentItem item = new BillPaymentItem(c);
                                billPaymentItems.add(item);
                            }
                            return billPaymentItems;
                        }
                    }).build(getActivity());
        }

        @Override
        public void onLoadFinished(Loader<List<BillPaymentItem>> loader, List<BillPaymentItem> data) {
            hide();
            productListViewAdapter.setList2(getSortedList(data));
            viewPagerIndicator.notifyDataSetChanged();
        }

        @Override
        public void onLoaderReset(Loader<List<BillPaymentItem>> loader) {
            Logger.d("onLoaderReset");
            if (getActivity() == null)
                return;
        }
    }

    private List<BillPaymentItem> getSortedList(List<BillPaymentItem> list) {
        Collections.sort(list, new Comparator<BillPaymentItem>() {

            @Override
            public int compare(BillPaymentItem lhs, BillPaymentItem rhs) {
                return lhs.masterBillerId.compareToIgnoreCase(rhs.masterBillerId);
            }
        });
        return list;
    }

    @UiThread
    protected void show() {
        WaitDialogFragment.show(getActivity(), getString(R.string.loading_message));
    }

    @UiThread
    protected void hide() {
        WaitDialogFragment.hide(getActivity());
    }

    @Override
    public Loader<List<WirelessItem>> onCreateLoader(int id, Bundle args) {

        show();
        CursorLoaderBuilder loader = CursorLoaderBuilder.forUri(URI_ORDER_ITEMS);

        final LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
        return loader.orderBy(ShopStore.WirelessTable.NAME)
                .transform(new Function<Cursor, List<WirelessItem>>() {
                    @Override
                    public List<WirelessItem> apply(Cursor c) {
                        List<WirelessItem> items = new ArrayList<WirelessItem>(c.getCount());

                        while (c.moveToNext()) {
                            WirelessItem item = new WirelessItem(c);
                            map.put(item.countryCode, item.countryName);
                            switch (prepaidMode) {
                                case PrepaidHomeFragment.LONGDISTANCE:
                                    if (item.isLongDistance()) {
                                        items.add(item);
                                    }
                                    break;
                                case PrepaidHomeFragment.WIRELESS:
                                    if (item.isWireless()&&!item.isPinless()) {
                                        items.add(item);
                                    }
                                    break;
                                case PrepaidHomeFragment.PINLESS:
                                    if (item.isPinless()) {
                                        items.add(item);
                                    }
                                    break;
                                case PrepaidHomeFragment.INTERNATIONAL:
                                    if (item.isWirelessInternational()) {
                                        items.add(item);
                                    }
                                    break;
                            }

                        }
                        return items;
                    }
                }).build(getActivity());
    }

    @Override
    public void onLoadFinished(Loader<List<WirelessItem>> loader, List<WirelessItem> wirelessItems) {
        productListViewAdapter.setList(wirelessItems);
        viewPagerIndicator.notifyDataSetChanged();
        hide();

    }

    @Override
    public void onLoaderReset(Loader<List<WirelessItem>> loader) {
        Logger.d("onLoaderReset");
        if (getActivity() == null)
            return;
    }

    public interface ProductFridViewInterface {
        void productSelected(WirelessItem item, int searchMode);

        void billPaymentItemSelected(BillPaymentItem billPaymentItem, int searchMode);
    }

    private AdapterView.OnItemClickListener globalItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            if (prepaidMode == PrepaidHomeFragment.BILLPAYMENT) {
                BillPaymentItem billPaymentItem = (BillPaymentItem) parent.getItemAtPosition(position);
                callback.billPaymentItemSelected(billPaymentItem, PrepaidLongDistanceActivity.ALL_SEARCH);
                return;
            }
            WirelessItem item = (WirelessItem) parent.getItemAtPosition(position);
            assert item != null;
            callback.productSelected(item, PrepaidLongDistanceActivity.ALL_SEARCH);
        }
    };


    private ProductFridViewInterface callback;

    public void setCallback(ProductFridViewInterface callback) {
        this.callback = callback;
    }


}
