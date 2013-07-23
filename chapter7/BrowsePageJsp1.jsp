//<start id="ne-setup"/>
<%
  int skip = (Integer) request.getAttribute("skip");
  Folder folder = (Folder) request.getAttribute("folder"); //<co id="folder-obj" />  
  List<CmisObject> childrenPage = 
     (List<CmisObject>) request.getAttribute("page"); //<co id="children2display" />
  long total = (Long) request.getAttribute("total"); //<co id="total-children" />
  boolean isFirstPage = (Boolean) request.getAttribute("isFirstPage");
  boolean isLastPage = (Boolean) request.getAttribute("isLastPage");
  Folder parent = (Folder) request.getAttribute("parent");  //<co id="parent-folder" />
%>
//<end id="ne-setup"/>

