import java.io.IOException;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.websocket.Session;

import org.json.JSONArray;
import org.json.JSONObject;

public class SessionHandler {
	
	private final Set<Session> sessionTable = new CopyOnWriteArraySet<>();
	private final HashMap<String, Player> players = new HashMap<>();
	
	
	public void addSession(Session session, Player player) {
		sessionTable.add(session);
		players.put(session.getId(), player);	
	}
	
	public void removeSession(Session session) {
		sessionTable.remove(session);
		players.remove(session.getId());
	}
	
	public void sendMessage(String message) {
		synchronized (sessionTable) {
			sessionTable.forEach(session -> {
				try {
					session.getBasicRemote().sendText(message);
				} catch(IOException e) {
					e.printStackTrace();
				}
			});
		}
	}
	public void sendBroadcast(String message, String currentSession) {
		synchronized (sessionTable) {
			sessionTable.forEach(session -> {
				if(currentSession != session.getId()) {
					try {
						session.getBasicRemote().sendText(message);
					} catch(IOException e) {
						e.printStackTrace();
					}	
				}
			});
		}
	}
	public boolean verifyColor(String color) {
		for(Player player: players.values()) {
			if(color.equals(player.getColor())) {
				return true;
			}
		}
		return false;
	}
	// Getters
	public Player getPlayer(String id) {
		return players.get(id);
	}
	public JSONArray getPlayers() {
		JSONArray arrayOfPlayers = new JSONArray();
		for(Player player: players.values()) {
			JSONObject data = new JSONObject();			
			data.put("username", player.getUsername());
			data.put("color", player.getColor());
			arrayOfPlayers.put(data);
		}
		return arrayOfPlayers;
	}	
}




