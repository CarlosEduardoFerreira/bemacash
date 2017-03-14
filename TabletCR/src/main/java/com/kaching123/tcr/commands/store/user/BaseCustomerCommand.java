package com.kaching123.tcr.commands.store.user;

import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.getbase.android.db.provider.ProviderAction;
import com.kaching123.tcr.commands.loyalty.AddLoyaltyPointsMovementCommand;
import com.kaching123.tcr.commands.store.AsyncCommand;
import com.kaching123.tcr.model.CustomerModel;
import com.kaching123.tcr.service.BatchSqlCommand;
import com.kaching123.tcr.service.OfflineCommandsService;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore;
import com.telly.groundy.TaskResult;
import com.telly.groundy.annotations.OnFailure;
import com.telly.groundy.annotations.OnSuccess;
import com.telly.groundy.annotations.Param;

import java.math.BigDecimal;
import java.util.ArrayList;

import static com.kaching123.tcr.model.ContentValuesUtil._decimal;
import static com.kaching123.tcr.model.SqlCommandHelper.getContentValues;
import static com.kaching123.tcr.store.ShopStore.CustomerTable;

/**
 * Created by Vladimir on 21.02.14.
 */
public abstract class BaseCustomerCommand extends AsyncCommand {

    public static enum Error {EMAIL_EXISTS, PHONE_EXISTS, BARCODE_EXISTS}

    protected static final Uri CUSTOMER_URI = ShopProvider.getContentUri(CustomerTable.URI_CONTENT);

    protected static final String ARG_CUSTOMER = "ARG_CUSTOMER";

    protected static final String EXTRA_ERROR = "EXTRA_ERROR";

    protected ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();

    protected CustomerModel model;
    protected SyncResult pointsMovementResult;

    protected BatchSqlCommand sql;

    @Override
    protected TaskResult doCommand() {

        model = (CustomerModel) getArgs().getSerializable(ARG_CUSTOMER);

        if (!ignoreChecks()){
            if (checkEmailExists())
                return failed().add(EXTRA_ERROR, Error.EMAIL_EXISTS);
            if (checkPhoneExists())
                return failed().add(EXTRA_ERROR, Error.PHONE_EXISTS);
            if (checkBarcodeExists())
                return failed().add(EXTRA_ERROR, Error.BARCODE_EXISTS);
        }

        pointsMovementResult = addPointsMovement();

        doQuery(operations);

        /** Upload *********************************************************/
        ContentValues values = getContentValues(sql, System.currentTimeMillis(), false);
        getContext().getContentResolver().insert(ShopProvider.contentUri(ShopStore.SqlCommandTable.URI_CONTENT), values);
        OfflineCommandsService.startUpload(getContext());
        /********************************************************* Upload **/

        return succeeded();
    }

    private SyncResult addPointsMovement(){
        Cursor c = ProviderAction.query(CUSTOMER_URI)
                .projection(CustomerTable.TMP_LOYALTY_POINTS)
                .where(CustomerTable.GUID + " = ?", model.guid)
                .perform(getContext());

        BigDecimal oldPoints;
        if (c.moveToFirst()){
            oldPoints = _decimal(c, 0, BigDecimal.ZERO);
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

    protected abstract boolean ignoreChecks();

    protected abstract void doQuery(ArrayList<ContentProviderOperation> operations);

    private boolean checkEmailExists(){
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

    private boolean checkPhoneExists(){
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

    private boolean checkBarcodeExists(){
        if (TextUtils.isEmpty(model.loyaltyBarcode))
            return false;

        Cursor c = ProviderAction
                .query(CUSTOMER_URI)
                .where(CustomerTable.LOYALTY_BARCODE + " = ?", model.loyaltyBarcode)
                .where(CustomerTable.GUID + " <> ?", model.guid)
                .perform(getContext());

        boolean exists = c.moveToFirst();
        c.close();
        return exists;
    }

    public static abstract class BaseCustomerCallback {

        @OnSuccess(BaseCustomerCommand.class)
        public void onSuccess2() {
            onSuccess();
        }

        @OnFailure(BaseCustomerCommand.class)
        public void onFailure2(@Param(EXTRA_ERROR) Error error) {
            if (error == null){
                onError();
            }else if (error == Error.EMAIL_EXISTS){
                onEmailExists();
            }else if (error == Error.PHONE_EXISTS){
                onPhoneExists();
            }else if (error == Error.BARCODE_EXISTS){
                onBarcodeExists();
            }
        }

        protected abstract void onSuccess();
        protected abstract void onError();
        protected abstract void onEmailExists();
        protected abstract void onPhoneExists();
        protected abstract void onBarcodeExists();
    }


}
