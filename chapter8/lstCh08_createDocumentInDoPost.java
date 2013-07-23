//<start id="ne-setup"/>
Map<String, Object> properties = new HashMap<String, Object>();
properties.put(PropertyIds.NAME, name);  //<co id="ch8_lst_createdocInDoPost0" />
properties.put(PropertyIds.OBJECT_TYPE_ID, typeId);

ObjectId parentFolderId = session.createObjectId(parentId); //<co id="ch8_lst_createdocInDoPost1" />

File file = new File("my-content.txt");

ContentStream contentStream = //<co id="ch8_lst_createdocInDoPost2" />
  session.getObjectFactory().createContentStream(file.getName(), 
    file.length(), "text/plain", new FileInputStream(file));

VersioningState versioningState = null; //<co id="ch8_lst_createdocInDoPost2_1" />

ObjectId newDocumentId = 
  session.createDocument(properties, parentFolderId,
    contentStream, versioningState); //<co id="ch8_lst_createdocInDoPost3" />
//<end id="ne-setup"/>

