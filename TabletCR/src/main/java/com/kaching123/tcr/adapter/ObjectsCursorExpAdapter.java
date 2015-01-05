package com.kaching123.tcr.adapter;

import android.content.Context;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;

import com.kaching123.tcr.model.Unit;
import com.kaching123.tcr.model.payment.HistoryDetailedOrderItemModel;

import java.util.ArrayList;

/**
 * Created by mayer
 */
public abstract class ObjectsCursorExpAdapter extends BaseExpandableListAdapter {

    private ArrayList<Pair<HistoryDetailedOrderItemModel,ArrayList<Unit>>> mGroups;
    private Context mContext;

    public ObjectsCursorExpAdapter(Context context, ArrayList<Pair<HistoryDetailedOrderItemModel, ArrayList<Unit>>> groups){
        mContext = context;
        mGroups = groups;
    }

    @Override
    public int getGroupCount() {
        return mGroups.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return mGroups.get(groupPosition).second.size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return mGroups.get(groupPosition).first;
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return mGroups.get(groupPosition).second.get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView,
                             ViewGroup parent) {

        if (convertView == null) {
            convertView = newView(groupPosition, parent);
        }
        return bindView(convertView, groupPosition, (HistoryDetailedOrderItemModel)getGroup(groupPosition));

    }

    protected abstract View newView(int position, ViewGroup parent);

    protected abstract View bindView(View convertView, int position, HistoryDetailedOrderItemModel item);

    protected abstract View newExpView(int position, int childposition, ViewGroup parent);

    protected abstract View bindExpView(View convertView, int childposition, int position, HistoryDetailedOrderItemModel item);

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild,
                             View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = newExpView(groupPosition, childPosition, parent);
        }
        return bindExpView(convertView, groupPosition, childPosition, (HistoryDetailedOrderItemModel) getGroup(groupPosition));
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}
