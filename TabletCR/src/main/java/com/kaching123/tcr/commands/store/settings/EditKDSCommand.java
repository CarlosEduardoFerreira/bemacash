package com.kaching123.tcr.commands.store.settings;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;

import com.getbase.android.db.provider.ProviderAction;
import com.kaching123.tcr.model.KDSModel;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore.KDSTable;
import com.telly.groundy.PublicGroundyTask;
import com.telly.groundy.TaskResult;
import com.telly.groundy.annotations.OnSuccess;

import java.util.UUID;

/**
 * Created by long.jiao on 06.21.16.
 */
public class EditKDSCommand extends PublicGroundyTask {

    private static final Uri URI_KDS = ShopProvider.getContentUri(KDSTable.URI_CONTENT);
    private static final String ARG_KDS = "ARG_KDS";

    private KDSModel kdsModel;

    @Override
    protected TaskResult doInBackground() {
        kdsModel = (KDSModel)getArgs().getSerializable(ARG_KDS);

        boolean needInsert = kdsModel.guid == null;
        if(needInsert){
            kdsModel.guid = UUID.randomUUID().toString();
        }
        ContentValues v = kdsModel.toValues();

        if(needInsert){
            ProviderAction
                    .insert(URI_KDS)
                    .values(v)
                    .perform(getContext());
        }else{
            v.remove(KDSTable.GUID);

            ProviderAction
                    .update(URI_KDS)
                    .values(v)
                    .where(KDSTable.GUID + " = ?", kdsModel.guid)
                    .perform(getContext());
        }
        return succeeded();
    }

   /* @Override
    protected ISqlCommand createSqlCommand() {
        return command(JdbcFactory.getConverter(kdsModel).updateSQL(kdsModel));
    }
*/
    public static void start(Context context, KDSModel model, Callback callback){
        create(EditKDSCommand.class)
                .arg(ARG_KDS, model)
                .callback(callback)
                .queueUsing(context);
    }

    public static void start(Context context, KDSModel model){
        create(EditKDSCommand.class)
                .arg(ARG_KDS, model)
                .queueUsing(context);
    }

    public static abstract class Callback {

        @OnSuccess(EditKDSCommand.class)
        public final void onSuccess() {
            handleSuccess();
        }

        protected abstract void handleSuccess();
    }
}
