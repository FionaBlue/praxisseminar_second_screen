var WildLiveApp = WildLiveApp || {};
WildLiveApp.DatabaseHandler = function() {
    var that = {}, itemsFromDatabase = [], database, signalRHandler, videoPlayer, prevPosition = 0, activatedPosition = 0;

    function init(videoId, player) {
        // referencing player element
        videoPlayer = player;

        // defining firebase database
        database = firebase.database();
        
        // getting local instances of necessary handlers
        signalRHandler = WildLiveApp.getSignalRClient();
        videoPlayer = WildLiveApp.YouTubePlayer();
        
        // getting content from database for current, given video-id
        getContentFromDatabase(videoId);
    }
    
    function getContentFromDatabase(videoId) {
        // setting back items-array
        itemsFromDatabase = [];
        activatedPosition = 0;
        prevPosition = 0;

        // defining reference as structure entry point for database retrieval
        var currentDatabaseVideoRef = database.ref('video/' + videoId + '/trigger_point');

        // getting database items once
        currentDatabaseVideoRef.once('value').then(function(snapshot) {
            // retrieving all stored firebase items at given database-reference
            var dataForVideoId = snapshot.val();
            for (var i = 0; i < dataForVideoId.length; i++) {
                var currentTriggerPoint = dataForVideoId[i];
                var currentId = i;
                var imageUrl = currentTriggerPoint.image;
                var imageFile = currentTriggerPoint.imageFile;
                var timestamp = currentTriggerPoint.timestamp;
                var title = currentTriggerPoint.title;

                // https://stackoverflow.com/questions/9640266/convert-hhmmss-string-to-seconds-only-in-javascript
                // calculating seconds for given trigger-point-timestamp
                var timestampTime = currentTriggerPoint.timestamp.split(':');                
                var timestampSeconds = parseInt(timestampTime[0])*60*60 + parseInt(timestampTime[1])*60 + parseInt(timestampTime[2]);

                // generating new trigger-point-object for each retrieval (for ui-timeline-binding)
                var triggerPoint = { currentId: currentId, imageUrl: imageUrl, imageFile: imageFile, timestamp: timestampSeconds, title: title, isActive: false, isPlaceholder: true };
                // adding trigger-point to list for later ui-binding
                itemsFromDatabase.push(triggerPoint);

                // if last database item: now trying to start timestamp timer and to fill timeline by receiving images from firebase storage (via url)
                if (currentId == dataForVideoId.length - 1) {
                    addContentToTimeline(itemsFromDatabase);
                    startTimestampTimer(itemsFromDatabase);
                }
            }
        }); 
    }
    
    function addContentToTimeline(itemsFromDatabase) {
        // getting timeline by id
        var timeline = document.getElementById('infoProgress');

        // getting each timeline item
        for (var i = 0; i < itemsFromDatabase.length; i++) {
            var databaseItem = itemsFromDatabase[i];

            // creating empty img elements
            var triggerPoint = document.createElement('img');
            var triggerPointDivider = document.createElement('img');

            // adding attributes to new img trigger-point-element
            triggerPoint.id = databaseItem.currentId;
            triggerPoint.classList.add('metaImg');
            triggerPoint.src = 'res/img/placeholder.png';

            // adding attributes to new divider-element
            triggerPointDivider.classList.add('metaDivider');
            triggerPointDivider.src = 'res/img/divider.png';

            // appending generated img elements to timeline
            timeline.appendChild(triggerPoint);
            if (triggerPoint.id != itemsFromDatabase.length-1) {
                timeline.appendChild(triggerPointDivider);
            }
        }
    }

    function startTimestampTimer(itemsFromDatabase) {
        var timerVar = setInterval(function() {
            // starting timer that scans video-timestamps for each second
            myTimer(itemsFromDatabase);
        }, 1000);
    }

    function myTimer(itemsFromDatabase) {
        // rounding current given time from player
        var currentTime = Math.floor(videoPlayer.getCurrentVideoTime());
        var triggerPoint = null;

        // checking if timestamp-seconds were reached
        for (var i = 0; i < itemsFromDatabase.length; i++) {
            
            // checking if timestamp was reached or skipped (move forward/backward)
            if ((itemsFromDatabase[i].timestamp-1 == currentTime || itemsFromDatabase[i].timestamp < currentTime) && itemsFromDatabase[i].isPlaceholder == true) {
                
                // revealing trigger-point (ui) by timestamp and id
                itemsFromDatabase[i].isPlaceholder = false;
                triggerPoint = document.getElementById(itemsFromDatabase[i].currentId);
                triggerPoint.src = itemsFromDatabase[i].imageUrl;
                setNavigationArrowVisibility(getActivationPosition(), itemsFromDatabase);

                // setting activation state if first item or greyscaling image if not
                if (i == 0) {
                    setActivatedItem(i);
                } else {
                    setTriggerPointSaturation(i, itemsFromDatabase, 0);
                }
                // telling android device to reveal trigger-point
                signalRHandler.sendMessageToAndroidDevice('index' + itemsFromDatabase[i].currentId);
            }
        }
    }
    
    function setActivationPosition(position) {
        activatedPosition = position;
    }
    function getActivationPosition() {
        return activatedPosition;
    }
    
    // handling data (activated item) sent by android device (click)
    function setActivatedItem(position) {
        // setting current activation
        setActivationPosition(position);
        
        // setting highlighting for current clicked trigger point
        setItemActivationState(position, itemsFromDatabase);

        // checking which direction to scroll to
        if (itemsFromDatabase[position].currentId < prevPosition) {
            // scrolling back, to the left
            scrollToPosition(prevPosition, itemsFromDatabase[position].currentId, -1);
        } else if (itemsFromDatabase[position].currentId > prevPosition) {
            // scrolling forward, to the right
            scrollToPosition(prevPosition, itemsFromDatabase[position].currentId, +1);
        }

        // setting new previous position
        prevPosition = itemsFromDatabase[position].currentId;
    }

    function setItemActivationState(position, itemsFromDatabase) {
        // changing activation state
        itemsFromDatabase[prevPosition].isActive = false;
        itemsFromDatabase[position].isActive = true;

        // setting highlighting
        setTriggerPointSaturation(position, itemsFromDatabase, 1);
        setNavigationArrowVisibility(position, itemsFromDatabase);
    }

    function setTriggerPointSaturation(triggerPointId, itemsFromDatabase, saturation) {
        if (saturation == 1) {
            document.getElementById(prevPosition).className = 'metaImg greyScaling';
            document.getElementById(triggerPointId).className = 'metaImg';
        } else if (saturation == 0) {
            document.getElementById(triggerPointId).className = 'metaImg greyScaling';
        }
    }

    function setNavigationArrowVisibility(position, itemsFromDatabase) {
        // activating/deactivating timeline arrows (on border reached) for emphasizing possible scroll-directions
        var placeholderCount = getItemPlaceholderCount(itemsFromDatabase);

        var arrowLeft = document.getElementById('chevronLeft');
        var arrowRight = document.getElementById('chevronRight');

        if (placeholderCount == itemsFromDatabase.length) {
            // checking if all placeholders are visible
            arrowLeft.classList.add("hiddenItem");
            arrowRight.classList.add("hiddenItem");
        } else if (placeholderCount < itemsFromDatabase.length) {
            if (placeholderCount == itemsFromDatabase.length-1) {
                // checking if single placeholder was revealed
                arrowLeft.classList.add("hiddenItem");
                arrowRight.classList.add("hiddenItem");
            } else if (placeholderCount < itemsFromDatabase.length-1) {
                if (position == (itemsFromDatabase.length-placeholderCount)-1) {
                    // checking if more than 1 placeholder was revealed and last available position was clicked
                    arrowLeft.classList.remove("hiddenItem");
                    arrowRight.classList.add("hiddenItem");
                } else if (position == 0) {
                    // checking if more than 1 placeholder was revealed and first available position was clicked
                    arrowLeft.classList.add("hiddenItem");
                    arrowRight.classList.remove("hiddenItem");
                } else {
                    // checking if more than 1 placeholder was revealed and any other available position was clicked
                    arrowLeft.classList.remove("hiddenItem");
                    arrowRight.classList.remove("hiddenItem");
                }
            }
        }
    }
    
    function getItemPlaceholderCount(itemsFromDatabase) {
        var placeholderCounter = 0;
        for (var i = 0; i < itemsFromDatabase.length; i++) {
            if (itemsFromDatabase[i].isPlaceholder == true) {
                placeholderCounter++;
            }
        }
        return placeholderCounter;
    }
    
    function scrollToPosition(fromPosition, toPosition, direction) {
        // defining fix scroll unit
        var scrollUnit = 40;

        // calculating absolute value of distance
        var scrollDistance = Math.abs(toPosition - fromPosition);
        
        // scrolling timeline (horizontally) to appropriate position
        document.getElementById('infoProgress').scrollBy(direction * scrollUnit * scrollDistance, 0);
    }

    that.setActivatedItem = setActivatedItem;
    that.init = init;
    return that;
};