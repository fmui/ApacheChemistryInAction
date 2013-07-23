//<start id="ne-setup"/>
protected void doGet(HttpServletRequest request,
  HttpServletResponse response, Session session)
    throws ServletException, IOException, TheBlendException {

  // --- get parameters ---
  String id = getStringParameter(request, "id");
  if (id == null) {
    throw new TheBlendException("No document id provided!");
  }

  // --- fetch document object ---
  OperationContext docOpCtx = session.createOperationContext();
  docOpCtx.setFilterString("*"); 
  docOpCtx.setIncludeAcls(false);
  docOpCtx.setIncludeAllowableActions(true);
  docOpCtx.setIncludePolicies(false);
  docOpCtx.setIncludeRelationships(IncludeRelationships.NONE);
  docOpCtx.setRenditionFilterString("cmis:thumbnail");
  docOpCtx.setIncludePathSegments(false);
  docOpCtx.setOrderBy(null);
  docOpCtx.setCacheEnabled(true);

  CmisObject object = null;
  try {
    object = session.getObject(id, docOpCtx);
  }
  catch (CmisObjectNotFoundException onfe) {
    throw new TheBlendException("The document does not exist!", onfe);
  }
  catch (CmisBaseException cbe) {
    throw new TheBlendException("Error getting document!", cbe);
  }

  Document doc = null;
  if (object instanceof Document) {
    doc = (Document) object;
  }
  else {
    throw new TheBlendException("Object is not a document!");
  }

  request.setAttribute("document", doc);

  // --- get thumbnail stream id ---
  String thumbnailStreamId = null;

  List<Rendition> renditions = doc.getRenditions();
  if (renditions != null && !renditions.isEmpty()) {
    thumbnailStreamId = renditions.get(0).getStreamId(); //<co id="taking-first-rendition" />
  }

  request.setAttribute("thumbnail", thumbnailStreamId);

  // --- show the page ---
  dispatch("show.jsp", doc.getName() + ". The Blend.",
    request, response);
}
//<end id="ne-setup"/>

