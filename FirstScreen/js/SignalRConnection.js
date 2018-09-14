// src:
// ***************
// https://docs.microsoft.com/en-us/aspnet/signalr/overview/guide-to-the-api/hubs-api-guide-javascript-client#nogenconnection


// setting up the hubs url for the connection
$.connection.hub.url = "http://pk029-audi-2nds.tvapp-server.de/SecondScreen";   //"http://localhost:8080/signalr";
// declaring a proxy for referencing the hub
var chat = $.connection.secondScreenHub;

// getting the connection id (temporary) and store it to prepend to messages
$('#displayname').val(prompt('Enter your name:', ''));
// setting initial focus to message input box
$('#message').focus();



// starting the connection
$.connection.hub.start().done(function () {
    console.log("Now connected");
    document.getElementById("castConnectedButton").classList.remove("hidden");
    document.getElementById("castNotConnectedButton").classList.add("hidden");
    
    // generating session connection
    chat.server.joinSession($('#displayname').val());
    
    // handling on click button behaviour
    $('#sendmessage').click(function () {
        
        // calling the send method on the hub (with id and chat-message)
        // -----------------------------------------------------------------
        chat.server.sendMessage($('#displayname').val(), $('#message').val()).done(function() {
            // adding the message to the page
            $('#discussion').append('<li><strong> </strong>' + $('#message').val() + '</li>');

            // clearing text box and reseting focus for next comment
            $('#message').val('').focus();

        }).fail(function(error) {
            console.log( 'sendMessage error: ' + error);
        });
        // -----------------------------------------------------------------
    });
    $('#disconnect').click(function () {
        chat.server.leaveSession($('#displayname').val());
        console.log("Now disconnected");
        document.getElementById("castNotConnectedButton").classList.remove("hidden");
        document.getElementById("castConnectedButton").classList.add("hidden");
    });

}).fail(function() {
    // if starting the connection failed
    console.log("Could not connect");
});



// receiving messages (function that the hub can call to broadcast messages)
// -----------------------------------------------------------------
chat.client.receiveMessage = function (message) {
    var encodedMsg = $('<div />').text(message).html();
    
    // adding the received message to the page content
    $('#discussion').append('<li><strong> </strong>:&nbsp;&nbsp;' + encodedMsg + '</li>');

    if(encodedMsg == "Antarktis"){
        document.body.style.backgroundImage = "url('res/img/Antarktis_map.jpg')";
    }
    else if (encodedMsg == "Afrika"){
        document.body.style.backgroundImage = "url('res/img/Afrika_map.jpg')";
    }
    else if (encodedMsg == "Asien"){
        document.body.style.backgroundImage = "url('res/img/Asien_map.jpg')";
    }
    else if (encodedMsg == "Europa"){
        document.body.style.backgroundImage = "url('res/img/Europa_map.jpg')";
    }
    else if (encodedMsg == "Nordamerika"){
        document.body.style.backgroundImage = "url('res/img/Nordamerika_map.jpg')";
    }
    else if (encodedMsg == "Australien"){
        document.body.style.backgroundImage = "url('res/img/Ozeanien_map.jpg')";
    }
    else if (encodedMsg == "Arktis"){
        document.body.style.backgroundImage = "url('res/img/Arktis_map.jpg')";
    }
    else if (encodedMsg == "Südamerika"){
        document.body.style.backgroundImage = "url('res/img/Südamerika_map.jpg')";
    }
    else if (encodedMsg == "Default"){
        document.body.style.backgroundImage = "url('res/img/Default_map.jpg')";
    }
}
// -----------------------------------------------------------------