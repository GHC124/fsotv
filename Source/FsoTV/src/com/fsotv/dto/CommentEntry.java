package com.fsotv.dto;

public class CommentEntry {
	String title;
	String content;
	String published;
	String author;
	
	public CommentEntry(){
		
	}
	public CommentEntry(String title, String content, String published, String author) {
		super();
		this.title = title;
		this.content = content;
		this.published = published;
		this.author = author;
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
	public String getAuthor() {
		return author;
	}
	public void setAuthor(String author) {
		this.author = author;
	}
	
}
