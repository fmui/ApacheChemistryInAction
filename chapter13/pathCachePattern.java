Document doc = (Document) session.getObjectByPath("path/to/doc");

...

ContentStream stream = null;
try {
  stream = doc.getContenStream();
}
catch(CmisObjectNotFoundException e) {
  session.removeObjectFromCache(doc); //<co id="ch13_pcp_1"/>

  try {
    //<co id="ch13_pcp_2"/>
    doc = (Document) session.getObjectByPath("/path/to/doc"); 
    stream = doc.getContenStream();
  } 
  catch(CmisObjectNotFoundException e) {
    // there is no object at this path anymore
  }
}