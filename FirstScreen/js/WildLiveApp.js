var WildLiveApp = (function() {
    var that = {}, signalRConnection, messageHandler;
    
        function init() {
            // initializing signalR component and set base instance
            signalRConnection = new WildLiveApp.SignalRConnection();
            signalRConnection.registerMessageReceiver();
            setSignalRClient(signalRConnection);

            // initializing message handler component for receiving messages from android device
            messageHandler = new WildLiveApp.MessageHandler();
            messageHandler.init();
            setMessageHandler(messageHandler);


            // handling ui display
            activatePage('#Weltkarte');
        }

        // setting and getting base instance for signalR component (using ref for specific component)
        function setSignalRClient(signalRConnection) { this.signalRConnection = signalRConnection; }
        function getSignalRClient() { return signalRConnection; }
        
        // setting and getting base instance for message handler (using ref for specific component)
        function setMessageHandler(messageHandler) { this.messageHandler = messageHandler; }
        function getMessageHandler() { return messageHandler; }
    
        function onVideoStarted() {
            deactivatePrevPage();
            activatePage('#VideoPlayer');
            document.body.style.backgroundImage = "url('res/img/img_background_stars.jpg')";
        }
    
        function onVideoEnded() {            
            deactivatePrevPage();
            activatePage('#Weltkarte');
            document.getElementById("loader").classList.add("hidden");
            document.body.style.backgroundImage = "url('res/img/Default_map.jpg')";
        }
    
        //src: Montagsmaler-project
        function activatePage(page) {
            templateString = document.querySelector(page).innerHTML;       // reading template in index.html
            tmpElement = document.createElement("div");                    // creating new div for loading template content
            tmpElement.innerHTML = templateString;
            start = document.querySelector(".templateBinding");
            start.appendChild(tmpElement);
          }
        
        function deactivatePrevPage() {
            deactivateRoot = document.querySelector(".templateBinding");
            deactivateRoot.innerHTML = "";
        }
    
        that.onVideoEnded = onVideoEnded;
        that.onVideoStarted = onVideoStarted;
        that.getSignalRClient = getSignalRClient;
        that.getMessageHandler = getMessageHandler;
        that.init = init;
        return that;
    }());