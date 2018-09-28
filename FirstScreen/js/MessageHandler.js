var WildLiveApp = WildLiveApp || {};
WildLiveApp.MessageHandler = function() {
    var that = {}, youTubePlayer, databaseHandler, androidConnection = false, falseBuffer = 0, asyncIsAlreadyLooping = false;

    function init() {
        youTubePlayer = new WildLiveApp.YouTubePlayer();
        
        // initializing database (firebase) component
        databaseHandler = new WildLiveApp.DatabaseHandler();
    }

    async function checkConnectionToAndroidDevice(){
        asyncIsAlreadyLooping = true;
        if(androidConnection == true){
            document.getElementById("castConnectedButton").classList.remove("hidden");
            document.getElementById("castNotConnectedButton").classList.add("hidden");
        } else {
            document.getElementById("castConnectedButton").classList.add("hidden");
            document.getElementById("castNotConnectedButton").classList.remove("hidden");
        }
        if(falseBuffer < 3){
            falseBuffer ++;
        } else {
            falseBuffer = 0;
            androidConnection = false;
        }
        var checkConnection = setTimeout(function() {
            checkConnectionToAndroidDevice();
        }, 3000);
    }

    function handleMessage(encodedMsg){
        // -----------------------------------------------------------------
        //CastButton-Handling
        if(encodedMsg.includes("secondScreenConnected")){
            //console.log("secondScreenConnected");
            androidConnection = true;
            if(asyncIsAlreadyLooping == false){
                checkConnectionToAndroidDevice();
            }  
        }
        // -----------------------------------------------------------------
        //Highlighting
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
        // -----------------------------------------------------------------
        //playVideo
        if(encodedMsg.includes("playVideo")){

            //while (youTubePlayer == null) { /*wait*/ } because at this position, the player somethimes is null (!)

            WildLiveApp.onVideoStarted();
            console.log("playVideo " + encodedMsg);
            videoID = encodedMsg.slice(9);         
            youTubePlayer.loadPlayer(videoID);            
            
            // binding database functionality and data from firebase by accessing id and player
            databaseHandler.init(videoID, youTubePlayer);

            WildLiveApp.removePopUpTemplate();
        }
        //pauseVideo
        if(encodedMsg.includes("pauseVideo")){
            youTubePlayer.pauseVideo();
        }
        //stopVideo
        if(encodedMsg.includes("stopVideo")){
            youTubePlayer.stopVideo();
            WildLiveApp.onVideoEnded();
        }
        //videoLoader
        if(encodedMsg.includes("startLoader")){            
            //document.getElementById("loader").classList.remove("hidden");
            WildLiveApp.addPopUpTemplate("#loaderContent"); // adding loader pop-up
        }

        // -----------------------------------------------------------------
        //playAdvertisement
        if(encodedMsg.includes("playAdvertisement")){
            youTubePlayer.playAd();
        }

        // -----------------------------------------------------------------
        //videohandling
        if(encodedMsg.includes("icon")){
            console.log("icon detected " + encodedMsg);
            if(encodedMsg.includes("play")){
                if(encodedMsg.includes("quiz")){
                    youTubePlayer.playAd();
                } else {
                    youTubePlayer.playVideo();
                }
            }

            if(encodedMsg.includes("pause")){
                if(encodedMsg.includes("quiz")){
                    youTubePlayer.pauseAd();
                } else {
                    youTubePlayer.pauseVideo();
                }
            }

            else if(encodedMsg.includes("forward")){
                youTubePlayer.fastForward();

                var templateString = document.querySelector("#moveForwardContent").innerHTML;
                var tmpElement = document.createElement("div");
                tmpElement.setAttribute("id", "iconPopUp");
                tmpElement.innerHTML = templateString;
                var templatePlaceholder = document.querySelector(".templateBinding");
                templatePlaceholder.appendChild(tmpElement);

                var removePopUp = setTimeout(function() {
                    var popUpTemplate = document.querySelector("#iconPopUp");
                    popUpTemplate.parentNode.removeChild(popUpTemplate);
                }, 1000);
            }

            else if(encodedMsg.includes("backward")){
                youTubePlayer.rewind();

                var templateString = document.querySelector("#moveBackwardContent").innerHTML;
                var tmpElement = document.createElement("div");
                tmpElement.setAttribute("id", "iconPopUp");
                tmpElement.innerHTML = templateString;
                var templatePlaceholder = document.querySelector(".templateBinding");
                templatePlaceholder.appendChild(tmpElement);

                var removePopUp = setTimeout(function() {
                    var popUpTemplate = document.querySelector("#iconPopUp");
                    popUpTemplate.parentNode.removeChild(popUpTemplate);
                }, 1000);
            }

            else if(encodedMsg.includes("volumeUp")){
                if(encodedMsg.includes("quiz")){
                    youTubePlayer.setAdVolumeUp();
                } else {
                    youTubePlayer.setVolumeUp();

                    var templateString = document.querySelector("#volumeUpContent").innerHTML;
                    var tmpElement = document.createElement("div");
                    tmpElement.setAttribute("id", "iconPopUp");
                    tmpElement.innerHTML = templateString;
                    var templatePlaceholder = document.querySelector(".templateBinding");
                    templatePlaceholder.appendChild(tmpElement);

                    var removePopUp = setTimeout(function() {
                        var popUpTemplate = document.querySelector("#iconPopUp");
                        popUpTemplate.parentNode.removeChild(popUpTemplate);
                    }, 1000);
                }
            }

            else if(encodedMsg.includes("volumeDown")){
                if(encodedMsg.includes("quiz")){
                    youTubePlayer.setAdVolumeDown();
                } else {
                    youTubePlayer.setVolumeDown();

                    var templateString = document.querySelector("#volumeDownContent").innerHTML;
                    var tmpElement = document.createElement("div");
                    tmpElement.setAttribute("id", "iconPopUp");
                    tmpElement.innerHTML = templateString;
                    var templatePlaceholder = document.querySelector(".templateBinding");
                    templatePlaceholder.appendChild(tmpElement);
                    
                    var removePopUp = setTimeout(function() {
                        var popUpTemplate = document.querySelector("#iconPopUp");
                        popUpTemplate.parentNode.removeChild(popUpTemplate);
                    }, 1000);
                }
            }
        }
        // -----------------------------------------------------------------
        //scorehandling
        if(encodedMsg.includes("score")){
            score = encodedMsg.slice(5);
            document.querySelector(".scoreSection").classList.remove("hidden");
            document.getElementById("quizScore").innerHTML = score;
        }

        if(encodedMsg.includes("closePopUp")){
            var popUpTemplate = document.querySelectorAll("#popUp");
            for(i=0; i<popUpTemplate.length; i++){
                console.log(popUpTemplate[i]);
                popUpTemplate[i].parentNode.removeChild(popUpTemplate[i]);
            }
            // setting colored map
            document.body.style.backgroundImage = "url('res/img/Coloured_map.jpg')";
        }

        // -----------------------------------------------------------------
        //handling trigger-points for timeline (getting index/position)
        if (encodedMsg.includes("activateItem")) {
            var triggerPointIdx = parseInt(encodedMsg.slice(12));
            databaseHandler.setActivatedItem(triggerPointIdx);
        }

    }

    that.init = init;
    that.handleMessage = handleMessage;
    return that;
};