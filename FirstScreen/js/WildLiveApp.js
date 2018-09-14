var WildLiveApp = (function() {
    var that = {},
        signalRConnection;
    
        function init() {
            activatePage('#Weltkarte');
            signalRConnection = new WildLiveApp.SignalRConnection();
            signalRConnection.init();
        }
    
        function onVideoStarted() {
            deactivatePrevPage();
            activatePage('#VideoPlayer');
        }
    
        function onVideoEnded() {            
            deactivatePrevPage();
            activatePage('#Weltkarte');
        }
    
        //src: Montagsmaler-Projekt
        function activatePage(page) {
            templateString = document.querySelector(page).innerHTML;       //template in index.html wird ausgelesen
            tmpElement = document.createElement("div");
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
        that.init = init;
        return that;
    }());