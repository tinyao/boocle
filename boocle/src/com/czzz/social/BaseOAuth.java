package com.czzz.social;

import java.io.Serializable;

public class BaseOAuth implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 7094668799221578674L;

	public int source = 0;
	
	public String accessToken = "";
	
	public String uid = "";
	public String name = "";
	public String avatar = "";
	public String desc = "";
	public String passwd = "";
	
	public int gender = 1;

}
