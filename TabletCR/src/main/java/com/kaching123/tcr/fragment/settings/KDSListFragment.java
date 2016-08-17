package com.kaching123.tcr.fragment.settings;

import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.Editable;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.kaching123.tcr.R;
import com.kaching123.tcr.TcrApplication;
import com.kaching123.tcr.activity.KDSAliasActivity;
import com.kaching123.tcr.commands.device.DeleteKDSCommand;
import com.kaching123.tcr.fragment.dialog.AlertDialogFragment;
import com.kaching123.tcr.fragment.dialog.AlertDialogFragment.DialogType;
import com.kaching123.tcr.fragment.dialog.StyledDialogFragment.OnDialogClickListener;
import com.kaching123.tcr.model.KDSModel;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore;
import com.kaching123.tcr.store.ShopStore.KDSView;
import com.kaching123.tcr.store.ShopSchema2.KDSView2.AliasTable;
import com.kaching123.tcr.store.ShopSchema2.KDSView2.KdsTable;
import com.mobeta.android.dslv.DragSortListView;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;

@EFragment(R.layout.settings_kds_list_fragment)
@OptionsMenu(R.menu.discover_kds_activity)
public class KDSListFragment extends Fragment implements LoaderCallbacks<Cursor> {

    private static final Uri URI_KDS = ShopProvider.getContentUri(KDSView.URI_CONTENT);
    public static final int LOADER_ID = 123;

    private KDSListAdapter adapter;

    @ViewById
    protected DragSortListView list;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        list.setEmptyView(getView().findViewById(android.R.id.empty));
        list.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View v, int i, long l) {
                KDSEditFragment.show(getActivity(), adapter.getModel(i));
            }
        });
        getLoaderManager().initLoader(LOADER_ID, null,this);
    }

    @OptionsItem
    protected void actionAddSelected() {
        KDSEditFragment.show(getActivity(), null);
    }

    @OptionsItem
    protected void actionManageKdsAliasSelected(){
        KDSAliasActivity.start(getActivity());
    }

//    @OptionsItem
//    protected void actionSearchSelected() {
//        if (list != null && list.getCount() > 0) {
//            Toast.makeText(getActivity(), "Only one active pinpad is available", Toast.LENGTH_LONG).show();
//        } else {
//            FindPAXFragment.show(getActivity(), getPaxTimeout());
//        }
//    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(), URI_KDS, new String[]{
                "0 as _id",
                KdsTable.GUID,
                KdsTable.IP,
                KdsTable.PORT,
                KdsTable.ALIAS_GUID,
                AliasTable.ALIAS
        }, null, null, AliasTable.ALIAS);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        adapter = new KDSListAdapter(getActivity());
        adapter.changeCursor(data);
        list.setAdapter(adapter);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        list.setAdapter(null);
    }

    private class KDSListAdapter extends ResourceCursorAdapter implements DragSortListView.RemoveListener {

        public KDSListAdapter(Context context) {
            super(context, R.layout.settings_printers_list_item_view, null, false);
        }


        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            View v = super.newView(context, cursor, parent);
            View edit = v.findViewById(R.id.btn_edit);
//            edit.setOnClickListener(editItemClickListener);

            View config = v.findViewById(R.id.btn_config);
            config.setOnClickListener(configItemClickListener);

            v.setTag(new UiHolder(
                            (TextView) v.findViewById(android.R.id.text1),
                            (TextView) v.findViewById(android.R.id.text2),
                            edit,
                            config)
            );
            return v;
        }

        @Override
        public void bindView(View v, Context context, Cursor c) {
            String aliasGuid = c.getString(c.getColumnIndex(KdsTable.ALIAS_GUID));
            String alias = c.getString(c.getColumnIndex(AliasTable.ALIAS));
            UiHolder holder = (UiHolder) v.getTag();
            holder.pos = c.getPosition();
            holder.editView.setTag(holder.pos);
            holder.configView.setTag(holder.pos);
            holder.editView.setVisibility(View.INVISIBLE);
            holder.text1.setText(TextUtils.isEmpty(aliasGuid) ? getString(R.string.edit_kds_default) : TextUtils.isEmpty(alias) ? getString(R.string.edit_kds_default) : alias);
            holder.text2.setText(c.getString(c.getColumnIndex(KdsTable.IP)) + ":" + c.getString(c.getColumnIndex(KdsTable.PORT)));

            holder.configView.setVisibility(View.INVISIBLE);
        }

        public KDSModel getModel(int position) {
            Cursor c = (Cursor) getItem(position);
            return new KDSModel(
                    c.getString(c.getColumnIndex(KdsTable.GUID)),
                    c.getString(c.getColumnIndex(KdsTable.IP)),
                    c.getInt(c.getColumnIndex(KdsTable.PORT)),
                    c.getString(c.getColumnIndex(AliasTable.ALIAS)));
        }

        @Override
        public void remove(int i) {
            handleRemove(getModel(i));
        }

    }

    private void handleRemove(final KDSModel model) {
        AlertDialogFragment.show(
                getActivity(),
                DialogType.CONFIRM_NONE,
                R.string.pax_delete_dialog_title,
                getString(R.string.pax_delete_dialog_msg),
                R.string.btn_confirm,
                new OnDialogClickListener() {
                    @Override
                    public boolean onClick() {
                        DeleteKDSCommand.start(getActivity(), model.guid);
                        return true;
                    }
                }, new OnDialogClickListener() {
                    @Override
                    public boolean onClick() {
                        adapter.notifyDataSetChanged();
                        return true;
                    }
                }, null
        );
    }

    private View.OnClickListener configItemClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            Integer pos = (Integer) v.getTag();
            final KDSModel model = adapter.getModel(pos);
//            PrinterConfigFragment.show(getActivity(), model);
        }
    };

    private static class UiHolder {

        View editView;
        View configView;

        TextView text1;

        TextView text2;

        int pos;

        private UiHolder(TextView text1, TextView text2, View editView, View configView) {
            this.text1 = text1;
            this.text2 = text2;
            this.editView = editView;
            this.configView = configView;
        }
    }

    public static Fragment instance() {
        return KDSListFragment_.builder().build();
    }
}
