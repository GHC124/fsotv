package com.fsotv.vo;

import java.io.Serializable;
import java.util.List;

import com.google.gson.annotations.SerializedName;

public class VideoDataVO implements Serializable {

	private static final long serialVersionUID = 5034779813796473495L;
	
	@SerializedName("updated")
	public String updated;
	
	@SerializedName("totalItems")
	public String totalItems;
	
	@SerializedName("startIndex")
	public String startindex;
	
	@SerializedName("itemsPerPage")
	public String itemsPerPage;
	
	@SerializedName("items")
	private List<ItemsVO> items;
	
	public VideoDataVO(){
		
	}
	
	public String getUpdated() {
		return updated;
	}
	
	public void setUpdated(String updated) {
		this.updated = updated;
	}

	
	public String getTotalItems() {
		return totalItems;
	}

	public void setTotalItems(String totalItems) {
		this.totalItems = totalItems;
	}

	/**
	 * @return the items
	 */
	public List<ItemsVO> getItems() {
		return items;
	}

	/**
	 * @param items the items to set
	 */
	public void setItems(List<ItemsVO> items) {
		this.items = items;
	}
}
