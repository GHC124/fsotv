package com.fsotv.utils;

import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.GridView;

public class EndlessScrollGridViewListener  implements OnScrollListener {

	private GridView gridView;
    private int threshold = 2;
   
    public EndlessScrollGridViewListener(GridView gridView) {
    	this.gridView = gridView;
    }
   
    public EndlessScrollGridViewListener(GridView gridView, int threshold) {
    	this.gridView = gridView;
    	this.threshold = threshold;
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem,
            int visibleItemCount, int totalItemCount) {
        
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
    	if (scrollState == SCROLL_STATE_IDLE) {
            if (gridView.getLastVisiblePosition() >= gridView.getCount() - threshold) {
                loadData();
            }
        }
    }
    
    public void loadData(){
    	
    }
}
