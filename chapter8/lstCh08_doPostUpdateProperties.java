//<start id="ne-setup"/>
protected void doPost(HttpServletRequest request,
    HttpServletResponse response, Session session)
    throws ServletException, IOException, TheBlendException {

    String path = getStringParameter(request, "path");
    String name = getStringParameter(request, "name");

    OperationContext opCtx = session.createOperationContext();
    opCtx.setFilterString("cmis:changeToken"); //<co id="ch8_dopost_uprop4" />
    opCtx.setIncludeAcls(false);
    opCtx.setIncludeAllowableActions(false);
    opCtx.setIncludePolicies(false);
    opCtx.setIncludeRelationships(IncludeRelationships.NONE);
    opCtx.setRenditionFilterString("cmis:none");
    opCtx.setIncludePathSegments(false);
    opCtx.setOrderBy(null);
    opCtx.setCacheEnabled(false);

    // --- get the object and update its name ---
    CmisObject object = null;

    try {
        object = session.getObjectByPath(path, opCtx);

        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put(PropertyIds.NAME, name);

        object.updateProperties(properties, false); 
    }
    catch (CmisObjectNotFoundException nfe) {
        throw new TheBlendException("The object doesn't exist.", nfe);
    }
    catch (CmisNameConstraintViolationException ncve) {
        throw new TheBlendException("The name is invalid. " +
            "Please try a different name.", ncve);
    }
    catch (CmisUpdateConflictException uce) {
        throw new 
          TheBlendException("Somebody else updated the object. " +
          "Please try again.", uce);
    }
    catch (CmisBaseException cbe) {
        throw new 
          TheBlendException("Could not update the object!", cbe);
    }

    // --- find the parent folder to redirect to the browse page ---
    String parentId = null;

    if (object instanceof FileableCmisObject) { //<co id="ch8_dopost_uprop1" />
       List<Folder> parents = 
         ((FileableCmisObject) object).getParents(); //<co id="ch8_dopost_uprop2" />

	    if (parents.size() > 0) {
		    parentId = parents.get(0).getId(); //<co id="ch8_dopost_uprop3" />
	    }
   }

    if (parentId == null) {
        throw new TheBlendException("Object is unfiled. " +
            "Don't know where to go.");
    }

    // --- redirect to the parents browse page ---
    try {
        String url = request.getRequestURL().toString();
        int lastSlash = url.lastIndexOf('/');

        url = url.substring(0, lastSlash) + "/browse?id=" +
            URLEncoder.encode(parentId, "UTF-8");
        
        redirect(url, request, response);  //<co id="ch8_dopost_uprop5" />
    }
    catch(UnsupportedEncodingException e) {
    	  throw new ServletException(e);   //<co id="ch8_dopost_uprop6" />
    }
}
//<end id="ne-setup"/>

