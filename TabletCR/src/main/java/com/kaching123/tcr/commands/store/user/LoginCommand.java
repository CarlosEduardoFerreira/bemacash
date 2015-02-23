package com.kaching123.tcr.commands.store.user;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;

import com.getbase.android.db.provider.ProviderAction;
import com.kaching123.tcr.Logger;
import com.kaching123.tcr.TcrApplication;
import com.kaching123.tcr.commands.local.EndUncompletedTransactionsCommand;
import com.kaching123.tcr.commands.local.EndUncompletedTransactionsCommand.EndUncompletedTransactionsResult;
import com.kaching123.tcr.commands.rest.sync.AuthResponse;
import com.kaching123.tcr.commands.rest.sync.AuthResponse.AuthInfo;
import com.kaching123.tcr.commands.rest.sync.GetResponse;
import com.kaching123.tcr.commands.rest.sync.SyncApi;
import com.kaching123.tcr.commands.rest.sync.SyncApi2;
import com.kaching123.tcr.commands.rest.sync.SyncUploadRequestBuilder;
import com.kaching123.tcr.commands.support.SendLogCommand;
import com.kaching123.tcr.jdbc.converters.ShopInfoViewJdbcConverter;
import com.kaching123.tcr.model.EmployeeModel;
import com.kaching123.tcr.model.EmployeePermissionsModel;
import com.kaching123.tcr.model.EmployeeStatus;
import com.kaching123.tcr.model.OrderStatus;
import com.kaching123.tcr.model.Permission;
import com.kaching123.tcr.model.RegisterModel.RegisterStatus;
import com.kaching123.tcr.service.SyncCommand;
import com.kaching123.tcr.service.SyncCommand.OfflineException;
import com.kaching123.tcr.service.SyncCommand.SyncException;
import com.kaching123.tcr.service.SyncCommand.SyncInconsistentException;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopProviderExt;
import com.kaching123.tcr.store.ShopStore;
import com.kaching123.tcr.store.ShopStore.EmployeePermissionTable;
import com.kaching123.tcr.store.ShopStore.RegisterTable;
import com.kaching123.tcr.store.ShopStore.SaleOrderTable;
import com.kaching123.tcr.store.SyncOpenHelper;
import com.kaching123.tcr.util.JdbcJSONObject;
import com.kaching123.tcr.util.Util;
import com.telly.groundy.Groundy;
import com.telly.groundy.GroundyTask;
import com.telly.groundy.TaskResult;
import com.telly.groundy.annotations.OnFailure;
import com.telly.groundy.annotations.OnSuccess;
import com.telly.groundy.annotations.Param;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.Set;

import retrofit.RetrofitError;

import static com.kaching123.tcr.model.ContentValuesUtil._enum;

//i think we should use command because it will be check subscription titledDate too
public class LoginCommand extends GroundyTask {

    public static enum Error {LOGIN_FAILED, SYNC_OUTDATED, SYNC_FAILED, REGISTER_CHECK_FAILED, EMPLOYEE_NOT_ACTIVE, OFFLINE, SYNC_INCONSISTENT, LOGIN_OFFLINE_FAILED, REGISTER_PENDING, BLOCK_MERCHANT}

    public static enum Mode {
        LOGIN, SWITCH
    }

    private static final String EXTRA_EMPLOYEE = "extra_employee";
    private static final String EXTRA_UPLOAD_TRANSACTION_INVALID = "extra_upload_transaction_invalid";
    private static final String EXTRA_UPLOAD_UNCOMPLETED_SALE_ORDER_GUID = "extra_upload_uncompleted_sale_order_guid";
    private static final String EXTRA_ERROR = "extra_error";

    private static final String ARG_PASSWORD = "arg_password";
    private static final String ARG_USER = "arg_username";
    private static final String ARG_MODE = "arg_mode";

    @Override
    protected TaskResult doInBackground() {
        Logger.d("LoginCommand.doInBackground");
        TcrApplication app = ((TcrApplication) getContext().getApplicationContext());
        String userName = getStringArg(ARG_USER);
        String password = getStringArg(ARG_PASSWORD);
        Mode mode = (Mode) getArgs().getSerializable(ARG_MODE);

        boolean isTrainingMode = TcrApplication.get().isTrainingMode();
        String registerSerial = app.getRegisterSerial();
        boolean isOffline = !Util.isNetworkAvailable(getContext());

        if (mode == Mode.LOGIN && !isTrainingMode && !isOffline) {
            Logger.d("Performing remote login... login: %s, serial: %s", userName, registerSerial);
            RemoteLoginResult remoteLoginResult = webLogin(registerSerial, userName, password);
            if (remoteLoginResult != null) {
                if (remoteLoginResult.registerNumber == null) {
                    Logger.d("Remote login FAILED! register check failed");
                    return failed().add(EXTRA_ERROR, Error.REGISTER_CHECK_FAILED);
                }
                else if(remoteLoginResult.registerNumber != RegisterStatus.ACTIVE)
                {
                    Logger.d("Remote login FAILED! register pending failed");
                    return failed().add(EXTRA_ERROR, Error.REGISTER_PENDING);
                }
                EmployeeModel employeeModel = remoteLoginResult.employeeModel;
                if (employeeModel == null) {
                    Logger.d("Remote login FAILED! employee is null");
                    return failed().add(EXTRA_ERROR, Error.LOGIN_FAILED);
                }
                if (employeeModel.status != EmployeeStatus.ACTIVE) {
                    Logger.d("Remote login FAILED! employee not active");
                    return failed().add(EXTRA_ERROR, Error.EMPLOYEE_NOT_ACTIVE);
                }

                boolean cleaned = checkDb(employeeModel);
                try {
                    syncShopInfo(employeeModel);
                } catch (SyncException e) {
                    e.printStackTrace();
                    return failed().add(EXTRA_ERROR, Error.LOGIN_FAILED);
                } catch (SyncCommand.BlockException e) {
                    e.printStackTrace();
                    return failed().add(EXTRA_ERROR, Error.BLOCK_MERCHANT);
                }

                Error syncError = syncData(employeeModel);
                if (syncError != null && syncError != Error.OFFLINE) {
                    SendLogCommand.start(getContext());
                }
                if (cleaned && syncError != null) {
                    return failed().add(EXTRA_ERROR, syncError);
                }
            }
        }

        Logger.d("Performing local login...");
        boolean isOfflineModeExpired = mode == Mode.LOGIN && !isTrainingMode && TcrApplication.get().isOfflineModeExpired();
        EmployeeModel employeeModel = null;
        if (!isOfflineModeExpired)
            employeeModel = loginLocal(userName, password);

        if (employeeModel == null) {
            Logger.d("Local login FAILED! employee is null");
            if (isOffline) {
                return failed().add(EXTRA_ERROR, Error.LOGIN_OFFLINE_FAILED);
            }
            return failed().add(EXTRA_ERROR, Error.LOGIN_FAILED);
        }

        if (employeeModel.status != EmployeeStatus.ACTIVE) {
            Logger.d("Local login FAILED! employee not active");
            return failed().add(EXTRA_ERROR, Error.EMPLOYEE_NOT_ACTIVE);
        }

        if (mode == Mode.LOGIN) {
            boolean registerChecked = setRegisterId();
            if (!registerChecked) {
                Logger.d("Local login FAILED! register check failed");
                return failed().add(EXTRA_ERROR, Error.REGISTER_CHECK_FAILED);
            }
        }

        boolean hadInvalidUploadTransaction = false;
        String lastUncompletedSaleOrderGuid = null;
        if (mode == Mode.LOGIN && !isTrainingMode) {
            EndUncompletedTransactionsResult result = new EndUncompletedTransactionsCommand().sync(getContext());
            if (hadInvalidUploadTransaction = result.hadInvalidUploadTransaction) {
                lastUncompletedSaleOrderGuid = tryGetLastUncompletedSaleOrderGuid();
                Logger.e("Login: had invalid upload transactions, last uncompleted sale order guid: " + lastUncompletedSaleOrderGuid);
            }
        }

        Logger.d("Login success!");
        return succeeded()
                .add(EXTRA_EMPLOYEE, new EmployeePermissionsModel(employeeModel, getEmployeePermissions(employeeModel)))
                .add(EXTRA_UPLOAD_TRANSACTION_INVALID, hadInvalidUploadTransaction)
                .add(EXTRA_UPLOAD_UNCOMPLETED_SALE_ORDER_GUID, lastUncompletedSaleOrderGuid);
    }
    private void syncShopInfo(EmployeeModel employeeModel) throws SyncException, SyncCommand.BlockException {
        TcrApplication app = TcrApplication.get();
        SyncApi2 api = app.getRestAdapter().create(SyncApi2.class);

        boolean isShopAliave = false;
        try {
            GetResponse resp = makeShopInfoRequest(api, app.emailApiKey, SyncUploadRequestBuilder.getReqCredentials(employeeModel, app));
            if (resp == null || !resp.isSuccess()) {
                Logger.e("can't parse shop", new RuntimeException());
                throw new SyncException();
            }
            JdbcJSONObject entity = resp.getEntity();
            isShopAliave = syncShop(entity.getJSONObject("SHOP"));

        } catch (Exception e) {
            Logger.e("Can't sync shop info", e);
            throw new SyncException();
        }
        if (!isShopAliave)
            throw new SyncCommand.BlockException();
    }

    private boolean syncShop(JdbcJSONObject shop) throws SyncException {
        if (shop == null) {
            Logger.e("can't parse shop", new RuntimeException());
            throw new SyncException();
        }
        ShopInfoViewJdbcConverter.ShopInfo info;
        try {
            info = ShopInfoViewJdbcConverter.read(shop);
            if (info.shopStatus == ShopInfoViewJdbcConverter.ShopStatus.BLOCKED || info.shopStatus == ShopInfoViewJdbcConverter.ShopStatus.DISABLED)
                return false;
        } catch (JSONException e) {
            Logger.e("can't parse shop", e);
            throw new SyncException();
        }        return true;
    }

    private GetResponse makeShopInfoRequest(SyncApi2 api, String apiKey, JSONObject credentials) throws JSONException, SyncException {
        int retry = 0;
        while (retry++ < 5) {
            try {
                return api.downloadShopInfo(apiKey, credentials);
            } catch (RetrofitError e) {
                Logger.e("attempt: " + retry, e);
            }
        }
        throw new SyncException();
    }
    private String tryGetLastUncompletedSaleOrderGuid() {
        String lastUncompletedSaleOrderGuid = TcrApplication.get().getShopPref().lastUncompletedSaleOrderGuid().get();
        if (TextUtils.isEmpty(lastUncompletedSaleOrderGuid))
            return null;

        Cursor c = ProviderAction.query(ShopProvider.contentUri(SaleOrderTable.URI_CONTENT))
                .projection("1")
                .where(SaleOrderTable.GUID + " = ?", lastUncompletedSaleOrderGuid)
                .where(SaleOrderTable.STATUS + " = ?", _enum(OrderStatus.ACTIVE))
                .perform(getContext());
        boolean orderIsActive = c.getCount() != 0;
        c.close();

        return orderIsActive ? lastUncompletedSaleOrderGuid : null;
    }

    protected boolean checkDb(EmployeeModel employeeModel) {
        TcrApplication app = ((TcrApplication) getContext().getApplicationContext());
        long shopId = app.getShopPref().shopId().getOr(0L);
        if (shopId != employeeModel.shopId) {
            clearDb();
            app.getShopPref().clear();
            app.getShopPref().shopId().put(employeeModel.shopId);
            return true;
        }
        return false;
    }

    private Error syncData(EmployeeModel employeeModel) {
        try {
            new SyncCommand(getContext(), true).syncNow(employeeModel, employeeModel.shopId);
        } catch (SyncCommand.BlockException e) {
            Logger.e("Login.sync error", e);
            return Error.BLOCK_MERCHANT;
        } catch (SyncException e) {
            Logger.e("Login.sync error", e);
            return Error.SYNC_FAILED;
        } catch (SyncCommand.DBVersionCheckException e) {
            Logger.e("Login.sync error", e);
            return Error.SYNC_OUTDATED;
        } catch (OfflineException e) {
            Logger.e("Login.sync error", e);
            return Error.OFFLINE;
        } catch (SyncInconsistentException e) {
            Logger.e("Login.sync error", e);
            return Error.SYNC_INCONSISTENT;
        } catch (Exception e) {
            Logger.e("Login.sync error", e);
            return Error.SYNC_FAILED;
        }
        return null;
    }

    private void clearDb() {
        boolean result = ShopProviderExt.callMethod(getContext(), ShopProviderExt.Method.METHOD_CLEAR_DATABASE_KEEP_SYNC, null, null);
        SyncOpenHelper syncOpenHelper = TcrApplication.get().getSyncOpenHelper();
        result = result && syncOpenHelper.clearDatabase();
        syncOpenHelper.close();
        if (!result) {
            throw new RuntimeException("Login clear db error");
        }
    }

    private RemoteLoginResult webLogin(String registerSerial, String userName, String password) {
        TcrApplication app = TcrApplication.get();
        SyncApi api = app.getRestAdapter().create(SyncApi.class);
        try {
            AuthResponse resp = api.auth(app.emailApiKey, SyncUploadRequestBuilder.getReqCredentials(userName, password, registerSerial, getContext()));
            if (resp == null) {
                Logger.e("Login web login error: can't get response");
                return null;
            }
            if (!resp.isSuccess()) {
                return new RemoteLoginResult(null, null);
            }
            AuthInfo info = resp.getResponse();
            if (info == null) {
                Logger.e("Login web login error: response is empty");
                return null;
            }
            return new RemoteLoginResult(info.register.status, info.employee);
        } catch (Exception e) {
            Logger.e("Remote login FAILED!", e);
        }
        return null;
    }

    private EmployeeModel loginLocal(String userName, String password) {
        EmployeeModel model = null;
        Cursor c = ProviderAction.query(ShopProvider.getContentUri(ShopStore.EmployeeTable.URI_CONTENT))
                .where(ShopStore.EmployeeTable.LOGIN + " = ?", userName)
                .where(ShopStore.EmployeeTable.PASSWORD + " = ?", password)
                .perform(getContext());

        if (c.moveToFirst()) {
            model = new EmployeeModel(c);
        }
        c.close();
        return model;
    }

    private Set<Permission> getEmployeePermissions(EmployeeModel employee) {
        HashSet<Permission> permissions = new HashSet<Permission>();
        Cursor c = ProviderAction.query(ShopProvider.getContentUri(ShopStore.EmployeePermissionTable.URI_CONTENT))
                .projection(ShopStore.EmployeePermissionTable.PERMISSION_ID)
                .where(EmployeePermissionTable.USER_GUID + " = ?", employee.guid)
                .where(EmployeePermissionTable.ENABLED + " = ?", "1")
                .perform(getContext());
        while (c.moveToNext()) {
            long id = c.getLong(0);
            Permission p = Permission.valueOfOrNull(id);
            if (p != null) {
                permissions.add(p);
            }
        }
        return permissions;
    }

    private boolean setRegisterId() {
        TcrApplication app = ((TcrApplication) getContext().getApplicationContext());

        Cursor c = ProviderAction.query(ShopProvider.getContentUri(RegisterTable.URI_CONTENT))
                .projection(RegisterTable.ID, RegisterTable.PREPAID_TID, RegisterTable.BLACKSTONE_PAYMENT_CID)
                .where(RegisterTable.REGISTER_SERIAL + " = ?", app.getRegisterSerial())
                .where(RegisterTable.STATUS + " <> ?", RegisterStatus.BLOCKED.ordinal())
                .perform(getContext());

        if (!c.moveToFirst()) {
            c.close();
            return false;
        }

        app.setRegisterId(c.getLong(0), c.getInt(1), c.getInt(2));
        c.close();

        return true;
    }

    public static void start(Context context, BaseLoginCommandCallback callback, String login, String password, Mode mode) {
        Groundy.create(LoginCommand.class)
                .arg(ARG_USER, login)
                .arg(ARG_PASSWORD, password)
                .arg(ARG_MODE, mode)
                .callback(callback)
                .queueUsing(context);
    }

    public static abstract class BaseLoginCommandCallback {

        @OnSuccess(LoginCommand.class)
        public void handleSuccess(@Param(EXTRA_EMPLOYEE) EmployeePermissionsModel employeeModel,
                                  @Param(EXTRA_UPLOAD_TRANSACTION_INVALID) boolean hadInvalidUploadTransaction,
                                  @Param(EXTRA_UPLOAD_UNCOMPLETED_SALE_ORDER_GUID) String lastUncompletedSaleOrderGuid) {
            if (hadInvalidUploadTransaction) {
                onInvalidUploadTransaction(employeeModel, lastUncompletedSaleOrderGuid);
                return;
            }
            onLoginSuccess(employeeModel);
        }

        @OnFailure(LoginCommand.class)
        public void handleFailure(@Param(EXTRA_ERROR) Error error) {
            if (error == null) {
                onLoginError();
                return;
            }

            switch (error) {
                case SYNC_FAILED:
                    onSyncError();
                    break;
                case SYNC_OUTDATED:
                    onOutDated();
                    break;
                case REGISTER_CHECK_FAILED:
                    onRegisterCheckError();
                    break;
                case REGISTER_PENDING:
                    onRegisterPending();
                    break;
                case EMPLOYEE_NOT_ACTIVE:
                    onEmployeeNotActive();
                    break;
                case OFFLINE:
                    onOffline();
                case SYNC_INCONSISTENT:
                    onSyncInconsistent();
                    break;
                case LOGIN_OFFLINE_FAILED:
                    onSyncInconsistent();
                    break;
                case BLOCK_MERCHANT:
                    onBlockMerchant();
                    break;
                default:
                    onLoginError();
            }
        }

        protected abstract void onEmployeeNotActive();

        protected abstract void onOutDated();

        protected abstract void onLoginSuccess(EmployeePermissionsModel employeeModel);

        protected abstract void onInvalidUploadTransaction(EmployeePermissionsModel employeeModel, String lastUncompletedSaleOrderGuid);

        protected abstract void onSyncError();

        protected abstract void onLoginError();

        protected abstract void onRegisterCheckError();

        protected abstract void onRegisterPending();

        protected abstract void onOffline();

        protected abstract void onSyncInconsistent();

        protected abstract void onLoginOfflineFailed();

        protected abstract void onBlockMerchant();
    }

    private static class RemoteLoginResult {
        final RegisterStatus registerNumber;
        final EmployeeModel employeeModel;

        private RemoteLoginResult(RegisterStatus registerNumber, EmployeeModel employeeModel) {
            this.registerNumber = registerNumber;
            this.employeeModel = employeeModel;
        }
    }
}
