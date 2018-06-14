package com.softpian.rxreddit.main;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.softpian.rxreddit.R;
import com.softpian.rxreddit.model.RedditNewsResponse;
import com.softpian.rxreddit.network.RestfulService;
import com.softpian.rxreddit.network.RxRedditApi;
import com.softpian.rxreddit.util.Constant;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

public class NewsListFragment extends Fragment implements IObservable {

    private CompositeDisposable mCompositeDisposable = new CompositeDisposable();
    private Observable<RedditNewsResponse> mObservableNewsPosts;

    @BindView(R.id.rvNewsList) RecyclerView mNewsListView;
    @BindView(R.id.tvError) TextView mErrorView;
    @BindView(R.id.pbLoading) ProgressBar mLoadingView;

    private Unbinder mUnbinder;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list, container, false);
        mUnbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        observeNewsPosts();

        mNewsListView.setAdapter(new NewsListAdapter(this));
        mNewsListView.setLayoutManager(new LinearLayoutManager(getContext()));

    }

    private void observeNewsPosts() {

        mNewsListView.setVisibility(View.GONE);
        mLoadingView.setVisibility(View.VISIBLE);
        mErrorView.setVisibility(View.GONE);

        RxRedditApi rxRedditApi = RestfulService.getInstance().createRetrofit(RxRedditApi.class, Constant.BASE_URL);
        mObservableNewsPosts = rxRedditApi.getTopPosts("", "20");

        mCompositeDisposable.add(
                mObservableNewsPosts
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(new DisposableObserver<RedditNewsResponse>() {
                            @Override
                            public void onNext(RedditNewsResponse redditNewsResponse) {
                                mNewsListView.setVisibility(View.VISIBLE);
                                mLoadingView.setVisibility(View.GONE);
                                mErrorView.setVisibility(View.GONE);
                                mErrorView.setText(null);
                            }

                            @Override
                            public void onError(Throwable e) {
                                mNewsListView.setVisibility(View.GONE);
                                mLoadingView.setVisibility(View.GONE);
                                mErrorView.setVisibility(View.VISIBLE);
                                mErrorView.setText("Error while loading news posts");
                            }

                            @Override
                            public void onComplete() { }
                        })
        );
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mUnbinder != null) {
            mUnbinder.unbind();
            mUnbinder = null;
        }

        mCompositeDisposable.clear();
    }

    @Override
    public Observable<RedditNewsResponse> getObservable() {
        return mObservableNewsPosts;
    }
}
