//<start id="ne-setup"/>
protected void doPost(HttpServletRequest request,
   HttpServletResponse response, Session session)
   throws ServletException, IOException, TheBlendException {

   String path = getStringParameter(request, "path");
   String target = getStringParameter(request, "target");

   // --- fetch the document ---
   CmisObject object = null;
   try {
     object = session.getObjectByPath(path); 
   } catch (CmisBaseException cbe) {
      throw new TheBlendException(
         "Could not retrieve the document!", cbe);
   }

   Document doc = null;
   if (object instanceof Document) {
     doc = (Document) object;
   }
   else {
     throw new TheBlendException("Object is not a document!");
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

   Document newDoc = null;
   try {
     newDoc = doc.copy(targetObject);
   } catch (CmisBaseException cbe) {
     throw new TheBlendException("Could not copy the document!", cbe);
   }

   // --- redirect to show page ---
   try {
     String url = request.getRequestURL().toString();
     int lastSlash = url.lastIndexOf('/');
     
     url = url.substring(0, lastSlash) + "/show?id=" +
         URLEncoder.encode(newDoc.getId(), "UTF-8");
     
     redirect(url, request, response);
   }
   catch(UnsupportedEncodingException e) {
     throw new ServletException(e);
   }
}
//<end id="ne-setup"/>

