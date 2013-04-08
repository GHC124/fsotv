package com.fsotv.utils;

import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListView;

public class EndlessScrollListener  implements OnScrollListener {

	private ListView listView;
    private int threshold = 5;
   
    public EndlessScrollListener(ListView listView) {
    	this.listView = listView;
    }
   
    public EndlessScrollListener(ListView listView, int threshold) {
    	this.listView = listView;
    	this.threshold = threshold;
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem,
            int visibleItemCount, int totalItemCount) {
        
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
    	if (scrollState == SCROLL_STATE_IDLE) {
            if (listView.getLastVisiblePosition() >= listView.getCount() - threshold) {
                loadData();
            }
        }
    }
    
    public void loadData(){
    	
    }
}
