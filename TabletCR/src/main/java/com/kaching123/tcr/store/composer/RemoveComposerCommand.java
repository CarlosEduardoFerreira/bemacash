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
import com.telly.groundy.PublicGroundyTask;
import com.telly.groundy.TaskHandler;
import com.telly.groundy.TaskResult;
import com.telly.groundy.annotations.OnFailure;
import com.telly.groundy.annotations.OnSuccess;
import com.telly.groundy.annotations.Param;

import java.util.ArrayList;

/**
 * @author Ivan v. Rikhmayer
 *         This class is intended to
 */
public class RemoveComposerCommand extends BaseComposerCommand {

    private static final Uri COMPOSER_URI = ShopProvider.contentUri(ShopStore.ComposerTable.URI_CONTENT);


    private static final String PARAM_COMPOSER_ITEM = "PARAM_COMPOSER_ITEM";

    private static final String RESULT_DESC = "RESULT_DESCR";

    private static final String PURPOSE_TOOL = "RESULT_TOOL";


    private static final int PURPOSE_TOOL_DELETE = 1;
    private static final int PURPOSE_TOOL_DELETE_BUNCH = 2;

    protected final int purposeNull = -1;

    protected int purpose = -purposeNull;

    protected ArrayList<ContentProviderOperation> ops;
    private BatchSqlCommand sqlCommand;
    private ComposerModel composer;
    private ArrayList<ComposerModel> composers;

    @Override
    protected TaskResult doCommand() {

        ops = new ArrayList<ContentProviderOperation>();
        sqlCommand = batchInsert(ComposerModel.class);
        JdbcConverter jdbcConverter = JdbcFactory.getConverter(ShopStore.ComposerTable.TABLE_NAME);

        /*****************************************************************************************************/

        if (purpose == purposeNull) {
            purpose = getArgs().getInt(PURPOSE_TOOL);
        }

        if (purpose == PURPOSE_TOOL_DELETE) {
            if (composer == null) {
                composer = (ComposerModel) getArgs().getSerializable(PARAM_COMPOSER_ITEM);
            }
            hostItemId = composer.itemHostId;
            ops.add(ContentProviderOperation.newUpdate(COMPOSER_URI)
                    .withValues(ShopStore.DELETE_VALUES)
                    .withSelection(ShopStore.ComposerTable.ID + " = ?", new String[]{composer.guid})
                    .build());
            sqlCommand.add(jdbcConverter.deleteSQL(composer, this.getAppCommandContext()));
        } else if (purpose == PURPOSE_TOOL_DELETE_BUNCH) {
            if (composers == null) {
                composers = (ArrayList<ComposerModel>) getArgs().getSerializable(PARAM_COMPOSER_ITEM);
            }
            hostItemId = composers.get(0).itemHostId;
            for (ComposerModel unit : composers) {
                ops.add(ContentProviderOperation.newUpdate(COMPOSER_URI)
                        .withValues(ShopStore.DELETE_VALUES)
                        .withSelection(ShopStore.ComposerTable.ID + " = ?", new String[]{unit.guid})
                        .build());
                sqlCommand.add(jdbcConverter.deleteSQL(unit, this.getAppCommandContext()));
            }
        }
        /*****************************************************************************************************/

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

    public static final TaskHandler start(Context context,
                                          ComposerModel unit,
                                          ComposerCallback callback) {
        return create(RemoveComposerCommand.class)
                .arg(PARAM_COMPOSER_ITEM, unit)
                .arg(PURPOSE_TOOL, PURPOSE_TOOL_DELETE)
                .callback(callback)
                .queueUsing(context);
    }

    public static final TaskHandler start(Context context,
                                          ArrayList<ComposerModel> units,
                                          ComposerCallback callback) {
        return create(RemoveComposerCommand.class)
                .arg(PARAM_COMPOSER_ITEM, units)
                .arg(PURPOSE_TOOL, PURPOSE_TOOL_DELETE_BUNCH)
                .callback(callback)
                .queueUsing(context);
    }

    public SyncResult sync(Context context, ArrayList<ComposerModel> composers, PublicGroundyTask.IAppCommandContext appCommandContext) {
        this.composers = composers;
        return syncDependent(context, appCommandContext);
    }

    public SyncResult sync(Context context, ComposerModel composer, PublicGroundyTask.IAppCommandContext appCommandContext) {
        this.composer = composer;
        return syncDependent(context, appCommandContext);
    }

    public static abstract class ComposerCallback {

        @OnSuccess(RemoveComposerCommand.class)
        public final void onSuccess() {
            handleSuccess();
        }

        protected abstract void handleSuccess();

        @OnFailure(RemoveComposerCommand.class)
        public final void onFailure(@Param(RESULT_DESC) String message) {
            handleError(message);
        }

        protected abstract void handleError(String message);
    }
}
