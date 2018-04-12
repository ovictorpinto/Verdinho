package com.github.ovictorpinto.verdinho.ui.main;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.ovictorpinto.verdinho.R;
import com.twitter.sdk.android.tweetui.TweetTimelineRecyclerViewAdapter;
import com.twitter.sdk.android.tweetui.TwitterListTimeline;

public class TimelineFragment extends Fragment {
    
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        
        View mainView = inflater.inflate(R.layout.ly_recycler, null);
        
        final RecyclerView recyclerView = mainView.findViewById(R.id.recyclerview);
        
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        
        TwitterListTimeline.Builder builderTimeline = new TwitterListTimeline.Builder();
        builderTimeline.slugWithOwnerScreenName("verdinho-news", "lucasromanom");
        final TwitterListTimeline timeline = builderTimeline.build();
        
        TweetTimelineRecyclerViewAdapter.Builder builderAdapter = new TweetTimelineRecyclerViewAdapter.Builder(getActivity());
        builderAdapter.setTimeline(timeline);
        builderAdapter.setViewStyle(R.style.tw__TweetLightWithActionsStyle);
        final TweetTimelineRecyclerViewAdapter adapter = builderAdapter.build();
        
        recyclerView.setAdapter(adapter);
        
        return mainView;
    }
}