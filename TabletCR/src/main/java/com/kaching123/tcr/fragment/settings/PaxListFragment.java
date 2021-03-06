package com.kaching123.tcr.fragment.settings;

import android.content.Context;
import android.content.Intent;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ResourceCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.kaching123.tcr.R;
import com.kaching123.tcr.TcrApplication;
import com.kaching123.tcr.activity.LogsViewerActivity_;
import com.kaching123.tcr.commands.device.DeletePaxCommand;
import com.kaching123.tcr.commands.payment.pax.processor.SettingINI;
import com.kaching123.tcr.fragment.dialog.AlertDialogFragment;
import com.kaching123.tcr.fragment.dialog.AlertDialogFragment.DialogType;
import com.kaching123.tcr.fragment.dialog.StyledDialogFragment.OnDialogClickListener;
import com.kaching123.tcr.model.PaxModel;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore.PaxTable;
import com.mobeta.android.dslv.DragSortListView;
import com.pax.poslink.LogSetting;

import org.androidannotations.annotations.AfterTextChange;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;

@EFragment(R.layout.settings_pax_list_fragment)
@OptionsMenu(R.menu.discover_pax_activity)
public class PaxListFragment extends Fragment implements LoaderCallbacks<Cursor> {
    private static final String TAG = "PaxListFragment";

    private static final Uri URI_PRINTER = ShopProvider.getContentUri(PaxTable.URI_CONTENT);
    public static final int LOADER_ID = 123;

    private PaxListAdapter adapter;

    @ViewById
    protected DragSortListView list;

    @ViewById
    protected EditText timeOutInput;

    @ViewById
    protected Spinner logSwitchEdit, logLevelEdit;

    @ViewById
    protected Button logSet, logView;

    private final int PAX_TIME_OUT_DEFAULT = 100;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        list.setEmptyView(getView().findViewById(android.R.id.empty));
        list.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View v, int i, long l) {
                PaxModel model = adapter.getModel(i);
                PaxEditFragment.show(getActivity(), model);
            }
        });
        getLoaderManager().initLoader(LOADER_ID, null, this);
    }

    @AfterTextChange(R.id.time_out_input)
    protected void afterTextChangedOntimeOutInput(Editable s, TextView view) {
        if (validTimeout(s.toString()))
            setPaxTimeout(Integer.parseInt(s.toString()));
    }

    @AfterViews
    protected void init() {
        timeOutInput.setText("" + (getPaxTimeout() == 0 ? PAX_TIME_OUT_DEFAULT : getPaxTimeout()));
        String LogSettingIniFile = TcrApplication.get().getApplicationContext().getFilesDir().getAbsolutePath() + "/" + SettingINI.FILENAME;
        if (!SettingINI.loadSettingFromFile(LogSettingIniFile)) {
            logSwitchEdit.setSelection(0);
            logLevelEdit.setSelection(0);

            LogSetting.setLogMode(true);
            LogSetting.setLevel(LogSetting.LOGLEVEL.ERROR);
            SettingINI.saveLogSettingToFile(LogSettingIniFile);
        } else {
            logSwitchEdit.setSelection(LogSetting.isLoggable() ? 0 : 1);
            logLevelEdit.setSelection(LogSetting.getLevel().ordinal());
        }

        logView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewLogFile();
            }
        });

        logSet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setLogSetting();
            }
        });
    }

    private void setLogSetting() {
        String LogSettingIniFile = getContext().getApplicationContext().getFilesDir().getAbsolutePath() + "/" + SettingINI.FILENAME;

        String[] mStrArrayLogSwitch = getResources().getStringArray(R.array.log_switch);

        LogSetting.setLogMode(logSwitchEdit.getSelectedItem().toString().compareTo(mStrArrayLogSwitch[0]) == 0);
        LogSetting.setLevel(LogSetting.LOGLEVEL.valueOf(logLevelEdit.getSelectedItem().toString()));

        android.util.Log.i(TAG, "isLoggable =" + LogSetting.isLoggable() + "; LogLevel=" + LogSetting.getLevel()
                + "; OutputPath=" + LogSetting.getOutputPath());

        SettingINI.saveLogSettingToFile(LogSettingIniFile);
    }

    private void viewLogFile() {

        Intent intent = new Intent(getActivity(), LogsViewerActivity_.class);
        Bundle bundle = new Bundle();
        bundle.putString("LogsPath", LogSetting.getOutputPath());
        intent.putExtras(bundle);
        startActivity(intent);
    }

    private int getPaxTimeout() {
        return ((TcrApplication) (getActivity().getApplication())).getPaxTimeOut();
    }

    private void setPaxTimeout(int timeout) {
        ((TcrApplication) (getActivity().getApplication())).setPaxTimeOut(timeout);
    }

    private boolean validTimeout(String timeout) {
        int t = 0;
        try {
            t = Integer.parseInt(timeout);
        } catch (NumberFormatException e) {
            return false;
        } catch (NullPointerException e) {
            return false;
        }
        return t > 0;
    }

    @OptionsItem
    protected void actionAddSelected() {
        if (list != null && list.getCount() > 0) {
            Toast.makeText(getActivity(), "Only one active pinpad is available", Toast.LENGTH_LONG).show();
        } else {
            PaxEditFragment.show(getActivity(), null);
        }
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
        return new CursorLoader(getActivity(), URI_PRINTER, new String[]{
                "0 as _id",
                PaxTable.GUID,
                PaxTable.IP,
                PaxTable.PORT,
                PaxTable.MAC,
                PaxTable.SUBNET,
                PaxTable.GATEWAY,
                PaxTable.DHCP,
                PaxTable.SERIAL,
        }, null, null, PaxTable.PORT);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        adapter = new PaxListAdapter(getActivity());
        adapter.changeCursor(data);
        list.setAdapter(adapter);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        list.setAdapter(null);
    }

    private class PaxListAdapter extends ResourceCursorAdapter implements DragSortListView.RemoveListener {

        public PaxListAdapter(Context context) {
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

            UiHolder holder = (UiHolder) v.getTag();
            holder.pos = c.getPosition();
            holder.editView.setTag(holder.pos);
            holder.configView.setTag(holder.pos);
            holder.editView.setVisibility(View.INVISIBLE);
            holder.text2.setText(c.getString(c.getColumnIndex(PaxTable.IP)) + ":" + c.getString(c.getColumnIndex(PaxTable.PORT)));

            holder.configView.setEnabled(!TextUtils.isEmpty(c.getString(4)));
        }

        public PaxModel getModel(int position) {
            Cursor c = (Cursor) getItem(position);
            return new PaxModel(
                    c.getString(1),
                    c.getString(2),
                    c.getInt(3),
                    c.getString(4),
                    c.getString(5),
                    c.getString(6),
                    c.getInt(7) == 1,
                    c.getString(8));
        }

        @Override
        public void remove(int i) {
            handleRemove(getModel(i));
        }

    }

    private void handleRemove(final PaxModel model) {
        AlertDialogFragment.show(
                getActivity(),
                DialogType.CONFIRM_NONE,
                R.string.pax_delete_dialog_title,
                getString(R.string.pax_delete_dialog_msg),
                R.string.btn_confirm,
                new OnDialogClickListener() {
                    @Override
                    public boolean onClick() {
                        DeletePaxCommand.start(getActivity(), model, false);
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
            final PaxModel model = adapter.getModel(pos);
            if (TextUtils.isEmpty(model.mac)) {
                return;
            }
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
        return PaxListFragment_.builder().build();
    }
}
