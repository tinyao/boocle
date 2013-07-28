package com.czzz.social.renren;

public class RenRenOAuth {
	
	public static final String OUATH_URL = "https://graph.renren.com/oauth/authorize" +
			"?client_id=218790&response_type=token" +
			"&redirect_uri=http://graph.renren.com/oauth/login_success.html";
	
	private String accessToken = "";
	private String renrenUid = "";
	private String expiresIn = "";

	
	
}
