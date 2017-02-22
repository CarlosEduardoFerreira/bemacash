package com.kaching123.tcr.adapter;

import android.content.AsyncQueryHandler;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.SparseBooleanArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.ResourceCursorTreeAdapter;
import android.widget.TextView;

import com.kaching123.tcr.R;
import com.kaching123.tcr.TcrApplication;
import com.kaching123.tcr.commands.store.inventory.DeleteVariantItemCommand;
import com.kaching123.tcr.commands.store.inventory.DeleteVariantSubItemCommand;
import com.kaching123.tcr.component.CheckableLinearLayout;
import com.kaching123.tcr.model.VariantItemModel;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore;

import java.util.ArrayList;

/**
 * Created by aakimov on 28/04/15.
 */
public class VariantsExpandableAdapter extends ResourceCursorTreeAdapter {
    private static final Uri URI_VARIANT_ITEM = ShopProvider.contentUri(ShopStore.VariantItemTable.URI_CONTENT);
    private static final Uri URI_VARIANT_SUB_ITEM = ShopProvider.contentUri(ShopStore.VariantSubItemTable.URI_CONTENT);
    private static final int QUERY_VARIANT_ITEM = 0x00000010;
    private static final int QUERY_VARIANT_SUB_ITEM = 0x00000020;
    private static final int QUERY_VARIANT_SUB_ITEM_FOR_DELETION = 0x00000030;

    private int varItemGuidIdx, varItemNameIdx, varSubItemNameIdx, varSubItemCountIdx;
    private SparseBooleanArray checkedChildren;
    private SparseBooleanArray checkedGroups;
    private QueryHandler queryHandler;

    public VariantsExpandableAdapter(Context context, Cursor cursor) {
        super(context, cursor, R.layout.variant_item, R.layout.variant_sub_item);
        checkedChildren = new SparseBooleanArray();
        checkedGroups = new SparseBooleanArray();
        queryHandler = new QueryHandler(context);
    }

    @Override
    protected Cursor getChildrenCursor(Cursor groupCursor) {
        queryHandler.startQuery(QUERY_VARIANT_SUB_ITEM, groupCursor.getPosition(),
                URI_VARIANT_SUB_ITEM, null, ShopStore.VariantSubItemTable.VARIANT_ITEM_GUID + "=?",
                new String[]{groupCursor.getString(varItemGuidIdx)}, null);
        return null;
    }

    protected void deleteGroupIds(ArrayList<Integer> list) {
        for (int i = 0; i < list.size(); i++) {
            queryHandler.startQuery(QUERY_VARIANT_ITEM, i == list.size() - 1, URI_VARIANT_ITEM,
                    null, ShopStore.VariantItemTable.ID + "=?", new String[]{String.valueOf(list.get(i))}, null);
        }
    }

    protected void queryChildrenByIdsForDeletion(ArrayList<Integer> list) {
        queryHandler.startQuery(QUERY_VARIANT_SUB_ITEM_FOR_DELETION, null, URI_VARIANT_SUB_ITEM,
                null, ShopStore.VariantSubItemTable.ID + " IN (" + convertToCommaDelimited(list) + ")", null, null);
    }

    protected void queryChildrenByVariantGuid(String guid) {
        queryHandler.startQuery(QUERY_VARIANT_SUB_ITEM_FOR_DELETION, null, URI_VARIANT_SUB_ITEM,
                null, ShopStore.VariantSubItemTable.VARIANT_ITEM_GUID + "=?", new String[]{guid}, null);
    }

    public VariantItemModel getVariantItemModel() {
        Cursor c = getCursor();
        int id = checkedGroups.keyAt(checkedGroups.indexOfValue(true));
        int varItemGuidIdx = c.getColumnIndex(ShopStore.VariantSubItemsCountView.VARIANT_ITEM_GUID);
        int varItemNameIdx = c.getColumnIndex(ShopStore.VariantSubItemsCountView.VARIANT_ITEM_NAME);
        int varParentGuidIdx = c.getColumnIndex(ShopStore.VariantSubItemsCountView.VARIANT_ITEM_PARENT_GUID);
        if (c.moveToFirst()) {
            do {
                if (id == c.getInt(0)) {
                    return new VariantItemModel(c.getString(varItemGuidIdx),
                            c.getString(varItemNameIdx),
                            c.getString(varParentGuidIdx),
                            TcrApplication.get().getShopId(),
                            null);
                }
            } while (c.moveToNext());
        }
        return null;
    }

    @Override
    public View newChildView(Context context, Cursor cursor, boolean isLastChild, ViewGroup parent) {
        if (cursor != null) {
            varSubItemNameIdx = cursor.getColumnIndex(ShopStore.VariantSubItemTable.NAME);
        }
        View view = super.newChildView(context, cursor, isLastChild, parent);
        view.setTag(new VariantSubItemHolder(view));
        return view;
    }

    @Override
    public View newGroupView(Context context, Cursor cursor, boolean isExpanded, ViewGroup parent) {
        if (cursor != null) {
            varItemGuidIdx = cursor.getColumnIndex(ShopStore.VariantSubItemsCountView.VARIANT_ITEM_GUID);
            varItemNameIdx = cursor.getColumnIndex(ShopStore.VariantSubItemsCountView.VARIANT_ITEM_NAME);
            varSubItemCountIdx = cursor.getColumnIndex(ShopStore.VariantsView.VARIANT_SUB_ITEMS_COUNT);
        }
        View view = super.newGroupView(context, cursor, isExpanded, parent);
        view.setTag(new VariantItemHolder(view));
        return view;
    }

    @Override
    protected void bindGroupView(View view, Context context, Cursor cursor, boolean isExpanded) {
        VariantItemHolder variantItemHolder = (VariantItemHolder) view.getTag();
        variantItemHolder.name.setText(cursor.getString(varItemNameIdx));
        variantItemHolder.count.setText(cursor.getString(varSubItemCountIdx));
        ((CheckableLinearLayout) view).setChecked(checkedGroups.get(cursor.getInt(0), false));

    }

    @Override
    protected void bindChildView(View view, Context context, Cursor cursor, boolean isLastChild) {
        VariantSubItemHolder variantSubItemHolder = (VariantSubItemHolder) view.getTag();
        variantSubItemHolder.name.setText(cursor.getString(varSubItemNameIdx));
        ((CheckableLinearLayout) view).setChecked(checkedChildren.get(cursor.getInt(0), false));

    }

    // handling choices

    public boolean setGroupChecked(int groupId) {
        boolean prev = checkedGroups.get(groupId, false);
        checkedGroups.put(groupId, !prev);
        notifyDataSetChanged(false);
        return !prev;
    }

    public boolean setChildChecked(int childId) {
        boolean prev = checkedChildren.get(childId, false);
        checkedChildren.put(childId, !prev);
        notifyDataSetChanged(false);
        return !prev;

    }

    public boolean hasGroupsInSelection() {
        return checkedGroups.indexOfValue(true) != -1;
    }

    public boolean hasChildrenInSelection() {
        return checkedChildren.indexOfValue(true) != -1;

    }

    public boolean hasNoInSelection() {
        return !hasChildrenInSelection() && !hasGroupsInSelection();

    }

    public boolean hasSingleGroupInSelection() {
        int count = 0;
        for (int i = 0; i < checkedGroups.size(); i++) {
            if (checkedGroups.get(checkedGroups.keyAt(i))) {
                count++;
            }
        }
        return count == 1;
    }

    public boolean hasSingleChildInSelection() {
        int count = 0;
        for (int i = 0; i < checkedChildren.size(); i++) {
            if (checkedChildren.get(checkedChildren.keyAt(i))) {
                count++;
            }
        }
        return count == 1;
    }

    public boolean hasOnlyGroupInSelection() {

        return hasSingleGroupInSelection() && checkedChildren.indexOfValue(true) == -1;
    }

    public boolean hasOnlyChildInSelection() {

        return hasSingleChildInSelection() && checkedGroups.indexOfValue(true) == -1;
    }

    public void deleteSelected() {
        ArrayList<Integer> groupIdsForDeletion = new ArrayList<Integer>();
        ArrayList<Integer> childIdsForDeletion = new ArrayList<Integer>();

        for (int i = 0; i < checkedGroups.size(); i++) {
            int groupId = checkedGroups.keyAt(i);
            if (checkedGroups.get(groupId)) {
                groupIdsForDeletion.add(groupId);
            }
        }
        for (int i = 0; i < checkedChildren.size(); i++) {
            int childId = checkedChildren.keyAt(i);
            if (checkedChildren.get(childId)) {
                childIdsForDeletion.add(childId);
            }
        }

        checkedGroups.clear();
        checkedChildren.clear();
        if (groupIdsForDeletion.size() > 0) {
            deleteGroupIds(groupIdsForDeletion);
        }
        if (childIdsForDeletion.size() > 0) {
            queryChildrenByIdsForDeletion(childIdsForDeletion);//rename
        }
        notifyDataSetChanged();
    }

    public void clearSelection() {
        checkedGroups.clear();
        checkedChildren.clear();
        notifyDataSetChanged();
    }

    public void configureSelection(long id, boolean checked) {
        if (checked) {
            if (ExpandableListView.getPackedPositionType(id) == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
                if (hasGroupsInSelection()) {
                    checkedGroups.clear();
                    notifyDataSetChanged(false);
                }
            } else if (ExpandableListView.getPackedPositionType(id) == ExpandableListView.PACKED_POSITION_TYPE_GROUP) {
                if (hasChildrenInSelection()) {
                    checkedChildren.clear();
                    notifyDataSetChanged(false);
                }
            }
        }
    }

    public int getSelectedChildId() {
        return checkedChildren.keyAt(checkedChildren.indexOfValue(true));
    }

    public static String convertToCommaDelimited(ArrayList<Integer> list) {
        StringBuffer ret = new StringBuffer("");
        for (int i = 0; list != null && i < list.size(); i++) {
            ret.append(list.get(i));
            if (i < list.size() - 1) {
                ret.append(", ");
            }
        }
        return ret.toString();
    }


    private static class VariantItemHolder {
        TextView name;
        TextView count;

        VariantItemHolder(View v) {
            name = (TextView) v.findViewById(android.R.id.text1);
            count = (TextView) v.findViewById(android.R.id.text2);
        }
    }

    private static class VariantSubItemHolder {
        TextView name;

        VariantSubItemHolder(View v) {
            name = (TextView) v.findViewById(android.R.id.text1);
        }
    }

    private final class QueryHandler extends AsyncQueryHandler {
        private Context context;

        public QueryHandler(Context context) {
            super(context.getContentResolver());
            this.context = context;
        }

        @Override
        protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
            switch (token) {
                case QUERY_VARIANT_ITEM:
                    if (cursor.moveToFirst()) {
                        String guid = cursor.getString(cursor.getColumnIndex(ShopStore.VariantItemTable.GUID));
                        queryChildrenByVariantGuid(guid);
                        DeleteVariantItemCommand.start(context, guid);
                    }
                    cursor.close();
                    break;
                case QUERY_VARIANT_SUB_ITEM:
                    if (cursor != null && cursor.getCount() > 0) {
                        setChildrenCursor((Integer) cookie, cursor);
                    }
                    break;
                case QUERY_VARIANT_SUB_ITEM_FOR_DELETION:
                    ArrayList<String> guids = new ArrayList<String>(cursor.getCount());
                    int guidIdx = cursor.getColumnIndex(ShopStore.VariantSubItemTable.GUID);
                    if (cursor.moveToFirst()) {
                        do {
                            guids.add(cursor.getString(guidIdx));
                        } while (cursor.moveToNext());
                        DeleteVariantSubItemCommand.start(context, guids);
                    }
                    break;
            }
        }

    }
}
