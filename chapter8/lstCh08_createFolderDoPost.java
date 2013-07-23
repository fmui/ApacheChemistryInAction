//<start id="ne-setup"/>
protected void doPost(HttpServletRequest request,
   HttpServletResponse response, Session session)
   throws ServletException, IOException, TheBlendException {

   // --- gather input ---
   String parentId = getStringParameter(request, "parent");
   String name = getStringParameter(request, "name");
   String typeId = getStringParameter(request, "type");

   // --- fetch the parent folder ---
   CmisObject parentObject = null;
   try {
      parentObject = session.getObject(parentId);  //<co id="use-op-context" />
   } catch (CmisBaseException cbe) {
      throw new TheBlendException(
        "Could not retrieve parent folder!", cbe);
   }

   // --- safety check for parent object ---
   Folder parent = null;
   if (parentObject instanceof Folder) {
      parent = (Folder) parentObject;
   } else {
      throw new TheBlendException("Parent is not a folder!");
   }

   // --- create new folder ---
   try {
      Map<String, Object> properties = new HashMap<String, Object>();
      properties.put(PropertyIds.NAME, name);
      properties.put(PropertyIds.OBJECT_TYPE_ID, typeId);

      parent.createFolder(properties); //<co id="returned-folder-notused" />
   } catch (CmisNameConstraintViolationException cncve) {
      throw new TheBlendException(
        "Please choose a different name and try again!", cncve);
   } catch (CmisBaseException cbe) {
      throw new TheBlendException(
        "Could not create the folder!", cbe);
   }

   // --- redirect to browse page ---
   try {
      StringBuffer url = request.getRequestURL();
      url.append("?id=");
      url.append(URLEncoder.encode(parent.getId(), "UTF-8"));

      redirect(url.toString(), request, response);
   }
   catch(UnsupportedEncodingException e) {
      throw new ServletException(e);
   }
}
//<end id="ne-setup"/>

