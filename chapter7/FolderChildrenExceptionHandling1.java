//<start id="ne-setup"/>
ItemIterable<CmisObject> children = folder.getChildren(childrenOpCtx); //<co id="no-round-trip" /> 

try {
  for (CmisObject child : children) {  //<co id="repo-contact-here" />
    System.out.println(child.getName());
  }
}
catch (CmisBaseException cbe) {
  throw new TheBlendException("Could not fetch children!", cbe);
}
//<end id="ne-setup"/>

