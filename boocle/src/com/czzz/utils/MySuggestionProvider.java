package com.czzz.utils;

import android.content.SearchRecentSuggestionsProvider;

public class MySuggestionProvider extends SearchRecentSuggestionsProvider {
	  public final static String AUTHORITY = "com.czzz.utils.MySuggestionProvider";
	  public final static int MODE = DATABASE_MODE_QUERIES;

	  public MySuggestionProvider() {
	      setupSuggestions(AUTHORITY, MODE);
	  }
}