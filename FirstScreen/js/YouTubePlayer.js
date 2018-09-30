// src:
// https://developers.google.com/youtube/iframe_api_reference?hl=de
// https://support.google.com/youtube/answer/2467968?hl=de
// https://stackoverflow.com/questions/23443476/some-options-to-youtube-api-embedded-player-not-being-respected
// https://stackoverflow.com/questions/12256382/youtube-iframe-api-not-triggering-onyoutubeiframeapiready
// https://stackoverflow.com/questions/27573017/failed-to-execute-postmessage-on-domwindow-https-www-youtube-com-http
// https://stackoverflow.com/questions/10652459/get-rid-of-an-embedded-youtube-player-after-video-has-finished
// https://stackoverflow.com/questions/14834520/html5-audio-stop-function
// https://stackoverflow.com/questions/3733227/javascript-seconds-to-minutes-and-seconds

var player;
var WildLiveApp = WildLiveApp || {};
WildLiveApp.YouTubePlayer = function() {
  var that = {},
      signalRClient,
      testAd = [["1:00", "0:20"],["2:00", "0:20"],["4:00", "0:20"], ["5:00", "0:20"]],
      adState = false,
      adPaused = false,
      startAd,
      adDurationInSeconds,
      currentTimerTime,
      pausedTimerTime,
      adJingle,
      videoProgress;

  // load player with specific parameters
  function loadPlayer(videoID) {
    // get SignalR Client instance from WildLiveApp for sending messages to second screen
    signalRClient = WildLiveApp.getSignalRClient();
    
      player = new YT.Player('player', {
        enablejsapi: 1,
        height: '460',
        width: '840',
        videoId: videoID,
        host: 'https://www.youtube.com',
        startSeconds: 0,
        playerVars: {
          showinfo: 0,
          controls: 0,
          rel: 0,
          disablekb: 0,
          fs: 0
        },
        events: {
          'onReady': onPlayerReady,
          'onStateChange': onPlayerStateChanged,
        }
      });
    }
  
  function getCurrentVideoTime() {
    return player.getCurrentTime();
  }

  // calculates and updates video-progressbar
  function updateProgressBar(currVideoTime, videoDuration) {
    var elem = document.getElementById("videoCurrentProgress");   
    var width = 0;
    var stride = 100/(videoDuration/currVideoTime);
    width = Math.round(stride);
    if (width < 100) {
      elem.style.width = width + '%'; 
    }
  }
  
  function onPlayerStateChanged(event) {
    // clearing youtube-player iframe when quitting video
    if (event.data == 5) {
      event.target.destroy();
    }
  }

  // The API will call this function when the video player is ready.
  function onPlayerReady(event) {
    event.target.playVideo();
    sendVideoProgressToAndroidDevice();    
    startVideoProgress();
    startAdTimer(testAd);
  }

  // steadily checks and updates video progress
  function startVideoProgress(){
    var progressTime = document.getElementById('videoProgressTime');
    var currentVideoTime = player.getCurrentTime();
    var videoDuration = player.getDuration();
    progressTime.innerHTML = formatTime(currentVideoTime) + " / " + formatTime(videoDuration);
    updateProgressBar(currentVideoTime, videoDuration);
    videoProgress = setTimeout(function() {
      startVideoProgress();
    }, 1000);
  }

  // starts timer to check if advertisement should be started
  function startAdTimer(adTimes) {
    var timerVar = setInterval(function() {
        adTimer(adTimes);
    }, 1000);
  }

  // checks if advertisement should be started
  function adTimer(adTimes) {

    var currentTime = Math.round(player.getCurrentTime());

    for(var i=0; i<adTimes.length; i++){
      // calculates passed advertisement time in seconds
      var adTimeInSeconds = timeInSeconds(adTimes[i][0]);

      // checks if current given advertisement time is bigger as current video time and no advertisement is already playing
      if(adTimeInSeconds <= currentTime && player.getPlayerState() == 1 && adState == false){
        // start quiz on second screen
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

        // start to play jingle-music during advertisement
        adJingle = document.createElement('audio');
        adJingle.src = "res/audio/Scott_Holmes_-_02_-_Hopeful_Journey.ogg";
        adJingle.play();
        
        // calculates advertisement duration in seconds and start advertisement
        adDurationInSeconds = timeInSeconds(adTimes[i][1]);
        startAdvertisement(adDurationInSeconds);
        
        // remove advertisement from array
        adTimes.shift();
        testAd = adTimes;
      }
    }
  }

  // start advertisement
  function startAdvertisement(duration) {
    adState = true;                  // set advertisement-state true
    adPaused = false;                // set advertisement-paused false
    currentTimerTime = duration;

    // start advertisement interval
    startAd = setInterval(function() {

      // calculate and display remaining advertisement time
      var min = Math.floor(currentTimerTime/60);
      var sec = Math.floor(currentTimerTime - min * 60);
      if(sec.toString().length == 1){
        document.getElementById('popUpTimer').innerHTML = min + ":0" + sec;
      } else {
        document.getElementById('popUpTimer').innerHTML = min + ":" + sec;
      }

      // count remaining time down
      currentTimerTime = currentTimerTime - 1;

      // if remaining time is less than zero (see count down above) end advertisement interval
      if(currentTimerTime < 0){

        clearInterval(startAd);

        // finish quiz on second screen
        signalRClient.sendMessageToAndroidDevice("finish Quiz");

        // remove advertisement PopUp
        var adTemplate = document.querySelector("#popUp");
        adTemplate.parentNode.removeChild(adTemplate);

        adState = false;            // set advertisement-state false
        adJingle.pause();           // pause jingle 
        adJingle.currentTime = 0;   // set jingle to beginning
        player.playVideo();         // play video
      }
    }, 1000);
    startAdTimer(testAd);           // begin to check for advertisement again
  }

  // calculate given time in seconds
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

  // steadily sending current video progress time to second screen for the visualization in seekbar there
  async function sendVideoProgressToAndroidDevice() {
    if(player.getPlayerState() == 1){
      signalRClient.sendMessageToAndroidDevice("time " + player.getCurrentTime());
    }    
    setTimeout(sendVideoProgressToAndroidDevice, 3000);
  }

  // pauses advertisement
  function pauseAd() {
    // checks if advertisement is really playing
    if(adState == true) {
      // disable quizanswers for non-cheating (answering questions while advertisement is paused)
      signalRClient.sendMessageToAndroidDevice("disable Quiz-answers");

      // save remaining advertisement time for advertisement restart
      pausedTimerTime = currentTimerTime;
      
      clearInterval(startAd);   // stop advertisement
      adJingle.pause();         // pause jingle music
      adState = false;          // set advertisement state to false (paused)
      adPaused = true;          // set advertisement paused to true
    }
  }

  // restarts advertisement after pause
  function playAd() {
    // checks if advertisement is really paused
    if(adState == false) {
      // enable quizanswers
      signalRClient.sendMessageToAndroidDevice("enable Quiz-answers");

      // start advertisement again with saved remaining time from pause
      startAdvertisement(pausedTimerTime);

      adJingle.play();        // start to play jingle music again (paused from pause)
      adPaused = false;       // set advertisement paused to false
    }
  }

  // pause video
  function pauseVideo() {
    // only pause when video is actually playing
    if(player.getPlayerState() == 1){
      player.pauseVideo();
    }
  }

  // play video
  function playVideo() {
    // only play video when video is actually paused
    // and no advertisement is playing
    // and no advertisement is paused
    if(player.getPlayerState() == 2 && adState == false && adPaused == false){
      player.playVideo();
    }    
  }

  // sets volume of player up
  function setVolumeUp() {
    var currentVolume = player.getVolume();
    // volume steps: 10%
    if(currentVolume <= 90){
      player.setVolume(currentVolume + 10);
    }
    popUpVolumeUp();
    // show new/current volume in popUp
    document.getElementById('iconPopUpTimer').innerHTML = player.getVolume() + "%";
    removeCurrentIconPopUp();
  }

  // add volumeUp popUp
  function popUpVolumeUp() {
    var templateString = document.querySelector("#volumeUpContent").innerHTML;
    var tmpElement = document.createElement("div");
    tmpElement.setAttribute("id", "iconPopUp");
    tmpElement.innerHTML = templateString;
    var templatePlaceholder = document.querySelector(".templateBinding");
    templatePlaceholder.appendChild(tmpElement);
  }
  
  // sets volume of advertisement jingle up
  function setAdVolumeUp() {
    var currentAdVolume = adJingle.volume;
    // volume steps: 20%
    if(currentAdVolume <= 0.8){
      adJingle.volume = currentAdVolume + 0.2;
    }    
    popUpVolumeUp();
    // show new/current volume in popUp
    if(adJingle.volume.toString().charAt(0) == "1"){
      document.getElementById('iconPopUpTimer').innerHTML = "100%";
    } else if(adJingle.volume.toString().charAt(2) == "0"){
      document.getElementById('iconPopUpTimer').innerHTML = "0%";
    } else {
      document.getElementById('iconPopUpTimer').innerHTML = adJingle.volume.toString().charAt(2) + "0%";
    }
    removeCurrentIconPopUp();
  }

  // sets volume of advertisement jingle down
  function setVolumeDown() {
    var currentVolume = player.getVolume();
    // volume steps: 10%
    if(currentVolume >= 10){
      player.setVolume(currentVolume - 10);
    }
    popUpVolumeDown();
    // show new/current volume in popUp
    document.getElementById('iconPopUpTimer').innerHTML = player.getVolume() + "%";
    removeCurrentIconPopUp();
  }

  // add volumeDown popUp
  function popUpVolumeDown() {
    var templateString = document.querySelector("#volumeDownContent").innerHTML;
    var tmpElement = document.createElement("div");
    tmpElement.setAttribute("id", "iconPopUp");
    tmpElement.innerHTML = templateString;
    var templatePlaceholder = document.querySelector(".templateBinding");
    templatePlaceholder.appendChild(tmpElement);
  }

  // sets volume of advertisement jingle down
  function setAdVolumeDown() {
    var currentAdVolume = adJingle.volume;
    // volume steps: 20%
    if(currentAdVolume >= 0.2){
      adJingle.volume = currentAdVolume - 0.2;
    }
    popUpVolumeDown();
    // show new/current volume in popUp
    if(adJingle.volume.toString().charAt(0) == "1"){
      document.getElementById('iconPopUpTimer').innerHTML = "100%";
    } else if(adJingle.volume.toString().charAt(2) == "0"){
      document.getElementById('iconPopUpTimer').innerHTML = "0%";
    } else {
      document.getElementById('iconPopUpTimer').innerHTML = adJingle.volume.toString().charAt(2) + "0%";
    }
    removeCurrentIconPopUp();
  }

  // seekTo forward
  function fastForward() {
    var currentTime = player.getCurrentTime();
    // seekTo step: 10 seconds
    var newTime = Math.ceil(currentTime) + 10;
    player.seekTo(newTime, true);

    // add forward popUp
    var templateString = document.querySelector("#moveForwardContent").innerHTML;
    var tmpElement = document.createElement("div");
    tmpElement.setAttribute("id", "iconPopUp");
    tmpElement.innerHTML = templateString;
    var templatePlaceholder = document.querySelector(".templateBinding");
    templatePlaceholder.appendChild(tmpElement);

    // show new time (seeked to)
    document.getElementById('iconPopUpTimer').innerHTML = formatTime(newTime);

    removeCurrentIconPopUp();
  }

  // seekTo backward
  function rewind() {
    var currentTime = player.getCurrentTime();
    // seekTo step: 10 seconds
    var newTime = Math.ceil(currentTime) - 10;
    player.seekTo(newTime, true);

    // add backward popUp
    var templateString = document.querySelector("#moveBackwardContent").innerHTML;
    var tmpElement = document.createElement("div");
    tmpElement.setAttribute("id", "iconPopUp");
    tmpElement.innerHTML = templateString;
    var templatePlaceholder = document.querySelector(".templateBinding");
    templatePlaceholder.appendChild(tmpElement);

    // show new time (seeked to)
    document.getElementById('iconPopUpTimer').innerHTML = formatTime(newTime);

    removeCurrentIconPopUp();
  }

  // removes current icon popUp
  function removeCurrentIconPopUp() {
    var removePopUp = setTimeout(function() {
      var popUpTemplate = document.querySelector("#iconPopUp");
      popUpTemplate.parentNode.removeChild(popUpTemplate);
    }, 1000);
  }

  // format time in seconds to hrs:mins:secs
  function formatTime(currTime){
    var hrs = currTime/3600;
    hrs = hrs.toString().split(".", 1);
    var mins = (currTime%3600)/60;
    mins = mins.toString().split(".", 1);
    var secs = currTime%60;
    secs = secs.toString().split(".", 1);
    var formattedTime = "";
    if(hrs.toString().length < 2) {
      formattedTime += "0" + hrs + ":";
    } else {
      formattedTime+= hrs + ":";
    }
    if(mins.toString().length < 2) {
      formattedTime += "0" + mins + ":";
    } else {
      formattedTime += mins + ":";
    }
    if(secs.toString().length < 2){
      formattedTime += "0" + secs;;
    } else {
      formattedTime += secs;
    }
    return formattedTime;
  }

  // stop video and stop updating videoprogress
  function stopVideo() {
    player.stopVideo();
    clearInterval(videoProgress);
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