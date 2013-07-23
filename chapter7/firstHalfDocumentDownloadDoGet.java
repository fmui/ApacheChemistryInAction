//<start id="ne-setup"/>
String id = getStringParameter(request, "id");
String streamId = getStringParameter(request, "stream");

OperationContext docOpCtx = session.createOperationContext();
docOpCtx.setFilterString("cmis:contentStreamFileName");
docOpCtx.setIncludeAcls(false);
docOpCtx.setIncludeAllowableActions(false);
docOpCtx.setIncludePolicies(false);
docOpCtx.setIncludeRelationships(IncludeRelationships.NONE);
docOpCtx.setRenditionFilterString("cmis:none"); 
docOpCtx.setIncludePathSegments(false);
docOpCtx.setOrderBy(null);
docOpCtx.setCacheEnabled(true);


CmisObject cmisObject = null;
try {
  cmisObject = session.getObject(id, docOpCtx);
} catch (CmisObjectNotFoundException onfe) {
  response.sendError(HttpServletResponse.SC_NOT_FOUND, 
    "Document not found!");
  return;
} catch (CmisBaseException cbe) {
  response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
    "Error: " + cbe.getMessage());
  return;
}

if (!(cmisObject instanceof Document)) {
  response.sendError(HttpServletResponse.SC_BAD_REQUEST, 
    "Object is not a document!");
 return;
}

Document document = (Document) cmisObject;
//<end id="ne-setup"/>

