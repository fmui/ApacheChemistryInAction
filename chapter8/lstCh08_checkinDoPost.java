//<start id="ne-setup"/>
protected void doPost(HttpServletRequest request,
  HttpServletResponse response, Session session)
  throws ServletException, IOException, TheBlendException {

  boolean isMultipart = ServletFileUpload.isMultipartContent(request);
  if (!isMultipart) {
    throw new TheBlendException("Invalid request!");
  }

  // --- get the content for the next version ---
  File uploadedFile = null;
  String mimeType = null;
  String docPath = null;
  ObjectId newVersionId = null;
     
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
          docPath = item.getString();
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
    // --- fetch the document ---
    CmisObject cmisObject = null;
    try {
      cmisObject = session.getObjectByPath(docPath);  
    } catch (CmisBaseException cbe) {
      throw new TheBlendException(
        "Could not retrieve the document!", cbe);
    }

    Document doc = null;
    if (cmisObject instanceof Document) {
      doc = (Document) cmisObject;
    } else {
      throw new TheBlendException("Object is not a document!");
    }

    // --- prepare the content ---
    stream = new FileInputStream(uploadedFile);
    ContentStream contentStream = 
      session.getObjectFactory().createContentStream(
        doc.getContentStreamFileName(), uploadedFile.length(), 
          mimeType, stream);

    // --- do the check out ---
    Document pwc = null;
    try {
      ObjectId pwcId = doc.checkOut();
      pwc = (Document) session.getObject(pwcId);
    } catch (CmisBaseException cbe) {
      throw new TheBlendException(
        "Could not check out the document!", cbe);
    }

    // --- do the check in ---
    try {
      newVersionId = pwc.checkIn(true, null, contentStream, null); //<co id="ch8_checkin_1" />
    } catch (CmisBaseException cbe) {
      throw new TheBlendException(  
        "Could not check in the document!", cbe); //<co id="ch8_checkin_2" />
    }
  }
  finally {
    if (stream != null) {
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
      URLEncoder.encode(newVersionId.getId(), "UTF-8");
     
    redirect(url, request, response);
  }
  catch(UnsupportedEncodingException e) {
    throw new ServletException(e);
  }
}
//<end id="ne-setup"/>

