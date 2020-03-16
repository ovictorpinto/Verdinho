package com.github.ovictorpinto.verdinho.ui.main;

import android.app.Fragment;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.ovictorpinto.verdinho.BuildConfig;
import com.github.ovictorpinto.verdinho.R;
import com.twitter.sdk.android.tweetui.TweetTimelineRecyclerViewAdapter;
import com.twitter.sdk.android.tweetui.TwitterListTimeline;

public class TimelineFragment extends Fragment {
    
    private RecyclerView recyclerView;
    private ProcessoLoadTimeline processoLoadTimeline;
    private View progress;
    
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        
        View mainView = inflater.inflate(R.layout.ly_recycler, null);
    
        ((Toolbar) mainView.findViewById(R.id.toolbar)).setTitle(R.string.twitter);
        
        recyclerView = mainView.findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setVisibility(View.GONE);
        
        progress = mainView.findViewById(R.id.layout_progress);
        progress.setVisibility(View.VISIBLE);
        
        processoLoadTimeline = new ProcessoLoadTimeline();
        processoLoadTimeline.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        
        return mainView;
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (processoLoadTimeline != null) {
            processoLoadTimeline.cancel(true);
        }
    }
    
    private class ProcessoLoadTimeline extends AsyncTask<Void, String, Boolean> {
        
        private TweetTimelineRecyclerViewAdapter adapter;
        
        @Override
        protected Boolean doInBackground(Void... voids) {
            TwitterListTimeline.Builder builderTimeline = new TwitterListTimeline.Builder();
            builderTimeline.slugWithOwnerScreenName(BuildConfig.TWITTER_LIST, BuildConfig.TWITTER_USER);
            final TwitterListTimeline timeline = builderTimeline.build();
            
            TweetTimelineRecyclerViewAdapter.Builder builderAdapter = new TweetTimelineRecyclerViewAdapter.Builder(getActivity());
            builderAdapter.setTimeline(timeline);
            builderAdapter.setViewStyle(R.style.tw__TweetLightWithActionsStyle);
            adapter = builderAdapter.build();
            return true;
        }
        
        @Override
        protected void onPostExecute(Boolean success) {
            recyclerView.setAdapter(adapter);
            recyclerView.setVisibility(View.VISIBLE);
            progress.setVisibility(View.GONE);
            processoLoadTimeline = null;
        }
    }
}