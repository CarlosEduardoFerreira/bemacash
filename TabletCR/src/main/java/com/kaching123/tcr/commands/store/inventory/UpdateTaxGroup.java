package com.kaching123.tcr.commands.store.inventory;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.net.Uri;

import com.kaching123.tcr.commands.store.AsyncCommand;
import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.model.TaxGroupModel;
import com.kaching123.tcr.service.ISqlCommand;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore;
import com.telly.groundy.TaskResult;
import com.telly.groundy.annotations.OnFailure;
import com.telly.groundy.annotations.OnSuccess;

import java.math.BigDecimal;
import java.util.ArrayList;

/**
 * Created by pkabakov on 25.12.13.
 */
public class UpdateTaxGroup extends AsyncCommand {

    private static final Uri TAX_GROUP_URI = ShopProvider.getContentUri(ShopStore.TaxGroupTable.URI_CONTENT);

    private static final String ARG_GUID = "ARG_GUID";
    private static final String ARG_TITLE = "ARG_TITLE";
    private static final String ARG_TAX = "ARG_TAX";

    private TaxGroupModel model;

    @Override
    protected TaskResult doCommand() {
        String guid = getStringArg(ARG_GUID);
        String title = getStringArg(ARG_TITLE);
        BigDecimal tax = (BigDecimal) getArgs().getSerializable(ARG_TAX);
        model = new TaxGroupModel(
                guid,
                title,
                tax
        );

        return succeeded();
    }

    @Override
    protected ArrayList<ContentProviderOperation> createDbOperations() {
        ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>(1);
        operations.add(ContentProviderOperation.newUpdate(TAX_GROUP_URI)
                .withValues(model.toUpdateValues())
                .withSelection(ShopStore.TaxGroupTable.GUID + " = ?", new String[]{model.guid})
                .build());
        return operations;
    }

    @Override
    protected ISqlCommand createSqlCommand() {
        return JdbcFactory.getConverter(model).updateSQL(model, getAppCommandContext());
    }

    public static void start(Context context, BaseUpdateTaxGroupCallback callback, String guid, String title, BigDecimal tax) {
        create(UpdateTaxGroup.class).arg(ARG_GUID, guid).arg(ARG_TITLE, title).arg(ARG_TAX, tax).callback(callback).queueUsing(context);
    }

    public static abstract class BaseUpdateTaxGroupCallback {

        @OnSuccess(UpdateTaxGroup.class)
        public void onSuccess() {
            onTaxGroupUpdated();
        }

        @OnFailure(UpdateTaxGroup.class)
        public void onFailure() {
            onTaxGroupUpdateError();
        }

        protected abstract void onTaxGroupUpdated();

        protected abstract void onTaxGroupUpdateError();

    }
}
