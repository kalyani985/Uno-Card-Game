var socket = null;
var gameId;
var conected = false;
var turn = 0;//not his turn
var value;
var StoreObject;
$(function () {
    $("#btnTemp").on("click", function () {
        socket = new WebSocket("ws://localhost:8080/Uno/game/player");
        socket.onopen = function () {
            console.log("connected");
        };
        socket.onmessage = function (evt) {
            conected = true;
            console.log("in onmessage" + evt.data);
            var data = JSON.parse(evt.data);
            console.log(data.ConnectType);
            if (data.ConnectType === "GameList") {
                console.log("Data Connect Type is GameList");
                GameList(data.Data);
                $("#connectGame").hide();
                $("#ShowGameList").show();
            }
            if (data.ConnectType === "PlayerConnectRet") {
                console.log("Data Connect Type is PlayerConnectRet");
                if (data.Data === "Success")
                    ConnectedToGame();
            }
            if (data.ConnectType === "GameStartSuccess") {
                console.log("Data Connect Type is GameStartSuccess");
                if (data.Data === "Success")
                    GameStarting();
            }
            if (data.ConnectType === "CardsList") {
                console.log("Data Connect Type is CardsList");
                ShowCardsList(data.CardInHand);
                DisplayTurnStatus(data.Turn);
            }
            if (data.ConnectType === "nextPlayerTurn") {
                DisplayTurnStatus(data.Turn);
            }
            if (data.ConnectType === "CardDropMsgReturn") {
                if (data.Data === "Success") {
                    console.log("CardDropMsgReturn is Success");
                } else if (data.Data === "Fail") {
                    console.log("CardDropMsgReturn is Fail");
                }
            }
            console.log("Msg is comming");
        };
    });
    var DisplayTurnStatus = function (data) {
        console.log("In DisplayTurnStatus Function" + data);
        if (data === "notYourTurn") {
            $("#turn").text("Not Your Turn");
            turn = 1;
        }
        if (data === "yourTurn") {
            $("#turn").text("Your Turn");
            turn = 0;
        }
    };
    var GameList = function (glist) {
        console.log("inGameList function");
        $("#gamelist").empty();
        if (glist === "Empty") {
            $("#gamelist").append("There is no game yet..");
        } else {
            $.each(glist, function (i, game) {
                console.log(">>game id = " + game.GameID);

                var temp = "<div style=\"border-style: groove;\" id=\"connect_" + game.GameID + "\"><h1><b>" + game.GameID;
                temp += "</b></h1><hr>Description : " + game.Description;
                temp += "</br>No of Player : " + game.NoOfPlayer + "</br>";
                temp += "<button onClick=\"conGame(" + game.GameID;
                temp += ")\">Join!</button></div>";
                $("#gamelist").append(temp);
            });
        }
        ;
    };
    var ConnectedToGame = function () {
        console.log("inConnectedToGame");
        $("#ShowGameList").hide();
        $("#WaitingGame").show();
    };
    var GameStarting = function () {
        console.log("in GameStarting");
        $("#WaitingGame").hide();
        $("#inGame").show();
    };
    var ShowCardsList = function (data) {
        var temp;
        var leftAlign = 0;
        $("#Cards").text("");
        $.each(data, function (i, card) {
            temp = "<div id=\"card\" ";
            temp += "onClick=\"selectCard('" + card.color + "','" + card.value + "',this)\"";
            temp += "style=\"position: absolute;left: " + leftAlign + "px;\">";
            temp += "<img src=\"img/" + card.frontimg + ".png\">";
            temp += "</div>";
            $("#Cards").append(temp);
            leftAlign += 70;
        });
    };

});
var selectCard = function (color, v, object) {
    console.log("in Select Card function");
    StoreObject = object;
    console.log("StoreObject is");
    console.log(StoreObject);
    if (color === "NA") {
        console.log("color is NA");
        value = v;
        $("#choosencolor").show();
        $("#CardinHand").hide();
    } else {
        console.log("color is not NA");
        var msg = {
            ConnectType: "dropCard",
            ConnectBy: "Player",
            color: color,
            value: v,
            choosencolor: "NA",
            gameid: gameId.toString()
        };
        console.log("Message send to server");
        socket.send(JSON.stringify(msg));
    }
};

var conGame = function (gameid) {
    gameId = gameid;
    var json = {
        ConnectType: "SendConnectTable",
        ConnectBy: "Player",
        GameId: gameid + ""
    };
    socket.send(JSON.stringify(json));
};
var wildCardSelected = function (choosencolor) {
    $("#choosencolor").hide();
    $("#CardinHand").show();

    var msg = {
        ConnectType: "dropCard",
        ConnectBy: "Player",
        color: "NA",
        value: value,
        choosencolor: choosencolor,
        gameid: gameId.toString()
    };
    socket.send(JSON.stringify(msg));
};