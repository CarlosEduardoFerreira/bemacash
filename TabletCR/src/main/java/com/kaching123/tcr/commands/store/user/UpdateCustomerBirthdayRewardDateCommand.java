package com.kaching123.tcr.commands.store.user;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.getbase.android.db.provider.ProviderAction;
import com.kaching123.tcr.TcrApplication;
import com.kaching123.tcr.commands.AtomicUpload;
import com.kaching123.tcr.commands.store.AsyncCommand;
import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.jdbc.converters.CustomerJdbcConverter;
import com.kaching123.tcr.model.CustomerModel;
import com.kaching123.tcr.service.BatchSqlCommand;
import com.kaching123.tcr.service.ISqlCommand;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopSchema2;
import com.kaching123.tcr.store.ShopStore;
import com.kaching123.tcr.store.ShopStore.CustomerTable;
import com.telly.groundy.TaskResult;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by vkompaniets on 22.07.2016.
 */
public class UpdateCustomerBirthdayRewardDateCommand extends AsyncCommand {

    private static final Uri CUSTOMER_URI = ShopProvider.contentUri(ShopStore.CustomerTable.URI_CONTENT);

    private static final String ARG_CUSTOMER_ID = "ARG_CUSTOMER_ID";

    private CustomerModel customer;

    @Override
    protected TaskResult doCommand() {
        String customerId = getStringArg(ARG_CUSTOMER_ID);

        Cursor c1 = ProviderAction.query(CUSTOMER_URI)
                .where(ShopStore.CustomerTable.GUID + " = ?", customerId)
                .perform(TcrApplication.get().getApplicationContext());
        if(c1.moveToNext()) {
            customer = new CustomerModel(c1);
            Log.d("BemaCarl23","UpdateCustomerBirthdayRewardDateCommand.doCommand.birthdayRewardApplyDate1: " + customer.birthdayRewardApplyDate);
        }else{
            customer = new CustomerModel(customerId, null);
            Log.d("BemaCarl23","UpdateCustomerBirthdayRewardDateCommand.doCommand.birthdayRewardApplyDate2: " + customer.birthdayRewardApplyDate);
        }
        Log.d("BemaCarl23","UpdateCustomerBirthdayRewardDateCommand.doCommand.birthday: " + customer.birthday);

        Date date = new Date();
        date.setTime(TcrApplication.get().getCurrentServerTimestamp());
        customer.birthdayRewardApplyDate = date;
        Log.d("BemaCarl23","UpdateCustomerBirthdayRewardDateCommand.doCommand.customer.birthdayRewardApplyDate              : " + customer.birthdayRewardApplyDate);
        Log.d("BemaCarl23","UpdateCustomerBirthdayRewardDateCommand.doCommand.customer.birthdayRewardApplyDate.getTime      : " + customer.birthdayRewardApplyDate.getTime());
        Log.d("BemaCarl23","UpdateCustomerBirthdayRewardDateCommand.doCommand.customer.birthdayRewardApplyDate.getTime/1000 : " + customer.birthdayRewardApplyDate.getTime()/1000);


        return succeeded();
    }

    @Override
    protected ISqlCommand createSqlCommand() {

        BatchSqlCommand batch = batchUpdate(customer);
        CustomerJdbcConverter converter = (CustomerJdbcConverter) JdbcFactory.getConverter(customer);
        batch.add(converter.updateBirthdayRewardDate(customer, getAppCommandContext()));
        Log.d("BemaCarl23","UpdateCustomerBirthdayRewardDateCommand.createSqlCommand.customer.birthdayRewardApplyDate: " + customer.birthdayRewardApplyDate);

        //new AtomicUpload().upload(batch, AtomicUpload.UploadType.WEB);

        return batch;
    }

    @Override
    protected ArrayList<ContentProviderOperation> createDbOperations() {
        ArrayList<ContentProviderOperation> ops = new ArrayList<>(1);
        ops.add(ContentProviderOperation.newUpdate(ShopProvider.contentUri(CustomerTable.URI_CONTENT))
                .withValue(CustomerTable.BIRTHDAY_REWARD_APPLY_DATE, customer.birthdayRewardApplyDate.getTime())
                .withSelection(CustomerTable.GUID + " = ?", new String[]{customer.guid})
                .build()
        );
        Log.d("BemaCarl23","UpdateCustomerBirthdayRewardDateCommand.createDbOperations.customer.birthdayRewardApplyDate: " + customer.birthdayRewardApplyDate);
        return ops;
    }

    public SyncResult sync(Context context, String customerId, IAppCommandContext appCommandContext){
        Bundle args = new Bundle(1);
        args.putString(ARG_CUSTOMER_ID, customerId);
        Log.d("BemaCarl23","UpdateCustomerBirthdayRewardDateCommand.sync.customer.customerId: " + customerId);
        return syncDependent(context, args, appCommandContext);
    }

    public static void start(Context context, String customerId){
        create(UpdateCustomerBirthdayRewardDateCommand.class)
                .arg(ARG_CUSTOMER_ID, customerId)
                .queueUsing(context);
    }
}

