package com.kaching123.tcr.store.composer;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.net.Uri;

import com.kaching123.tcr.commands.store.AsyncCommand;
import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.jdbc.converters.JdbcConverter;
import com.kaching123.tcr.model.ComposerModel;
import com.kaching123.tcr.service.BatchSqlCommand;
import com.kaching123.tcr.service.ISqlCommand;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore;
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
public class EditComposerCommand extends AsyncCommand {

    private static final Uri COMPOSER_URI = ShopProvider.contentUri(ShopStore.ComposerTable.URI_CONTENT);


    private static final String PARAM_COMPOSER_ITEM = "PARAM_COMPOSER_ITEM";

    private static final String RESULT_DESC = "RESULT_DESCR";

    ArrayList<ContentProviderOperation> ops;
    private BatchSqlCommand sqlCommand;

    @Override
    protected TaskResult doCommand() {

        ops = new ArrayList<ContentProviderOperation>();
        sqlCommand = batchInsert(ComposerModel.class);
        JdbcConverter jdbcConverter = JdbcFactory.getConverter(ShopStore.ComposerTable.TABLE_NAME);

        ComposerModel composer = (ComposerModel) getArgs().getSerializable(PARAM_COMPOSER_ITEM);


        /*****************************************************************************************************/
        ops.add(ContentProviderOperation.newUpdate(COMPOSER_URI)
                .withValues(composer.toUpdateValues())
                .withSelection(ShopStore.ComposerTable.ID + " = ?", new String[]{composer.guid})
                .build());
        sqlCommand.add(jdbcConverter.updateSQL(composer, this.getAppCommandContext()));
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
        return create(EditComposerCommand.class)
                .arg(PARAM_COMPOSER_ITEM, unit)
                .callback(callback)
                .queueUsing(context);
    }

    public static abstract class ComposerCallback {

        @OnSuccess(EditComposerCommand.class)
        public final void onSuccess() {
            handleSuccess();
        }

        protected abstract void handleSuccess();

        @OnFailure(EditComposerCommand.class)
        public final void onFailure(@Param(RESULT_DESC) String message) {
            handleError(message);
        }

        protected abstract void handleError(String message);
    }
}
