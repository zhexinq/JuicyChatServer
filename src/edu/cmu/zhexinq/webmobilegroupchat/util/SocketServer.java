package edu.cmu.zhexinq.webmobilegroupchat.util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import javax.websocket.server.ServerEndpoint;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.common.collect.Maps;

import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;

import edu.cmu.zhexinq.webmobilegroupchat.*;

@ServerEndpoint("/chat")
public class SocketServer {

	// map group code to sessions
	private static final Map<String, GroupSessions> groupRecords = Collections
			.synchronizedMap(new LinkedHashMap<String, GroupSessions>());
	
//	// set to store all the live sessions
//	private static final Set<Session> sessions = Collections
//			.synchronizedSet(new HashSet<Session>());
	
	// mapping between session and person name
	private static final HashMap<String, String> nameSessionPair = new HashMap<String, String>();
	
	// mapping between group and session
	private static final HashMap<String, String> groupSessionPair = new HashMap<String, String>();
	
	// json utils 
	private JSONUtils jsonUtils = new JSONUtils();
	
	// get query params
	public static Map<String, String> getQueryMap(String query) {
		Map<String, String> map = Maps.newHashMap();
		if (query != null) {
			String[] params = query.split("&");
			for (String param : params) {
				String[] nameval = param.split("=");
				map.put(nameval[0], nameval[1]);
			}
		}
		return map;
	}
	
	// decode query param
	public static String decodeQueryParam(Map<String, String> queryParams, String key) {
		if (queryParams.containsKey(key)) {
			// getting param value
			String value = queryParams.get(key);
			try {
				value = URLDecoder.decode(value, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			// return the value back
			return value;
		}
		return null;
	}
	
	// called when a socket connection opened
	@OnOpen
	public void onOpen(Session session) {
		
		System.out.println(session.getId() + " has opened a connection");
		
		Map<String, String> queryParams = getQueryMap(session.getQueryString());
		
		String name = "", groupCode = "", action = "", isOldUser = "";
		
		// get user name, groupCode, action, isOldUser of the session
		name = decodeQueryParam(queryParams, "name");
		groupCode = decodeQueryParam(queryParams, "groupCode");
		action = decodeQueryParam(queryParams, "action");
		// determine whether the user already in the group
		isOldUser = decodeQueryParam(queryParams, "isOldUser");
		System.out.printf("get session name: %s, session group code: %s, session action: %s, isOldUser: %s\n", 
				 name, groupCode, action, isOldUser);
		
		// get sessions out of the group code
		GroupSessions singleGroupInfo = groupRecords.get(groupCode);
		
		// request for create
		if (action.equals("create") && singleGroupInfo == null) {
			System.out.println("Create a new group for: " + groupCode);
			GroupSessions newGroup = new GroupSessions();
			newGroup.addMember();
			newGroup.addSession(session);
			groupRecords.put(groupCode, newGroup);
			printRecords();
		} else if (action.equals("join") && singleGroupInfo != null) { // request for join
			System.out.println(""
					+ "join a exist group for: " + groupCode);
			singleGroupInfo.addSession(session);
			if (isOldUser.equals("false"))
				singleGroupInfo.addMember();
			printRecords();
		} else if (action.equals("delete") && singleGroupInfo != null) { // do operation according to delete
			System.out.println("Delete a exist group for: " + groupCode);
			// not member in the group, remove it, otherwise decrement count
			if (singleGroupInfo.getMemberCount() == 1)
				groupRecords.remove(groupCode);
			else 
				singleGroupInfo.decrementMember();
			// close the connection
			try {
				session.close();
			} catch(IOException e) {
				System.out.println("Error closing session " + session.getId());
				e.printStackTrace();
			} 
			printRecords();
			return;
		} else {
			System.out.println("Bad request closing the client.");
			printRecords();
			try {
				session.getBasicRemote().sendText(jsonUtils
						.getClientDetailsJson(groupCode, session.getId(), "reject"));
				session.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return;
		}
		
		
		// mapping client session id and name
		nameSessionPair.put(session.getId(), name);
		// mapping client session id and group code
		groupSessionPair.put(session.getId(), groupCode);
		
//		// adding session to session list
//		sessions.add(session);
		
		try {
			// send session id to the client just connected 
			session.getBasicRemote().sendText(jsonUtils.getClientDetailsJson(groupCode, session.getId(), 
					"Your session details"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// notify all clients about a new person joined
		sendMessageToAll(groupCode, session.getId(), name, " joined conversation", true, false);
	}
	
	// method called when new message received from any client
	@OnMessage
	public void onMessage(String message, Session session) {
		System.out.println("Message from " + session.getId() + ": " + message);
		
		String msg = null;
		String flag = null;
		String groupCode = null;
		
		// parse the json and getting message
		try {
			JSONObject clientMsg = new JSONObject(message);
			flag = clientMsg.getString("flag");
			msg = clientMsg.getString("message");
			groupCode = clientMsg.getString("groupCode");
			if (flag.equals(JSONUtils.FLAG_MESSAGE)) {
				// normal message
				// send the message to all clients
				sendMessageToAll(groupCode, session.getId(), nameSessionPair.get(session.getId()), msg, false, false);
			} else if (flag.equals(JSONUtils.FLAG_DELETE_GROUP)) {
				
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
	}
	
	// method called when a connection is closed
	@OnClose
	public void onClose(Session session) {
		
		System.out.println("Session " + session.getId() + " has ended");
		
		// get the client name that exited
		String name = nameSessionPair.get(session.getId());
		// get the session's group code
		String groupCode = groupSessionPair.get(session.getId());
		
		// remove the session from sessions list and corresponding session-group, session-name pair
		GroupSessions singleGroupInfo = groupRecords.get(groupCode);
		singleGroupInfo.removeSession(session);
		nameSessionPair.remove(session.getId());
		groupSessionPair.remove(session.getId());
		
		// notify all the clients about person exit
		sendMessageToAll(groupCode, session.getId(), name, " left conversation!", false, true);
	}
	
	// send message to all clients
	private void sendMessageToAll(String groupCode, String sessionId, String name, String message,
			boolean isNewClient, boolean isExit) {
		// get sessions in a certain group
		HashSet<Session> sessions = groupRecords.get(groupCode).getSessions();
		
		// loop through all the sessions and send the message individually
		for (Session s : sessions) {
				String json = null;
			System.out.println("send message to session: " + s.getId());
			// check if the message is about new client joined
			if (isNewClient) {
				json = jsonUtils.getNewClientJson(groupCode, sessionId, name, message, sessions.size());
			} else if (isExit) {
				// check if the person left the conversation
				json = jsonUtils.getClientExitJson(groupCode, sessionId, name, message, sessions.size());
			} else {
				// normal chat conversation message
				json = jsonUtils.getSendAllMessageJson(groupCode, sessionId, name, message);
			}
			
			try {
				System.out.println("Sending message to: " + sessionId + ", " + json);
				s.getBasicRemote().sendText(json);
			} catch (IOException e) {
				System.out.print("error in sending. " + s.getId() + ", " + e.getMessage());
				e.printStackTrace();
			}
		}
	}
	
	// helper method to print the group records
	public void printRecords() {
		System.out.println("(----- Group records ---)");
		for (String key : groupRecords.keySet()) {
			System.out.println("group code: " + key);
			groupRecords.get(key).printInfo();
		}
	}
}
