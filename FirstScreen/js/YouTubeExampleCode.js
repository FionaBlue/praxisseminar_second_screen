//https://developers.google.com/youtube/iframe_api_reference?hl=de
var player;
var WildLiveApp = WildLiveApp || {};
WildLiveApp.YouTubePlayer = function() {
  var that = {},
      signalRClient,
      testAd = [["1:00", "1:00"],["2:00", "0:30"]],
      timerVar;

  function loadPlayer(videoID) {
    signalRClient = WildLiveApp.getSignalRClient();
    // 3. This function creates an <iframe> (and YouTube player)
    //    after the API code downloads.
    //https://stackoverflow.com/questions/23443476/some-options-to-youtube-api-embedded-player-not-being-respected
    //https://stackoverflow.com/questions/12256382/youtube-iframe-api-not-triggering-onyoutubeiframeapiready
    //window.onYouTubeIframeAPIReady = function() {
      //console.log("inner apiready");
      player = new YT.Player('player', {
        enablejsapi: 1,
        height: '560',
        width: '940',
        videoId: videoID,
        startSeconds: 0,
        playerVars: {
          showinfo: 1,
          controls: 1,
          rel: 1,
          disablekb: 1,
          fs: 1
        },
        events: {
          'onReady': onPlayerReady,
          //'onStateChange': onPlayerStateChanged
        }
      });
    }
  //}
    
  function getCurrentVideoTime() {
    return player.getCurrentTime();
  }

  // 4. The API will call this function when the video player is ready.
  function onPlayerReady(event) {
    event.target.playVideo();
    sendVideoProgressToAndroidDevice();
    startAdTimer(testAd);
  }

  function startAdTimer(adTimes) {
    timerVar = setInterval(function() {
        adTimer(adTimes);
    }, 1000);
  }

  function adTimer(adTimes) {
    var currentTime = Math.round(player.getCurrentTime());
    for(var i=0; i<adTimes.length; i++){
      var adTimeInSeconds = timeInSeconds(adTimes[i][0]);
      if(adTimeInSeconds == currentTime && player.getPlayerState() == 1){
        signalRClient.sendMessageToAndroidDevice("start Quiz");
        player.pauseVideo();

        templateString = document.querySelector('#Advertisement').innerHTML;       // reading template in index.html
        tmpElement = document.createElement("div");                 // creating new div for loading template content
        tmpElement.setAttribute("id", "AD-PopUp");
        tmpElement.innerHTML = templateString;
        start = document.querySelector(".templateBinding");
        start.appendChild(tmpElement);        

        var adDurationInSeconds = timeInSeconds(adTimes[i][1]);
        var startAd = setInterval(function() {
            var min = Math.floor(adDurationInSeconds/60);
            var sec = Math.floor(adDurationInSeconds - min * 60);
            if(sec.toString().length == 1){
              document.getElementById('AD-Timer').innerHTML = min + ":0" + sec;
            } else {
              document.getElementById('AD-Timer').innerHTML = min + ":" + sec;
            }            
            adDurationInSeconds = adDurationInSeconds - 1;
          if(adDurationInSeconds < 0){
            clearInterval(startAd);
            signalRClient.sendMessageToAndroidDevice("finish Quiz");
            var adTemplate = document.querySelector("#AD-PopUp");
            adTemplate.parentNode.removeChild(adTemplate);

            testAd.splice(i); //remove Ad from testAdArray
            player.playVideo();
          }
        }, 1000);
        startAdTimer(testAd);
      }
    }
  }

  function timeInSeconds(time) {
    var inSeconds;
    var timeUnits = time.split(':');
    if (timeUnits.length == 3) {
      inSeconds = parseInt(timeUnits[0])*60*60 + parseInt(timeUnits[1])*60 + parseInt(timeUnits[2]);
    } else if (timeUnits.length == 2) {
      inSeconds = parseInt(timeUnits[0])*60 + parseInt(timeUnits[1]);
    } else if (timeUnits.length == 1) {
      inSeconds = parseInt(timeUnits[0]);
    }
    return inSeconds;
  }

  async function sendVideoProgressToAndroidDevice() {
    if(player.getPlayerState() == 1){
      signalRClient.sendMessageToAndroidDevice("time " + player.getCurrentTime());
    }    
    setTimeout(sendVideoProgressToAndroidDevice, 3000);
  }

  /*function onPlayerStateChanged(event) {
    
    if(event.data == 2) {
      signalRClient.sendMessageToAndroidDevice("enable Quiz");
    }
    if(event.data == 1) {
      signalRClient.sendMessageToAndroidDevice("disable Quiz");
    }
  }*/

  function pauseVideo() {
    if(player.getPlayerState() == 1){
      player.pauseVideo();
    }
  }

  function playVideo() {
    if(player.getPlayerState() == 2){
      player.playVideo();
    }    
  }

  function setVolumeUp() {
    currentVolume = player.getVolume();
    if(currentVolume <= 95){
      player.setVolume(currentVolume + 5);
    }
    console.log("volumeUp " + currentVolume);
  }

  function setVolumeDown() {
    currentVolume = player.getVolume();
    if(currentVolume >= 5){
      player.setVolume(currentVolume - 5);
    }
    console.log("volumeDown " + currentVolume);
  }

  function fastForward() {
    currentTime = player.getCurrentTime();
    console.log("current time " + currentTime);
    newTime = Math.ceil(currentTime) + 10;
    player.seekTo(newTime, true);
    console.log("forward, seekTo " + newTime);
  }

  function rewind() {
    currentTime = player.getCurrentTime();
    newTime = Math.ceil(currentTime) - 10;
    player.seekTo(newTime, true);
    console.log("backward, seekTo " + newTime);
  }

  that.rewind = rewind;
  that.fastForward = fastForward;
  that.setVolumeDown = setVolumeDown;
  that.setVolumeUp = setVolumeUp;
  that.playVideo = playVideo;
  that.pauseVideo = pauseVideo;
  that.loadPlayer = loadPlayer;
  that.getCurrentVideoTime = getCurrentVideoTime;
  return that;
};

// 4. The API will call this function when the video player is ready.
/*function onPlayerReady(event) {
  event.target.playVideo();
}

// 5. The API calls this function when the player's state changes.
//    The function indicates that when playing a video (state=1),
//    the player should play for six seconds and then stop.
var done = false;
function onPlayerStateChange(event) {
  if (event.data == YT.PlayerState.PLAYING && !done) {
    setTimeout(stopVideo, 6000);
    done = true;
  }
}
function stopVideo() {
  player.stopVideo();
}*/