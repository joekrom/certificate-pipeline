package de.axxepta.metaselect;

public class User {

    public String userID;
	public String apiToken;
	private static final String TOKEN = "xyz";
	
	public User() {
		
	}

	public static String getToken() {
		return TOKEN;
	}
    
}
