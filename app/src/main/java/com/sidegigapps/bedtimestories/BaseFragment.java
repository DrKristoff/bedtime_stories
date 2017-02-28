package com.sidegigapps.bedtimestories;

import android.os.Bundle;
import android.support.v4.app.Fragment;

/**
 * Created by ryand on 2/18/2017.
 */

public abstract class BaseFragment extends Fragment {

    protected BaseActivity baseActivity;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        baseActivity = ((BaseActivity)getActivity());
    }
}
