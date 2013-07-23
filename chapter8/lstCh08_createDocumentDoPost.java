//<start id="ne-setup"/>
protected void doPost(HttpServletRequest request,
  HttpServletResponse response, Session session)
  throws ServletException, IOException, TheBlendException {

  // --- get parent folder, properties, and content ---
  boolean isMultipart = ServletFileUpload.isMultipartContent(request);
  if (!isMultipart) {
    throw new TheBlendException("Invalid request!");
  }

  Map<String, Object> properties = new HashMap<String, Object>();
  File uploadedFile = null;
  String mimeType = null;
  String parentPath = null;
  ObjectId newDocId = null;

  try {
    DiskFileItemFactory factory = new DiskFileItemFactory();
    ServletFileUpload upload = new ServletFileUpload(factory);
    upload.setSizeMax(50 * 1024 * 1024);

    @SuppressWarnings("unchecked")
    List<FileItem> items = upload.parseRequest(request);

    Iterator<FileItem> iter = items.iterator();
    while (iter.hasNext()) {
      FileItem item = iter.next();

      if (item.isFormField()) {
        String name = item.getFieldName();

        if ("path".equalsIgnoreCase(name)) {  
          parentPath = item.getString();
        }
        else if ("name".equalsIgnoreCase(name)) {
          properties.put(PropertyIds.NAME, item.getString());
        }
        else if ("type".equalsIgnoreCase(name)) {
          properties.put(PropertyIds.OBJECT_TYPE_ID, 
            item.getString());
        }
      } 
      else {
        uploadedFile = File.createTempFile("blend", "tmp");
        item.write(uploadedFile);
         
        mimeType = item.getContentType();
        if (mimeType == null) {
          mimeType = "application/octet-stream";
        }
      }
    }
  } catch (Exception e) {
    throw new TheBlendException("Upload failed: " + e, e);
  }

  if (uploadedFile == null) { 
    throw new TheBlendException("No content!");
  }

  FileInputStream stream = null;

  try {

    // --- fetch the parent folder ---
    CmisObject parentObject = null;
    try {
      parentObject = session.getObjectByPath(parentPath);  
    } catch (CmisBaseException cbe) {
      throw new 
        TheBlendException("Could not retrieve parent folder!", cbe);
    }

    Folder parent = null;
    if (parentObject instanceof Folder) {
      parent = (Folder) parentObject;
    }
    else {
      throw new TheBlendException("Parent is not a folder!");
    }

    // --- determine the VersioningState ---
    VersioningState versioningState = VersioningState.NONE;

    String typeId = (String)properties.get(PropertyIds.OBJECT_TYPE_ID);
    DocumentType docType = 
      (DocumentType) session.getTypeDefinition(typeId);

    if (Boolean.TRUE.equals(docType.isVersionable())) {
      versioningState = VersioningState.MAJOR;
    }

    // --- prepare the content ---
    stream = new FileInputStream(uploadedFile);
    String name = (String) properties.get(PropertyIds.NAME);
    ContentStream contentStream = 
      session.getObjectFactory().createContentStream(name, 
        uploadedFile.length(), mimeType, stream);

    // --- create the document ---
    newDocId = session.createDocument(properties, parent,
         contentStream, versioningState);
  }
  finally {
    if (stream != null) { <co id="ch8_create_deltemp" />
      try {
        stream.close();
      }
      catch (IOException ioe) {
        // ignore
      }
    }
     
    uploadedFile.delete();
  }

  // --- redirect to show page ---
  try {
    String url = request.getRequestURL().toString();
    int lastSlash = url.lastIndexOf('/');
     
    url = url.substring(0, lastSlash) + "/show?id=" +
      URLEncoder.encode(newDocId.getId(), "UTF-8");
     
    redirect(url, request, response);
  }
  catch(UnsupportedEncodingException e) {
    throw new ServletException(e);
  }
}
//<end id="ne-setup"/>

