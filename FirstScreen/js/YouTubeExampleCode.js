//https://developers.google.com/youtube/iframe_api_reference?hl=de
var player;
var WildLiveApp = WildLiveApp || {};
WildLiveApp.YouTubePlayer = function() {
  var that = {};//,
      //player;

  function loadPlayer(videoID) {
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
        playerVars: {
          showinfo: 0,
          controls: 0,
          rel: 0,
          disablekb: 1,
          fs: 0
        },
        events: {
          'onReady': onPlayerReady
        }
      });
    }
  //}

  // 4. The API will call this function when the video player is ready.
  function onPlayerReady(event) {
    //console.log("onPlayerReady");
    event.target.playVideo();
  }

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