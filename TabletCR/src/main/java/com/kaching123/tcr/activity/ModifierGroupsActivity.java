package com.kaching123.tcr.activity;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.getbase.android.db.loaders.CursorLoaderBuilder;
import com.google.common.base.Function;
import com.kaching123.tcr.R;
import com.kaching123.tcr.adapter.ObjectCursorDragAdapter;
import com.kaching123.tcr.commands.store.inventory.BatchUpdateModifierGroupOrderCommand;
import com.kaching123.tcr.model.ModifierGroupModel;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore.ModifierGroupTable;
import com.mobeta.android.dslv.DragSortListView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ViewById;

import java.util.List;

/**
 * Created by vkompaniets on 30.08.2016.
 */
@EActivity(R.layout.modifier_groups_activity)
public class ModifierGroupsActivity extends SuperBaseActivity {

    @ViewById
    protected DragSortListView list;

    @Extra
    protected String itemId;

    @Extra
    protected String itemName;

    private GroupsAdapter adapter;

    @AfterViews
    protected void init(){
        getActionBar().setTitle(itemName);
        adapter = new GroupsAdapter(self());
        list.setAdapter(adapter);
        getSupportLoaderManager().initLoader(0, null, new GroupsLoader());
    }

    private class GroupsLoader implements LoaderCallbacks<List<ModifierGroupModel>>{

        @Override
        public Loader<List<ModifierGroupModel>> onCreateLoader(int id, Bundle args) {
            return CursorLoaderBuilder.forUri(ShopProvider.contentUri(ModifierGroupTable.URI_CONTENT))
                    .where(ModifierGroupTable.ITEM_GUID + " = ?", itemId)
                    .orderBy(ModifierGroupTable.ORDER_NUM)
                    .transform(new Function<Cursor, ModifierGroupModel>() {
                        @Override
                        public ModifierGroupModel apply(Cursor input) {
                            return new ModifierGroupModel(input);
                        }
                    }).build(self());
        }

        @Override
        public void onLoadFinished(Loader<List<ModifierGroupModel>> loader, List<ModifierGroupModel> data) {
            adapter.changeCursor(data);
        }

        @Override
        public void onLoaderReset(Loader<List<ModifierGroupModel>> loader) {
            adapter.changeCursor(null);
        }
    }

    private class GroupsAdapter extends ObjectCursorDragAdapter<ModifierGroupModel> {

        public GroupsAdapter(Context context) {
            super(context);
        }

        @Override
        protected View newView(int position, ViewGroup parent) {
            View view = LayoutInflater.from(getContext()).inflate(R.layout.categories_item_drag_view, parent, false);
            ViewHolder holder = new ViewHolder(view);
            view.setTag(holder);
            return view;
        }

        @Override
        protected View bindView(View convertView, int position, ModifierGroupModel item) {
            ViewHolder holder = (ViewHolder) convertView.getTag();
            holder.title.setText(item.title);
            return convertView;
        }

        @Override
        public void drop(int from, int to) {
            super.drop(from, to);
            if (from == to) {
                return;
            }

            int count = getCount();
            String[] guids = new String[count];
            for (int i = 0; i < count; i++) {
                guids[i] = getItem(i).getGuid();
            }
            BatchUpdateModifierGroupOrderCommand.start(getContext(), guids);
        }
    }

    private class ViewHolder {
        protected TextView title;
        protected ImageView drag;

        public ViewHolder(View v) {
            title = (TextView) v.findViewById(R.id.title);
            drag = (ImageView) v.findViewById(R.id.drag);
        }
    }

    public static void start(FragmentActivity context, String itemId, String itemName){
        ModifierGroupsActivity_.intent(context).itemId(itemId).itemName(itemName).start();
    }

}
