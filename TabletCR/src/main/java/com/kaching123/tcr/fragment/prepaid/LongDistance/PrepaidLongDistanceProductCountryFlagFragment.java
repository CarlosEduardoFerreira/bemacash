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
import com.kaching123.tcr.Logger;
import com.kaching123.tcr.R;
import com.kaching123.tcr.activity.PrepaidActivity.PrepaidLongDistanceActivity;
import com.kaching123.tcr.fragment.dialog.WaitDialogFragment;
import com.kaching123.tcr.fragment.prepaid.PrepaidHomeFragment;
import com.kaching123.tcr.fragment.prepaid.utilities.ProductFlagItemsPageAdapter;
import com.kaching123.tcr.fragment.prepaid.utilities.ProductItemsPageAdapter;
import com.kaching123.tcr.model.payment.blackstone.prepaid.wireless.WirelessItem;
import com.kaching123.tcr.model.payment.blackstone.prepaid.wireless.request.BillPaymentItem;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore;
import com.kaching123.tcr.util.CountryFlags;
import com.viewpagerindicator.LinePageIndicator;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by teli.yin on 11/3/2014.
 */
@EFragment
public class PrepaidLongDistanceProductCountryFlagFragment extends PrepaidLongDistanceBaseBodyFragment {
    private static final int DEFAULT_LOADER = 0;
    private static final Uri URI_ORDER_ITEMS = ShopProvider.getContentUri(ShopStore.WirelessTable.URI_CONTENT);
    private static final Uri URI_BILLPAYMENT_ITEMS = ShopProvider.getContentUri(ShopStore.BillPayment.URI_CONTENT);

    @ViewById
    protected ViewPager viewPager;
    @FragmentArg
    protected int prepaidMode;
    @ViewById
    protected LinePageIndicator viewPagerIndicator;
    private ProductFlagItemsPageAdapter productFlagItemsPageAdapter;
    private ProductItemsPageAdapter itemsPageAdapter;
    private CountryNameLoader countryNameLoader = new CountryNameLoader();
    private ProductLoader productLoader = new ProductLoader();
    private Map.Entry<String, Integer> country;
    private Map.Entry<String, WirelessItem> carrier;
    private CountryFlagFragmentCallbak countryFlagFragmentCallbak;
    private String selectedCountryInit;
    private LinkedList<BillPaymentItem> items;
    private LinkedList<String> carrierNames;
    private String chosenCategory;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.prepaid_long_distance_product_grid_view_fragment, container, false);
    }


    @AfterViews
    public void init() {
        carrierNames = new LinkedList<String>();
        initFragment();

    }

    private void initFragment() {
        productFlagItemsPageAdapter = new ProductFlagItemsPageAdapter(getActivity(), countryClickListener, prepaidMode);
        itemsPageAdapter = new ProductItemsPageAdapter(getActivity(), productClickListener, prepaidMode);

        if (prepaidMode != PrepaidHomeFragment.BILLPAYMENT) {
            show();
            getLoaderManager().restartLoader(DEFAULT_LOADER, null, countryNameLoader);
        } else {
            getLoaderManager().restartLoader(DEFAULT_LOADER, null, new BillPaymentCategoryLoader());
//            productFlagItemsPageAdapter.setList(getBillPaymentCategories());
        }
    }

    private void setCarrierSearchWords() {

        for (BillPaymentItem item : items) {
            carrierNames.add(item.categoryDescription.substring(0, 1));
        }

        countryFlagFragmentCallbak.refreshCountryCharacter(carrierNames.toArray(new String[carrierNames.size()]));
        selectedCountryInit = null;
    }


    @UiThread
    protected void show() {
        WaitDialogFragment.show(getActivity(), getString(R.string.loading_message));
    }

    private class BillPaymentCategoryLoader implements LoaderManager.LoaderCallbacks<List<BillPaymentItem>> {

        @Override
        public Loader<List<BillPaymentItem>> onCreateLoader(int id, Bundle args) {

            show();
            CursorLoaderBuilder loader = CursorLoaderBuilder.forUri(URI_BILLPAYMENT_ITEMS);
            final LinkedList<BillPaymentItem> billPaymentItems = new LinkedList<BillPaymentItem>();
            return loader.orderBy(ShopStore.BillPayment.CATEGORYDESCRIPTION)
                    .transform(new Function<Cursor, List<BillPaymentItem>>() {
                        @Override
                        public List<BillPaymentItem> apply(Cursor c) {
                            String currentCategory = null;
                            while (c.moveToNext()) {
                                BillPaymentItem item = new BillPaymentItem(c);
                                if (!item.categoryDescription.equalsIgnoreCase(currentCategory))
                                    if (selectedCountryInit == null)
                                        billPaymentItems.add(item);
                                    else if (item.categoryDescription.substring(0, 1).equalsIgnoreCase(selectedCountryInit)) {
                                        billPaymentItems.add(item);
                                        chosenCategory = item.categoryDescription.substring(0, 1);
                                    }

                                currentCategory = item.categoryDescription;
                            }
                            return billPaymentItems;
                        }
                    }).build(getActivity());
        }

        @Override
        public void onLoadFinished(Loader<List<BillPaymentItem>> loader, List<BillPaymentItem> data) {
            hide();
            items = (LinkedList<BillPaymentItem>) data;
            viewPager.setAdapter(productFlagItemsPageAdapter);
            viewPagerIndicator.setViewPager(viewPager);
            productFlagItemsPageAdapter.setList(data);
            setCarrierSearchWords();
            viewPagerIndicator.notifyDataSetChanged();
        }

        @Override
        public void onLoaderReset(Loader<List<BillPaymentItem>> loader) {
            Logger.d("onLoaderReset");
            if (getActivity() == null)
                return;
        }
    }


    private class BillPaymentMasterBillerLoader implements LoaderManager.LoaderCallbacks<List<BillPaymentItem>> {

        @Override
        public Loader<List<BillPaymentItem>> onCreateLoader(int id, Bundle args) {

            show();
            CursorLoaderBuilder loader = CursorLoaderBuilder.forUri(URI_BILLPAYMENT_ITEMS);
            final LinkedList<BillPaymentItem> billPaymentItems = new LinkedList<BillPaymentItem>();

            return loader.orderBy(ShopStore.BillPayment.CATEGORYDESCRIPTION)
                    .transform(new Function<Cursor, List<BillPaymentItem>>() {
                        @Override
                        public List<BillPaymentItem> apply(Cursor c) {

                            while (c.moveToNext()) {
                                BillPaymentItem item = new BillPaymentItem(c);
                                if (item.categoryDescription.substring(0, 1).equalsIgnoreCase(chosenCategory))
                                    billPaymentItems.add(item);
                            }
                            return billPaymentItems;
                        }
                    }).build(getActivity());
        }

        @Override
        public void onLoadFinished(Loader<List<BillPaymentItem>> loader, List<BillPaymentItem> data) {
            hide();
            viewPager.setAdapter(itemsPageAdapter);
            viewPagerIndicator.setViewPager(viewPager);
            itemsPageAdapter.setList2(data);
            setCarrierSearchWords();
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
    protected void hide() {
        WaitDialogFragment.hide(getActivity());
    }

    private class ProductLoader implements LoaderManager.LoaderCallbacks<List<WirelessItem>> {
        @Override
        public Loader<List<WirelessItem>> onCreateLoader(int id, Bundle args) {

            CursorLoaderBuilder loader = CursorLoaderBuilder.forUri(URI_ORDER_ITEMS);

            return loader.orderBy(ShopStore.WirelessTable.NAME)
                    .transform(new Function<Cursor, List<WirelessItem>>() {
                        @Override
                        public List<WirelessItem> apply(Cursor c) {
                            List<WirelessItem> items = new ArrayList<WirelessItem>(c.getCount());

                            while (c.moveToNext()) {
                                WirelessItem item = new WirelessItem(c);
                                switch (prepaidMode) {
                                    case PrepaidHomeFragment.LONGDISTANCE:
                                        if (item.isLongDistance()) {
                                            if (item.countryName.equalsIgnoreCase(country.getKey().toString()))
                                                items.add(item);
                                        }
                                        break;
                                    case PrepaidHomeFragment.WIRELESS:
                                        if (item.isWireless() && item.carrierName.toString().equalsIgnoreCase(carrier.getKey().toString())) {
                                            items.add(item);

                                        }
                                        break;
                                    case PrepaidHomeFragment.PINLESS:
                                        if (item.isPinless() && item.carrierName.toString().equalsIgnoreCase(carrier.getKey().toString())) {
                                            items.add(item);

                                        }
                                        break;
                                    case PrepaidHomeFragment.INTERNATIONAL:
                                        if (item.isWirelessInternational() && item.countryName.equalsIgnoreCase(country.getKey().toString())) {
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
            hide();
            itemsPageAdapter.setList(wirelessItems);
            viewPagerIndicator.notifyDataSetChanged();
        }

        @Override
        public void onLoaderReset(Loader<List<WirelessItem>> loader) {
            Logger.d("onLoaderReset");
            if (getActivity() == null)
                return;
        }
    }


    private class CountryNameLoader implements LoaderManager.LoaderCallbacks<List> {
        @Override
        public Loader<List> onCreateLoader(int id, Bundle args) {

            CursorLoaderBuilder loader = CursorLoaderBuilder.forUri(URI_ORDER_ITEMS);

            return loader.orderBy(ShopStore.WirelessTable.NAME)
                    .transform(new Function<Cursor, List>() {
                        @Override
                        public List<WirelessItem> apply(Cursor c) {
//                        List<WirelessItem> items = new ArrayList<WirelessItem>(c.getCount());
                            Map<String, Integer> counts = new LinkedHashMap<String, Integer>();
                            Map<String, WirelessItem> wireless_counts = new LinkedHashMap<String, WirelessItem>();
                            Set<String> localCache = new HashSet<String>();

                            ArrayList list = new ArrayList();
                            while (c.moveToNext()) {
                                WirelessItem item = new WirelessItem(c);
                                switch (prepaidMode) {
                                    case PrepaidHomeFragment.LONGDISTANCE:
                                        if (selectedCountryInit == null) {
                                            if (item.isLongDistance()) {
                                                counts.put(item.countryName, CountryFlags.getCountryFlag(item.countryName.toLowerCase()));
                                            }
                                        } else {
                                            if (item.isLongDistance() && item.countryName.substring(0, 1).equalsIgnoreCase(selectedCountryInit)) {
                                                counts.put(item.countryName, CountryFlags.getCountryFlag(item.countryName.toLowerCase()));
                                            }
                                        }
                                        break;
                                    case PrepaidHomeFragment.WIRELESS:
                                        if (selectedCountryInit == null) {
                                            if (item.isWireless()) {
                                                String token = item.carrierName;
                                                if (!localCache.contains(token)) {
                                                    wireless_counts.put(item.carrierName, item);
                                                    localCache.add(token);
                                                }
                                            }
                                        } else {
                                            if (item.isWireless() && item.carrierName.substring(0, 1).equalsIgnoreCase(selectedCountryInit)) {
                                                wireless_counts.put(item.carrierName, item);
                                            }
                                        }

                                        break;
                                    case PrepaidHomeFragment.PINLESS:
                                        if (selectedCountryInit == null) {
                                            if (item.isPinless()) {
                                                String token = item.carrierName;
                                                if (!localCache.contains(token)) {
                                                    wireless_counts.put(item.carrierName, item);
                                                    localCache.add(token);
                                                }
                                            }
                                        } else {
                                            if (item.isPinless() && item.carrierName.substring(0, 1).equalsIgnoreCase(selectedCountryInit)) {
                                                wireless_counts.put(item.carrierName, item);
                                            }
                                        }
                                        break;
                                    case PrepaidHomeFragment.INTERNATIONAL:
                                        if (selectedCountryInit == null) {
                                            if (item.isWirelessInternational()) {
                                                counts.put(item.countryName, CountryFlags.getCountryFlag(item.countryName.toLowerCase()));
                                            }
                                        } else {
                                            if (item.isWirelessInternational() && item.countryName.substring(0, 1).equalsIgnoreCase(selectedCountryInit)) {
                                                counts.put(item.countryName, CountryFlags.getCountryFlag(item.countryName.toLowerCase()));
                                            }
                                        }
                                        break;
                                }

                            }
                            if (prepaidMode == PrepaidHomeFragment.LONGDISTANCE || prepaidMode == PrepaidHomeFragment.INTERNATIONAL)
                                list.addAll(counts.entrySet());
                            else
                                list.addAll(wireless_counts.entrySet());
                            return list;
                        }
                    }).build(getActivity());
        }

        @Override
        public void onLoadFinished(Loader<List> loader, List data) {
            hide();
            productFlagItemsPageAdapter.setList(data);
            viewPager.setAdapter(productFlagItemsPageAdapter);
            viewPagerIndicator.setViewPager(viewPager);
            String[] strs = new String[data.size()];
            for (int i = 0; i < data.size(); i++) {
                Map.Entry temp = (Map.Entry) data.get(i);
                strs[i] = temp.getKey().toString().substring(0, 1);
            }
            countryFlagFragmentCallbak.refreshCountryCharacter(strs);
            selectedCountryInit = null;
            viewPagerIndicator.notifyDataSetChanged();
        }

        @Override
        public void onLoaderReset(Loader<List> loader) {
            Logger.d("onLoaderReset");
            if (getActivity() == null)
                return;
        }

    }

    private AdapterView.OnItemClickListener productClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            if (prepaidMode == PrepaidHomeFragment.BILLPAYMENT) {
                BillPaymentItem item = (BillPaymentItem) parent.getItemAtPosition(position);
                countryFlagFragmentCallbak.billPaymentItemSelected(item, PrepaidLongDistanceActivity.COUNTRY_SEARCH);
                countryFlagFragmentCallbak.onProductLayer(false);
                return;
            }
            WirelessItem item = (WirelessItem) parent.getItemAtPosition(position);
            assert item != null;
            countryFlagFragmentCallbak.productSelected(item, PrepaidLongDistanceActivity.COUNTRY_SEARCH);
            countryFlagFragmentCallbak.onProductLayer(false);
        }
    };
    private AdapterView.OnItemClickListener countryClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            if (prepaidMode == PrepaidHomeFragment.BILLPAYMENT) {
                viewPager.setAdapter(itemsPageAdapter);
                viewPagerIndicator.setViewPager(viewPager);
                if (chosenCategory == null) {
                    chosenCategory = carrierNames.get(position);
                    countryFlagFragmentCallbak.billPaymentCategoryChosen(carrierNames.get(position));
                } else
                    countryFlagFragmentCallbak.billPaymentCategoryChosen(chosenCategory);
                countryFlagFragmentCallbak.headMessage(PrepaidLongDistanceHeadFragment.SELECT_BILLER);
                getLoaderManager().restartLoader(DEFAULT_LOADER, null, new BillPaymentMasterBillerLoader());
                return;
            }
            if (prepaidMode == PrepaidHomeFragment.PINLESS) {
                Map.Entry item = (Map.Entry<String, WirelessItem>) parent.getItemAtPosition(position);
                countryFlagFragmentCallbak.productSelected((WirelessItem) item.getValue(), PrepaidLongDistanceActivity.COUNTRY_SEARCH);
                countryFlagFragmentCallbak.onProductLayer(false);
                return;
            }
            if (prepaidMode == PrepaidHomeFragment.LONGDISTANCE || prepaidMode == PrepaidHomeFragment.INTERNATIONAL) {
                country = (Map.Entry<String, Integer>) parent.getItemAtPosition(position);
                assert country != null;
                countryFlagFragmentCallbak.headMessage(PrepaidLongDistanceProductInfoMenuFragment.SELECT_PRODUCT);
                countryFlagFragmentCallbak.hideCountryGridFragment(country.getKey().toString(), country.getValue());
            } else {
                carrier = (Map.Entry<String, WirelessItem>) parent.getItemAtPosition(position);
                assert carrier != null;
                countryFlagFragmentCallbak.hideCarrierGridFragment(carrier.getKey().toString(), carrier.getValue().iconUrl);
            }
            viewPager.setAdapter(itemsPageAdapter);
            viewPagerIndicator.setViewPager(viewPager);
            show();
            getLoaderManager().restartLoader(DEFAULT_LOADER, null, productLoader);
            countryFlagFragmentCallbak.onProductLayer(true);
        }
    };

    public interface CountryFlagFragmentCallbak {

        void productSelected(WirelessItem item, int searchMode);

        void billPaymentItemSelected(BillPaymentItem item, int mode);

        void showCountryGridFragment();

        void hideCountryGridFragment(String name, int drawable);

        void hideCarrierGridFragment(String name, String url);

        void refreshCountryCharacter(String[] chs);

        void onProductLayer(boolean isProductLayer);

        void billPaymentCategoryChosen(String key);

        void headMessage(int mode);
    }

    public void setCountryFlagFramentCallback(CountryFlagFragmentCallbak callback) {
        this.countryFlagFragmentCallbak = callback;
    }

    public void startSearch(String name) {
        selectedCountryInit = name;

        if (prepaidMode == PrepaidHomeFragment.BILLPAYMENT) {
            countryFlagFragmentCallbak.headMessage(PrepaidLongDistanceHeadFragment.SELECT_CATEGORY);
            getLoaderManager().restartLoader(DEFAULT_LOADER, null, new BillPaymentCategoryLoader());
            hide();
            return;
        } else
            show();
        getLoaderManager().restartLoader(DEFAULT_LOADER, null, countryNameLoader);
    }

}
