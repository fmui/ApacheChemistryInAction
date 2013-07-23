//<start id="ne-setup"/>
protected void doGet(HttpServletRequest request,
  HttpServletResponse response, Session session)
    throws ServletException, IOException, TheBlendException {

  // --- get parameters ---
  String id = getStringParameter(request, "id");
  if (id == null) {
    id = session.getRepositoryInfo().getRootFolderId();
  }

  int skip = getIntParameter(request, "skip", 0);
  if (skip < 0) {
    skip = 0;
  }

  request.setAttribute("skip", skip);

  // --- fetch folder object ---
  OperationContext folderOpCtx 
           = session.createOperationContext();
  folderOpCtx.setFilterString("cmis:name,cmis:path");
  folderOpCtx.setIncludeAcls(false);
  folderOpCtx.setIncludeAllowableActions(false);
  folderOpCtx.setIncludePolicies(false);
  folderOpCtx.setIncludeRelationships(IncludeRelationships.NONE);
  folderOpCtx.setRenditionFilterString("cmis:none");
  folderOpCtx.setIncludePathSegments(false);
  folderOpCtx.setOrderBy(null);
  folderOpCtx.setCacheEnabled(true);

  CmisObject object = null;
  try {
    object = session.getObject(id, folderOpCtx);
  } catch (CmisBaseException cbe) {
    throw new TheBlendException("Could not retrieve folder!", cbe);
  }

  Folder folder = null;
  if (object instanceof Folder) {
    folder = (Folder) object;
  } else {
    throw new TheBlendException("Object is not a folder!");
  }

  request.setAttribute("folder", folder);

  // --- fetch children ---
  OperationContext childrenOpCtx = session.createOperationContext();
  childrenOpCtx.setFilterString("cmis:objectId,cmis:baseTypeId,"
      + "cmis:name,cmis:contentStreamLength,"
      + "cmis:contentStreamMimeType");
  childrenOpCtx.setIncludeAcls(false);
  childrenOpCtx.setIncludeAllowableActions(true);
  childrenOpCtx.setIncludePolicies(false);
  childrenOpCtx.setIncludeRelationships(IncludeRelationships.NONE);
  childrenOpCtx.setRenditionFilterString("cmis:none");
  childrenOpCtx.setIncludePathSegments(true);
  childrenOpCtx.setOrderBy("cmis:name");
  childrenOpCtx.setCacheEnabled(false);
  childrenOpCtx.setMaxItemsPerPage(10);

  ItemIterable<CmisObject> children = 
      folder.getChildren(childrenOpCtx);
  ItemIterable<CmisObject> page = children.skipTo(skip).getPage(10);

  List<CmisObject> childrenPage = new ArrayList<CmisObject>();

  try {
    for (CmisObject child : page) {
      childrenPage.add(child);
    }
  } catch (CmisBaseException cbe) {
    throw new TheBlendException("Could not fetch children!");
  }

  request.setAttribute("page", childrenPage);
  request.setAttribute("total", page.getTotalNumItems());

  // --- determine paging links ---
  request.setAttribute("isFirstPage", skip == 0);
  request.setAttribute("isLastPage", !page.getHasMoreItems());

  // --- fetch parent ---
  Folder parent = null;
  if (!folder.isRootFolder()) {
     try { 
         parent = folder.getParents(childrenOpCtx).get(0);
      } catch (CmisBaseException cbe) {
         throw new TheBlendException("Could not fetch parent folder!");
      }
  }

  request.setAttribute("parent", parent);

  // --- show browse page ---
  dispatch("browse.jsp", folder.getName() + ". The Blend.",
      request, response);
}

//<end id="ne-setup"/>

