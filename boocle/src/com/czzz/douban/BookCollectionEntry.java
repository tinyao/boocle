package com.czzz.douban;


public class BookCollectionEntry {

	public String detail;
	public String updated;
	public String status;
	
	public DoubanBook book;
	
//	public String title;
//	public String subtitle;
//	public String pages;
//	public String image;
//	public String mobile_link;
//	public String link;
//	
//	public String isbn10;
//	public String isbn13;
//	public String author;
//	public String translator;
//	public String price;
//	public String publisher;
//	public String pubdate;
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		if(status == null){
			return "BookCollection@null";
		}
		
		return "{detail=" + detail + "\n"
				+ "updated=" + updated + "\n"
				+ "status=" + status + "\n"
				+ "book: {" + book + "}";
	}
	
	
}
