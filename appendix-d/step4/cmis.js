var baseUrl;
var rootFolderId;

$(document).ready(function() {
        $('#getchildren').click(function() {
            getChildren($('#folderidfield').val());
 		});
        rootFolderId = "100";
        baseUrl = "http://localhost:8081/inmemory/browser/A1";
        $('#folderidfield').val(rootFolderId);
});

function getChildren(folderId) {
        $("#foldersection").html(null);
        trace("get children of folder: " + folderId);
        var params = {
           filter: "*",
           includePolicyIds: "false",
           includeACL: "false",
           includePathSegment: "true",
           objectId: folderId,
           searchAllVersions: "false",
           includeAllowableAction: "false",
           includeRelationships: "none",
           suppressResponseCodes: "false"
        };

	    performRequest(baseUrl + "/root",  params, "GET", createChildrenTable, true);
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
// from an object list, sequence in array determines display order 
function getPropIdsToDisplayForObjectList(objectList, baseType) {
    var propsToDisplay = ["cmis:name", "cmis:objectId", "cmis:objectTypeId","cmis:baseTypeId","cmis:createdBy",
    		"cmis:creationDate", "cmis:lastModifiedBy", "cmis:lastModificationDate"];
    var propsToDisplayLabel = ["Name", "Object-Id", "Type-Id", "Base-Type", "Created By", "Created At",
                               "Modified By", "Modified At"];
    if ("cmis:folder" === baseType) {
        propsToDisplay.splice(3, 0, "cmis:parentId");
        propsToDisplayLabel.splice(3, 0, "Parent Folder Id");

    } else if ("cmis:document" === baseType) {
        propsToDisplay.push("cmis:contentStreamMimeType");
        propsToDisplayLabel.push("Content Type");
    }

    for (var child in objectList.objects) {
        if (baseType === objectList.objects[child].object.properties["cmis:baseTypeId"].value) {
	        for (var prop in objectList.objects[child].object.properties) {
	            var propId = objectList.objects[child].object.properties[prop].id;
	            if (propId.indexOf("cmis:") != 0 && $.inArray(propId, propsToDisplay) < 0) {
	                propsToDisplay.push(propId);
	                propsToDisplayLabel.push(objectList.objects[child].object.properties[prop].displayName);
	        	}
	        }
        }
    }

    return { ids: propsToDisplay, labels: propsToDisplayLabel};
}

function drillDownChildren(folderId) {
	trace("drill down to " + folderId);

    $('#folderidfield').val(folderId);
    getChildren(folderId);
}

function createChildrenTable(response) {
	createTable(response, "cmis:folder", "#foldersection");
	createTable(response, "cmis:document", "#docsection");
}

function createTable(children, baseType, responseSection) {
	var row;
	var tbody;
	$(responseSection).html(null);
	var tbl = $('<table>').attr('id', 'childrenTable').attr('border', 1);

	var propsDisp = getPropIdsToDisplayForObjectList(children, baseType);
	var propsToDisplay = propsDisp.ids;
	var propsToDisplayLabel = propsDisp.labels;

	tbl.append($('<thead>').append(row = $('<tr>')));
	row.append($('<td>')); // document or folder icon
	for (var propKey in propsToDisplay) {
		row.append($('<td>').text(propsToDisplayLabel[propKey]));
	}

	tbl.append(tbody = $('<tbody>'));
	for (var child in children.objects) {
		row = null;
		if (baseType === children.objects[child].object.properties["cmis:baseTypeId"].value) {
			row = null;
			tbody.append(row = $('<tr>'));
			// add icons for document, folder in first column:
			if (baseType == "cmis:document") {
				var src = $("<img>").attr("src", "images/document.png");
				row.append($('<td>').html(src));
			} else if (baseType == "cmis:folder") {
				var src = $("<img>").attr("src", "images/folder.png");
				row.append($('<td>').html(src));
			}
			for (var propKey in propsToDisplay) {
				var props = children.objects[child].object.properties;
				var prop = props[propsToDisplay[propKey]];
				if (null != prop && null != prop.value) {
					var text = convertValue(prop.value, prop.type);
					if (baseType == "cmis:document" && propsToDisplay[propKey] == "cmis:name" && props["cmis:contentStreamFileName"] != null &&
							props["cmis:contentStreamFileName"].value != null) {
						text = "<a href='" + baseUrl + "/root?cmisselector=content&objectId=" + props["cmis:objectId"].value + "' target='_new' >" + text + "</a>";
					} else if (baseType == "cmis:folder" && propsToDisplay[propKey] == "cmis:name") {
						text = $('<a>').attr("href", "javascript:drillDownChildren(\"" + props["cmis:objectId"].value + "\")").html(text);
					}
					row.append($('<td>').html(text));
				} else
					row.append($('<td>'));
			}
		}
	}

	if (baseType == "cmis:document")
		$(responseSection).append($('<h4>').text("Documents")).append(tbl);
	else
		$(responseSection).append($('<h4>').text("Folders")).append(tbl);
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

