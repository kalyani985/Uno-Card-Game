/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sa41.team11.control;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import javax.enterprise.context.ApplicationScoped;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.websocket.Session;
import sa41.team11.model.ActionCard;
import sa41.team11.model.Card;
import sa41.team11.model.GameDesk;
import sa41.team11.model.NumberCard;
import sa41.team11.model.Player;
import sa41.team11.model.Table;
import sa41.team11.model.WildCard;

/**
 *
 * @author HeinHtetZaw
 */
@ApplicationScoped
public class Game {

    private Map<String, GameDesk> gameDeskList = new HashMap<>();
    private List<Session> unconnectedlist = new LinkedList<>();
    private String choosencolor = "NA";

    public String connectTable(String gameId, Session s) {
        System.out.println("in connectTable function from Game.java");
        System.out.println(">>>>>>>connectTable function");
        Player p = new Player(s, "PlayerName");
        String ret = addplayer(gameId, p);
        if (ret.equals("PlayerIsAdded")) {
            System.out.println("UnconnectedList size - " + unconnectedlist.size());
            unconnectedlist.remove(s);
            System.out.println(">>UnconnectedList size - " + unconnectedlist.size());
            JsonObjectBuilder JsonBuilder = Json.createObjectBuilder();
            JsonBuilder.add("ConnectType", "PlayerConnectRet");
            JsonBuilder.add("ConnectBy", "Server");
            JsonBuilder.add("Data", "Success");
            JsonObject json = JsonBuilder.build();
            try {
                s.getBasicRemote().sendText(json.toString());
                JsonObjectBuilder JsonBuilder1 = Json.createObjectBuilder();
                JsonBuilder1.add("ConnectType", "SendJoinedPlayerNo");
                JsonBuilder1.add("ConnectBy", "Server");
                JsonBuilder1.add("Data", gameDeskList.get(gameId).getPlayers().size());
                JsonObject json1 = JsonBuilder1.build();
                gameDeskList.get(gameId).getTable().getSessionid().getBasicRemote().sendText(json1.toString());
                return "Success";
            } catch (Exception ex) {
                System.out.println("Exception in connectTable");
            }
        }
        return null;
    }

    public String addplayer(String gameId, Player player) {
        System.out.println("in addplayer function from Game.java");
        System.out.println("in Addplayer");
        GameDesk gameList = gameDeskList.get(gameId);
        if (gameList != null) {
            System.out.print(">>> Player size" + gameList.getPlayers().size());
            System.out.print(">>> NoOfMaxPlayer" + gameList.getNoOfMaxPlayer());
            if (gameList.getPlayers().size() <= gameList.getNoOfMaxPlayer()) {
                gameList.getPlayers().add(player);
                gameDeskList.put(gameId, gameList);
                return "PlayerIsAdded";
            } else {
                return "GameDeskIsFull";
            }
        } else {
            return "GameDeskIsNotExist";
        }
    }

    public String createGame(String gameId, GameDesk gamedesk) {
        System.out.println("in createGame function from Game.java");
        GameDesk gameIdList = gameDeskList.get(gameId);
        if (gameIdList == null) {
            gameDeskList.put(gameId, gamedesk);
            return "GameIsCreated";
        }
        return "GameIsAlreadyExist";
    }

    public String sendGameDeskList() {
        System.out.println("in sendGameDeskList function from Game.java");
        int size = 0;

        JsonObjectBuilder innerJsonBuilder = Json.createObjectBuilder();
        JsonArrayBuilder arrayJsonBuilder = Json.createArrayBuilder();
        JsonObjectBuilder outerJsonBuilder = Json.createObjectBuilder();

        JsonObject innerJson = null;
        JsonArray arrayJson = null;
        JsonObject outerJson = null;

        System.out.println("gamedesklist " + gameDeskList.toString());
        for (String key : gameDeskList.keySet()) {
            if (gameDeskList.get(key).getPlayers().size() < gameDeskList.get(key).getNoOfMaxPlayer()) {
                if (gameDeskList.get(key).getGameStatus().equals("FindingPlayer")) {
                    System.out.println("now sending");
                    size = gameDeskList.get(key).getPlayers().size();
                    innerJsonBuilder.add("GameID", key);
                    System.out.println("Game ID : " + key);
                    innerJsonBuilder.add("Description", gameDeskList.get(key).getDescription());
                    innerJsonBuilder.add("NoOfPlayer", size);
                    System.out.println("No Of Player : " + size);
                    innerJson = innerJsonBuilder.build();
                    System.out.println("json - " + innerJson.toString());
                    arrayJsonBuilder.add(innerJson);
                }
            }
        }
        arrayJson = arrayJsonBuilder.build();
        System.out.println("builder - " + arrayJson.toString());
        outerJsonBuilder.add("ConnectType", "GameList");
        outerJsonBuilder.add("ConnectBy", "Server");
        if (arrayJson.isEmpty()) {
            outerJson = outerJsonBuilder.add("Data", "Empty").build();
        } else {
            outerJson = outerJsonBuilder.add("Data", arrayJson).build();
        }
        System.out.println("outerJson - " + outerJson.toString());
        try {
            System.out.println(">>unconnectedlist size - " + unconnectedlist.size());
            for (Session s : unconnectedlist) {
                s.getBasicRemote().sendText(outerJson.toString());
            }
        } catch (Exception ex) {
            System.out.println("Exception in sendGameDeskList");
            return "Exception";
        }
        System.out.println("JSON is sent to all unconnectList");
        return "Success";
    }

    public String addUnconnectedList(Session s) {
        System.out.println("in addUnconnectedList function from Game.java");
        unconnectedlist.add(s);
        return null;
    }

    public String gameStartMsgSend(String gameid) {
        System.out.println("in gameStartMsgSend function from Game.java");
        GameDesk gd = gameDeskList.get(gameid);
        List<Player> playerList = gd.getPlayers();

        //builder for table
        JsonObjectBuilder tableJsonBuilder = Json.createObjectBuilder();
        tableJsonBuilder.add("ConnectType", "GameStartSuccess");
        tableJsonBuilder.add("ConnectBy", "Server");
        tableJsonBuilder.add("Data", "Success");
        
        //send player list
        JsonArrayBuilder playerArray = Json.createArrayBuilder();

        

        //build for player
        JsonObjectBuilder playerJsonBuilder = Json.createObjectBuilder();
        playerJsonBuilder.add("ConnectType", "GameStartSuccess");
        playerJsonBuilder.add("ConnectBy", "Server");
        playerJsonBuilder.add("Data", "Success");

        JsonObject playerJson = playerJsonBuilder.build();

        try {
            JsonObjectBuilder innerBuilder = Json.createObjectBuilder();
            int i =0;
            for (Player p : playerList) {
                innerBuilder.add("Name", p.getName());
                innerBuilder.add("playerIndex", i++);
                playerArray.add(innerBuilder.build());
                p.getSession().getBasicRemote().sendText(playerJson.toString());
            }
            tableJsonBuilder.add("playerList", playerArray);
            gd.getTable().getSessionid().getBasicRemote().sendText(tableJsonBuilder.build().toString());

        } catch (Exception ex) {
            System.out.println("Exception in gameStart function");
        }
        return "Success";
    }

    public String startGame(String gameid) {
        System.out.println("in startGame function from Game.java");
        try {
            //forTable
            Table table = gameDeskList.get(gameid).getTable();
            gameDeskList.get(gameid).setGameStatus("StartGame");
            //forPlayer
            List<Player> playerList = gameDeskList.get(gameid).getPlayers();
            for (int i = 0; i < 7; i++) {
                for (Player p : playerList) {
                    p.drawCard(table.drawCard());
                }
            }
            table.getDiscardedPile().add(table.drawCard());
            for (Player p : playerList) {
                JsonObjectBuilder jbuilder = Json.createObjectBuilder();
                jbuilder.add("ConnectType", "CardsList");
                jbuilder.add("ConnectBy", "Server");
                jbuilder.add("CardInHand", cardsToJson(p.getCardInHand()));
                jbuilder.add("Turn", "notYourTurn");
                p.getSession().getBasicRemote().sendText(jbuilder.build().toString());

            }
            //for first player
            JsonObjectBuilder jbuilder = Json.createObjectBuilder();
            jbuilder.add("ConnectType", "nextPlayerTurn");
            jbuilder.add("ConnectBy", "Server");
            jbuilder.add("Turn", "yourTurn");
            playerList.get(0).getSession().getBasicRemote().sendText(jbuilder.build().toString());
            updateCardListsTable(gameid);
        } catch (Exception ex) {
            System.out.println("Exception in startGame");
        }
        sendGameDeskList();
        return "Started";
    }

    public String updateCardListsTable(String gameid) {
        System.out.println("in updateCardListsTable function from Game.java");
        Table table = gameDeskList.get(gameid).getTable();

        JsonObjectBuilder jbuilder = Json.createObjectBuilder();
        jbuilder.add("ConnectType", "CardsList");
        jbuilder.add("ConnectBy", "Server");
        jbuilder.add("DrawPileList", cardsToJson(table.getDrawPile()));
        jbuilder.add("DiscardedList", cardsToJson(table.getDiscardedPile()));
        try {
            table.getSessionid().getBasicRemote().sendText(jbuilder.build().toString());
        } catch (Exception ex) {
            System.out.println("Exception in updateCardListsTable");
        }
        return "Success";
    }

    public JsonArray cardsToJson(List<Card> clist) {
        System.out.println("in cardsToJson function from Game.java");
        JsonObjectBuilder jbuilder = Json.createObjectBuilder();
        JsonArrayBuilder jArrayBuilder = Json.createArrayBuilder();
        NumberCard numcrd = null;
        ActionCard actcrd = null;
        WildCard widcrd = null;

        for (Card c : clist) {
            jbuilder.add("point", c.getPoint());
            jbuilder.add("backimg", c.getBackimg());
            jbuilder.add("frontimg", c.getFrontimg());
            if (c.getClass().getName().equals("sa41.team11.model.NumberCard")) {
                numcrd = (NumberCard) c;
                jbuilder.add("color", numcrd.getColor());
                jbuilder.add("value", numcrd.getValue());
            } else if (c.getClass().getName().equals("sa41.team11.model.ActionCard")) {
                actcrd = (ActionCard) c;
                jbuilder.add("color", actcrd.getColor());
                jbuilder.add("value", actcrd.getValue());
            } else if (c.getClass().getName().equals("sa41.team11.model.WildCard")) {
                widcrd = (WildCard) c;
                jbuilder.add("color", "NA");
                jbuilder.add("value", widcrd.getValue());
            }
            jArrayBuilder.add(jbuilder.build());
        }
        return jArrayBuilder.build();
    }

    public String nextPlayerTurn(String gameid) {
        System.out.println("in nextPlayerTurn function from Game.java");
        GameDesk gd = gameDeskList.get(gameid);
        Player p = gd.nextPlayer("NA");

        JsonObjectBuilder jbuilder = Json.createObjectBuilder();
        jbuilder.add("ConnectType", "nextPlayerTurn");
        jbuilder.add("ConnectBy", "Server");
        jbuilder.add("Turn", "yourTurn");
        try {
            p.getSession().getBasicRemote().sendText(jbuilder.build().toString());
        } catch (Exception ex) {
            System.out.println("Exception in nextPlayerTurn");
        }
        return "Success";
    }

    public String dropCard(String color, String val, String gameid, Session s, String choosencolor) {
        System.out.println("in dropCard function from Game.java");
        System.out.println("Color" + color);
        System.out.println("val" + val);
        System.out.println("choosencolor" + this.choosencolor);
        System.out.println("choosencolor" + choosencolor);

        try {
            GameDesk gd = gameDeskList.get(gameid);
            Player curPlayer = gd.getCurrentPlayer();
            String msg = gd.playerDropCard(color, val, s, choosencolor);
            if (msg.equals("Success") || msg.equals("Draw4Card")) {
                System.err.println("Current Player card list size - " + curPlayer.getCardInHand().size());
                if (curPlayer.getCardInHand().isEmpty()) {
                    gameFinish();
                }
                System.out.println("Drop Card is success");
                Player p = null;
                if (msg.equals("Draw4Card")) {
                    p = gd.nextPlayer("drawCard");
                } else {
                    p = gd.nextPlayer("NA");
                }
                for (Player p1 : gd.getPlayers()) {
                    JsonObjectBuilder jbuilder = Json.createObjectBuilder();
                    jbuilder.add("ConnectType", "CardsList");
                    jbuilder.add("ConnectBy", "Server");
                    if (p == p1) {
                        jbuilder.add("Turn", "yourTurn");
                    } else {
                        jbuilder.add("Turn", "notYourTurn");
                    }
                    jbuilder.add("CardInHand", cardsToJson(p1.getCardInHand()));

                    p1.getSession().getBasicRemote().sendText(jbuilder.build().toString());
                }
                updateCardListsTable(gameid);
                JsonObjectBuilder retDropMsgJson = Json.createObjectBuilder();
                retDropMsgJson.add("ConnectType", "CardDropMsgReturn");
                retDropMsgJson.add("ConnectBy", "Server");
                retDropMsgJson.add("Data", "Success");
                curPlayer.getSession().getBasicRemote().sendText(retDropMsgJson.build().toString());

            } else {
                System.out.println("Message in dropCard is" + msg);
            }
        } catch (Exception ex) {
            System.out.println("Exception in dropCard");
        }
        return "Success";
    }

    public String gameFinish() {
        System.out.println("Game is finish");
        return "";
    }
}
