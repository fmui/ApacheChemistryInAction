
$(document).ready(function() {

});

function doRepositoryInfo() {
	performRequest("http://localhost:8081/inmemory/browser",  null, "GET", printRepositoryInfos, true);
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
        username: username,
        password: password,
        success: cbFct,
        error: errorHandler,
        timeout: 5000
    });
}

function printRepositoryInfos(infos) {
	for(repId in infos) {
		var ri = infos[repId];		
		$('#repositoryInfo').html(
			'<h4>Repository "' + ri.repositoryName + '" (' + ri.repositoryId + ')</h4>' +
			'<table>' +
			'<tr><td>Id:</td><td>' + ri.repositoryId + '</td></tr>' +
			'<tr><td>Name:</td><td>' + ri.repositoryName + '</td></tr>' +
			'<tr><td>Description:</td><td>' + ri.repositoryDescription + '</td></tr>' +
			'<tr><td>Product:</td><td>' + ri.vendorName + ' ' + ri.productName + ' ' + ri.productVersion + '</td></tr>' +
			'<tr><td>Root folder id:</td><td>' + ri.rootFolderId + '</td></tr>' +
			'<tr><td>Repository URL:</td><td>' + ri.repositoryUrl + '</td></tr>' +
			'<tr><td>Root folder URL:</td><td>' + ri.rootFolderUrl + '</td></tr>' +
			'</table>');
	}
}