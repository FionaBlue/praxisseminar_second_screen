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
}
// -----------------------------------------------------------------