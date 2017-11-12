package io.github.keep2iron.sample;

import android.databinding.ObservableArrayList;
import android.util.Log;

import java.util.List;

import javax.inject.Inject;

import io.github.keep2iron.orange.annotations.BindOnLoadMore;
import io.github.keep2iron.orange.annotations.BindOnRefresh;
import io.github.keep2iron.orange.annotations.LoadMoreAble;
import io.github.keep2iron.orange.annotations.Refreshable;
import io.github.keep2iron.sample.repository.DataServer;

/**
 * @author keep2iron <a href="http://keep2iron.github.io">Contract me.</a>
 * @version 1.0
 * @since 2017/11/05 10:53
 */
public class RecyclerModule {

//    @Inject
//    Refreshable mRefreshable;

    @Inject
    LoadMoreAble mLoadMoreAble;


    public ObservableArrayList<String> mData;

    public RecyclerModule() {
        mData = new ObservableArrayList<>();
        for (int i = 0; i < 10; i++) {
            mData.add(Math.random() * 100 + "");
        }
    }

    int loadMoreCount = 1;

    @BindOnLoadMore
    public void onLoadMore() {
        DataServer.httpData(new DataServer.Callback<String>() {
            @Override
            public void onSuccess(List<String> list) {
                Log.e("test","加载更多的次数为 : " + (loadMoreCount++));


                mData.addAll(list);
                mLoadMoreAble.showLoadMoreComplete();
            }

            @Override
            public void onError() {

            }
        });
    }

    int refreshCount = 1;

//    @BindOnRefresh
//    public void onRefresh() {
//        DataServer.httpData(new DataServer.Callback<String>() {
//            @Override
//            public void onSuccess(List<String> list) {
//
//                Log.e("test","刷新的次数为 : " + (refreshCount++));
//
//                mData.clear();
//                mData.addAll(list);
//                mRefreshable.showRefreshComplete();
//            }

//            @Override
//            public void onError() {
//
//            }
//        });
//    }
}
