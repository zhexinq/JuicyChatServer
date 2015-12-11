package edu.cmu.zhexinq.webmobilegroupchat.util;

import java.util.HashSet;

import javax.websocket.Session;

// group sessions

public class GroupSessions {
	HashSet<Session> sessions;
	int memberCount;
	
	public GroupSessions() {
		sessions = new HashSet<Session>();
		memberCount = 0;
	}
	
	public HashSet<Session> getSessions() {
		return sessions;
	}
	public void setSessions(HashSet<Session> sessions) {
		this.sessions = sessions;
	}
	public int getMemberCount() {
		return memberCount;
	}
	public void setMemberCount(int memberCount) {
		this.memberCount = memberCount;
	}
	
	// increment member count
	public void addMember() {
		memberCount++;
	}
	
	// decrement member count
	public void decrementMember() {
		memberCount--;
	}
	
	// add a session
	public void addSession(Session s) {
		sessions.add(s);
	}
	// remove a session
	public void removeSession(Session s) {
		sessions.remove(s);
	}
	
	public void printInfo() {
		for (Session s : sessions) {
			System.out.println("session: " + s.getId()); 
		}
		System.out.println("member count: " + memberCount);
	}
}
