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
import java.util.UUID;

/**
 * Created by pkabakov on 25.12.13.
 */
public class CreateTaxGroup extends AsyncCommand {

    private static final Uri TAX_GROUP_URI = ShopProvider.getContentUri(ShopStore.TaxGroupTable.URI_CONTENT);

    private static final String ARG_TITLE = "ARG_TITLE";
    private static final String ARG_TAX = "ARG_TAX";

    private TaxGroupModel model;

    @Override
    protected TaskResult doCommand() {
        String title = getStringArg(ARG_TITLE);
        BigDecimal tax = (BigDecimal) getArgs().getSerializable(ARG_TAX);
        model = new TaxGroupModel(
                UUID.randomUUID().toString(),
                title,
                tax
        );

        return succeeded();
    }

    @Override
    protected ArrayList<ContentProviderOperation> createDbOperations() {
        ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>(1);
        operations.add(ContentProviderOperation.newInsert(TAX_GROUP_URI)
                .withValues(model.toValues())
                .build());
        return operations;
    }

    @Override
    protected ISqlCommand createSqlCommand() {
        return JdbcFactory.insert(model, getAppCommandContext());
    }

    public static void start(Context context, BaseCreateTaxGroupCallback callback, String title, BigDecimal tax) {
        create(CreateTaxGroup.class).arg(ARG_TITLE, title).arg(ARG_TAX, tax).callback(callback).queueUsing(context);
    }

    public static abstract class BaseCreateTaxGroupCallback {

        @OnSuccess(CreateTaxGroup.class)
        public void onSuccess() {
            onTaxGroupCreated();
        }

        @OnFailure(CreateTaxGroup.class)
        public void onFailure() {
            onTaxGroupCreateError();
        }

        protected abstract void onTaxGroupCreated();

        protected abstract void onTaxGroupCreateError();

    }
}
