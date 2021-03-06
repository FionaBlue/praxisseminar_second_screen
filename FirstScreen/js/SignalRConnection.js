// src:
// ***************
// https://docs.microsoft.com/en-us/aspnet/signalr/overview/guide-to-the-api/hubs-api-guide-javascript-client#nogenconnection

var WildLiveApp = WildLiveApp || {};
WildLiveApp.SignalRConnection = function() {

    var that = {}, messageHandler, chat, sessionID = "1234567890";

    $.connection.hub.url = "http://pk029-audi-2nds.tvapp-server.de/SecondScreen";   // server; "http://localhost:8080/signalr";
    chat = $.connection.secondScreenHub;
    // starting connection to server
    $.connection.hub.start().done(function () {
        
        // generating session connection
        chat.server.joinSession(sessionID);

        //start pinging Second Screen
        pingToAndroidDevice();

    }).fail(function() {
        // if starting the connection failed
    });

    function registerMessageReceiver() {
        // receiving messages (function that the hub can call to broadcast messages)
        chat.client.receiveMessage = function (message) {

            // only handling message if message is not empty
            if (message != "" || message == null) {
                messageHandler = WildLiveApp.getMessageHandler();
                messageHandler.handleMessage(message);
            }
        }
    }
    
    // steadily sending messages to second screen (cast-buttons)
    async function pingToAndroidDevice(){
        sendMessageToAndroidDevice("firstScreenConnected");
        var ping = setTimeout(function() {
            pingToAndroidDevice();
        }, 1000);
    }

    function sendMessageToAndroidDevice(message) {
        // sending message for session id
        chat.server.sendMessage(sessionID, message).done(function() {
            //console.log('sendMessage done: ' + message);

        }).fail(function(error) {
            //console.log( 'sendMessage error: ' + error);
        });
    }

    // function not necessary for prototype concept but maybe for future work
    function disconnect() {
        chat.server.leaveSession(sessionID);

        // switching cast button if connection was disconnected
        document.getElementById("castNotConnectedButton").classList.remove("hidden");
        document.getElementById("castConnectedButton").classList.add("hidden");
    }

    that.sendMessageToAndroidDevice = sendMessageToAndroidDevice;
    that.registerMessageReceiver = registerMessageReceiver;
    return that;
};