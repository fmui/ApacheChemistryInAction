//<start id="ne-setup"/>
protected void doPost(HttpServletRequest request,
   HttpServletResponse response, Session session)
   throws ServletException, IOException, TheBlendException {

   String path = getStringParameter(request, "path");

   // --- fetch the object ---
   CmisObject object = null;
   try {
     object = session.getObjectByPath(path); 

   } catch (CmisBaseException cbe) {
      throw new TheBlendException(
        "Could not retrieve the object!", cbe);
   }

   // --- delete the object ---
   try {
     if (object instanceof Folder) {
         Folder folder = (Folder) object;
         List<String> failedToDelete = 
           folder.deleteTree(true, UnfileObject.DELETE, true);

         if (failedToDelete != null && !failedToDelete.isEmpty()) {
             throw new TheBlendException("Deletion failed!");
         }
     }
     else {
         object.delete(true);
     }
   }
   catch (CmisBaseException cbe) {
     throw new TheBlendException("Could not delete the object!", cbe);
   }

   // --- redirect to browse page of the root folder ---
   String url = request.getRequestURL().toString();
   int lastSlash = url.lastIndexOf('/');
   url = url.substring(0, lastSlash) + "/browse";
     
   redirect(url, request, response);
}
//<end id="ne-setup"/>

