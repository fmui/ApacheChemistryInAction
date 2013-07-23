
function doRepositoryInfo() {
   callCmisRepository("http://localhost:8081/inmemory/browser", function (json_object) {		   
	   printRepositoryInfos(json_object);
   });
}

function callCmisRepository(url, callback) {
	var json_object = {};
	var http_request = new XMLHttpRequest();
	http_request.open("GET", url, true);
	http_request.onreadystatechange = function() {
		   if (http_request.readyState == 4 ) {
			   json_object = JSON.parse(http_request.responseText);
			   callback(json_object);
		   }
	};

	http_request.send(null);
}
	
function trace(text) {
    if (window.console && window.console.log) {  
      window.console.log(text);  
    }  
}

function doRepositoryInfo2() {
	var url = "http://localhost:8081/inmemory/browser";
	window.open(url, '_blank');
}


function printRepositoryInfos(infos) {
	for(repId in infos) {
		var ri = infos[repId];		
		document.getElementById('repositoryInfo').innerHTML =
			'<h4>Repository "' + ri.repositoryName + '" (' + ri.repositoryId + ')</h4>' +
			'<table>' +
			'<tr><td>Id:</td><td>' + ri.repositoryId + '</td></tr>' +
			'<tr><td>Name:</td><td>' + ri.repositoryName + '</td></tr>' +
			'<tr><td>Description:</td><td>' + ri.repositoryDescription + '</td></tr>' +
			'<tr><td>Product:</td><td>' + ri.vendorName + ' ' + ri.productName + ' ' + ri.productVersion + '</td></tr>' +
			'<tr><td>Root folder id:</td><td>' + ri.rootFolderId + '</td></tr>' +
			'<tr><td>Repository URL:</td><td>' + ri.repositoryUrl + '</td></tr>' +
			'<tr><td>Root folder URL:</td><td>' + ri.rootFolderUrl + '</td></tr>' +
			'</table>';
	}
}