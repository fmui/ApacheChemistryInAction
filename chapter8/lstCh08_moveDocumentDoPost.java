//<start id="ne-setup"/>
protected void doPost(HttpServletRequest request,
   HttpServletResponse response, Session session)
   throws ServletException, IOException, TheBlendException {

   String path = getStringParameter(request, "path");
   String target = getStringParameter(request, "target");

   // --- fetch the object ---
   CmisObject object = null;
   try {
      object = session.getObjectByPath(path);  
   } catch (CmisBaseException cbe) {
      throw new TheBlendException(
         "Could not retrieve the object!", cbe);
   }

   if (!(object instanceof FileableCmisObject)) {
      throw new TheBlendException("Object is not fileable!"); //<co id="ch8_move_1" />
   }

   FileableCmisObject fileableCmisObject = (FileableCmisObject) object;

   // --- fetch the source folder ---
   CmisObject sourceObject = null;
   try {
      int lastSlash = path.lastIndexOf('/');
      String parentPath = path.substring(0, lastSlash); //<co id="ch8_move_2" />
      if (parentPath.length() == 0) {
         parentPath = "/";
      }
 
      sourceObject = session.getObjectByPath(parentPath);  
   } catch (CmisBaseException cbe) {
      throw new TheBlendException(
      "Could not retrieve target folder!", cbe);
   }

   // --- fetch the target folder ---
   CmisObject targetObject = null;
   try {
      targetObject = session.getObjectByPath(target);
   } catch (CmisBaseException cbe) {
      throw new TheBlendException(
         "Could not retrieve target folder!", cbe);
   }

   if (!(targetObject instanceof Folder)) {
      throw new TheBlendException("Target is not a folder!");
   }

   try {
     fileableCmisObject.move(sourceObject, targetObject);
   } catch (CmisBaseException cbe) {
     throw new TheBlendException("Could not move the object!", cbe);
   }

   // --- redirect to browse page ---
   try {
     String url = request.getRequestURL().toString();
     int lastSlash = url.lastIndexOf('/');
     
     url = url.substring(0, lastSlash) + "/browse?id=" +
         URLEncoder.encode(targetObject.getId(), "UTF-8");
     
     redirect(url, request, response);
   }
   catch(UnsupportedEncodingException e) {
     throw new ServletException(e);
   }
}
//<end id="ne-setup"/>

