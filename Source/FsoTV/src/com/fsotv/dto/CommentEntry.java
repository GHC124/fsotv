package com.fsotv.dto;

public class CommentEntry {
	String title;
	String content;
	String published;
	public CommentEntry(){
		
	}
	public CommentEntry(String title, String content, String published) {
		super();
		this.title = title;
		this.content = content;
		this.published = published;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public String getPublished() {
		return published;
	}
	public void setPublished(String published) {
		this.published = published;
	}
	
}
