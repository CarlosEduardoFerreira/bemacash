package com.kaching123.tcr.commands.store.inventory;

import android.content.ContentProviderOperation;
import android.content.Context;

import com.kaching123.tcr.commands.store.AsyncCommand;
import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.jdbc.converters.CategoryJdbcConverter;
import com.kaching123.tcr.model.CategoryModel;
import com.kaching123.tcr.service.BatchSqlCommand;
import com.kaching123.tcr.service.ISqlCommand;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore.CategoryTable;
import com.telly.groundy.TaskResult;

import java.util.ArrayList;

public class UpdateCategoryOrderCommand extends AsyncCommand {

    private static final String ARG_ORDER_NUMBERS = "arg_order_numbers";

    private ArrayList<ContentProviderOperation> ops;
    private BatchSqlCommand sqlCommand;

	@Override
	protected TaskResult doCommand() {
        String[] orderNumbers = getArgs().getStringArray(ARG_ORDER_NUMBERS);

        CategoryJdbcConverter jdbcConverter = (CategoryJdbcConverter)JdbcFactory.getConverter(CategoryTable.TABLE_NAME);
        sqlCommand = batchUpdate(CategoryModel.class);
        ops = new ArrayList<ContentProviderOperation>();
        for (int i = 0; i < orderNumbers.length; i++) {
            String guid = orderNumbers[i];
            ops.add(ContentProviderOperation.newUpdate(ShopProvider.getContentUri(CategoryTable.URI_CONTENT))
                            .withSelection(CategoryTable.GUID + " = ?", new String[]{guid})
                            .withValue(CategoryTable.ORDER_NUM, i)
                            .build());
            sqlCommand.add(jdbcConverter.updateOrderSQL(new CategoryModel(guid, null, null, null, i, false, null), this.getAppCommandContext()));
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

	public static void start(Context context, String[] orderNumbers){
		create(UpdateCategoryOrderCommand.class).arg(ARG_ORDER_NUMBERS, orderNumbers).queueUsing(context);
	}
}
