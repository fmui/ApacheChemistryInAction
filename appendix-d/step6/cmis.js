var baseUrl;
var rootUrl;
var rootFolderId;
var init;

$(document).ready(function() {
        rootFolderId = "100";
        baseUrl = "http://localhost:8081/inmemory/browser/A1";
        rootUrl = baseUrl + "/root";
});

function prepareCreate() {

    // dynamically create an invisible iframe:
	// $('#invisibleareaid').html($('<iframe>').attr('id', 'createresultframe').attr('style', "width:0px;height:0px;visibility:hidden").
	// attr('onload', "createDocumentDone()").attr('name', 'createresultframe')); 		
    init = true;
	$("#transactionId").val(createRandomString());
    $("#createdochtmlid").attr("action", rootUrl);
    trace("create doc from html form with action: " + $("#createdochtmlid").attr("action"));
    return true;
}

function createDocumentDone() {

	// Note that this function is called if we have a static iframe on initial page load.
	// This statement can be removed if we have a dynamically created iframe on submit
	if (init == null || !init)
	    return;

	var transId = $("#transactionId").val();
	// if the server is on the same domain (same origin policy) this will work and save you another request...
/*     	try {
        cont = $('#createresultframe').contents().text();
        if (cont) {
        	var json = jQuery.parseJSON(cont);
        	if (!checkError(json, "#responsesection")) {              	
              	$("#responsesection").html("Document successfully created with id: " + json.properties["cmis:objectId"].value + " and transaction id: " + transId);
        	}
        }
    } catch (ex) { 
        // gives a permission denied exception if on another server
        trace("Same origin policy for transaction: " + transId + ", exception: " + ex);
*/
		// if the server is NOT on the same domain we will have to do another request using the transaction id
		trace("Creating document in transaction: " + transId);
        getObjectFromTransaction(transId, function(data) {
   			var text = "Document successfully created with id: " + data.objectId + " and transaction id: " + transId;
   			$("#createdocsection").html(text);
        });
//    }
}

function createRandomString() {
    return Math.round(Math.random()*1000).toString();
}

function trace(text) {
    if (window.console && window.console.log) {  
      window.console.log(text);  
    }  
}

function getObjectFromTransaction(transId, cbFct) {
    var params = {
        cmisselector: "lastResult",
        token: transId,
        suppressResponseCodes: true
    };

    trace("getObjectFromTransaction(): " + baseUrl  + ", transaction-id: " + transId);
    $.ajax( { 
        url: baseUrl,
        data: params,
        dataType: "jsonp",
        type:  "GET",
        success: cbFct
    });
}
