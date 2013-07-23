//<start id="ne-setup"/>
ContentStream contentStream = null;
if (streamId == null) {
  contentStream = document.getContentStream();
}
else {
  contentStream = document.getContentStream(streamId);
}

if (contentStream == null) {
  response.sendError(HttpServletResponse.SC_NOT_FOUND, "No content!"); //<co id="document-has-no-content" />
  return;
}

InputStream in = contentStream.getStream();
try {
  String mimeType = contentStream.getMimeType();
  if (mimeType == null || mimeType.length() == 0) {
    mimeType = "application/octet-stream"; //<co id="no-mime-type" />
  }

  response.setContentType(mimeType);
  OutputStream out = response.getOutputStream();

  byte[] buffer = new byte[64 * 1024]; 
  int b;
  while ((b = in.read(buffer)) > -1) {
    out.write(buffer, 0, b);
  }

  out.flush();
} finally {
  in.close(); //<co id="close-stream" />
}
//<end id="ne-setup"/>

