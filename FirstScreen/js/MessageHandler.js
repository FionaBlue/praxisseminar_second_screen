var WildLiveApp = WildLiveApp || {};
WildLiveApp.MessageHandler = function() {
    var that = {}, youTubePlayer, databaseHandler, androidConnection = false, androidBuffer = 0;

    function init() {
        youTubePlayer = new WildLiveApp.YouTubePlayer();
        
        // initializing database (firebase) component
        databaseHandler = new WildLiveApp.DatabaseHandler();
    }

    function checkConnectionToAndroidDevice(){
        if(androidConnection == true){
            document.getElementById("castConnectedButton").classList.remove("hidden");
            document.getElementById("castNotConnectedButton").classList.add("hidden");
        } else {
            document.getElementById("castConnectedButton").classList.add("hidden");
            document.getElementById("castNotConnectedButton").classList.remove("hidden");
        }
        androidConnection = false;
    }

    function handleMessage(encodedMsg){
        // -----------------------------------------------------------------
        //CastButton-Handling
        if(encodedMsg.includes("castConnected")){
            if(androidBuffer == 10){
                androidConnection = true;
                checkConnectionToAndroidDevice();
                androidBuffer = 0;
            }
            androidBuffer = androidBuffer + 1;            
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
            WildLiveApp.onVideoStarted();
            console.log("playVideo " + encodedMsg);
            videoID = encodedMsg.slice(9);         
            youTubePlayer.loadPlayer(videoID);            
            
            // binding database functionality and data from firebase by accessing id and player
            databaseHandler.init(videoID, youTubePlayer);
        }
        //stopVideo
        if(encodedMsg.includes("stopVideo")){
            youTubePlayer.stopVideo();
            WildLiveApp.onVideoEnded();
        }
        //videoLoader
        if(encodedMsg.includes("startLoader")){            
            document.getElementById("loader").classList.remove("hidden");
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
            }

            else if(encodedMsg.includes("backward")){
                youTubePlayer.rewind();
            }

            else if(encodedMsg.includes("volumeUp")){
                if(encodedMsg.includes("quiz")){
                    youTubePlayer.setAdVolumeUp();
                } else {
                    youTubePlayer.setVolumeUp();
                }
            }

            else if(encodedMsg.includes("volumeDown")){
                if(encodedMsg.includes("quiz")){
                    youTubePlayer.setAdVolumeDown();
                } else {
                    youTubePlayer.setVolumeDown();
                }
            }
        }
        // -----------------------------------------------------------------
        //scorehandling
        if(encodedMsg.includes("score")){
            score = encodedMsg.slice(5);
            document.getElementById("quizScore").innerHTML = score;
        }

        if(encodedMsg.includes("endguide")){
            document.getElementById("onBoardingGuide").hidden = true;
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