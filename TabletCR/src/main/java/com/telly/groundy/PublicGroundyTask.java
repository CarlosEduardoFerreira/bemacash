package com.telly.groundy;

import android.content.Context;
import android.os.Bundle;

import com.kaching123.tcr.BuildConfig;
import com.kaching123.tcr.TcrApplication;
import com.kaching123.tcr.model.payment.blackstone.payment.User;
import com.kaching123.tcr.model.payment.blackstone.prepaid.PrepaidUser;

/**
 * Created by hamst_000 on 05/11/13.
 */
public abstract class PublicGroundyTask extends GroundyTask {

    private static final String ARG_APP_CONTEXT_SHOP_ID = "ARG_APP_CONTEXT_SHOP_ID";
    private static final String ARG_APP_CONTEXT_EMPLOYEE_GUID = "ARG_APP_CONTEXT_EMPLOYEE_GUID";
    private static final String ARG_APP_CONTEXT_REGISTER_ID = "ARG_APP_CONTEXT_REGISTER_ID";
    private static final String ARG_APP_CONTEXT_SHIFT_GUID = "ARG_APP_CONTEXT_SHIFT_GUID";
    private static final String ARG_APP_CONTEXT_PREPAID_USER = "ARG_APP_CONTEXT_PREPAID_USER";
    private static final String ARG_APP_CONTEXT_EMPLOYEE_LOGIN = "ARG_APP_CONTEXT_EMPLOYEE_LOGIN";
    private static final String ARG_APP_CONTEXT_BLACKSTONE_USER = "ARG_APP_CONTEXT_BLACKSTONE_USER";
    private static final String ARG_APP_CONTEXT_EMPLOYEE_FULL_NAME = "ARG_APP_CONTEXT_EMPLOYEE_FULL_NAME";
    private static final String ARG_APP_CONTEXT_MAX_ITEMS_COUNT = "ARG_APP_CONTEXT_MAX_ITEMS_COUNT";

    static {
        L.logEnabled = BuildConfig.DEBUG;
    }

    private boolean isSync;

    protected static <T extends GroundyTask> Groundy create(Class<T> clazz){
        TcrApplication app = TcrApplication.get();
        return Groundy.create(clazz)
                .arg(ARG_APP_CONTEXT_SHOP_ID, app.getShopId())
                .arg(ARG_APP_CONTEXT_EMPLOYEE_GUID, app.getOperatorGuid())
                .arg(ARG_APP_CONTEXT_REGISTER_ID, app.getRegisterId())
                .arg(ARG_APP_CONTEXT_SHIFT_GUID, app.getShiftGuid())
                .arg(ARG_APP_CONTEXT_PREPAID_USER, app.getPrepaidUser())
                .arg(ARG_APP_CONTEXT_EMPLOYEE_LOGIN, app.getOperatorLogin())
                .arg(ARG_APP_CONTEXT_BLACKSTONE_USER, app.getBlackStoneUser())
                .arg(ARG_APP_CONTEXT_EMPLOYEE_FULL_NAME, app.getOperatorFullName())
                .arg(ARG_APP_CONTEXT_MAX_ITEMS_COUNT, app.getShopInfo().maxItemsCount);
    }

    private IAppCommandContext appCommandContext;

    protected PublicGroundyTask(){}

    protected PublicGroundyTask(Context context, Bundle args){
        setContext(context);
        addArgs(args);
    }

    protected boolean isSync() {
        return isSync;
    }

    protected PublicGroundyTask(Context context, Bundle args, IAppCommandContext appCommandContext){
        setContext(context);
        addArgs(args);
        this.appCommandContext = appCommandContext;
    }

    private IAppCommandContext extractAppCommandContext() {
        return new AppCommandContext(getLongArg(ARG_APP_CONTEXT_SHOP_ID),
                getStringArg(ARG_APP_CONTEXT_EMPLOYEE_GUID),
                getLongArg(ARG_APP_CONTEXT_REGISTER_ID),
                getStringArg(ARG_APP_CONTEXT_SHIFT_GUID),
                (PrepaidUser) getArgs().getSerializable(ARG_APP_CONTEXT_PREPAID_USER),
                getStringArg(ARG_APP_CONTEXT_EMPLOYEE_LOGIN),
                (User) getArgs().getParcelable(ARG_APP_CONTEXT_BLACKSTONE_USER),
                getStringArg(ARG_APP_CONTEXT_EMPLOYEE_FULL_NAME),
                getLongArg(ARG_APP_CONTEXT_MAX_ITEMS_COUNT));
    }

    protected IAppCommandContext getAppCommandContext() {
        if (appCommandContext == null)
            appCommandContext = extractAppCommandContext();
        return appCommandContext;
    }

    @Override
    void stopTask(int reason) {
        super.stopTask(reason);
        onCancel();
    }

    protected void onCancel(){};

    public TaskResult sync(Context context, Bundle args, IAppCommandContext appCommandContext){
        isSync = true;
        setContext(context);
        addArgs(args);
        this.appCommandContext = appCommandContext;
        TaskResult result = doInBackground();
        isSync = false;
        return result;
    }

    protected static boolean isFailed(TaskResult result){
        return result instanceof Failed;
    }

    protected TcrApplication getApp(){
        return (TcrApplication)getContext().getApplicationContext();
    }

    protected static Bundle getBundle(TaskResult result){
        return result.getResultData();
    }


    protected static class AppCommandContext implements IAppCommandContext {

        private final String employeeGuid;
        private final long shopId;
        private final long registerId;
        private final String shiftGuid;
        private final PrepaidUser prepaidUser;
        private final String employeeLogin;
        private final User blackstoneUser;
        private final String employeeFullName;
        private final long maxItemsCount;

        public AppCommandContext(long shopId, String employeeGuid, long registerId, String shiftGuid, PrepaidUser prepaidUser, String employeeLogin, User blackstoneUser, String employeeFullName,
                                 long maxItemsCount) {
            this.shopId = shopId;
            this.employeeGuid = employeeGuid;
            this.registerId = registerId;
            this.shiftGuid = shiftGuid;
            this.prepaidUser = prepaidUser;
            this.employeeLogin = employeeLogin;
            this.blackstoneUser = blackstoneUser;
            this.employeeFullName = employeeFullName;
            this.maxItemsCount = maxItemsCount;
        }

        @Override
        public long getShopId() {
            return shopId;
        }

        @Override
        public String getEmployeeGuid() {
            return employeeGuid;
        }

        @Override
        public long getRegisterId() {
            return registerId;
        }

        @Override
        public String getShiftGuid() {
            return shiftGuid;
        }

        @Override
        public PrepaidUser getPrepaidUser() {
            return prepaidUser;
        }

        @Override
        public String getEmployeeLogin() {
            return employeeLogin;
        }

        @Override
        public User getBlackstoneUser() {
            return blackstoneUser;
        }

        @Override
        public String getEmployeeFullName() {
            return employeeFullName;
        }

        @Override
        public long getMaxItemsCount() {
            return maxItemsCount;
        }
    }

    public interface IAppCommandContext {

        public long getShopId();

        public String getEmployeeGuid();

        public long getRegisterId();

        public String getShiftGuid();

        public PrepaidUser getPrepaidUser();

        public String getEmployeeLogin();

        public User getBlackstoneUser();

        public String getEmployeeFullName();

        public long getMaxItemsCount();
    }
}
