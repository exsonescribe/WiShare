package com.example.exson.another2;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

    private List<Contacts> mContactList = new ArrayList<>();
    private RecyclerView mRecyclerView;
    private ContactRecyclerAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private FragmentManager mFragment;

    private NSDHelper mNSDHelper;
    private SwipeRefreshLayout mSwipeLayout;

    private SharedPreferences mPreferences;
    private String mUser;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // initilialize views and widgets
        setContentView(R.layout.main_activity);

        mPreferences = getSharedPreferences("WiShare", MODE_PRIVATE);
        mUser = mPreferences.getString ("Fullname", null);

        if (mUser != null) {
            initViews();
        }
        else {
            mFragment = getFragmentManager();
            RegisterDialog dialogFragment = new RegisterDialog();
            dialogFragment.show(mFragment, "New User");
        }
    }

    private void initViews() {

        mNSDHelper = NSDHelper.getInstance(getApplicationContext());
        mNSDHelper.start();

        mSwipeLayout = (SwipeRefreshLayout) findViewById(R.id.main_SRL);
        mSwipeLayout.setOnRefreshListener(new OnRefreshListener());
        mSwipeLayout.setColorSchemeResources(android.R.color.holo_blue_bright);

        mRecyclerView = (RecyclerView) findViewById(R.id.main_RV);
        mAdapter = new ContactRecyclerAdapter(mContactList);

        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);

    }

    @Override
    protected void onResume() {
        super.onResume();

        // reload data or instances
    }

    @Override
    protected void onPause() {
        // save data or instances
        super.onPause();
    }


    @Override
    protected void onDestroy() {
        // unregister and destroy listeners
        mNSDHelper.stop();
        super.onDestroy();
    }

    private class OnRefreshListener implements SwipeRefreshLayout.OnRefreshListener {
        @Override
        public void onRefresh() {
//            Contacts mName = new Contacts(mNSDHelper.getmNames());
//
//            if (mName != null) {
//                mContactList.add(mName);
//                mAdapter.notifyDataSetChanged();
//            }

            mSwipeLayout.setRefreshing(false);
        }
    }

}
