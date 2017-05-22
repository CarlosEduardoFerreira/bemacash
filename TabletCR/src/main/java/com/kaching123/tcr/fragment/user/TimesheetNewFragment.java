package com.kaching123.tcr.fragment.user;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.widget.Button;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import com.getbase.android.db.loaders.CursorLoaderBuilder;
import com.kaching123.tcr.R;
import com.kaching123.tcr.fragment.dialog.DialogUtil;
import com.kaching123.tcr.fragment.dialog.StyledDialogFragment;
import com.kaching123.tcr.model.EmployeeBreakTimesheetModel;
import com.kaching123.tcr.model.EmployeeTimesheetModel;
import com.kaching123.tcr.model.converter.ListConverterFunction;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore;

import static com.kaching123.tcr.model.ContentValuesUtil._nullableDate;
import static com.kaching123.tcr.util.CursorUtil._wrapOrNull;


/**
 * Created by pkabakov on 06.01.14.
 */
@EFragment
public class TimesheetNewFragment extends StyledDialogFragment {

    private static final String DIALOG_NAME = TimesheetNewFragment.class.getSimpleName();
    private static final String ARG_LAST_TIMESHEET = "ARG_LAST_TIMESHEET";

    @ViewById
    protected Button clockIn;

    @ViewById
    protected Button clockOut;

    @ViewById
    protected Button startBreak;

    @ViewById
    protected Button stopBreak;

    /*@ViewById
    protected EditText password;*/

    private OnTimesheetListener onTimesheetListener;

    @Override
    protected int getDialogContentLayout() {
        return R.layout.timesheet_chekin_or_out_fragment;
    }

    @Override
    protected int getDialogTitle() {
        return R.string.timesheet_new_dialog_title;
    }

    @Override
    protected int getPositiveButtonTitle() {
        return R.string.btn_cancel;
    }

    @Override
    protected int getNegativeButtonTitle() {
        return 0;
    }

    @Override
    protected boolean hasSkipButton() {
        return false;
    }
    @Override
    protected boolean hasNegativeButton() {
        return false;
    }
    @Override
    protected boolean hasPositiveButton() {
        return true;
    }

    @Override
    protected OnDialogClickListener getPositiveButtonListener() {
        return new OnDialogClickListener() {
            @Override
            public boolean onClick() {
                return true;
            }
        };
    }

    public void setOnTimesheetListener(OnTimesheetListener onTimesheetListener) {
        this.onTimesheetListener = onTimesheetListener;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getDialog().getWindow().setLayout(getResources().getDimensionPixelOffset(R.dimen.timeshift_dlg_width),
                getDialog().getWindow().getAttributes().height);
    }

    @AfterViews
    protected void onIniView() {
        getLoaderManager().restartLoader(0, null, clockInStateLoader);
    }

    public static void show(FragmentActivity activity, OnTimesheetListener onTimesheetListener) {
        TimesheetNewFragment fragment = TimesheetNewFragment_.builder().build();
        fragment.setOnTimesheetListener(onTimesheetListener);
        DialogUtil.show(activity, DIALOG_NAME, fragment);
    }

    public static void hide(FragmentActivity activity) {
        DialogUtil.hide(activity, DIALOG_NAME);
    }

    @Click
    protected void startBreakClicked() {
            onTimesheetListener.onBreakStartSelected();
    }

    @Click
    protected void stopBreakClicked() {
            onTimesheetListener.onBreakStopSelected();
    }

    @Click
    protected void clockInClicked() {
        onTimesheetListener.onCheckInSelected();
    }

    @Click
    protected void clockOutClicked() {
        onTimesheetListener.onCheckOutSelected();
    }

    private LoaderManager.LoaderCallbacks breaksStateLoader = new LoaderManager.LoaderCallbacks<Cursor>() {

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            EmployeeTimesheetModel lastTimesheet = (EmployeeTimesheetModel)args.getSerializable(ARG_LAST_TIMESHEET);
            CursorLoaderBuilder builder = CursorLoaderBuilder.forUri(ShopProvider.getContentWithLimitUri(ShopStore.EmployeeBreaksTimesheetTable.URI_CONTENT, 1));
            builder.where(ShopStore.EmployeeBreaksTimesheetTable.EMPLOYEE_GUID + " = ?", getApp().getOperatorGuid());
            if (lastTimesheet != null) {
                builder.where(ShopStore.EmployeeBreaksTimesheetTable.CLOCK_IN_GUID + " = ?", lastTimesheet.guid);
            }
            builder.orderBy(ShopStore.EmployeeBreaksTimesheetTable.BREAK_START + " DESC");
            return builder.build(getContext());
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            EmployeeBreakTimesheetModel lastBreak = _wrapOrNull(data, new ListConverterFunction<EmployeeBreakTimesheetModel>() {
                @Override
                public EmployeeBreakTimesheetModel apply(Cursor cursor) {
                    super.apply(cursor);
                    return new EmployeeBreakTimesheetModel(
                            cursor.getString(indexHolder.get(ShopStore.EmployeeBreaksTimesheetTable.GUID)),
                            _nullableDate(cursor, indexHolder.get(ShopStore.EmployeeBreaksTimesheetTable.BREAK_START)),
                            _nullableDate(cursor, indexHolder.get(ShopStore.EmployeeBreaksTimesheetTable.BREAK_END)),
                            cursor.getString(indexHolder.get(ShopStore.EmployeeBreaksTimesheetTable.EMPLOYEE_GUID)),
                            cursor.getString(indexHolder.get(ShopStore.EmployeeBreaksTimesheetTable.CLOCK_IN_GUID)),
                            null
                    );
                }
            });
            if (lastBreak != null) {
                if (lastBreak.breakEnd == null) {
                    startBreak.setEnabled(false);
                    stopBreak.setEnabled(true);
                } else {
                    startBreak.setEnabled(true);
                    stopBreak.setEnabled(false);
                }
            } else {
                startBreak.setEnabled(true);
                stopBreak.setEnabled(false);
            }
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {

        }
    };

    private LoaderManager.LoaderCallbacks clockInStateLoader = new LoaderManager.LoaderCallbacks<Cursor>() {

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            return CursorLoaderBuilder.forUri(ShopProvider.getContentWithLimitUri(ShopStore.EmployeeTimesheetTable.URI_CONTENT, 1))
                    .where(ShopStore.EmployeeTimesheetTable.EMPLOYEE_GUID + " = ?", getApp().getOperatorGuid())
                    .orderBy(ShopStore.EmployeeTimesheetTable.CLOCK_IN + " DESC")
                    .build(getContext());

        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            EmployeeTimesheetModel lastTimesheet = _wrapOrNull(data, new ListConverterFunction<EmployeeTimesheetModel>() {
                @Override
                public EmployeeTimesheetModel apply(Cursor cursor) {
                    super.apply(cursor);
                    return new EmployeeTimesheetModel(
                            cursor.getString(indexHolder.get(ShopStore.EmployeeTimesheetTable.GUID)),
                            _nullableDate(cursor, indexHolder.get(ShopStore.EmployeeTimesheetTable.CLOCK_IN)),
                            _nullableDate(cursor, indexHolder.get(ShopStore.EmployeeTimesheetTable.CLOCK_OUT)),
                            cursor.getString(indexHolder.get(ShopStore.EmployeeTimesheetTable.EMPLOYEE_GUID)),
                            null
                    );
                }
            });

            if(lastTimesheet != null) {
                if(lastTimesheet.clockOut == null) {
                    clockIn.setEnabled(false);
                    clockOut.setEnabled(true);
                    startBreak.setEnabled(true);
                    stopBreak.setEnabled(true);
                    Bundle bundle = new Bundle();
                    bundle.putSerializable(ARG_LAST_TIMESHEET, lastTimesheet);
                    getLoaderManager().restartLoader(1, bundle, breaksStateLoader);
                } else {
                    clockIn.setEnabled(true);
                    clockOut.setEnabled(false);
                    startBreak.setEnabled(false);
                    stopBreak.setEnabled(false);
                }
            } else {
                clockIn.setEnabled(true);
                clockOut.setEnabled(false);
                startBreak.setEnabled(false);
                stopBreak.setEnabled(false);
            }
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
        }
    };


    public interface OnTimesheetListener {

        public void onCheckInSelected();
        public void onCheckOutSelected();
        public void onBreakStartSelected();
        public void onBreakStopSelected();

    }

}
