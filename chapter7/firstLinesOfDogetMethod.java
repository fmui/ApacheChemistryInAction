//<start id="ne-setup"/>
String id = getStringParameter(request, "id");
if (id == null) { //<co id="rootfolder-fallback" />
  id = session.getRepositoryInfo().getRootFolderId(); 
}

int skip = getIntParameter(request, "skip", 0); 
if (skip < 0) { //<co id="skip-fallback" />
  skip = 0;
}
//<end id="ne-setup"/>
