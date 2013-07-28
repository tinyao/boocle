package com.czzz.douban;

public class BookCommentEntry {

	public String id;
	public String title;
	
	public String author_link;
	public String author_avatar;
	public String author_name;
	public String author_uid;
	
	public String published;
	public String updated;
	public String link;
	public String summary;
	
	public String votes;
	public String useless;
	public String rating;
	
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		if(id == null){
			return "BookCommentEntry@null";
		}
		
		return "{id=" + id + "\n"
				+ "title=" + title + "\n"
				+ "author_link=" + author_link + "\n"
				+ "author_avatar=" + author_avatar + "\n"
				+ "author_name=" + author_name + "\n"
				+ "author_id=" + author_uid + "\n"
				+ "published=" + published + "\n"
				+ "updated=" + updated + "\n"
				+ "link=" + link + "\n"
				+ "summary=" + summary + "\n"
				+ "votes=" + votes + "\n"
				+ "useless=" + useless + "\n"
				+ "rating=" + rating + "}"; 
		
	}
	
	
}
