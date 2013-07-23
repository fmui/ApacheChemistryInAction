//<start id="ne-setup"/>
OperationContext docOpCtx = session.createOperationContext();
docOpCtx.setFilterString("*"); //<co id="select-all-properties" /> 
docOpCtx.setIncludeAcls(false);
docOpCtx.setIncludeAllowableActions(true); //<co id="allowable-actions-on2" /> 
docOpCtx.setIncludePolicies(false);
docOpCtx.setIncludeRelationships(IncludeRelationships.NONE);
docOpCtx.setRenditionFilterString("cmis:thumbnail"); //<co id="get-thumbnail-if-avail" /> 
docOpCtx.setIncludePathSegments(false);
docOpCtx.setOrderBy(null);
docOpCtx.setCacheEnabled(true);


CmisObject object = null;
try {
 object = session.getObject(id, docOpCtx);  //<co id="use-the-oc" /> 
}
catch (CmisBaseException cbe) {
  throw new TheBlendException("Could not retrieve document!", cbe);
}

Document document = null; //<co id="should-be-a-document" /> 
if (object instanceof Document) {
  document = (Document) object;
}
else {
  throw new TheBlendException("Object is not a document!");
}

//<end id="ne-setup"/>