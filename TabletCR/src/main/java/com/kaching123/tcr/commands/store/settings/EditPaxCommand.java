package com.kaching123.tcr.commands.store.settings;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;

import com.getbase.android.db.provider.ProviderAction;
import com.kaching123.tcr.TcrApplication;
import com.kaching123.tcr.commands.payment.pax.blackstone.PaxBlackstoneHelloCommand;
import com.kaching123.tcr.commands.payment.pax.processor.PaxProcessorHelloCommand;
import com.kaching123.tcr.commands.payment.pax.processor.PaxProcessorInitCommand;
import com.kaching123.tcr.model.PaxModel;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore.PaxTable;
import com.telly.groundy.PublicGroundyTask;
import com.telly.groundy.TaskResult;
import com.telly.groundy.annotations.OnFailure;
import com.telly.groundy.annotations.OnSuccess;

import java.util.UUID;

public class EditPaxCommand extends PublicGroundyTask {

    private static final Uri URI_PRINTER = ShopProvider.getContentUri(PaxTable.URI_CONTENT);
    private static final String ARG_PRINTER = "ARG_PRINTER";

    private PaxModel printerModel;

    @Override
    protected TaskResult doInBackground() {
        printerModel = getArgs().getParcelable(ARG_PRINTER);

        boolean needInsert = printerModel.guid == null;
        if (needInsert) {
            printerModel.guid = UUID.randomUUID().toString();
        }
        ContentValues v = printerModel.toValues();
        boolean ok;
        if (TcrApplication.get().isBlackstonePax()) {
            ok = !isFailed(new PaxBlackstoneHelloCommand().sync(getContext(), printerModel));
        } else {
            if (!isFailed(new PaxProcessorInitCommand().sync(getContext(), printerModel))) ;
            ok = !isFailed(new PaxProcessorHelloCommand().sync(getContext(), printerModel));
        }
        if (!ok) {
            return failed();
        }
        if (needInsert) {
            ProviderAction
                    .insert(URI_PRINTER)
                    .values(v)
                    .perform(getContext());
        } else {
            v.remove(PaxTable.GUID);

            ProviderAction
                    .update(URI_PRINTER)
                    .values(v)
                    .where(PaxTable.GUID + " = ?", printerModel.guid)
                    .perform(getContext());
        }
        return succeeded();
    }

    /* @Override
     protected ISqlCommand createSqlCommand() {
         return command(JdbcFactory.getConverter(printerModel).updateSQL(printerModel));
     }
 */
    public static void start(Context context, PaxModel model, PaxEditCommandBaseCallback callback) {
        create(EditPaxCommand.class)
                .arg(ARG_PRINTER, model)
                .callback(callback)
                .queueUsing(context);
    }

    public static abstract class PaxEditCommandBaseCallback {

        @OnSuccess(EditPaxCommand.class)
        public final void onSuccess() {
            handleSuccess();
        }

        protected abstract void handleSuccess();

        @OnFailure(EditPaxCommand.class)
        public final void onFailure() {
            handleError();
        }

        protected abstract void handleError();
    }
}
