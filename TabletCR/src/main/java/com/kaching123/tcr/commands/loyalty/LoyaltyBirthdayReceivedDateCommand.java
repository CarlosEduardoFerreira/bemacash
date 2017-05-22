package com.kaching123.tcr.commands.loyalty;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.getbase.android.db.provider.ProviderAction;
import com.kaching123.tcr.commands.store.AsyncCommand;
import com.kaching123.tcr.commands.store.user.UpdateCustomerBirthdayRewardDateCommand;
import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.jdbc.converters.CustomerJdbcConverter;
import com.kaching123.tcr.jdbc.converters.JdbcConverter;
import com.kaching123.tcr.model.CustomerModel;
import com.kaching123.tcr.model.LoyaltyIncentiveModel;
import com.kaching123.tcr.model.LoyaltyRewardType;
import com.kaching123.tcr.model.LoyaltyType;
import com.kaching123.tcr.model.SaleIncentiveModel;
import com.kaching123.tcr.service.BatchSqlCommand;
import com.kaching123.tcr.service.ISqlCommand;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore;
import com.kaching123.tcr.store.ShopStore.LoyaltyIncentiveTable;
import com.kaching123.tcr.store.ShopStore.SaleIncentiveTable;
import com.kaching123.tcr.store.ShopStore.SaleItemTable;
import com.telly.groundy.TaskResult;
import com.telly.groundy.annotations.OnSuccess;
import com.telly.groundy.annotations.Param;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

/**
 * Created by vkompaniets on 18.07.2016.
 */
public class LoyaltyBirthdayReceivedDateCommand extends AsyncCommand {

    private static final String ARG_CUSTOMER_ID = "ARG_CUSTOMER_ID";

    private CustomerModel customerModel;

    @Override
    protected TaskResult doCommand() {

        Long now = System.currentTimeMillis()/1000;
        Log.d("BemaCarl23","LoyaltyBirthdayReceivedDateCommand.doCommand.now: " + now);
        Date currenTimeZone = new Date();
        Log.d("BemaCarl23","LoyaltyBirthdayReceivedDateCommand.doCommand.currenTimeZone: " + currenTimeZone);

        String customerId = getStringArg(ARG_CUSTOMER_ID);
        Log.d("BemaCarl23","LoyaltyBirthdayReceivedDateCommand.doCommand.customerId: " + customerId);
        Cursor c1 = ProviderAction.query(ShopProvider.contentUri(ShopStore.CustomerTable.URI_CONTENT))
                .where(ShopStore.CustomerTable.GUID + " = ?", customerId)
                .perform(getContext());

        if (!c1.moveToFirst())
            return failed();

        customerModel = new CustomerModel(c1);
        customerModel.birthdayRewardReceivedDate = currenTimeZone;

        return succeeded();
    }

    @Override
    protected ArrayList<ContentProviderOperation> createDbOperations() {
        Log.d("BemaCarl23","LoyaltyBirthdayReceivedDateCommand.createDbOperations");
        ArrayList<ContentProviderOperation> cv = new ArrayList<>();
        cv.add(ContentProviderOperation.newUpdate(ShopProvider.contentUri(ShopStore.CustomerTable.URI_CONTENT))
                .withValue(ShopStore.CustomerTable.BIRTHDAY_REWARD_RECEIVED_DATE, customerModel.birthdayRewardReceivedDate.getTime())
                .build());
        return cv;
    }

    @Override
    protected ISqlCommand createSqlCommand() {
        Log.d("BemaCarl23","LoyaltyBirthdayReceivedDateCommand.createSqlCommand.birthdayRewardReceivedDate: " + customerModel.birthdayRewardReceivedDate);
        BatchSqlCommand batch = batchInsert(customerModel);
        CustomerJdbcConverter customerJdbcConverter = (CustomerJdbcConverter) JdbcFactory.getConverter(customerModel);
        batch.add(customerJdbcConverter.updateLoyaltyBirthdayReceivedDate(customerModel, getAppCommandContext()));

        return batch;
    }

    public static void start(Context context, String customerId){
        Log.d("BemaCarl23","LoyaltyBirthdayReceivedDateCommand.start.customerId: " + customerId);
        create(LoyaltyBirthdayReceivedDateCommand.class)
                .arg(ARG_CUSTOMER_ID, customerId)
                .queueUsing(context);
    }

}

