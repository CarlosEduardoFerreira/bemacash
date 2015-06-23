package com.kaching123.tcr.fragment.reports.sub;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.widget.ListView;
import android.widget.TextView;

import com.kaching123.tcr.adapter.ObjectsCursorAdapter;
import com.kaching123.tcr.fragment.UiHelper;
import com.kaching123.tcr.fragment.reports.RegisterReportsDetailsFragment.IDetailsFragment;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;

import java.math.BigDecimal;
import java.util.List;

/**
 * Created by gdubina on 28.01.14.
 */
@EFragment
public abstract class SalesBaseFragment<T> extends Fragment implements LoaderCallbacks<List<T>>, IDetailsFragment {

    @ViewById(android.R.id.list)
    protected ListView listView;

    @FragmentArg
    protected long startTime;

    @FragmentArg
    protected long endTime;

    @FragmentArg
    protected long resisterId;

    @FragmentArg
    protected int type;

    @ViewById
    protected TextView total;

    protected BigDecimal totalValue = BigDecimal.ZERO;

    protected ObjectsCursorAdapter<T> adapter;

    protected String managerGuid;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        listView.setAdapter(adapter = createAdapter());
        getLoaderManager().initLoader(0, null, this).forceLoad();
    }

    protected abstract ObjectsCursorAdapter<T> createAdapter();

    @Override
    public void updateData(long startTime, long endTime, long resisterId, int type, String managerGuid) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.resisterId = resisterId;
        this.managerGuid = managerGuid;
        if (type != -1)
            this.type = type;
        if (getActivity() != null) {
            getLoaderManager().restartLoader(0, null, this).forceLoad();
        }
    }

    @Override
    public void onLoadFinished(Loader<List<T>> listLoader, List<T> reportItemInfos) {
        adapter.changeCursor(reportItemInfos);
        UiHelper.showPrice(total, totalValue);
    }

    @Override
    public void onLoaderReset(Loader<List<T>> listLoader) {

    }

}
