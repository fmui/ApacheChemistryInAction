//<start id="ne-setup"/>
<%
  String q = (String) request.getAttribute("q");
  Map<String, String> results = 
    (Map<String, String>) request.getAttribute("results");
%>

<h1>Search</h1>

<form method="GET">
Enter the name of the document:
<input type="text" name="q" value="<%= (q == null ? "" : q) %>">
<input type="submit" value="Search"> 
</form>

<% if (results != null) { %>
<ul>
<% for (Map.Entry<String, String> result: results.entrySet()) { %>
  <li>
    <a href="show?id=<%= result.getKey() %>">
      <%= result.getValue() %></a>
  </li>
<% } %>
</ul>
<% } %>
//<end id="ne-setup"/>

