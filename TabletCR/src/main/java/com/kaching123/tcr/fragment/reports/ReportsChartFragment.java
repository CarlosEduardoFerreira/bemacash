package com.kaching123.tcr.fragment.reports;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;

import com.googlecode.androidannotations.annotations.EFragment;
import com.googlecode.androidannotations.annotations.ViewById;
import com.kaching123.tcr.R;
import com.kaching123.tcr.fragment.reports.ReportsFragment.ReportsFragmentListener;

/**
 * Created by hamsterksu on 23.01.14.
 */
@EFragment(R.layout.reports_chart_fragment)
public class ReportsChartFragment extends Fragment implements ReportsFragmentListener {

    @ViewById
    protected View topItemsFragmentShadow;

    @ViewById
    protected FrameLayout topItemsFragmentContainer;

    //@FragmentById
    private TopItemsFragment topItemsFragment;

    //@FragmentById
    private ReportsFragment reportsFragment;

/*
    @FragmentArg
    protected Mode mode;
*/

    private Animation fadeInAnimation;
    private Animation fadeOutAnimation;
    private Animation moveInAnimation;
    private Animation moveOutAnimation;

/*
    public void setMode(Mode mode) {
        this.mode = mode;
        if (reportsFragment != null)
            reportsFragment.setMode(mode);
    }
*/

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        reportsFragment = ReportsFragment.instantiate();
        topItemsFragment = TopItemsFragment.instantiate();

        getChildFragmentManager().beginTransaction()
                .replace(R.id.reports_fragment, reportsFragment)
                .replace(R.id.top_items_fragment, topItemsFragment)
                .commit();

        reportsFragment.setReportsFragmentListener(this);

        fadeInAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.fade_in);
        fadeOutAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.fade_out);
        moveInAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.move_in);
        moveOutAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.move_out);

        topItemsFragmentShadow.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                setTopItemsFragmentVisibility(false);
            }
        });
    }

    private void setTopItemsFragmentVisibility(boolean visible) {
        if (isTopItemsFragmentVisible() == visible)
            return;

        if (visible) {
            topItemsFragmentContainer.setVisibility(View.VISIBLE);
            topItemsFragmentContainer.startAnimation(moveInAnimation);
            topItemsFragmentShadow.setVisibility(View.VISIBLE);
            topItemsFragmentShadow.startAnimation(fadeInAnimation);

            topItemsFragment.setSelectedEntities(reportsFragment.getSelectedRegisterId());
            topItemsFragment.setDates(reportsFragment.getFromDate(), reportsFragment.getToDate());
        } else {
            topItemsFragmentContainer.setVisibility(View.GONE);
            topItemsFragmentContainer.startAnimation(moveOutAnimation);
            topItemsFragmentShadow.setVisibility(View.GONE);
            topItemsFragmentShadow.startAnimation(fadeOutAnimation);
        }
    }

    private boolean isTopItemsFragmentVisible() {
        return topItemsFragmentContainer.getVisibility() == View.VISIBLE;
    }

    @Override
    public void onShowTopItems() {
        setTopItemsFragmentVisibility(true);
    }

    public boolean onBackPressed() {
        if (isTopItemsFragmentVisible()) {
            setTopItemsFragmentVisibility(false);
            return true;
        }
        return false;
    }

    public static ReportsChartFragment instance(/*Mode mode*/) {
        return ReportsChartFragment_.builder().build();
    }
}
