package com.kaching123.tcr.commands.store.user;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.os.Bundle;

import com.kaching123.tcr.commands.store.AsyncCommand;
import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.jdbc.converters.CustomerJdbcConverter;
import com.kaching123.tcr.model.CustomerModel;
import com.kaching123.tcr.service.ISqlCommand;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore.CustomerTable;
import com.telly.groundy.TaskResult;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by vkompaniets on 22.07.2016.
 */
public class UpdateCustomerBirthdayRewardDateCommand extends AsyncCommand {

    private static final String ARG_CUSTOMER_ID = "ARG_CUSTOMER_ID";
    private static final String ARG_REWARD_DATE = "ARG_REWARD_DATE";

    private CustomerModel customer;

    @Override
    protected TaskResult doCommand() {
        String customerId = getStringArg(ARG_CUSTOMER_ID);
        Date rewardDate = (Date) getArgs().getSerializable(ARG_REWARD_DATE);

        customer = new CustomerModel(customerId, null);
        customer.birthdayRewardApplyDate = rewardDate;

        return succeeded();
    }

    @Override
    protected ISqlCommand createSqlCommand() {
        CustomerJdbcConverter converter = (CustomerJdbcConverter) JdbcFactory.getConverter(customer);
        return converter.updateBirthdayRewardDate(customer, getAppCommandContext());
    }

    @Override
    protected ArrayList<ContentProviderOperation> createDbOperations() {
        ArrayList<ContentProviderOperation> ops = new ArrayList<>(1);
        ops.add(ContentProviderOperation.newUpdate(ShopProvider.contentUri(CustomerTable.URI_CONTENT))
                .withValue(CustomerTable.BIRTHDAY_REWARD_APPLY_DATE, customer.birthdayRewardApplyDate.getTime())
                .withSelection(CustomerTable.GUID + " = ?", new String[]{customer.guid})
                .build()
        );
        return ops;
    }

    public boolean sync(Context context, String customerId, Date rewardDate, IAppCommandContext appCommandContext){
        Bundle args = new Bundle(2);
        args.putString(ARG_CUSTOMER_ID, customerId);
        args.putSerializable(ARG_REWARD_DATE, rewardDate);
        return !isFailed(syncStandalone(context, args, appCommandContext));
    }
}
