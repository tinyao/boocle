package com.czzz.bookcircle;

import org.json.JSONException;
import org.json.JSONObject;

public class BugItem {

	public int id, uid;
	public String name, content, device, system, state;
	
	public long create_at;
	
	public BugItem(JSONObject json) { 
		// TODO Auto-generated constructor stub
		
		try {
			
			id = Integer.valueOf(json.getString("id"));
			uid = Integer.valueOf(json.getString("uid"));
			create_at = Long.valueOf(json.getString("create_at"));
			name = json.getString("name");
			content = json.getString("content");
			device = json.getString("device");
			system = json.getString("system");
			state = json.getString("state");
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
