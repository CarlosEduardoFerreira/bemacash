package com.kaching123.tcr.fragment.settings;

import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.kaching123.tcr.Logger;
import com.kaching123.tcr.R;
import com.kaching123.tcr.activity.SettingsActivity;
import com.kaching123.tcr.commands.display.DisplayMessageCommand;
import com.kaching123.tcr.commands.display.DisplayWelcomeMessageCommand;
import com.kaching123.tcr.fragment.SuperBaseFragment;
import com.kaching123.tcr.fragment.dialog.AlertDialogFragment;
import com.kaching123.tcr.fragment.dialog.StyledDialogFragment;
import com.kaching123.tcr.fragment.dialog.WaitDialogFragmentWithCallback;
import com.kaching123.tcr.service.DisplayService;
import com.kaching123.tcr.service.ScaleService;
import com.kaching123.tcr.service.ScannerBinder;
import com.kaching123.tcr.service.ScannerListener;
import com.kaching123.tcr.service.ScannerService;
import com.kaching123.tcr.service.USBScannerService;
import com.kaching123.tcr.util.ReceiverWrapper;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;

/**
 * Created by long.jiao on 8/14/2015.
 */

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
@EFragment(R.layout.settings_device_diagnose_fragment)
@OptionsMenu(R.menu.settings_device_diagnose_fragment)
public class DiagnoseFragment extends SuperBaseFragment implements DisplayService.IDisplayBinder {
    final static String[] DEVICE_ARRAY = {"Network","Printer","Display","Scanner","Scale"};
    @ViewById
    protected ListView list;

    @ViewById
    protected View emptyItem;

    @ViewById
    protected EditText usbScannerInput;

    private ArrayAdapter<String> adapter;
    private ScaleService scaleService;
    private boolean scaleServiceBound;
    protected ServiceConnection scaleServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            scaleServiceBound = false;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            scaleBinder = (ScaleService.ScaleBinder) service;
            scaleService = scaleBinder.getService();
            scaleServiceBound = true;
        }
    };
    private ScaleService.ScaleBinder scaleBinder;
    private ScannerBinder scannerBinder;

    public String getScannerRead() {
        return scannerRead;
    }

    public void setScannerRead(String scannerRead) {
        this.scannerRead = scannerRead;
    }

    private String scannerRead;

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
                    case 2:
                        FindDeviceFragment.show(getActivity(), findDisplayListener, FindDeviceFragment.Mode.DISPLAY);
                        task = new DisplayTask(itemPosition);
                        break;
                    case 3:
                        FindDeviceFragment.show(getActivity(), findScannerListener, FindDeviceFragment.Mode.SCANNER);
                        task = new DisplayTask(itemPosition);
                        break;
                    case 4:
                        if(getApp().isFreemium()) {
                            AlertDialogFragment.showAlert(getActivity(), R.string.unavailable_option_title, getString(R.string.unavailable_option_message));
                        } else {
                            FindDeviceFragment.show(getActivity(), findScaleListener, FindDeviceFragment.Mode.SCALE);
                            task = new DisplayTask(itemPosition);
                        }
                        break;
                }
            }
        });
    }

    private FindDeviceFragment.FindDeviceListener findScannerListener = new FindDeviceFragment.FindDeviceListener() {

        @Override
        public void onDeviceSelected() {
            ((SettingsActivity)getActivity()).bindToScannerService();
            task.execute();
        }

    };

    private FindDeviceFragment.FindDeviceListener findScaleListener = new FindDeviceFragment.FindDeviceListener() {

        @Override
        public void onDeviceSelected() {
            bindToScaleService();
            task.execute();
        }

    };

    private DisplayTask task;
    private FindDeviceFragment.FindDeviceListener findDisplayListener = new FindDeviceFragment.FindDeviceListener() {

        @Override
        public void onDeviceSelected() {
            task.execute();

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

        unbindFromScaleService();
    }

    private void bindToDisplayService() {
        boolean displayConfigured = !TextUtils.isEmpty(getApp().getShopPref().displayAddress().get()); //Serial Port?
        if (displayConfigured)
            DisplayService.bind(getActivity(), displayServiceConnection);
    }

    private void unbindFromDisplayService() {
        if (displayBinder != null) {
            displayBinder = null;
            getActivity().unbindService(displayServiceConnection);
        }
    }

    private void bindToScaleService() {
        boolean displayConfigured = !TextUtils.isEmpty(getApp().getShopPref().scaleName().get()); //Serial Port?
        if (displayConfigured) {
            ScaleService.bind(getActivity(), scaleServiceConnection);
        }
    }

    private void unbindFromScaleService() {
        if (scaleBinder != null) {
            scaleBinder = null;
            getActivity().unbindService(scaleServiceConnection);
        }
    }

    private class DisplayTask extends AsyncTask<String, Void, Void> {
        int pos;
        private String scaleRead;

        public DisplayTask(int pos){
            this.pos = pos;
        }

        @Override
        protected Void doInBackground(String... params) {
                if(pos == 2) {
                    if(getApp().getShopPref().displayName().get().toUpperCase().contains("LCI") || getApp().getShopPref().displayName().get().toUpperCase().contains("LDX")) {
                        while (displayBinder == null) {
                            try {
                                Thread.sleep(300);
                                startCommand(new DisplayMessageCommand("Connected"));
                                Thread.sleep(4000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }else
                        startCommand(new DisplayMessageCommand("Connected"));
                }
                else if(pos == 4){
//                    scaleRead = scaleService.readScale();
                    while(true){
                        if(scaleService != null && scaleServiceBound){
                            Logger.d(scaleServiceBound + " read = "+scaleService.readScale());
                            scaleRead = scaleService.readScale();
                            break;
                        }
                    }
                }else if(pos == 3){
                    while(true){
                        if(scannerRead != null){
                            break;
                        }
                    }
                }
            return null;
        }

        @Override
        protected void onPreExecute() {
            if (pos == 2) {
                bindToDisplayService();
                WaitDialogFragmentWithCallback.show(getActivity(), getString(R.string.wait_dialog_title),false);
            }
            else if(pos == 3){
                WaitDialogFragmentWithCallback.show(getActivity(), getString(R.string.scan_dialog_title),true);
            }
        }

        @Override
        protected void onPostExecute(Void none) {
            String confirmStr = null;
            if (pos == 2) {
                confirmStr = getString(R.string.confirm_display_title);
                unbindFromDisplayService();
            }
            else if(pos == 4){
                confirmStr = String.format(getString(R.string.confirm_scale_title),scaleRead);
            }
            else if(pos == 3){
                if(scannerRead == ""){
                    scannerRead = null;
                    return;
                }
                confirmStr = String.format(getString(R.string.confirm_scanner_title),scannerRead);
                scannerRead = null;
                ((SettingsActivity)getActivity()).unbindFromScannerService();
            }
            AlertDialogFragment.show(getActivity(), AlertDialogFragment.DialogType.CONFIRM,
                    R.string.btn_confirm,
                    confirmStr,
                    R.string.btn_yes,
                    R.string.btn_retry,
                    true,
                    new StyledDialogFragment.OnDialogClickListener() {
                        @Override
                        public boolean onClick() {
                            return true;
                        }
                    },
                    new StyledDialogFragment.OnDialogClickListener() {
                        @Override
                        public boolean onClick() {
                            task = new DisplayTask(pos);
                            task.execute();
                            return true;
                        }
                    },
                    null
            );
            WaitDialogFragmentWithCallback.hide(getActivity());
        }
    }



    public void receivedScannerCallback(String barcode){
            scannerRead = barcode;
      }

}
