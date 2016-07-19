package com.kaching123.tcr.commands.store.user;

import android.content.ContentProviderOperation;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;

import com.getbase.android.db.provider.ProviderAction;
import com.kaching123.tcr.commands.loyalty.AddLoyaltyPointsMovementCommand;
import com.kaching123.tcr.commands.store.AsyncCommand;
import com.kaching123.tcr.model.CustomerModel;
import com.kaching123.tcr.store.ShopProvider;
import com.telly.groundy.TaskResult;

import java.math.BigDecimal;
import java.util.ArrayList;

import static com.kaching123.tcr.model.ContentValuesUtil._decimal;
import static com.kaching123.tcr.store.ShopStore.CustomerTable;

/**
 * Created by Vladimir on 21.02.14.
 */
public abstract class BaseCustomerCommand extends AsyncCommand {

    public static enum Error {EMAIL_EXISTS}

    protected static final Uri CUSTOMER_URI = ShopProvider.getContentUri(CustomerTable.URI_CONTENT);

    protected static final String ARG_CUSTOMER = "ARG_CUSTOMER";

    protected static final String EXTRA_ERROR = "EXTRA_ERROR";

    protected ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();

    protected CustomerModel model;
    protected SyncResult pointsMovementResult;

    @Override
    protected TaskResult doCommand() {

        model = (CustomerModel) getArgs().getSerializable(ARG_CUSTOMER);

        if ((!ignoreEmailCheck() && checkEmail()) || checkPhone())
            return failed().add(EXTRA_ERROR, Error.EMAIL_EXISTS);

        pointsMovementResult = addPointsMovement();

        doQuery(operations);

        return succeeded();
    }

    private SyncResult addPointsMovement(){
        Cursor c = ProviderAction.query(CUSTOMER_URI)
                .projection(CustomerTable.TMP_LOYALTY_POINTS)
                .where(CustomerTable.GUID + " = ?", model.guid)
                .perform(getContext());

        BigDecimal oldPoints;
        if (c.moveToFirst()){
            oldPoints = _decimal(c, 0);
        }else {
            oldPoints = BigDecimal.ZERO;
        }
        c.close();
        BigDecimal newPoints = model.loyaltyPoints == null ? BigDecimal.ZERO : model.loyaltyPoints;
        BigDecimal difference = newPoints.subtract(oldPoints);
        if (BigDecimal.ZERO.compareTo(difference) != 0){
            return new AddLoyaltyPointsMovementCommand().sync(getContext(), model.guid, difference, getAppCommandContext());
        }

        return null;
    }

    @Override
    protected ArrayList<ContentProviderOperation> createDbOperations() {
        return operations;
    }

    protected abstract boolean ignoreEmailCheck();

    protected abstract void doQuery(ArrayList<ContentProviderOperation> operations);

    private boolean checkEmail(){
        if (TextUtils.isEmpty(model.email))
            return false;

        Cursor c = ProviderAction
                .query(CUSTOMER_URI)
                .where(CustomerTable.EMAIL + " = ?", model.email)
                .where(CustomerTable.GUID + " <> ?", model.guid)
                .perform(getContext());
        boolean exists = c.moveToFirst();
        c.close();
        return exists;
    }

    private boolean checkPhone(){
        if (TextUtils.isEmpty(model.phone))
            return false;

        Cursor c = ProviderAction
                .query(CUSTOMER_URI)
                .where(CustomerTable.PHONE + " = ?", model.phone)
                .where(CustomerTable.GUID + " <> ?", model.guid)
                .perform(getContext());
        boolean exists = c.moveToFirst();
        c.close();
        return exists;
    }


}
