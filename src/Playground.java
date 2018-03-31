import java.io.IOException;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import org.json.JSONArray;
import org.json.JSONObject;


@ServerEndpoint("/sharedBoard/{username}/{color}")
public class Playground {
	
	private static SessionHandler sessionHandler = new SessionHandler();	
	
	@OnMessage
	public void onMessage (String msg, Session session) throws IOException {		
		sessionHandler.sendMessage(msg);
	}

	@OnOpen
	public void onOpen(Session session, @PathParam("username") String username, @PathParam("color") String color) throws IOException {
		JSONObject message = new JSONObject();
		Player player = new Player();
		player.setUsername(username);
		player.setColor(color);
		if(!sessionHandler.verifyColor(color)) {
			sessionHandler.addSession(session, player);
			JSONObject newPlayer = new JSONObject();
			message.put("event", "join");
			newPlayer.put("username", username);
			newPlayer.put("color", color);
			message.put("player", newPlayer);
			JSONArray players = sessionHandler.getPlayers();
			message.put("players", players);
			sessionHandler.sendMessage(message.toString());
		} else {
			message.put("event", "denied");
			session.getBasicRemote().sendText(message.toString());
		}
	}
	
	@OnClose
	public void onClose(Session session) throws IOException {
		Player player = sessionHandler.getPlayer(session.getId());
		sessionHandler.removeSession(session);
		JSONObject message = new JSONObject();
		JSONObject newPlayer = new JSONObject();
		message.put("event", "leave");
		newPlayer.put("username", player.getUsername());
		newPlayer.put("color", player.getColor());
		message.put("player", newPlayer);
		JSONArray players = sessionHandler.getPlayers();
		message.put("players", players);
		sessionHandler.sendMessage(message.toString());
	}
	
	@OnError	
	public void onError(Throwable e) throws IOException {
	    e.printStackTrace();
	}

	 
}