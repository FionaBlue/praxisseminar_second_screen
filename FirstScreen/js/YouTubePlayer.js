//https://developers.google.com/youtube/iframe_api_reference?hl=de
//https://support.google.com/youtube/answer/2467968?hl=de
//https://stackoverflow.com/questions/23443476/some-options-to-youtube-api-embedded-player-not-being-respected
//https://stackoverflow.com/questions/12256382/youtube-iframe-api-not-triggering-onyoutubeiframeapiready
//https://stackoverflow.com/questions/27573017/failed-to-execute-postmessage-on-domwindow-https-www-youtube-com-http
var player;
var WildLiveApp = WildLiveApp || {};
WildLiveApp.YouTubePlayer = function() {
  var that = {},
      signalRClient,
      testAd = [["1:00", "0:20"],["2:00", "0:20"],["4:00", "0:20"], ["5:00", "0:20"]],
      timerVar,
      adState = false,
      adPaused = false,
      startAd,
      adDurationInSeconds,
      currentTimerTime,
      pausedTimerTime,
      adJingle;

  function loadPlayer(videoID) {
    signalRClient = WildLiveApp.getSignalRClient();
    
      player = new YT.Player('player', {
        enablejsapi: 1,
        height: '460',
        width: '840',
        videoId: videoID,
        host: 'https://www.youtube.com',
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
        }
      });
    }
    
  function getCurrentVideoTime() {
    return player.getCurrentTime();
  }

  // The API will call this function when the video player is ready.
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
      if(adTimeInSeconds <= currentTime && player.getPlayerState() == 1 && adState == false){
        signalRClient.sendMessageToAndroidDevice("start Quiz");
        player.pauseVideo();

        // creating new node (div-element) to be filled with pop-up-id
        var tmpElement = document.createElement("div");
        tmpElement.setAttribute("id", "popUp");
        // filling div with current content (specific popup content)
        var templateString = document.querySelector("#advertisementContent").innerHTML;
        tmpElement.innerHTML = templateString;
        // adding new pop-up-div to template placeholder
        var templatePlaceholder = document.querySelector(".templateBinding");
        templatePlaceholder.appendChild(tmpElement);


        adJingle = document.createElement('audio');
        adJingle.src = "res/audio/Scott_Holmes_-_02_-_Hopeful_Journey.ogg";
        adJingle.play();

        adDurationInSeconds = timeInSeconds(adTimes[i][1]);
        startAdvertisement(adDurationInSeconds);
        
        adTimes.shift();
        testAd = adTimes;
      }
    }
  }

  function startAdvertisement(duration) {
    adState = true;                  //set advertisement-state true
    adPaused = false;
    currentTimerTime = duration;
    startAd = setInterval(function() {
      var min = Math.floor(currentTimerTime/60);
      var sec = Math.floor(currentTimerTime - min * 60);
      if(sec.toString().length == 1){
        document.getElementById('popUpTimer').innerHTML = min + ":0" + sec;
      } else {
        document.getElementById('popUpTimer').innerHTML = min + ":" + sec;
      }            
      currentTimerTime = currentTimerTime - 1;
      if(currentTimerTime < 0){
        clearInterval(startAd);
        signalRClient.sendMessageToAndroidDevice("finish Quiz");
        var adTemplate = document.querySelector("#popUp");
        adTemplate.parentNode.removeChild(adTemplate);

        adState = false;    // set advertisement-state false
        //https://stackoverflow.com/questions/14834520/html5-audio-stop-function
        adJingle.pause();
        adJingle.currentTime = 0;
        player.playVideo();
      }
    }, 1000);
    startAdTimer(testAd);
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

  function pauseAd() {
    if(adState == true) {
      signalRClient.sendMessageToAndroidDevice("disable Quiz-answers");
      pausedTimerTime = currentTimerTime;
      clearInterval(startAd);
      adJingle.pause();
      adState = false;
      adPaused = true;
    }
  }

  function playAd() {
    if(adState == false) {
      signalRClient.sendMessageToAndroidDevice("enable Quiz-answers");
      startAdvertisement(pausedTimerTime);
      adJingle.play();
      adPaused = false;
    }
  }

  function pauseVideo() {
    if(player.getPlayerState() == 1){
      player.pauseVideo();
    }
  }

  function playVideo() {
    if(player.getPlayerState() == 2 && adState == false && adPaused == false){
      player.playVideo();
    }    
  }

  function setVolumeUp() {
    var currentVolume = player.getVolume();
    if(currentVolume <= 90){
      player.setVolume(currentVolume + 10);
    }
    var templateString = document.querySelector("#volumeUpContent").innerHTML;
    var tmpElement = document.createElement("div");
    tmpElement.setAttribute("id", "iconPopUp");
    tmpElement.innerHTML = templateString;
    var templatePlaceholder = document.querySelector(".templateBinding");
    templatePlaceholder.appendChild(tmpElement);
    document.getElementById('iconPopUpTimer').innerHTML = player.getVolume() + "%";

    var removePopUp = setTimeout(function() {
      var popUpTemplate = document.querySelector("#iconPopUp");
      popUpTemplate.parentNode.removeChild(popUpTemplate);
    }, 1000);
  }
  
  function setAdVolumeUp() {
    var currentAdVolume = adJingle.volume;
    if(currentAdVolume <= 0.8){
      adJingle.volume = currentAdVolume + 0.2;
    }
    var templateString = document.querySelector("#volumeUpContent").innerHTML;
    var tmpElement = document.createElement("div");
    tmpElement.setAttribute("id", "iconPopUp");
    tmpElement.innerHTML = templateString;
    var templatePlaceholder = document.querySelector(".templateBinding");
    templatePlaceholder.appendChild(tmpElement);

    if(adJingle.volume.toString().charAt(0) == "1"){
      document.getElementById('iconPopUpTimer').innerHTML = "100%";
    } else if(adJingle.volume.toString().charAt(2) == "0"){
      document.getElementById('iconPopUpTimer').innerHTML = "0%";
    } else {
      document.getElementById('iconPopUpTimer').innerHTML = adJingle.volume.toString().charAt(2) + "0%";
    }

    var removePopUp = setTimeout(function() {
      var popUpTemplate = document.querySelector("#iconPopUp");
      popUpTemplate.parentNode.removeChild(popUpTemplate);
    }, 1000);
  }

  function setVolumeDown() {
    var currentVolume = player.getVolume();
    if(currentVolume >= 10){
      player.setVolume(currentVolume - 10);
    }
    var templateString = document.querySelector("#volumeDownContent").innerHTML;
    var tmpElement = document.createElement("div");
    tmpElement.setAttribute("id", "iconPopUp");
    tmpElement.innerHTML = templateString;
    var templatePlaceholder = document.querySelector(".templateBinding");
    templatePlaceholder.appendChild(tmpElement);
    document.getElementById('iconPopUpTimer').innerHTML = player.getVolume() + "%";

    var removePopUp = setTimeout(function() {
      var popUpTemplate = document.querySelector("#iconPopUp");
      popUpTemplate.parentNode.removeChild(popUpTemplate);
    }, 1000);
  }

  function setAdVolumeDown() {
    var currentAdVolume = adJingle.volume;
    if(currentAdVolume >= 0.2){
      adJingle.volume = currentAdVolume - 0.2;
    }
    var templateString = document.querySelector("#volumeDownContent").innerHTML;
    var tmpElement = document.createElement("div");
    tmpElement.setAttribute("id", "iconPopUp");
    tmpElement.innerHTML = templateString;
    var templatePlaceholder = document.querySelector(".templateBinding");
    templatePlaceholder.appendChild(tmpElement);

    if(adJingle.volume.toString().charAt(0) == "1"){
      document.getElementById('iconPopUpTimer').innerHTML = "100%";
    } else if(adJingle.volume.toString().charAt(2) == "0"){
      document.getElementById('iconPopUpTimer').innerHTML = "0%";
    } else {
      document.getElementById('iconPopUpTimer').innerHTML = adJingle.volume.toString().charAt(2) + "0%";
    }    

    var removePopUp = setTimeout(function() {
      var popUpTemplate = document.querySelector("#iconPopUp");
      popUpTemplate.parentNode.removeChild(popUpTemplate);
    }, 1000);
  }

  function fastForward() {
    var currentTime = player.getCurrentTime();
    var newTime = Math.ceil(currentTime) + 10;
    player.seekTo(newTime, true);
    var templateString = document.querySelector("#moveForwardContent").innerHTML;
    var tmpElement = document.createElement("div");
    tmpElement.setAttribute("id", "iconPopUp");
    tmpElement.innerHTML = templateString;
    var templatePlaceholder = document.querySelector(".templateBinding");
    templatePlaceholder.appendChild(tmpElement);
    document.getElementById('iconPopUpTimer').innerHTML = formatTime(newTime);

    var removePopUp = setTimeout(function() {
      var popUpTemplate = document.querySelector("#iconPopUp");
      popUpTemplate.parentNode.removeChild(popUpTemplate);
    }, 1000);
  }

  function rewind() {
    var currentTime = player.getCurrentTime();
    var newTime = Math.ceil(currentTime) - 10;
    player.seekTo(newTime, true);

    var templateString = document.querySelector("#moveBackwardContent").innerHTML;
    var tmpElement = document.createElement("div");
    tmpElement.setAttribute("id", "iconPopUp");
    tmpElement.innerHTML = templateString;
    var templatePlaceholder = document.querySelector(".templateBinding");
    templatePlaceholder.appendChild(tmpElement);
    document.getElementById('iconPopUpTimer').innerHTML = formatTime(newTime);

    var removePopUp = setTimeout(function() {
      var popUpTemplate = document.querySelector("#iconPopUp");
      popUpTemplate.parentNode.removeChild(popUpTemplate);
    }, 1000);
  }

  // https://stackoverflow.com/questions/3733227/javascript-seconds-to-minutes-and-seconds
  function formatTime(currTime){
    var hrs = currTime/3600;
    hrs = hrs.toString().split(".", 1);
    var mins = (currTime%3600)/60;
    mins = mins.toString().split(".", 1);
    var secs = currTime%60;
    var formattedTime = "";
    if(hrs != "0") {
      formattedTime += hrs + ":";
      if(mins.length < 2) {
        formattedTime += "0";
      }
    }
    formattedTime += mins + ":";
    if(secs.toString().length < 2){
      formattedTime += "0";
    }
    formattedTime += secs;
    return formattedTime;
  }

  function stopVideo() {
    player.stopVideo();
  }

  that.setAdVolumeUp = setAdVolumeUp;
  that.setAdVolumeDown = setAdVolumeDown;
  that.stopVideo = stopVideo;
  that.pauseAd = pauseAd;
  that.playAd = playAd;
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