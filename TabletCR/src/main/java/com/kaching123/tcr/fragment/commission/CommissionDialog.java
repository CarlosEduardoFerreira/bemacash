package com.kaching123.tcr.fragment.commission;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

import com.getbase.android.db.loaders.CursorLoaderBuilder;
import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.EFragment;
import com.googlecode.androidannotations.annotations.FragmentArg;
import com.googlecode.androidannotations.annotations.ViewById;
import com.kaching123.tcr.R;
import com.kaching123.tcr.adapter.ObjectsCursorAdapter;
import com.kaching123.tcr.fragment.dialog.DialogUtil;
import com.kaching123.tcr.fragment.dialog.StyledDialogFragment;
import com.kaching123.tcr.model.EmployeeStatus;
import com.kaching123.tcr.model.converter.ListConverterFunction;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore.EmployeeTable;

import java.util.HashSet;
import java.util.List;

import static com.kaching123.tcr.fragment.UiHelper.concatFullname;

/**
 * Created by vkompaniets on 15.07.2014.
 */
@EFragment
public class CommissionDialog extends StyledDialogFragment {

    private static final String DIALOG_NAME = CommissionDialog.class.getSimpleName();

    @FragmentArg
    protected HashSet<String> salesmanGuids;

    @ViewById
    protected ListView list;

    private ICommissionDialogListener listener;

    private EmployeeAdapter adapter;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getDialog().getWindow().setLayout(
                getResources().getDimensionPixelOffset(R.dimen.default_dlg_width),
                getResources().getDimensionPixelOffset(R.dimen.default_dlg_heigth));

    }

    @AfterViews
    protected void init(){
        adapter = new EmployeeAdapter(getActivity());
        adapter.pickedGuids = salesmanGuids;
        list.setAdapter(adapter);
        list.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (!adapter.pickedGuids.remove(adapter.getItem(position).guid)){
                    adapter.pickedGuids.add(adapter.getItem(position).guid);
                }
                adapter.notifyDataSetChanged();
                checkPositiveBtnCondition();
            }
        });
        checkPositiveBtnCondition();
        getLoaderManager().initLoader(0, null, new EmployeeLoader());
    }

    @Override
    protected int getDialogContentLayout() {
        return R.layout.commission_dialog_fragment;
    }

    @Override
    protected int getDialogTitle() {
        return R.string.commission_dialog_title;
    }

    @Override
    protected int getNegativeButtonTitle() {
        return R.string.btn_cancel;
    }

    @Override
    protected int getPositiveButtonTitle() {
        return R.string.btn_ok;
    }

    @Override
    protected OnDialogClickListener getPositiveButtonListener() {
        return new OnDialogClickListener() {
            @Override
            public boolean onClick() {
                if (listener != null)
                    listener.onSalesmanPicked(adapter.pickedGuids);
                return true;
            }
        };
    }

    public void setListener(ICommissionDialogListener listener) {
        this.listener = listener;
    }

    private class EmployeeLoader implements LoaderCallbacks<List<EmployeeModel>>{

        @Override
        public Loader<List<EmployeeModel>> onCreateLoader(int id, Bundle args) {
            return CursorLoaderBuilder.forUri(ShopProvider.getContentUri(EmployeeTable.URI_CONTENT))
                    .projection(EmployeeTable.GUID, EmployeeTable.FIRST_NAME, EmployeeTable.LAST_NAME)
                    .where(EmployeeTable.IS_MERCHANT + " = ?", 0)
                    .where(EmployeeTable.STATUS + " = ?", EmployeeStatus.ACTIVE.ordinal())
                    .where(EmployeeTable.ELIGIBLE_FOR_COMMISSION + " = ?", 1)
                    .transform(new ListConverterFunction<EmployeeModel>() {
                        @Override
                        public EmployeeModel apply(Cursor cursor) {
                            return new EmployeeModel(
                                    cursor.getString(0),
                                    cursor.getString(1),
                                    cursor.getString(2)
                            );
                        }
                    }).build(getActivity());
        }

        @Override
        public void onLoadFinished(Loader<List<EmployeeModel>> loader, List<EmployeeModel> data) {
            adapter.changeCursor(data);
        }

        @Override
        public void onLoaderReset(Loader<List<EmployeeModel>> loader) {
            adapter.changeCursor(null);
        }
    }

    private class EmployeeAdapter extends ObjectsCursorAdapter<EmployeeModel> {

        public EmployeeAdapter(Context context) {
            super(context);
        }

        private HashSet<String> pickedGuids = new HashSet<String>();

        @Override
        protected View newView(int position, ViewGroup parent) {
            View convertView = LayoutInflater.from(getContext()).inflate(R.layout.commission_dialog_list_item_view, parent, false);

            ViewHolder holder = new ViewHolder(
                    (TextView)convertView.findViewById(R.id.name),
                    (CheckBox)convertView.findViewById(R.id.box)
            );
            convertView.setTag(holder);

            return convertView;
        }

        @Override
        protected View bindView(View convertView, int position, EmployeeModel item) {
            ViewHolder holder = (ViewHolder) convertView.getTag();

            if (item == null)
                return convertView;

            holder.name.setText(concatFullname(item.firstName, item.lastName));
            holder.box.setChecked(pickedGuids.contains(item.guid));

            return convertView;
        }

        private class ViewHolder{
            TextView name;
            CheckBox box;

            private ViewHolder(TextView name, CheckBox box) {
                this.name = name;
                this.box = box;
            }
        }
    }

    private void checkPositiveBtnCondition() {
        boolean hasPickedGuids = !adapter.pickedGuids.isEmpty();
        getPositiveButton().setTextColor(hasPickedGuids ? normalBtnColor : disabledBtnColor);
        getPositiveButton().setEnabled(hasPickedGuids);
    }

    private static class EmployeeModel {
        String guid;
        String firstName;
        String lastName;

        private EmployeeModel(String guid, String firstName, String lastName) {
            this.guid = guid;
            this.firstName = firstName;
            this.lastName = lastName;
        }
    }

    public static interface ICommissionDialogListener{
        void onSalesmanPicked (HashSet<String> salesmanGuids);
    }

    public static void show (FragmentActivity activity, HashSet<String> salesmanGuids, ICommissionDialogListener listener){
        DialogUtil.show(activity, DIALOG_NAME, CommissionDialog_.builder().salesmanGuids(salesmanGuids).build()).setListener(listener);
    }
}
