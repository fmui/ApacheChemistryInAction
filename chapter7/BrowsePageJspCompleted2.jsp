//<start id="ne-setup"/>
<h1><%= folder.getName() %></h1>

Path: <%= folder.getPath() %><br>
Number of children: 
  <%= (total == -1 ? "unknown" : String.valueOf(total)) %><br>

<table>

<% if (parent != null) { %>
<tr>
  <td><a href="browse?id=<%= parent.getId() %>">..</a></td>
  <td></td>
  <td></td>
<tr>
<% } %>

<% for (CmisObject child: childrenPage) { %> 
<tr>
  <% if (child instanceof Folder) { %>

  <td>
    <a href="browse?id=<%= child.getId() %>"><%= child.getName() %></a>
  </td>
  <td></td>
  <td></td>

  <% } else if (child instanceof Document) { %>
  <%   
       Document doc = (Document) child;

       String mimeType = doc.getContentStreamMimeType();
       if (mimeType == null) { 
         mimeType = "";
       }

       String contentLength = "";
       if (doc.getContentStreamLength() > 0) {     
         contentLength = 
           String.valueOf(doc.getContentStreamLength()) + " bytes";
       }
  %>

  <td><a href="show?id=<%= doc.getId() %>"><%= doc.getName() %></a></td>
  <td><%= mimeType %></td>
  <td><%= contentLength %></td>

  <% } else { %>

  <td><%= child.getName() %></td>
  <td></td>
  <td></td>

  <% } %>
<tr>
<% } %>

</table>

<% if (!isFirstPage) { %>
<% String skipParam = (skip < 10 ? "0" : String.valueOf(skip - 10)); %>
<a href="browse?id=<%= folder.getId() %>&skip=<%= skipParam %>">
Previous Page<a>
<% } %>

<% if (!isLastPage) { %>
<a href="browse?id=<%= folder.getId() %>&skip=<%= skip + 10 %>">
Next Page<a>
<% } %>
//<end id="ne-setup"/>

