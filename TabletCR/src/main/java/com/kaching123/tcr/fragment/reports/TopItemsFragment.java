package com.kaching123.tcr.fragment.reports;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;

import com.getbase.android.db.loaders.CursorLoaderBuilder;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;
import com.kaching123.tcr.R;
import com.kaching123.tcr.component.chart.HorizontalBarChart;
import com.kaching123.tcr.component.chart.HorizontalBarChart.DescriptionBarChartData;
import com.kaching123.tcr.component.chart.HorizontalBarChart.DescriptionBarData;
import com.kaching123.tcr.fragment.SuperBaseFragment;
import com.kaching123.tcr.model.TopItemModel;
import com.kaching123.tcr.model.converter.TopItemsFunction;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopSchema2.ReportsTopItemsView2;
import com.kaching123.tcr.store.ShopStore;

import java.util.Date;
import java.util.List;

/**
 * Created by pkabakov on 17.01.14.
 */
@EFragment(R.layout.top_items_fragment)
public class TopItemsFragment extends SuperBaseFragment {

    private static final Uri TOP_ITEMS_URI = ShopProvider.getContentUri(ShopStore.ReportsTopItemsView.URI_CONTENT);

    private static final int LOADER_TOP_ITEMS_ID = 1;

    private Date fromDate;

    private Date toDate;

    //private long selectedShiftId;
    private long selectedRegisterId;

    @ViewById
    protected HorizontalBarChart barChart;

    public static TopItemsFragment instantiate(){
        return TopItemsFragment_.builder().build();
    }

    public void setDates(Date fromDate, Date toDate) {
        this.fromDate = fromDate;
        this.toDate = toDate;
        loadData();
    }

    public void setSelectedEntities(/*ShiftViewModel selectedShift, */long selectedRegisterId) {
        this.selectedRegisterId = selectedRegisterId;
    }

    private void loadData() {
        getLoaderManager().restartLoader(LOADER_TOP_ITEMS_ID, null, topItemsLoader);
    }

    private LoaderManager.LoaderCallbacks<Optional<DescriptionBarChartData>> topItemsLoader = new LoaderManager.LoaderCallbacks<Optional<DescriptionBarChartData>>() {

        @Override
        public Loader<Optional<DescriptionBarChartData>> onCreateLoader(int id, Bundle bundle) {
            CursorLoaderBuilder builder = CursorLoaderBuilder
                    .forUri(TOP_ITEMS_URI)
                    .where(ReportsTopItemsView2.SaleOrderTable.CREATE_TIME + " >= ?", fromDate.getTime())
                    .where(ReportsTopItemsView2.SaleOrderTable.CREATE_TIME + " <= ?", toDate.getTime());
            /*if (selectedShift != null)
                builder.where(ReportsTopItemsView2.SaleOrderTable.SHIFT_GUID + " = ?", selectedShift.guid);*/
            if (selectedRegisterId > 0)
                builder.where(ReportsTopItemsView2.SaleOrderTable.REGISTER_ID + " = ?", selectedRegisterId);
            return builder
                    .wrap(new ChartTopItemsFunction())
                    .build(getActivity());
        }

        @Override
        public void onLoadFinished(Loader<Optional<DescriptionBarChartData>> loader, Optional<DescriptionBarChartData> result) {
            barChart.setData(result.orNull());
        }

        @Override
        public void onLoaderReset(Loader<Optional<DescriptionBarChartData>> loader) {

        }
    };

    private class ChartTopItemsFunction implements Function<Cursor, Optional<DescriptionBarChartData>> {

        private  final TopItemsFunction topItemsFunction = new TopItemsFunction();

        @Override
        public Optional<DescriptionBarChartData> apply(Cursor cursor) {
            Optional<List<TopItemModel>> topItemsListOptional = topItemsFunction.apply(cursor);

            if (!topItemsListOptional.isPresent())
                return Optional.fromNullable(null);

            List<TopItemModel> topItemsList = topItemsListOptional.get();

            DescriptionBarData[] barData = new DescriptionBarData[topItemsList.size()];
            int i = 0;
            for (TopItemModel item: topItemsList) {
                barData[i++] = new DescriptionBarData(item.description, item.quantity);
            }

            return Optional.fromNullable(new DescriptionBarChartData(barData));
        }

    }

}
