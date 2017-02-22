package com.kaching123.tcr.fragment.dialog;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.widget.Toast;

import com.kaching123.tcr.R;
import com.kaching123.tcr.TcrApplication;
import com.kaching123.tcr.activity.SuperBaseActivity;
import com.kaching123.tcr.service.SyncCommand;
import com.kaching123.tcr.service.UploadTask;
import com.kaching123.tcr.store.ShopStore;
import com.kaching123.tcr.store.ShopStore.BillPaymentDescriptionTable;
import com.kaching123.tcr.store.ShopStore.CashDrawerMovementTable;
import com.kaching123.tcr.store.ShopStore.CategoryTable;
import com.kaching123.tcr.store.ShopStore.ComposerTable;
import com.kaching123.tcr.store.ShopStore.CreditReceiptTable;
import com.kaching123.tcr.store.ShopStore.LoyaltyPointsMovementTable;
import com.kaching123.tcr.store.ShopStore.CustomerTable;
import com.kaching123.tcr.store.ShopStore.DepartmentTable;
import com.kaching123.tcr.store.ShopStore.EmployeeCommissionsTable;
import com.kaching123.tcr.store.ShopStore.EmployeePermissionTable;
import com.kaching123.tcr.store.ShopStore.EmployeeTable;
import com.kaching123.tcr.store.ShopStore.EmployeeTimesheetTable;
import com.kaching123.tcr.store.ShopStore.EmployeeTipsTable;
import com.kaching123.tcr.store.ShopStore.ItemMovementTable;
import com.kaching123.tcr.store.ShopStore.ItemTable;
import com.kaching123.tcr.store.ShopStore.LoyaltyIncentiveItemTable;
import com.kaching123.tcr.store.ShopStore.LoyaltyIncentivePlanTable;
import com.kaching123.tcr.store.ShopStore.LoyaltyIncentiveTable;
import com.kaching123.tcr.store.ShopStore.LoyaltyPlanTable;
import com.kaching123.tcr.store.ShopStore.ModifierGroupTable;
import com.kaching123.tcr.store.ShopStore.ModifierTable;
import com.kaching123.tcr.store.ShopStore.PaymentTransactionTable;
import com.kaching123.tcr.store.ShopStore.PrinterAliasTable;
import com.kaching123.tcr.store.ShopStore.PrinterTable;
import com.kaching123.tcr.store.ShopStore.RegisterTable;
import com.kaching123.tcr.store.ShopStore.SaleAddonTable;
import com.kaching123.tcr.store.ShopStore.SaleIncentiveTable;
import com.kaching123.tcr.store.ShopStore.SaleItemTable;
import com.kaching123.tcr.store.ShopStore.SaleOrderTable;
import com.kaching123.tcr.store.ShopStore.ShiftTable;
import com.kaching123.tcr.store.ShopStore.TaxGroupTable;
import com.kaching123.tcr.store.ShopStore.UnitLabelTable;
import com.kaching123.tcr.store.ShopStore.UnitTable;
import com.kaching123.tcr.store.ShopStore.WirelessTable;
import com.kaching123.tcr.util.ReceiverWrapper;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.res.StringRes;

import java.util.HashMap;
import java.util.Locale;

/**
 * Created by gdubina on 24.12.13.
 */
@EFragment
public class SyncWaitDialogFragment extends WaitDialogFragment {

    private static final String DIALOG_NAME = "syncProgressDialog";

    public static final String SYNC_LOCAL = "local:";

    @StringRes(R.string.sync_wait_msg_tmpl)
    protected String waitMsgTmpl;

    private static final IntentFilter intentFilter = new IntentFilter();

    static {
        intentFilter.addAction(UploadTask.ACTION_UPLOAD_STARTED);
        intentFilter.addAction(UploadTask.ACTION_INVALID_UPLOAD_TRANSACTION);
        intentFilter.addAction(SyncCommand.ACTION_SYNC_PROGRESS);
        intentFilter.addAction(SuperBaseActivity.ACTION_APK_DOWNLOAD_PROGRESS);
    }

    @StringRes(R.string.sync_wait_msg_tmpl2)
    protected String waitMsgTmpl2;

    private ReceiverWrapper progressReceiver = new ReceiverWrapper(intentFilter) {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (getActivity() == null || intent == null)
                return;

            if (UploadTask.ACTION_UPLOAD_STARTED.equals(intent.getAction())) {
                progressMsg.setText(R.string.sync_wait_upload_started);
                return;
            }
            if (UploadTask.ACTION_INVALID_UPLOAD_TRANSACTION.equals(intent.getAction())) {
                Toast.makeText(context, context.getString(R.string.warning_message_incomplete_order), Toast.LENGTH_SHORT).show();
                return;
            }
            if (SyncCommand.ACTION_SYNC_PROGRESS.equals(intent.getAction())) {
                String table = intent.getStringExtra(SyncCommand.EXTRA_TABLE);
                String dataLabel = intent.getStringExtra(SyncCommand.EXTRA_DATA_LABEL);
                int pages = intent.getIntExtra(SyncCommand.EXTRA_PAGES, 0);
                int progress = intent.getIntExtra(SyncCommand.EXTRA_PROGRESS, 0);

                if (table != null)
                    if (table.contains(SYNC_LOCAL)) {
                        table = table.substring(6);
                        dataLabel = SYNC_LOCAL + getString(TABLE_NAMES.get(table));
                    } else
                        dataLabel = getString(TABLE_NAMES.get(table));

                if (pages == 0 && progress == 0) {
                    progressMsg.setText(String.format(Locale.US, waitMsgTmpl2, dataLabel));
                } else {
                    progressMsg.setText(String.format(Locale.US, waitMsgTmpl, dataLabel, progress, pages));
                }
            }
            if (SuperBaseActivity.ACTION_APK_DOWNLOAD_PROGRESS.equals(intent.getAction())) {
                int progress = intent.getIntExtra(SuperBaseActivity.EXTRA_PROGRESS, 0);
                progressMsg.setText(String.format(getString(R.string.str_download_progress), progress) + "%");
            }
        }
    };

    public static String processMessage(Context context, String dataLabel, String table, int pages, int progress){
        if (context == null){
            if (TcrApplication.get().getApplicationContext() == null) {
                return "";
            } else {
                context = TcrApplication.get().getApplicationContext();
            }
        }

        if (table != null && TABLE_NAMES.get(table) != null)
            dataLabel = context.getString(R.string.sync_data, context.getString(TABLE_NAMES.get(table)));

        String msgString = "";
        if (dataLabel != null) {
            if (pages == 0 && progress == 0) {
                msgString = String.format(Locale.US, context.getString(R.string.sync_wait_msg_tmpl2), dataLabel);
            } else {
                msgString = String.format(Locale.US, context.getString(R.string.sync_wait_msg_tmpl), dataLabel, progress, pages);
            }
        }

        return msgString;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getDialog().getWindow().setLayout(getResources().getDimensionPixelOffset(R.dimen.sync_dlg_width),
                getDialog().getWindow().getAttributes().height);
    }

    @Override
    public void onResume() {
        super.onResume();
        progressReceiver.register(getActivity());
    }

    @Override
    public void onPause() {
        super.onPause();
        progressReceiver.unregister(getActivity());
    }

    public static final HashMap<String, Integer> TABLE_NAMES = new HashMap<String, Integer>();

    static {
        TABLE_NAMES.put(CategoryTable.TABLE_NAME, R.string.sync_category);
        TABLE_NAMES.put(DepartmentTable.TABLE_NAME, R.string.sync_department);

        TABLE_NAMES.put(ItemTable.TABLE_NAME, R.string.sync_item);
        TABLE_NAMES.put(ModifierTable.TABLE_NAME, R.string.sync_modifier);
        TABLE_NAMES.put(ItemMovementTable.TABLE_NAME, R.string.sync_item_movement);
        TABLE_NAMES.put(ShopStore.VariantItemTable.TABLE_NAME, R.string.sync_item_variant);
        TABLE_NAMES.put(ShopStore.VariantSubItemTable.TABLE_NAME, R.string.sync_item_sub_variant);
        TABLE_NAMES.put(ShopStore.ItemMatrixTable.TABLE_NAME, R.string.sync_item_matrix);
        TABLE_NAMES.put(ShopStore.TBPTable.TABLE_NAME, R.string.sync_tbp);
        TABLE_NAMES.put(ShopStore.TBPxRegisterTable.TABLE_NAME, R.string.sync_tbp_x_register);
        TABLE_NAMES.put(ShopStore.MultipleDiscountTable.TABLE_NAME, R.string.sync_multiple_discount);

        TABLE_NAMES.put(ModifierGroupTable.TABLE_NAME, R.string.sync_units);
        TABLE_NAMES.put(ComposerTable.TABLE_NAME, R.string.sync_item_composition);

        TABLE_NAMES.put(SaleOrderTable.TABLE_NAME, R.string.sync_sale_order);
        TABLE_NAMES.put(SaleItemTable.TABLE_NAME, R.string.sync_sale_order_item);
        TABLE_NAMES.put(SaleAddonTable.TABLE_NAME, R.string.sync_sale_order_item_addons);
        TABLE_NAMES.put(PaymentTransactionTable.TABLE_NAME, R.string.sync_sale_order_payments);
        TABLE_NAMES.put(UnitTable.TABLE_NAME, R.string.sync_units);

        TABLE_NAMES.put(EmployeeTable.TABLE_NAME, R.string.sync_employee);
        TABLE_NAMES.put(EmployeePermissionTable.TABLE_NAME, R.string.sync_employee);
        TABLE_NAMES.put(EmployeeTimesheetTable.TABLE_NAME, R.string.sync_employee);

        TABLE_NAMES.put(ShiftTable.TABLE_NAME, R.string.sync_shift);
        TABLE_NAMES.put(CashDrawerMovementTable.TABLE_NAME, R.string.sync_cashdrawer_movemennt);

        TABLE_NAMES.put(TaxGroupTable.TABLE_NAME, R.string.tax_group);
        TABLE_NAMES.put(ModifierGroupTable.TABLE_NAME, R.string.sync_mod_groups_table);

        TABLE_NAMES.put(UnitLabelTable.TABLE_NAME, R.string.sync_unit_label_table);

        TABLE_NAMES.put(RegisterTable.TABLE_NAME, R.string.register);

        TABLE_NAMES.put(CustomerTable.TABLE_NAME, R.string.customer);

        TABLE_NAMES.put(PrinterAliasTable.TABLE_NAME, R.string.printer_alias);
        TABLE_NAMES.put(PrinterTable.TABLE_NAME, R.string.printer);
        TABLE_NAMES.put(BillPaymentDescriptionTable.TABLE_NAME, R.string.bill_description);

        TABLE_NAMES.put(CreditReceiptTable.TABLE_NAME, R.string.credit_receipts_description);
        TABLE_NAMES.put(WirelessTable.TABLE_NAME, R.string.prepaid_wireless_description);

        TABLE_NAMES.put(EmployeeTipsTable.TABLE_NAME, R.string.sync_employee_tips);
        TABLE_NAMES.put(EmployeeCommissionsTable.TABLE_NAME, R.string.sync_employee_commissions);

        TABLE_NAMES.put(LoyaltyIncentiveTable.TABLE_NAME, R.string.sync_loyalty_incentive);
        TABLE_NAMES.put(LoyaltyIncentiveItemTable.TABLE_NAME, R.string.sync_loyalty_incentive_item);
        TABLE_NAMES.put(LoyaltyPlanTable.TABLE_NAME, R.string.sync_loyalty_plan);
        TABLE_NAMES.put(LoyaltyIncentivePlanTable.TABLE_NAME, R.string.sync_loyalty_incentive_plan);
        TABLE_NAMES.put(LoyaltyPointsMovementTable.TABLE_NAME, R.string.sync_customer_loyalty_points);
        TABLE_NAMES.put(SaleIncentiveTable.TABLE_NAME, R.string.sync_sale_incentives);
    }

    public static void show(FragmentActivity activity, String msg) {
        DialogUtil.show(activity, DIALOG_NAME, SyncWaitDialogFragment_.builder().msg(msg).build());
    }

    public static void hide(FragmentActivity activity) {
        DialogUtil.hide(activity, DIALOG_NAME);
    }

}
