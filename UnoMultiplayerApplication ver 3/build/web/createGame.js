var socket = null;
var gameId;
var leftAlign = 0;
$(function () {
    $("#creategame").on("click", function () {
        if ($("#NoOfPlayer").val() < 2) {
            alert("No of player need to be greater than 2");
        } else if ($("#NoOfPlayer").val() > 10) {
            alert("No of player should'nt greater than 10");
        } else {
            console.log("Connecting Request");
            socket = new WebSocket("ws://localhost:8080/Uno/game/" + $("#GameId").val());
            gameId = $("#GameId").val();
            console.log(">>game id is " + gameId);
            socket.onopen = function () {
                console.log("connected");
            };
            socket.onmessage = function (evt) {

                if (evt.data === "Table") {
                    console.log("connect ok is comming");
                    connectOK();
                } else
                {
                    if (JSON.parse(evt.data).ConnectType === "SendJoinedPlayerNo") {
                        console.log();
                        getJoinedPlayerNo(JSON.parse(evt.data).Data);
                    } else if (JSON.parse(evt.data).ConnectType === "GameStartSuccess") {
                        if (JSON.parse(evt.data).Data === "Success")
                            GameStarting(JSON.parse(evt.data).playerList);
                    } else if (JSON.parse(evt.data).ConnectType === "CardsList") {
                        console.log("in showing");
                        showDrawPileListList(JSON.parse(evt.data).DrawPileList);
                        showDiscardedListList(JSON.parse(evt.data).DiscardedList);
                    }
                }
                console.log(">>>>>>Message came");
            };
        }
    });
    var showDrawPileListList = function (data) {
        var temp;
        $.each(data, function (i, card) {
            temp = "<div id=\"card\" ";
            temp += "data-color=\"" + card.color + "\" ";
            temp += "data-value=\"" + card.value + "\">";
            temp += "<img src=\"img/" + card.frontimg + ".png\">";
            temp += "</div>";
            $("#DrawPile").append(temp);
        });
    };

    var showDiscardedListList = function (data) {
        $("#DiscardedCards").text("");
        var temp;
        leftAlign = 0;
        $.each(data, function (i, card) {
            temp = "<div id=\"card\" ";
            temp += "data-color=\"" + card.color + "\" ";
            temp += "data-value=\"" + card.value + "\"";
            temp += "style=\"position: absolute;left: " + leftAlign + "px;\">";
            temp += "<img src=\"img/" + card.frontimg + ".png\">";
            temp += "</div>";
            $("#DiscardedCards").append(temp);
            leftAlign += 15;
        });
    };

    $("#StartGamebtn").on("click", function () {
        var msg = {
            ConnectType: "GameStart",
            ConnectBy: "Table",
            Data: $("#GameId").val()
        };
        socket.send(JSON.stringify(msg));
    });

    var GameStarting = function (data) {
        console.log("in GameStarting");
        $("PlayerList").text("");
        var temp;
        $.each(data, function (i, player) {
            temp = "<img src='img/profile.jpg' style='width: 150px;' ";
            temp += "onclick=drawCard('" + player.playerIndex + "') id='player' >";
            $("#PlayerList").append(temp);
        });
                /*
                 *     padding: 1px;
                 border: 1px solid #021a40;
                 background-color: red;
                 
                 * 
                 */
        $("#WaitingGameDiv").hide();
        $("#inGame").show();
    };
});
var connectOK = function () {
    $("#CreateGameDiv").hide();
    $("#WaitingGameDiv").show();
    $("#gameIdShow").text(gameId);
    $("#playersjoined").text(0);
    var msg = {
        ConnectType: "SendDescNoPlayer",
        ConnectBy: "Table",
        Description: $("#Description").val(),
        NoOfPlayer: $("#NoOfPlayer").val()
    };
    socket.send(JSON.stringify(msg));
};
var getJoinedPlayerNo = function (data) {
    $("#playersjoined").text(data);
    if (data >= 2) {
        $("#StartGamebtn").prop('disabled', false);
    } else {
        $("#StartGamebtn").prop('disabled', true);
    }
};