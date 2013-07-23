//<start id="ne-setup"/>
OperationContext childrenOpCtx = session.createOperationContext();
childrenOpCtx.setFilterString(
   "cmis:objectId,cmis:baseTypeId," + 
   "cmis:name,cmis:contentStreamLength," + //<co id="using-query-names" />
   "cmis:contentStreamMimeType"); 
childrenOpCtx.setIncludeAcls(false);
childrenOpCtx.setIncludeAllowableActions(true); //<co id="allowable-actions-on" />
childrenOpCtx.setIncludePolicies(false);
childrenOpCtx.setIncludeRelationships(IncludeRelationships.NONE);
childrenOpCtx.setRenditionFilterString("cmis:none");
childrenOpCtx.setIncludePathSegments(false);
childrenOpCtx.setOrderBy("cmis:name");  //<co id="order-by-name" />
childrenOpCtx.setCacheEnabled(false);
childrenOpCtx.setMaxItemsPerPage(10);  //<co id="set-batch-size" />

ItemIterable<CmisObject> children = folder.getChildren(childrenOpCtx); 
for (CmisObject child : children) {
  System.out.println(child.getName());
}
//<end id="ne-setup"/>

