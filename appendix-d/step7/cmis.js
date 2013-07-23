
$(document).ready(function() {
	baseUrl = "http://localhost:8081/inmemory/browser/A1";
    rootUrl = baseUrl + "/root";
    $('#doquery').click(function() {
        doQuery($('#queryfield').val());
	});
});

function doQuery(queryString) {
        $("#queryresponsesection").html(null);
        trace("doing query: " + queryString);
        var params = {
            cmisaction: "query",
            q: queryString,
            searchAllVersions: "false",
            includeAllowableAction: "false",
            includeRelationships: "none",
            suppressResponseCodes: "false"
        };

	    performRequest(baseUrl,  params, "POST", createQueryTable, false);
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

// utility function to display most important cmis properties plus all custom properties
// from a query result list, sequence in array determines display order 
function getPropIdsToDisplayForQuery(queryResp) {
	var propsToDisplay = ["cmis:name", "cmis:objectId", "cmis:objectTypeId","cmis:baseTypeId","cmis:createdBy",
			"cmis:creationDate", "cmis:lastModifiedBy", "cmis:lastModificationDate","cmis:contentStreamMimeType"];
	var propsToDisplayLabel = ["Name", "Object-Id", "Type-Id", "Base-Type", "Created By", "Created At",
							   "Modified By", "Modified At", "Content Type"];
	for (var child in queryResp.results) {
		for (var prop in queryResp.results[child].properties) {
			var propQueryName = queryResp.results[child].properties[prop].queryName;
			if (propQueryName.indexOf("cmis:") != 0 && $.inArray(propQueryName, propsToDisplay) < 0) {
				propsToDisplay.push(propQueryName);
				propsToDisplayLabel.push(queryResp.results[child].properties[prop].queryName);
			}
		}
	}
	return { ids: propsToDisplay, labels: propsToDisplayLabel};
}

function createQueryTable(queryResp) {
	var row;
	var tbody;
	var tbl = $('<table>').attr('id', 'queryRespTable').attr('border', 1);
	
	var propsDisp = getPropIdsToDisplayForQuery(queryResp);
	var propsToDisplay = propsDisp.ids;
	var propsToDisplayLabel = propsDisp.labels;
	
	trace("create result table from query");
	tbl.append($('<thead>').append(row = $('<tr>')));
	for (var propKey in propsToDisplay) {
		row.append($('<td>').text(propsToDisplayLabel[propKey]));
	}
	
	tbl.append(tbody = $('<tbody>'));
	for (var child in queryResp.results) {
	    trace("add row to table");
		row = null;
		tbody.append(row = $('<tr>'));
		for (var propKey in propsToDisplay) {
			var props = queryResp.results[child].properties;
			var prop = props[propsToDisplay[propKey]];
			if (null != prop && null != prop.value) {
				var text = convertValue(prop.value, prop.type);
				var cell = $('<td>').html(text.toString());
				row.append(cell);
			} else
				row.append($('<td>'));
		}
	}
	
	$("#queryresponsesection").append($('<h4>').text("Result")).append(tbl);
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

