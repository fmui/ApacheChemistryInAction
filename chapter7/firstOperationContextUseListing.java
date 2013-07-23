//<start id="ne-setup"/>
CmisObject object = null;
try {
 object = session.getObject(id, folderOpCtx);  //<co id="using-the-oc" />
}
catch (CmisBaseException cbe) {
  throw new TheBlendException("Could not retrieve folder!", cbe);
}

Folder folder = null;
if (object instanceof Folder) {
  folder = (Folder) object;
}
else {
  throw new TheBlendException("Object is not a folder!");
}
//<end id="ne-setup"/>

