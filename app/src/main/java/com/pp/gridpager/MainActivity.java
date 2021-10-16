package com.pp.gridpager;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.PagerSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.widget.TextView;

import com.pp.gridpager.adapter.GridPagerAdapter;
import com.pp.gridpager.snaphelp.GridPagerSnapHelper;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initGridePager();
    }

    private void initGridePager() {
        final int colum = 4;
        int row = 3;
        int orientation = GridLayoutManager.HORIZONTAL;
        int spanCount = (GridLayoutManager.VERTICAL == orientation) ? colum : row;

        // 设置布局管理器
//        GridLayoutManager gridLayoutManager = new GridLayoutManager(getApplicationContext(), colum, GridLayoutManager.VERTICAL, false){
//
//            @Override
//            public boolean canScrollHorizontally() {
//                return true;
//            }
//
//            @Override
//            public boolean canScrollVertically() {
//                return false;
//            }
//        };

        GridLayoutManager gridLayoutManager = new GridLayoutManager(getApplicationContext()
                , spanCount, orientation, false);
        mRecyclerView.setLayoutManager(gridLayoutManager);

        // 设置设配器
        List<String> list = new ArrayList<>();
        for (int i = 0 ; i < 100 ; i++ ){
            list.add(String.valueOf(i));
        }
        GridPagerAdapter gridPagerAdapter = new GridPagerAdapter(getApplicationContext(), orientation, colum, row , list);
        mRecyclerView.setAdapter(gridPagerAdapter);

        //　设置页面滑动帮助类　sanaphelp
        GridPagerSnapHelper pagerSnapHelper = new GridPagerSnapHelper(colum, row);
        pagerSnapHelper.attachToRecyclerView(mRecyclerView);

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                if (newState == RecyclerView.SCROLL_STATE_IDLE) {

                    RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();

                    int position = 0;
                    if (layoutManager instanceof GridLayoutManager) {
                        GridLayoutManager gridLayoutManager = (GridLayoutManager) layoutManager;
                        position = gridLayoutManager.findFirstVisibleItemPosition();
                        int row = gridLayoutManager.getSpanCount();
                        int allPageCount = gridLayoutManager.getItemCount() / (row * colum);
                        position = position / (row * colum) + 1;

                        ((TextView)findViewById(R.id.tv)).setText("pos:" + position + "/" + allPageCount);
                    }

                }

            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);



            }
        });


    }

    private void initView() {
        mRecyclerView = findViewById(R.id.main_rl);
    }

}
