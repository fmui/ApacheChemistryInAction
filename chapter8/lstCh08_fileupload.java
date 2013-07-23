//<start id="ne-setup"/>
boolean isMultipart = ServletFileUpload.isMultipartContent(request); //<co id="ch8_ful_1" />
if (!isMultipart) {
  throw new TheBlendException("Invalid request!"); 
}

Map<String, Object> properties = new HashMap<String, Object>(); 
File uploadedFile = null; //<co id="ch8_ful_2" /> 
String mimeType = null;   //<co id="ch8_ful_3" />
String parentPath = null; //<co id="ch8_ful_4" />

try {
  DiskFileItemFactory factory = new DiskFileItemFactory();

  ServletFileUpload upload = new ServletFileUpload(factory); 
  upload.setSizeMax(50 * 1024 * 1024); //<co id="ch8_ful_5" />

  @SuppressWarnings("unchecked")
  List<FileItem> items = upload.parseRequest(request);

  Iterator<FileItem> iter = items.iterator();
  while (iter.hasNext()) {
    FileItem item = iter.next();

    if (item.isFormField()) {
      String name = item.getFieldName();

      if ("path".equalsIgnoreCase(name)) {  
        parentPath = item.getString(); //<co id="ch8_ful_6" />
      }
      else if ("name".equalsIgnoreCase(name)) {
        properties.put(PropertyIds.NAME, item.getString()); //<co id="ch8_ful_7" />
      }
      else if ("type".equalsIgnoreCase(name)) {
        properties.put(PropertyIds.OBJECT_TYPE_ID, item.getString()); //<co id="ch8_ful_8" />
      }
    } 
    else {
      uploadedFile = File.createTempFile("blend", "tmp");

      item.write(uploadedFile);  //<co id="ch8_ful_9" />
      
      mimeType = item.getContentType(); //<co id="ch8_ful_10" />
      if (mimeType == null) { 
        mimeType = "application/octet-stream"; //<co id="ch8_ful_11" />
      }
    }
  }
} catch (Exception e) {
  throw new TheBlendException("Upload failed: " + e, e);
}

if (uploadedFile == null) { //<co id="ch8_ful_12" />
  throw new TheBlendException("No content!");
}
//<end id="ne-setup"/>