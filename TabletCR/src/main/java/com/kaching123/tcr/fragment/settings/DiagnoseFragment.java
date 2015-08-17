package com.kaching123.tcr.fragment.settings;

import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.kaching123.tcr.R;
import com.kaching123.tcr.commands.display.DisplayWelcomeMessageCommand;
import com.kaching123.tcr.fragment.SuperBaseFragment;
import com.kaching123.tcr.fragment.dialog.AlertDialogFragment;
import com.kaching123.tcr.fragment.dialog.StyledDialogFragment;
import com.kaching123.tcr.fragment.dialog.WaitDialogFragment;
import com.kaching123.tcr.service.DisplayService;
import com.mobeta.android.dslv.DragSortListView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;

/**
 * Created by long.jiao on 8/14/2015.
 */
@EFragment(R.layout.settings_device_diagnose_fragment)
@OptionsMenu(R.menu.settings_device_diagnose_fragment)
public class DiagnoseFragment extends SuperBaseFragment implements DisplayService.IDisplayBinder {
    final static String[] DEVICE_ARRAY = {"Network","Printer","Pax","Drawer","Display","Scanner","Msr","Scale"};
    @ViewById
    protected ListView list;

    @ViewById
    protected View emptyItem;

    private ArrayAdapter<String> adapter;

    public static Fragment instance() {
        return DiagnoseFragment_.builder().build();
    }

    @AfterViews
    protected void initViews() {
        final Context context = DiagnoseFragment.this.getActivity();
        adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, DEVICE_ARRAY);
        list.setAdapter(adapter);
        list.setEmptyView(emptyItem);

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1,
                                    int position, long arg3) {
                // TODO Auto-generated method stub
                int itemPosition = position;
                switch (itemPosition){
                    case 0:
                        ConnectivityManager cm =
                                (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

                        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                        boolean isConnected = activeNetwork != null &&
                                activeNetwork.isConnectedOrConnecting();
                        if(isConnected)
                            Toast.makeText(context,"Internet is connected",Toast.LENGTH_SHORT).show();
                        else
                            Toast.makeText(context,"Internet is not connected, please check your settings",Toast.LENGTH_SHORT).show();
                        break;
                    case 1:
                        TestPrinterFragment.show(getActivity());
                        break;
                    case 4:
                        WaitDialogFragment.show(getActivity(), getString(R.string.wait_printing));
                        bindToDisplayService();
                        FindDeviceFragment.show(getActivity(), findDisplayListener, FindDeviceFragment.Mode.DISPLAY);
                        break;
                }
            }
        });
    }

    private FindDeviceFragment.FindDeviceListener findDisplayListener = new FindDeviceFragment.FindDeviceListener() {

        @Override
        public void onDeviceSelected() {
            startCommand(new DisplayWelcomeMessageCommand());
            unbindFromDisplayService();
            WaitDialogFragment.hide(getActivity());
        }

    };

    @Override
    public void startCommand(DisplayService.Command displayCommand) {
        if (displayBinder != null)
            displayBinder.startCommand(displayCommand);
    }

    @Override
    public void setDisplayListener(DisplayService.DisplayListener displayListener) {
        if (displayBinder != null)
            displayBinder.setDisplayListener(displayListener);
    }

    @Override
    public void tryReconnectDisplay() {
        if (displayBinder != null)
            displayBinder.tryReconnectDisplay();
    }

    private DisplayService.DisplayBinder displayBinder;
    private ServiceConnection displayServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder binder) {
            displayBinder = (DisplayService.DisplayBinder) binder;
            setDisplayListener(displayListener);
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
            displayBinder = null;
        }
    };

    private DisplayService.DisplayListener displayListener = new DisplayService.DisplayListener() {

        @Override
        public void onDisconnected() {
            if (getActivity().isFinishing() || getActivity().isDestroyed())
                return;

            AlertDialogFragment.showAlert(
                    getActivity(),
                    R.string.error_dialog_title,
                    getString(R.string.error_message_display_disconnected),
                    R.string.btn_try_again,
                    new StyledDialogFragment.OnDialogClickListener() {

                        @Override
                        public boolean onClick() {
                            tryReconnectDisplay();
                            return true;
                        }

                    }
            );
        }

        @Override
        public void onError() {
            if (getActivity().isFinishing() || getActivity().isDestroyed())
                return;

            Toast.makeText(getActivity(), R.string.error_message_display_command_error, Toast.LENGTH_SHORT).show();
        }

    };

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    private void bindToDisplayService() {
        boolean displayConfigured = !TextUtils.isEmpty(getApp().getShopPref().displayAddress().get()); //Serial Port?
        if (displayConfigured)
            DisplayService.bind(getActivity(), displayServiceConnection);
    }

    private void unbindFromDisplayService() {
        startCommand(new DisplayWelcomeMessageCommand());

        if (displayBinder != null) {
            displayBinder = null;
            getActivity().unbindService(displayServiceConnection);
        }
    }



}
