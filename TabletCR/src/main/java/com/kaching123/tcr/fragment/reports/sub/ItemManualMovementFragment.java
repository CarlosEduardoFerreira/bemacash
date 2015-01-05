package com.kaching123.tcr.fragment.reports.sub;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.googlecode.androidannotations.annotations.EFragment;
import com.kaching123.tcr.R;
import com.kaching123.tcr.adapter.ObjectsCursorAdapter;
import com.kaching123.tcr.reports.ItemManualMovementQuery;
import com.kaching123.tcr.reports.ItemManualMovementQuery.MovementInfo;
import com.kaching123.tcr.util.DateUtils;

import java.util.List;

import static com.kaching123.tcr.fragment.UiHelper.showQuantityInteger;

/**
 * Created by vkompaniets on 30.01.14.
 */
@EFragment(R.layout.reports_item_manual_movement_fragment)
public class ItemManualMovementFragment extends SalesBaseFragment<MovementInfo> {

    @Override
    protected ObjectsCursorAdapter<MovementInfo> createAdapter() {
        return new Adapter(getActivity());
    }

    @Override
    public Loader<List<MovementInfo>> onCreateLoader(int i, Bundle bundle) {
        return new AsyncTaskLoader<List<MovementInfo>>(getActivity()) {
            @Override
            public List<MovementInfo> loadInBackground() {
                return ItemManualMovementQuery.getItems(getActivity(), startTime, endTime);
            }
        };
    }

    private class Adapter extends ObjectsCursorAdapter<MovementInfo> {

        public Adapter(Context context) {
            super(context);
        }

        @Override
        protected View newView(int position, ViewGroup parent) {
            View convertView = LayoutInflater.from(getContext()).inflate(R.layout.reports_item_manual_movement_item_view, null, false);

            ViewHolder holder = new ViewHolder();
            holder.itemName = (TextView) convertView.findViewById(R.id.item_name);
            holder.date = (TextView) convertView.findViewById(R.id.date);
            holder.qty = (TextView) convertView.findViewById(R.id.qty);

            convertView.setTag(holder);
            return convertView;
        }

        @Override
        protected View bindView(View convertView, int position, MovementInfo item) {
            ViewHolder holder = (ViewHolder) convertView.getTag();

            if (item == null)
                return convertView;

            holder.itemName.setText(item.itemName);
            holder.date.setText(DateUtils.dateOnlyFormat(item.date));
            showQuantityInteger(holder.qty, item.qty);

            return convertView;
        }

        private class ViewHolder {
            TextView itemName;
            TextView date;
            TextView qty;
        }
    }

    public static ItemManualMovementFragment instance(long startTime, long endTime) {
        return ItemManualMovementFragment_.builder().startTime(startTime).endTime(endTime).build();
    }


}
