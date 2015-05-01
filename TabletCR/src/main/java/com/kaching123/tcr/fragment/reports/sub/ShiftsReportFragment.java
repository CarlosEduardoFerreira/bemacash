package com.kaching123.tcr.fragment.reports.sub;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.getbase.android.db.loaders.CursorLoaderBuilder;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;

import com.kaching123.tcr.R;
import com.kaching123.tcr.adapter.ObjectsCursorAdapter;
import com.kaching123.tcr.fragment.SuperBaseFragment;
import com.kaching123.tcr.fragment.reports.RegisterReportsDetailsFragment.IDetailsFragment;
import com.kaching123.tcr.fragment.shift.PrintXReportFragment;
import com.kaching123.tcr.model.ShiftViewModel;
import com.kaching123.tcr.model.converter.ListConverterFunction;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopSchema2.ShiftView2.CloseManagerTable;
import com.kaching123.tcr.store.ShopSchema2.ShiftView2.OpenManagerTable;
import com.kaching123.tcr.store.ShopSchema2.ShiftView2.RegisterTable;
import com.kaching123.tcr.store.ShopSchema2.ShiftView2.ShiftTable;
import com.kaching123.tcr.store.ShopStore.ShiftView;
import com.kaching123.tcr.util.DateUtils;

import java.util.Date;
import java.util.List;

import static com.kaching123.tcr.fragment.UiHelper.concatFullname;
import static com.kaching123.tcr.model.ContentValuesUtil._decimal;
import static com.kaching123.tcr.model.ContentValuesUtil._nullableDate;

/**
 * Created by pkabakov on 18.02.14.
 */

@EFragment(R.layout.reports_shifts_report_list_fragment)
public class ShiftsReportFragment extends SuperBaseFragment implements IDetailsFragment, LoaderManager.LoaderCallbacks<List<ShiftViewModel>> {

    private static final Uri SHIFTS_URI = ShopProvider.getContentUri(ShiftView.URI_CONTENT);

    @ViewById(android.R.id.list)
    protected ListView listView;

    @FragmentArg
    protected long startTime;

    @FragmentArg
    protected long endTime;

    @FragmentArg
    protected long registerId;

    private ItemsAdapter adapter;

    public static ShiftsReportFragment instance(long startTime, long endTime, long resisterId) {
        return ShiftsReportFragment_.builder().startTime(startTime).endTime(endTime).registerId(resisterId).build();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        listView.setAdapter(adapter = new ItemsAdapter(getActivity()));
        listView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ShiftViewModel shiftViewModel = adapter.getItem(position);
                PrintXReportFragment.show(getActivity(), shiftViewModel.guid);
            }
        });

        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public void updateData(long startTime, long endTime, long registerId, long type, String managerGuid) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.registerId = registerId;
        if (getActivity() != null) {
            getLoaderManager().restartLoader(0, null, this);
        }
    }

    @Override
    public Loader<List<ShiftViewModel>> onCreateLoader(int i, Bundle bundle) {
        CursorLoaderBuilder loader = CursorLoaderBuilder.forUri(SHIFTS_URI)
                .where(ShiftTable.START_TIME + " >= ? AND " + ShiftTable.START_TIME + " <= ?", startTime, endTime)
                .where(ShiftTable.END_TIME + " IS NOT NULL")
                .orderBy(ShiftTable.START_TIME);
        if (registerId > 0) {
            loader.where(ShiftTable.REGISTER_ID + " = ?", registerId);
        }
        loader.orderBy(ShiftTable.START_TIME + " DESC");

        return loader.transform(new ListConverterFunction<ShiftViewModel>() {
            @Override
            public ShiftViewModel apply(Cursor cursor) {
                super.apply(cursor);
                return new ShiftViewModel(cursor.getString(indexHolder.get(ShiftTable.GUID)),
                        _nullableDate(cursor, indexHolder.get(ShiftTable.START_TIME)),
                        _nullableDate(cursor, indexHolder.get(ShiftTable.END_TIME)),
                        cursor.getString(indexHolder.get(ShiftTable.OPEN_MANAGER_ID)),
                        cursor.getString(indexHolder.get(ShiftTable.CLOSE_MANAGER_ID)),
                        cursor.getLong(indexHolder.get(ShiftTable.REGISTER_ID)),
                        _decimal(cursor, indexHolder.get(ShiftTable.OPEN_AMOUNT)),
                        _decimal(cursor, indexHolder.get(ShiftTable.CLOSE_AMOUNT)),
                        concatFullname(cursor.getString(indexHolder.get(OpenManagerTable.FIRST_NAME)), cursor.getString(indexHolder.get(OpenManagerTable.LAST_NAME))),
                        concatFullname(cursor.getString(indexHolder.get(CloseManagerTable.FIRST_NAME)), cursor.getString(indexHolder.get(CloseManagerTable.LAST_NAME))),
                        cursor.getString(indexHolder.get(RegisterTable.TITLE))
                );
            }
        }).build(getActivity());
    }

    @Override
    public void onLoadFinished(Loader<List<ShiftViewModel>> listLoader, List<ShiftViewModel> shiftModels) {
        adapter.changeCursor(shiftModels);
    }

    @Override
    public void onLoaderReset(Loader<List<ShiftViewModel>> listLoader) {
        adapter.changeCursor(null);
    }

    private class ItemsAdapter extends ObjectsCursorAdapter<ShiftViewModel> {

        public ItemsAdapter(Context context) {
            super(context);
        }

        @Override
        protected View newView(int position, ViewGroup parent) {
            View v = View.inflate(getContext(), R.layout.reports_shift_report_item_view, null);
            v.setTag(new UiHolder(
                    (TextView) v.findViewById(R.id.date),
                    (TextView) v.findViewById(R.id.employee_name),
                    (TextView) v.findViewById(R.id.register_number),
                    (TextView) v.findViewById(R.id.start_time),
                    (TextView) v.findViewById(R.id.end_time),
                    (TextView) v.findViewById(R.id.time_spent)
            ));
            return v;
        }

        @Override
        protected View bindView(View view, int position, ShiftViewModel item) {
            UiHolder holder = (UiHolder) view.getTag();

            holder.date.setText(DateUtils.dateOnlyFormat(item.startTime));
            if (item.closeManagerId != null && !item.openManagerId.equals(item.closeManagerId)) {
                holder.employeeName.setText(item.openEmployeeFullName + " - " + item.closeEmployeeFullName);
            } else {
                holder.employeeName.setText(item.openEmployeeFullName);
            }
            holder.registerNumber.setText(item.registerTitle);
            holder.startTime.setText(DateUtils.timeWithSecondsOnlyFormat(item.startTime));
            holder.endTime.setText(DateUtils.timeWithSecondsOnlyFormat(item.endTime));
            long timeDiff = (item.endTime == null ? new Date().getTime() : item.endTime.getTime()) - item.startTime.getTime();
            holder.timeSpent.setText(DateUtils.formatMilisecWithSecond(timeDiff));

            return view;
        }
    }

    private static class UiHolder {
        TextView date;
        TextView employeeName;
        TextView registerNumber;
        TextView startTime;
        TextView endTime;
        TextView timeSpent;

        private UiHolder(TextView date, TextView employeeName, TextView registerNumber, TextView startTime, TextView endTime, TextView timeSpent) {
            this.date = date;
            this.employeeName = employeeName;
            this.registerNumber = registerNumber;
            this.startTime = startTime;
            this.endTime = endTime;
            this.timeSpent = timeSpent;
        }
    }
}
