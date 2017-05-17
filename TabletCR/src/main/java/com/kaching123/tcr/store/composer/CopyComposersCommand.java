package com.kaching123.tcr.store.composer;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.net.Uri;

import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.jdbc.converters.JdbcConverter;
import com.kaching123.tcr.model.ComposerModel;
import com.kaching123.tcr.service.BatchSqlCommand;
import com.kaching123.tcr.service.ISqlCommand;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore;
import com.telly.groundy.TaskResult;
import com.telly.groundy.annotations.OnFailure;
import com.telly.groundy.annotations.OnSuccess;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by mboychenko on 5/17/2017.
 */

public class CopyComposersCommand extends BaseComposerCommand {

    private static final Uri COMPOSER_URI = ShopProvider.contentUri(ShopStore.ComposerTable.URI_CONTENT);

    private static final String FROM_HOST_ITEM_GUID = "FROM_HOST_ITEM_GUID";
    private static final String TO_HOST_ITEM_GUID = "TO_HOST_ITEM_GUID";

    ArrayList<ContentProviderOperation> ops;
    private BatchSqlCommand sqlCommand;

    @Override
    protected TaskResult doCommand() {

        ops = new ArrayList<ContentProviderOperation>();
        sqlCommand = batchInsert(ComposerModel.class);
        JdbcConverter jdbcConverter = JdbcFactory.getConverter(ShopStore.ComposerTable.TABLE_NAME);

        String fromHostItemGuid = getStringArg(FROM_HOST_ITEM_GUID);
        String toHostItemGuid = getStringArg(TO_HOST_ITEM_GUID);

        List<ComposerModel> composers = ComposerModel.getChildsByHostId(getContext(), fromHostItemGuid);

        for (ComposerModel composer : composers) {
            composer.guid =  UUID.randomUUID().toString();
            composer.itemHostId = toHostItemGuid;

            ops.add(ContentProviderOperation.newInsert(COMPOSER_URI).withValues(composer.toValues()).build());
            sqlCommand.add(jdbcConverter.insertSQL(composer, this.getAppCommandContext()));
        }

        return succeeded();
    }

    @Override
    protected ArrayList<ContentProviderOperation> createDbOperations() {
        return ops;
    }

    @Override
    protected ISqlCommand createSqlCommand() {
        return sqlCommand;
    }

    public static void start(Context context, String fromHostItemGuid, String toHostItemGuid, CopyComposersCommandCallback callback){
        create(CopyComposersCommand.class).arg(FROM_HOST_ITEM_GUID, fromHostItemGuid).arg(TO_HOST_ITEM_GUID, toHostItemGuid).callback(callback).queueUsing(context);
    }

    public static void start(Context context, String fromHostItemGuid, String toHostItemGuid){
        create(CopyComposersCommand.class).arg(FROM_HOST_ITEM_GUID, fromHostItemGuid).arg(TO_HOST_ITEM_GUID, toHostItemGuid).queueUsing(context);
    }

    public static abstract class CopyComposersCommandCallback {

        @OnSuccess(CopyComposersCommand.class)
        public void onSuccess() {
            handleFinish();
        }

        @OnFailure(CopyComposersCommand.class)
        public void onFailure() {
            handleFinish();
        }

        protected abstract void handleFinish();


    }
}
