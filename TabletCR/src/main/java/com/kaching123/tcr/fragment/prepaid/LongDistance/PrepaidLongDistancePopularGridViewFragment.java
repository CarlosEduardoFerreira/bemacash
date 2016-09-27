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
import com.kaching123.tcr.websvc.api.prepaid.Category;
import com.kaching123.tcr.websvc.api.prepaid.MasterBiller;
import com.viewpagerindicator.LinePageIndicator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by teli.yin on 11/3/2014.
 */
@EFragment
public class PrepaidLongDistancePopularGridViewFragment extends PrepaidLongDistanceBaseBodyFragment implements LoaderManager.LoaderCallbacks<List<WirelessItem>> {
    private static final int DEFAULT_LOADER = 0;
    private static final Uri URI_ORDER_ITEMS = ShopProvider.getContentUri(ShopStore.WirelessTable.URI_CONTENT);
    private static final Uri URI_BILLPAYMENT_ITEMS = ShopProvider.getContentUri(ShopStore.BillPayment.URI_CONTENT);
    @ViewById
    protected ViewPager viewPager;

    @FragmentArg
    protected int prepaidMode;


    @ViewById
    protected LinePageIndicator viewPagerIndicator;
    private ProductItemsPageAdapter productListViewAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.prepaid_long_distance_product_grid_view_fragment, container, false);
    }

    @AfterViews
    public void init() {

        initFragment();
    }

    private void initFragment() {
        productListViewAdapter = new ProductItemsPageAdapter(getActivity(), globalItemClickListener, prepaidMode);
        viewPager.setAdapter(productListViewAdapter);
        viewPagerIndicator.setViewPager(viewPager);
        if (prepaidMode == PrepaidHomeFragment.BILLPAYMENT) {
            getLoaderManager().restartLoader(DEFAULT_LOADER, null, new BillPaymentItemsLoader());
//            ArrayList<MasterBiller> values = new ArrayList<MasterBiller>(vectorMasterBiller.length);
//            for (MasterBiller biller : vectorMasterBiller)
//                values.add(biller);
//            productListViewAdapter.setList2(values);
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
            productListViewAdapter.setList2(data);
            viewPagerIndicator.notifyDataSetChanged();
        }

        @Override
        public void onLoaderReset(Loader<List<BillPaymentItem>> loader) {
            Logger.d("onLoaderReset");
            if (getActivity() == null)
                return;
        }
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
                        int merchantListLength = 0;
                        int zipListLength = 0;
                        int alphaListLength = 0;
                        while (c.moveToNext()) {
                            WirelessItem item = new WirelessItem(c);
                            map.put(item.countryCode, item.countryName);
                            switch (prepaidMode) {
                                case PrepaidHomeFragment.LONGDISTANCE:
                                    if (item.isLongDistance()) {
                                        if (item.merchantBuyingFrequency != 0)
                                            merchantListLength++;
                                        else if (item.zipCodeBuyingFrequency != 0)
                                            zipListLength++;
                                        else
                                            alphaListLength++;
                                        items.add(item);
                                    }
                                    break;
                                case PrepaidHomeFragment.WIRELESS:
                                    if (item.isWireless()) {
                                        if (item.merchantBuyingFrequency != 0)
                                            merchantListLength++;
                                        else if (item.zipCodeBuyingFrequency != 0)
                                            zipListLength++;
                                        else
                                            alphaListLength++;
                                        items.add(item);
                                    }
                                    break;
                                case PrepaidHomeFragment.PINLESS:
                                    if (item.isPinless()) {
                                        if (item.merchantBuyingFrequency != 0)
                                            merchantListLength++;
                                        else if (item.zipCodeBuyingFrequency != 0)
                                            zipListLength++;
                                        else
                                            alphaListLength++;
                                        items.add(item);
                                    }
                                    break;
                                case PrepaidHomeFragment.INTERNATIONAL:
                                    if (item.isWirelessInternational()) {
                                        if (item.merchantBuyingFrequency != 0)
                                            merchantListLength++;
                                        else if (item.zipCodeBuyingFrequency != 0)
                                            zipListLength++;
                                        else
                                            alphaListLength++;
                                        items.add(item);
                                    }
                                    break;
                            }

                        }

                        items = getSortedList(items, merchantListLength, zipListLength, alphaListLength);
                        return items;
                    }
                }).build(getActivity());
    }

    @Override
    public void onLoadFinished(Loader<List<WirelessItem>> loader, List<WirelessItem> wirelessItems) {
        hide();
        productListViewAdapter.setList(wirelessItems);
        viewPagerIndicator.notifyDataSetChanged();
    }

    @Override
    public void onLoaderReset(Loader<List<WirelessItem>> loader) {
        Logger.d("onLoaderReset");
        if (getActivity() == null)
            return;
    }

    private AdapterView.OnItemClickListener globalItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (prepaidMode == PrepaidHomeFragment.BILLPAYMENT) {
                BillPaymentItem item = (BillPaymentItem) parent.getItemAtPosition(position);
                callback.billPaymentItemSelected(item, PrepaidLongDistanceActivity.MOST_POPULAR);
                return;
            }
            WirelessItem item = (WirelessItem) parent.getItemAtPosition(position);
            assert item != null;
            callback.productSelected(item, PrepaidLongDistanceActivity.MOST_POPULAR);
        }
    };

    private PrepaidLongDistanceProductGridViewFragment.ProductFridViewInterface callback;

    public void setCallback(PrepaidLongDistanceProductGridViewFragment.ProductFridViewInterface callback) {
        this.callback = callback;
    }

    public List<WirelessItem> getSortedList(List<WirelessItem> list, int merchantListLength, int zipListLength, int alphaListLength) {
        List listBeforeSort = list;
        List<WirelessItem> listAfterSort;
        WirelessItem[] merchantList = new WirelessItem[merchantListLength];
        WirelessItem[] zipList = new WirelessItem[zipListLength];
        WirelessItem[] alphaList = new WirelessItem[alphaListLength];
        int merchantCount = 0;
        int zipCount = 0;
        int alphaCount = 0;
        for (int i = 0; i < listBeforeSort.size(); i++) {
            WirelessItem item = (WirelessItem) listBeforeSort.get(i);
            if (item.merchantBuyingFrequency != 0) {
                merchantList[merchantCount++] = item;
            } else if (item.zipCodeBuyingFrequency != 0) {
                zipList[zipCount++] = item;
            } else {
                alphaList[alphaCount++] = item;
            }

        }
        listAfterSort = sortList(merchantList, zipList, alphaList);


        return listAfterSort;
    }

    public List<WirelessItem> sortList(WirelessItem[] merchantList, WirelessItem[] zipList, WirelessItem[] alphaList) {
        WirelessItem temp = null;
        if (merchantList.length > 1)
            for (int i = 1; i < merchantList.length; i++) {
                int j = i - 1;
                temp = merchantList[i];
                for (; j >= 0 && temp.merchantBuyingFrequency < merchantList[j].merchantBuyingFrequency; j--) {
                    merchantList[j + 1] = merchantList[j];
                }
                merchantList[j + 1] = temp;
            }
        if (zipList.length > 1)
            for (int i = 1; i < zipList.length; i++) {
                int j = i - 1;
                temp = zipList[i];
                for (; j >= 0 && temp.zipCodeBuyingFrequency < zipList[j].zipCodeBuyingFrequency; j--) {
                    zipList[j + 1] = zipList[j];
                }
                zipList[j + 1] = temp;
            }
        WirelessItem[] listAfterSort = new WirelessItem[merchantList.length + zipList.length + alphaList.length];

        System.arraycopy(merchantList, 0, listAfterSort, 0, merchantList.length);
        System.arraycopy(zipList, 0, listAfterSort, merchantList.length, zipList.length);
        if (alphaList.length != 0)
            System.arraycopy(alphaList, 0, listAfterSort, merchantList.length + zipList.length, alphaList.length);
        return Arrays.asList(listAfterSort);
    }
}
