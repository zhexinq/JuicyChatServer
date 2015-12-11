package edu.cmu.zhexinq.webmobilegroupchat.util;

import org.json.JSONException;
import org.json.JSONObject;



// flag to identify the kind of json response on the client side
public class JSONUtils {

	// flag to identify the kind of json response on the client side
	public static final String FLAG_SELF = "self",
								FLAG_NEW = "new",
								FLAG_MESSAGE = "message",
								FLAG_EXIT = "exit",
								FLAG_DELETE_GROUP = "delete";
	
	public JSONUtils() {
		
	}
	
	// tell client its session detail
	// {groupCode, sessionId, flag, message}
	public String getClientDetailsJson(String groupCode, String sessionId, String message) {
		String json = null;
		
		try {
			JSONObject sessionInfo = new JSONObject();
			sessionInfo.put("groupCode", groupCode);
			sessionInfo.put("flag", FLAG_SELF);
			sessionInfo.put("sessionId", sessionId);
			sessionInfo.put("message", message);
			
			json = sessionInfo.toString();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return json;
	}
	
	// notify all clients about new person joined
	public String getNewClientJson(String groupCode, String sessionId, String name, String message, int onlineCount) {
		String json = null;
		
		try {
			JSONObject newClientInfo = new JSONObject();
			newClientInfo.put("flag", FLAG_NEW);
			newClientInfo.put("name", name);
			newClientInfo.put("sessionId", sessionId);
			newClientInfo.put("message", message);
			newClientInfo.put("onlineCount", onlineCount);
			newClientInfo.put("groupCode", groupCode);
			
			json = newClientInfo.toString();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return json;
	}
	
	// when a client exits the socket connection
	public String getClientExitJson(String groupCode, String sessionId, String name, String message, int onlineCount) {
		String json = null;
		
		try {
			JSONObject exitClientInfo = new JSONObject();
			exitClientInfo.put("flag", FLAG_EXIT);
			exitClientInfo.put("name", name);
			exitClientInfo.put("sessionId", sessionId);
			exitClientInfo.put("message", message);
			exitClientInfo.put("onlineCount", onlineCount);
			exitClientInfo.put("groupCode", groupCode);
			
			json = exitClientInfo.toString();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return json;
	}
	
	// send messages to all clients
	public String getSendAllMessageJson(String groupCode, String sessionId, String fromName, String message) {
		String json = null;
		
		try {
			JSONObject speakerInfo = new JSONObject();
			speakerInfo.put("flag", FLAG_MESSAGE);
			speakerInfo.put("sessionId", sessionId);
			speakerInfo.put("name", fromName);
			speakerInfo.put("message", message);
			speakerInfo.put("groupCode", groupCode);
			json = speakerInfo.toString();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return json;
	}
}
