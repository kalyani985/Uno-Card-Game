/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sa41.team11.control;

import java.awt.BorderLayout;
import java.io.ByteArrayInputStream;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import sa41.team11.model.GameDesk;
import sa41.team11.model.Table;

/**
 *
 * @author HeinHtetZaw
 */
@RequestScoped
@ServerEndpoint("/game/{data}")
public class GameSocket {

    @Inject
    private Game game;
    private Session session;
    private String data;

    @OnOpen
    public void open(Session s, @PathParam("data") String data) {
        System.out.println("in open function from GameSocket.java");
        session = s;
        try {
            if (data.equals("player")) {
                System.out.println("this is player");
                player();

            } else {
                System.out.println("this is table");
                this.data = data;
                session.getBasicRemote().sendText("Table");
            }

        } catch (Exception ex) {
            System.err.println("Exception in open");
        }
        System.out.println(">>>Connected to server " + session.getId());
    } 

    @OnMessage
    public void message(String msg) {
        System.out.println("in message function from GameSocket.java");
        JsonReader reader = Json.createReader(
                new ByteArrayInputStream(msg.getBytes()));
        JsonObject json = reader.readObject();

        System.out.println("Incomming Message");
        System.out.println(json.toString());

        if (json.getString("ConnectBy").equals("Table")) {
            System.out.println("GetMessageFrom Table");
            if (json.getString("ConnectType").equals("SendDescNoPlayer")) {
                SendDescNoPlayer(json);
            } else if (json.getString("ConnectType").equals("GameStart")) {
                StartGame(json);
            }
        } else if (json.getString("ConnectBy").equals("Player")) {
            System.out.println("GetMessageFrom Player");
            System.out.println(json.toString());
            if (json.getString("ConnectType").equals("SendConnectTable")) {
                System.out.println(json.getString("GameId"));
                SendConnectTable(json.getString("GameId"));
            } else if (json.getString("ConnectType").equals("dropCard")) {
                System.out.println("msg is in dropcard");
                this.PlayerdropCard(json.getString("color"), json.getString("value"), json.getString("gameid"), this.session, json.getString("choosencolor"));
            }
        }
    }

    @OnClose
    public void close() {
        System.out.println("in close function from GameSocket.java");
        System.out.println(">>>Disconnected from server " + session.getId());
    }

    public void PlayerdropCard(String color, String value, String gameid, Session s, String choosenColor) {
        System.out.println("in dropCard function from GameSocket.java");
        game.dropCard(color, value, gameid, s, choosenColor);
    }

    public void player() {
        System.out.println("in player function from GameSocket.java");
        System.out.println("player >> before added to add unconnectedlist");
        game.addUnconnectedList(session);
        System.out.println("player >>after added to add unconnectedlist");
        game.sendGameDeskList();
    }

    public void SendDescNoPlayer(JsonObject js) {
        System.out.println("in SendDescNoPlayer function from GameSocket.java");
        try {
            System.out.println("No Of Player ==== " + Integer.parseInt(js.getString("NoOfPlayer")));
            game.createGame(data, new GameDesk(session.getId(), js.getString("Description"), new Table(session), Integer.parseInt(js.getString("NoOfPlayer"))));
            game.sendGameDeskList();
        } catch (Exception ex) {
            System.out.println("Exception in Table function of GameSocket");
        }
    }

    public void SendConnectTable(String gameid) {
        System.out.println("in SendConnectTable function from GameSocket.java");
        game.connectTable(gameid, session);
        game.sendGameDeskList();
    }

    public void StartGame(JsonObject js) {
        System.out.println("in StartGame function from GameSocket.java");
        System.out.println("Game is starting now");
        game.gameStartMsgSend(js.getString("Data"));
        game.startGame(js.getString("Data"));
    }
}
