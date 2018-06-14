package com.softpian.rxreddit.network;


import com.softpian.rxreddit.model.RedditNewsResponse;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface RxRedditApi {

    @GET("top.json")
    Observable<RedditNewsResponse> getTopPosts(@Query("after") String after, @Query("limit") String limit);
}
