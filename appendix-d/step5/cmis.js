var baseUrl;
var rootFolderId;
var myPlaylist =null;

$(document).ready(function() {
        $('#getchildren').click(function() {
            getChildren($('#folderpathfield').val());
 		});
        rootFolderId = "241";
        baseUrl = "http://localhost:8081/inmemory/browser/A1";
        $('#folderpathfield').val("/blend/Songs");
});

function getChildren(folderPath) {
        $("#foldersection").html(null);
        trace("get children of folder: " + folderPath);
        var params = {
           filter: "*",
           includePolicyIds: "false",
           includeACL: "false",
           includePathSegment: "true",
           searchAllVersions: "false",
           includeAllowableAction: "false",
           includeRelationships: "none",
           suppressResponseCodes: "false"
        };

	    performRequest(baseUrl + "/root" + folderPath,  params, "GET", createPlayerFromFolder, true);
}

function convertValue(value, type) {
    if (type == 'string') {
        if (value == '*')
            return "'*'"; // confuses text() method	
        else 
            return value;
    } else if (type == 'datetime') {
        return new Date(value).toLocaleString();
    } else
    	return value.toString();
}

function getContentUrl(id) {
    return baseUrl + "/root?cmisselector=content&objectId=" + id;
}

function getPropertyValue(props, name) {
    var prop = props[name];
    if (null != prop && null != prop.value) {
        return convertValue(prop.value, prop.type);
    } else
        return "";
}

function createPlayerFromFolder(response) {
        loadPlayer($('#player'), function() {
            $("#playerheader").html($("<h3 class=\"ui-widget ui-widget-header\">").text("The Blend: Songs"));
            createPlaylist(response);
        });
}

function createChildrenTable(response) {
	createTable(response, "cmis:folder", "#foldersection");
	createTable(response, "cmis:document", "#docsection");
}

function createPlaylist(response) {
	var playList = [];
	
	for (var child in response.objects) {
		var props = response.objects[child].object.properties;
		var typeId = getPropertyValue(props, "cmis:objectTypeId");
		if (typeId != null) {
			var playItem = {};
			playItem.artist = getPropertyValue(props, "cmisbook:artist");
			playItem.title = getPropertyValue(props, "cmisbook:title");
			var mimeType = getPropertyValue(props, "cmis:contentStreamMimeType");
			if (mimeType.indexOf("audio/") == 0) {
				playItem.mp3 = getContentUrl(getPropertyValue(props, "cmis:objectId"));
				// playItem.poster = "images/musical-notes.jpg";
			trace("add track: " + playItem.mp3);
			playList.push(playItem);
		   }
	    }
	}
	myPlaylist.setPlaylist(playList);
}


function loadPlayer(domObj, cbFct) {
	domObj.load('jplayer.html', function() {

		myPlaylist = new jPlayerPlaylist({
			jPlayer: "#jquery_jplayer_N",
			cssSelectorAncestor: "#jplayer"
		}, [
			{
				title:"<Empty>",
				artist:"",
				mp3:"",
				oga:""
			}
		], {
			playlistOptions: {
				enableRemoveControls: false
			},
			swfPath: "../js/jplayer",
			supplied: "mp3, ogg"
		});

		return cbFct.call(this);
	});        
}

function errorHandler(event, jqXHR, settings, excep) {
	alert("Call was aborted:" + jqXHR);
}

function performRequest(url, params, method, cbFct, jsonp, username, password) {
    $.ajax( { 
        url: url,
        data: params,
        dataType: (jsonp ? "jsonp" : "json"),
        type:  method,
        success: cbFct
    });
}

function trace(text) {
    if (window.console && window.console.log) {  
      window.console.log(text);  
    }  
}
