//<start id="ne-setup"/>
OperationContext folderOpCtx = session.createOperationContext();
folderOpCtx.setFilterString("cmis:name,cmis:path"); //<co id="select-name-path" />
folderOpCtx.setIncludeAcls(false);
folderOpCtx.setIncludeAllowableActions(false); //<co id="allowable-actions-off" />
folderOpCtx.setIncludePolicies(false);
folderOpCtx.setIncludeRelationships(IncludeRelationships.NONE);
folderOpCtx.setRenditionFilterString("cmis:none");
folderOpCtx.setIncludePathSegments(false);
folderOpCtx.setOrderBy(null);
folderOpCtx.setCacheEnabled(true);
//<end id="ne-setup"/>

