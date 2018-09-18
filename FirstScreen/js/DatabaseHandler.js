var WildLiveApp = WildLiveApp || {};
WildLiveApp.DatabaseHandler = function() {
    var that = {}, itemsFromDatabase = [], database, storage, signalRHandler;

    function init() {
        console.log("DatabaseHandler init");

        database = firebase.database();
        storage = firebase.storage();
        
        signalRHandler = WildLiveApp.getSignalRClient();

        // start progress loader ? 

        getContentFromDatabase('Nbrx5tFJzyQ');
    }

    function getContentFromDatabase(videoId) {
        // defining reference as structure entry point for database retrieval
        var currentDatabaseVideoRef = database.ref('video/' + videoId + '/trigger_point');

        // getting database items once
        currentDatabaseVideoRef.once('value').then(function(snapshot) {
            // retrieving all stored firebase items at given database-reference
            var dataForVideoId = snapshot.val();

            // 
            for (var i = 0; i < dataForVideoId.length; i++) {
                var currentTriggerPoint = dataForVideoId[i];

                var currentId = i;
                var imageUrl = currentTriggerPoint.image;
                var imageFile = currentTriggerPoint.imageFile;
                var timestamp = currentTriggerPoint.timestamp;
                var title = currentTriggerPoint.title;

                // generating new trigger-point-object for each retrieval (for ui-timeline-binding)
                var triggerPoint = { currentId: currentId, imageUrl: imageUrl, imageFile: imageFile, timestamp: timestamp, title: title }

                // adding trigger-point to list for later ui-binding
                itemsFromDatabase.push(triggerPoint);

                // if last database item: now trying to receive images from firebase storage (via url)
                if (currentId == dataForVideoId.length - 1) {

                    // opening up video-template
                    //WildLiveApp.onVideoStarted();

                    addContentToTimeline(itemsFromDatabase);
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
            triggerPoint.className = 'metaImg';
            //triggerPoint.src = 'res/img/placeholder.png';
            triggerPoint.src = databaseItem.imageUrl;

            // adding attributes to new divider-element
            triggerPointDivider.className = 'metaDivider';
            triggerPointDivider.src = 'res/img/divider.png';

            // adding event-listener for on-clicking trigger-point
            triggerPoint.addEventListener('click', function(triggerPoint) {
                console.log(triggerPoint.srcElement.id);

                // send message to android device
                signalRHandler.sendMessageToAndroidDevice(triggerPoint.srcElement.id);
            });

            // appending generated img elements to timeline
            timeline.appendChild(triggerPoint);
            if (triggerPoint.id != itemsFromDatabase.length-1) {
                timeline.appendChild(triggerPointDivider);
            }
        }
    }

    that.init = init;
    return that;
};