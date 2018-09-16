var WildLiveApp = WildLiveApp || {};
WildLiveApp.MessageHandler = function() {
    var that = {},
        youTubePlayer;

    function init() {
        youTubePlayer = new WildLiveApp.YouTubePlayer();
    }

    function handleMessage(encodedMsg){
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
            //document.getElementById("player").classList.remove("hidden");
            //document.getElementById("infoProgress").classList.remove("invisible");
            //document.getElementById("onBoardingGuide").classList.add("hidden");            
            youTubePlayer.loadPlayer(videoID);
            //window.player.loadVideoById(videoID, 0);
            //window.player.playVideo();
        }

    // -----------------------------------------------------------------
        //videohandling
        if(encodedMsg.includes("icon")){
            console.log("icon detected " + encodedMsg);
            if(encodedMsg.includes("play")){
                youTubePlayer.playVideo();
            }

            if(encodedMsg.includes("pause")){
                youTubePlayer.pauseVideo();
            }

            else if(encodedMsg.includes("forward")){
                youTubePlayer.fastForward();
            }

            else if(encodedMsg.includes("backward")){
                youTubePlayer.rewind();
            }

            else if(encodedMsg.includes("volumeUp")){
                youTubePlayer.setVolumeUp();
            }

            else if(encodedMsg.includes("volumeDown")){
                youTubePlayer.setVolumeDown();
            }
        }
         // -----------------------------------------------------------------
        //scorehandling
        if(encodedMsg.includes("score")){
            score = encodedMsg.slice(5);
            document.getElementById("quizScore").innerHTML = score;
        }
    }

    that.init = init;
    that.handleMessage = handleMessage;
    return that;
};