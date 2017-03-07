package com.kaching123.tcr.commands.store.user;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;

import com.getbase.android.db.provider.ProviderAction;
import com.kaching123.tcr.Logger;
import com.kaching123.tcr.TcrApplication;
import com.kaching123.tcr.commands.local.EndUncompletedTransactionsCommand;
import com.kaching123.tcr.commands.local.EndUncompletedTransactionsCommand.EndUncompletedTransactionsResult;
import com.kaching123.tcr.commands.rest.sync.AuthResponse;
import com.kaching123.tcr.commands.rest.sync.AuthResponse.AuthInfo;
import com.kaching123.tcr.commands.rest.sync.SyncApi;
import com.kaching123.tcr.commands.rest.sync.SyncUploadRequestBuilder;
import com.kaching123.tcr.commands.support.SendLogCommand;
import com.kaching123.tcr.model.EmployeeModel;
import com.kaching123.tcr.model.EmployeePermissionsModel;
import com.kaching123.tcr.model.EmployeeStatus;
import com.kaching123.tcr.model.OrderStatus;
import com.kaching123.tcr.model.Permission;
import com.kaching123.tcr.model.RegisterModel.RegisterStatus;
import com.kaching123.tcr.service.LocalSyncHelper;
import com.kaching123.tcr.service.SyncCommand;
import com.kaching123.tcr.service.SyncCommand.OfflineException;
import com.kaching123.tcr.service.SyncCommand.SyncException;
import com.kaching123.tcr.service.SyncCommand.SyncInconsistentException;
import com.kaching123.tcr.service.SyncCommand.SyncInterruptedException;
import com.kaching123.tcr.service.v2.UploadTaskV2;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopProviderExt;
import com.kaching123.tcr.store.ShopStore;
import com.kaching123.tcr.store.ShopStore.EmployeePermissionTable;
import com.kaching123.tcr.store.ShopStore.RegisterTable;
import com.kaching123.tcr.store.ShopStore.SaleOrderTable;
import com.kaching123.tcr.store.SyncOpenHelper;
import com.kaching123.tcr.util.Util;
import com.telly.groundy.Groundy;
import com.telly.groundy.GroundyTask;
import com.telly.groundy.TaskResult;
import com.telly.groundy.annotations.OnFailure;
import com.telly.groundy.annotations.OnSuccess;
import com.telly.groundy.annotations.Param;

import java.util.HashSet;
import java.util.Set;

import static com.kaching123.tcr.model.ContentValuesUtil._enum;

//i think we should use command because it will be check subscription titledDate too
public class LoginCommand extends GroundyTask {

    public static enum Error {LOGIN_FAILED, SYNC_OUTDATED, SYNC_FAILED, REGISTER_PENDING, REGISTER_CHECK_FAILED, EMPLOYEE_NOT_ACTIVE, OFFLINE, SYNC_INCONSISTENT, LOGIN_OFFLINE_FAILED, SYNC_LOCKED, SYNC_INTERRUPTED,BLOCK_MERCHANT}

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
    protected UploadTaskV2 uploadTaskV2Adapter;
    protected static final Uri URI_SQL_COMMAND_NO_NOTIFY = ShopProvider.getNoNotifyContentUri(ShopStore.SqlCommandTable.URI_CONTENT);
    protected static final Uri URI_EMPLOYEE_SYNCED = ShopProvider.getNoNotifyContentUri(ShopStore.EmployeeTable.URI_CONTENT);

    @Override
    protected TaskResult doInBackground() {
        Logger.d("LoginCommand.doInBackground");
        LocalSyncHelper.disableLocalSync();

        try {
            TcrApplication app = ((TcrApplication) getContext().getApplicationContext());
            String userName = getStringArg(ARG_USER);
            String password = getStringArg(ARG_PASSWORD);
            Mode mode = (Mode) getArgs().getSerializable(ARG_MODE);

            boolean isTrainingMode = TcrApplication.get().isTrainingMode();
            String registerSerial = app.getRegisterSerial();
            boolean isOffline = !Util.isNetworkAvailable(getContext());

            String lastUserName = getLastUserName() == null ? userName : getLastUserName();
            String lastUserPassword = getLastUserPassword() == null ? password : getLastUserPassword();
            boolean isEmployeeSuccessUpload = true;
            if (lastUserName != null) {
                uploadTaskV2Adapter = new UploadTaskV2(loginLocal(lastUserName, lastUserPassword));
                try {
                    isEmployeeSuccessUpload = doEmployeeUpload();
                } catch (SyncCommand.SyncLockedException e) {
                    e.printStackTrace();
                }
            }

            if (mode == Mode.LOGIN && !isTrainingMode && !isOffline) {
                Logger.d("Performing remote login... login: %s, serial: %s", userName, registerSerial);
                RemoteLoginResult remoteLoginResult = webLogin(registerSerial, userName, password);
                if (remoteLoginResult != null) {
                    if (remoteLoginResult.registerNumber == null) {
                        Logger.d("Remote login FAILED! register check failed");
                        return failed().add(EXTRA_ERROR, Error.REGISTER_CHECK_FAILED);
                    } else if (remoteLoginResult.registerNumber != RegisterStatus.ACTIVE) {
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

                    if (employeeModel.login != null && !isOffline)
                        setLastUserName(employeeModel.login);
                    if (employeeModel.password != null && !isOffline)
                        setLastUserPassword(employeeModel.password);

                    Error syncError = null;
                    try {
                        syncError = syncData(employeeModel);
                    } catch (OfflineException e) {
                        e.printStackTrace();
                    }

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
            Logger.d("logincommand: success : " + app.getLastUserName());
            Logger.d("Login success!");

            return succeeded()
                    .add(EXTRA_EMPLOYEE, new EmployeePermissionsModel(employeeModel, getEmployeePermissions(employeeModel)))
                    .add(EXTRA_UPLOAD_TRANSACTION_INVALID, hadInvalidUploadTransaction)
                    .add(EXTRA_UPLOAD_UNCOMPLETED_SALE_ORDER_GUID, lastUncompletedSaleOrderGuid);

        } finally {
            LocalSyncHelper.enableLocalSync();
        }

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

    private void markEmployeeSynced() {

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

    private boolean doEmployeeUpload() throws SyncCommand.SyncLockedException {
        Logger.d("[OfflineService] doUpload: isManual = false");
//        executor.submit(new UploadTask(this, false, true));
        ContentResolver cr = getContext().getContentResolver();
        boolean updateEmployeeInfoSucceed = uploadTaskV2Adapter.employeeUpload(cr, getContext());
        if (updateEmployeeInfoSucceed) {
            cr.delete(URI_SQL_COMMAND_NO_NOTIFY, ShopStore.SqlCommandTable.IS_SENT + " = ?", new String[]{"1"});
            ContentValues v = new ContentValues(1);
            v.put(ShopStore.EmployeeTable.IS_SYNC, "1");
            cr.update(URI_EMPLOYEE_SYNCED, v, ShopStore.EmployeeTable.IS_SYNC + " = ?", new String[]{"0"});
        }

        return updateEmployeeInfoSucceed;
    }

    private String getLastUserName() {
        return ((TcrApplication) getContext().getApplicationContext()).getLastUserName();
    }

    private void setLastUserName(String name) {
        ((TcrApplication) getContext().getApplicationContext()).setLastUserName(name);
    }

    private String getLastUserPassword() {
        return ((TcrApplication) getContext().getApplicationContext()).getLastUserPassword();
    }

    private void setLastUserPassword(String password) {
        ((TcrApplication) getContext().getApplicationContext()).setLastUserPassword(password);
    }

    private Error syncData(EmployeeModel employeeModel) throws OfflineException {
        try {
            new SyncCommand(getContext(), true).syncNow(employeeModel);
        } catch (SyncException e) {
            Logger.e("Login.sync error", e);
            return Error.SYNC_FAILED;
        } catch (SyncCommand.DBVersionCheckException e) {
            Logger.e("Login.sync error", e);
            return Error.SYNC_OUTDATED;
        } catch (SyncInconsistentException e) {
            Logger.e("Login.sync error", e);
            return Error.SYNC_INCONSISTENT;
        } catch (SyncCommand.SyncLockedException e) {
            Logger.e("Login.sync error", e);
            return Error.SYNC_LOCKED;
        } catch (SyncInterruptedException e) {
            Logger.e("Login.sync error", e);
            return Error.SYNC_INTERRUPTED;
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

            try {
                AuthInfo info = resp.getResponse();
                if (info == null) {
                    Logger.e("Login web login error: response is empty");
                    return null;
                }
                return new RemoteLoginResult(info.register.status, info.employee);
            }catch(Exception e){
                Logger.e("Login FAILED!", e);
            }

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

//    private EmployeeModel LastSuccessLogin(String userName, String lastPassword) {
//        EmployeeModel model = null;
//        Cursor c = ProviderAction.query(ShopProvider.getContentUri(ShopStore.EmployeeTable.URI_CONTENT))
//                .where(ShopStore.EmployeeTable.LOGIN + " = ?", userName)
//                .perform(getContext());
//
//        if (c.moveToFirst()) {
//            model = new EmployeeModel(c);
//            model.password = lastPassword;
//        }
//        c.close();
//        return model;
//    }

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

    public static boolean setRegisterId() {
        TcrApplication app = TcrApplication.get();

        Cursor c = ProviderAction.query(ShopProvider.getContentUri(RegisterTable.URI_CONTENT))
                .projection(RegisterTable.ID, RegisterTable.PREPAID_TID, RegisterTable.BLACKSTONE_PAYMENT_CID,
                            RegisterTable.DESCRIPTION, RegisterTable.TITLE)
                .where(RegisterTable.REGISTER_SERIAL + " = ?", app.getRegisterSerial())
                .where(RegisterTable.STATUS + " <> ?", RegisterStatus.BLOCKED.ordinal())
                .perform(app.getApplicationContext());

        if (!c.moveToFirst()) {
            c.close();
            return false;
        }

        app.setRegisterInfo(c.getLong(0), c.getInt(1), c.getInt(2), c.getString(3), c.getString(4));
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
                    onLoginOfflineFailed();
                    break;
                case SYNC_LOCKED:
                    onSyncLocked();
                    break;
                case SYNC_INTERRUPTED:
                    onSyncInterrupted();
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
        protected abstract void onSyncLocked();

        protected abstract void onSyncInterrupted();
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
