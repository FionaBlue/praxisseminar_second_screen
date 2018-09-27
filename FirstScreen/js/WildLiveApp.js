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

            // adding onboarding-guide pop-up
            addPopUpTemplate("#onBoardingContent");
        }

        // setting and getting base instance for signalR component (using ref for specific component)
        function setSignalRClient(signalRConnection) { this.signalRConnection = signalRConnection; }
        function getSignalRClient() { return signalRConnection; }
        
        // setting and getting base instance for message handler (using ref for specific component)
        function setMessageHandler(messageHandler) { this.messageHandler = messageHandler; }
        function getMessageHandler() { return messageHandler; }
    
        function onVideoStarted() {
            removePopUpTemplate();  // removing loader
            document.body.style.backgroundImage = "url('res/img/Empty_map.jpg')";
            activateTemplate('#videoTimelineContent');
        }
    
        function onVideoEnded() {
            deactivatePrevTemplate();
            removePopUpTemplate();  // removing loader
            document.body.style.backgroundImage = "url('res/img/Coloured_map.jpg')";
        }
        
        function activateTemplate(templateContent) {
            // loading content (identified by id) from template
            var templateString = document.querySelector(templateContent).innerHTML;
            
            // appending temporary content to local placeholder
            var templatePlaceholder = document.querySelector(".templateBinding");
            templatePlaceholder.innerHTML = templateString;
        }
        
        function deactivatePrevTemplate() {
            deactivateRoot = document.querySelector(".templateBinding");
            deactivateRoot.innerHTML = "";
        }

        function addPopUpTemplate(templateContent) {
            // creating new node (div-element) to be filled with pop-up-id
            var tmpElement = document.createElement("div");
            tmpElement.setAttribute("id", "popUp");

            // filling div with current content (specific popup content)
            var templateString = document.querySelector(templateContent).innerHTML;
            tmpElement.innerHTML = templateString;

            // adding new pop-up-div to template placeholder
            var templatePlaceholder = document.querySelector(".templateBinding");
            templatePlaceholder.appendChild(tmpElement);
        }

        function removePopUpTemplate() {
            var popUpTemplate = document.querySelector("#popUp");
            if (popUpTemplate != null) {
                popUpTemplate.parentNode.removeChild(popUpTemplate);
            }
        }
    
        that.onVideoEnded = onVideoEnded;
        that.onVideoStarted = onVideoStarted;
        that.getSignalRClient = getSignalRClient;
        that.getMessageHandler = getMessageHandler;
        that.addPopUpTemplate = addPopUpTemplate;
        that.removePopUpTemplate = removePopUpTemplate;
        that.init = init;
        return that;
    }());