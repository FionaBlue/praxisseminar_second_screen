// src:
// ***************
// https://docs.microsoft.com/en-us/aspnet/signalr/overview/guide-to-the-api/hubs-api-guide-javascript-client#nogenconnection

var WildLiveApp = WildLiveApp || {};
WildLiveApp.SignalRConnection = function() {

    var that = {}, messageHandler, chat;

    $.connection.hub.url = "http://pk029-audi-2nds.tvapp-server.de/SecondScreen";   // server; "http://localhost:8080/signalr";
    chat = $.connection.secondScreenHub;
    // starting connection to server
    $.connection.hub.start().done(function () {
        console.log("Now connected");

        // switching cast button if connection could be initialized
        document.getElementById("castConnectedButton").classList.remove("hidden");
        document.getElementById("castNotConnectedButton").classList.add("hidden");
        
        // generating session connection
        chat.server.joinSession('123456789');

    }).fail(function() {
        // if starting the connection failed
        console.log("Could not connect");
    });

    function registerMessageReceiver() {
        // receiving messages (function that the hub can call to broadcast messages)
        chat.client.receiveMessage = function (message) {

            // only handling message if message is not empty
            if (message != "") {
                messageHandler = WildLiveApp.getMessageHandler();
                messageHandler.handleMessage(message);
            }
        }
    }

    function sendMessageToAndroidDevice(message) {
        // sending message for session id
        chat.server.sendMessage("123456789", message).done(function() {
            console.log('sendMessage done: ' + message);

        }).fail(function(error) {
            console.log( 'sendMessage error: ' + error);
        });
    }

    function disconnect() {
        chat.server.leaveSession('123456789');
        console.log("Now disconnected");

        // switching cast button if connection was disconnected
        document.getElementById("castNotConnectedButton").classList.remove("hidden");
        document.getElementById("castConnectedButton").classList.add("hidden");
    }

    that.sendMessageToAndroidDevice = sendMessageToAndroidDevice;
    that.registerMessageReceiver = registerMessageReceiver;
    return that;
};