package com.kaching123.tcr.commands.store.inventory;

import android.content.ContentProviderOperation;
import android.content.Context;

import com.kaching123.tcr.commands.AtomicUpload;
import com.kaching123.tcr.commands.store.AsyncCommand;
import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.model.CategoryModel;
import com.kaching123.tcr.service.BatchSqlCommand;
import com.kaching123.tcr.service.ISqlCommand;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore.CategoryTable;
import com.telly.groundy.TaskResult;
import com.telly.groundy.annotations.OnSuccess;

import java.util.ArrayList;
import java.util.UUID;

public class AddCategoryCommand extends AsyncCommand {

    private static final String ARG_MODEL = "arg_model";

	private static final String RESULT_GUID = "result_guid";

	private CategoryModel model;

	@Override
	protected TaskResult doCommand() {
        if (model == null)
            model = (CategoryModel) getArgs().getSerializable(ARG_MODEL);

		model = new CategoryModel(
                UUID.randomUUID().toString(),
                model.departmentGuid,
                model.title,
                model.image,
                0,
                model.commissionEligible,
                model.commission, null);

		return succeeded().add(RESULT_GUID, model.guid);
	}

    @Override
    protected ArrayList<ContentProviderOperation> createDbOperations() {
        ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();
        operations.add(ContentProviderOperation.newInsert(ShopProvider.getContentUri(CategoryTable.URI_CONTENT))
                .withValues(model.toValues())
                .build());
        return operations;
    }

    @Override
	protected ISqlCommand createSqlCommand() {
        BatchSqlCommand batch = batchInsert(model);
        batch.add(JdbcFactory.insert(model, getAppCommandContext()));

        new AtomicUpload().upload(batch, AtomicUpload.UploadType.WEB);

        return batch;
	}

	public static void start(Context context, CategoryModel model, BaseAddCategoryCallback callback){
		create(AddCategoryCommand.class).arg(ARG_MODEL, model).callback(callback).queueUsing(context);
	}

    /** will be used in import. can be standalone **/
    public String sync(Context context, String departmentGuid, String categoryName, IAppCommandContext appCommandContext) {
        CategoryModel model = new CategoryModel(departmentGuid, categoryName);
        return sync(context, model, appCommandContext);
    }

    /** will be used in import. can be standalone **/
    private String sync(Context context, CategoryModel model, IAppCommandContext appCommandContext){
        this.model = model;
        TaskResult result = super.syncStandalone(context, appCommandContext);
        return isFailed(result) ? null : this.model.guid;
    }

    public static abstract class BaseAddCategoryCallback {

        @OnSuccess(AddCategoryCommand.class)
        public void onSuccess() {
            onCategoryAddedSuccess();
        }

        protected abstract void onCategoryAddedSuccess();


    }
}
