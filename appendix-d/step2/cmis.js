
function doRepositoryInfo() {
	performJsonpRequest("http://localhost:8081/inmemory/browser", "processGetRepositories");
}

function processGetRepositories(json_object) {
	printRepositoryInfos(json_object);
} 

function performJsonpRequest(url, callback) {
	var callUrl = url;

	var paramChar = (url.indexOf('?') == -1) ? '?' : '&';
    callUrl = url + paramChar + 'callback=' + callback; 

	var script = document.createElement('script');
	script.setAttribute('src', callUrl);
	script.setAttribute('type', 'text/javascript');
	document.body.appendChild(script);
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