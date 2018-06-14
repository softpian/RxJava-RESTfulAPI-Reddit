package com.softpian.rxreddit.main;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.softpian.rxreddit.R;
import com.softpian.rxreddit.model.ChildrenItem;
import com.softpian.rxreddit.model.RedditNewsResponse;
import com.softpian.rxreddit.util.CommonUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

public class NewsListAdapter extends RecyclerView.Adapter<NewsListAdapter.NewsViewHolder> {

    private final String TAG = NewsViewHolder.class.getSimpleName();

    private final List<ChildrenItem> mItems = new ArrayList<>();

    private DisposableObserver mDisposableObserver;

    public NewsListAdapter(IObservable observable) {
        Observable<RedditNewsResponse> observableNewsPosts = observable.getObservable();
        mDisposableObserver = observableNewsPosts.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<RedditNewsResponse>() {
                    @Override
                    public void onNext(RedditNewsResponse redditNewsResponse) {
                        List<ChildrenItem> childrenItems = redditNewsResponse.getData().getChildren();

                        if (childrenItems == null) {
                            mItems.clear();
                            notifyDataSetChanged();
                            return;
                        }

                        mItems.addAll(childrenItems);
                        notifyDataSetChanged();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "While observing, Exception happened : " + e);
                    }

                    @Override
                    public void onComplete() {}
                });
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        if (!mDisposableObserver.isDisposed()) {
            mDisposableObserver.dispose();
        }
    }

    @NonNull
    @Override
    public NewsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.news_list_item, parent, false);
        return new NewsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NewsViewHolder holder, int position) {
        holder.bind(mItems.get(position));
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    static class NewsViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.ivThumbnail) ImageView mThumbnailView;
        @BindView(R.id.tvTitle) TextView mTitleView;
        @BindView(R.id.tvAuthor) TextView mAuthorView;
        @BindView(R.id.tvCreated) TextView mCreatedTimeView;
        @BindView(R.id.tvComment) TextView mCommentCountView;

        public NewsViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        void bind(ChildrenItem childrenItem) {

            String thumbnailUrl = childrenItem.getData().getThumbnail();
            if (thumbnailUrl != null && thumbnailUrl.contains("https")) {
                Glide.with(itemView)
                        .load(childrenItem.getData().getThumbnail())
                        .into(mThumbnailView);
            } else {
                Glide.with(itemView)
                        .load(R.drawable.ic_placeholder)
                        .into(mThumbnailView);
            }

            mTitleView.setText(childrenItem.getData().getTitle());
            mAuthorView.setText(childrenItem.getData().getAuthor());
            mCreatedTimeView.setText(CommonUtils.getDateFromUnixTimestamp(childrenItem.getData().getCreated()));
            mCommentCountView.setText(String.valueOf(childrenItem.getData().getNumComments()));
        }
    }
}
