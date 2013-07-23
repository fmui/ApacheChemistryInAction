//<start id="ne-setup"/>
public List<ObjectType> getCreatableTypes(Session session,
    String rootTypeId) {
	
    List<ObjectType> result = new ArrayList<ObjectType>();

    ObjectType rootType = null;
    try {
        rootType = session.getTypeDefinition(rootTypeId); //<co id="ch8_gct_1" />
    }
    catch (CmisObjectNotFoundException e) {
        return result; //<co id="ch8_gct_1_1" />
    }

    boolean isCreatable = 
        (rootType.isCreatable() == null ? true : 
            rootType.isCreatable().booleanValue()); //<co id="ch8_gct_1_2" />
		        
    if (isCreatable) {
        result.add(rootType);  //<co id="ch8_gct_2" />
    }

    List<Tree<ObjectType>> types =
        session.getTypeDescendants(rootTypeId, -1, false); //<co id="ch8_gct_3" />
    addType(types, result); //<co id="ch8_gct_4" />

    return result;
}

private void addType(List<Tree<ObjectType>> types, 
    List<ObjectType> resultList) {
	
    for (Tree<ObjectType> tt : types) {
        if (tt.getItem() != null) {
            boolean isCreatable = 
                (tt.getItem().isCreatable() == null ? true :
                    tt.getItem().isCreatable().booleanValue());

            if (isCreatable) {
                resultList.add(tt.getItem());
            }

            addType(tt.getChildren(), resultList);
        }
    }
}
//<end id="ne-setup"/>

