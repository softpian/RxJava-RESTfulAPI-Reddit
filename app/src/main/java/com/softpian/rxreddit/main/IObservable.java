package com.softpian.rxreddit.main;

import com.softpian.rxreddit.model.RedditNewsResponse;

import io.reactivex.Observable;

public interface IObservable {

    Observable<RedditNewsResponse> getObservable();
}
