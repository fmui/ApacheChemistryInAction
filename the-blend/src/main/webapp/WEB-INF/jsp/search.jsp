<%-- 
   Copyright 2012 Manning Publications Co.

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
--%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ page import="com.manning.cmis.theblend.util.HTMLHelper" %>
<%@ page import="org.apache.chemistry.opencmis.client.api.*" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ include file="header.jsp" %>

<%
String query = (String) request.getAttribute("q");
String cql = (String) request.getAttribute("cql");
%>

<h1>Search</h1>

<div class="cmisdetail cmisservicedetail">CMIS Service: <code>query</code></div>

<form method="POST">
<input type="text" name="q" size="50" value="<%= (query == null ? "" : HTMLHelper.formatAttribute(query)) %>"> <input type="submit" value="Search"> 
</form>


<% 
List<Map<String, Object>> results = (List<Map<String, Object>>) request.getAttribute("results");
if (results != null) {
%>
<div class="cmisdetail">
<br>Query:<br>
<code><%= HTMLHelper.format(cql) %></code>
</div>
<%
	if(results.size() == 0) {
%>

<div style="font-style: italic;"><br>Nothing found!<br></div>

<%	} else { %>

<div><br>
<% for (Map<String, Object> row: results) { %>
<div class="objectbox">
  <a href="<%= HTMLHelper.encodeUrlWithId(request, "show", (String) row.get("cmis:objectId")) %>"><%= HTMLHelper.format(row.get("cmis:name")) %></a><br>
  <%= HTMLHelper.format(row.get("type")) %><br>
  Modified: <%= HTMLHelper.format(row.get("cmis:lastModificationDate")) %>
</div>
<% } %>
</div>

<% } } %>

<%@ include file="footer.jsp" %>