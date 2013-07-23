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
<%@ page import="com.manning.cmis.theblend.util.HTMLHelper"%>
<%@ page import="com.manning.cmis.theblend.session.IdMapping" %>
<%@ page import="org.apache.chemistry.opencmis.client.api.*"%>
<%@ page import="java.util.List" %>
<%@ include file="header.jsp" %>

<%
	List<CmisObject> children = (List<CmisObject>) request.getAttribute("children");
	String cql = (String) request.getAttribute("cql");
    List<CmisObject> recentlyUpdated = (List<CmisObject>) request.getAttribute("recentlyUpdated");
	String path = (String) request.getAttribute("path");
	List<ObjectType> docTypes = (List<ObjectType>) request.getAttribute("docTypes");
%>

<div style="text-align: center;"><h1>Your Blend. Your Dashboard.</h1></div>

<div style="float: left; width: 350px;">

<div class="dashboardbox">
<div style="font-weight: bold;">Browse the repository</div>
<div class="cmisdetail cmisservicedetail">CMIS Service: <code>getChildren</code></div>
<div style="padding: 10px;">
<% for (CmisObject child: children) {
   if (child instanceof Document) { %>
  <a href="<%= HTMLHelper.encodeUrlWithId(request, "show", child.getId()) %>"><%= HTMLHelper.format(child.getName()) %></a><br>
<% } else if (child instanceof Folder) { %>
  <a href="<%= HTMLHelper.encodeUrlWithId(request, "browse", child.getId()) %>"><%= HTMLHelper.format(child.getName()) %></a><br>
<% } } %>
</div>
Explore <a href="browse">more</a>!<br><br>
</div>

<div class="dashboardbox">
<div style="font-weight: bold;">Looking for something?</div>
<div class="cmisdetail cmisservicedetail">CMIS Service: <code>query</code></div>
<form method="POST" action="search">
<br><input name="q" type="text" size="30"> <input type="submit" value="Find it!"><br><br>
</form>
</div>

<div class="dashboardbox">
<div style="font-weight: bold;">Quick Add</div>
<div class="cmisdetail cmisservicedetail">CMIS Service: <code>createDocument</code></div>
<form action="add" method="POST" enctype="multipart/form-data">
<br>
<table style="width: 350px;">
  <tr><td style="width: 90px;">Where?</td><td><input name="parentpath" type="text" size="30" value="<%= HTMLHelper.formatAttribute(path) %>"></td></tr>
  <tr><td>What?</td><td><input name="content" type="file"></td></tr>
  <tr><td>Which&nbsp;is?</td><td><select name="typeid">  
  <% for(ObjectType type: docTypes) { %>
    <option value="<%= HTMLHelper.formatAttribute(type.getId()) %>"<%= ("cmis:document".equals(type.getId()) ? " selected" : "") %>>
    <%= HTMLHelper.format(type.getDisplayName()) %>
    </option>
  <% } %>
  </select></td></tr>
  <tr><td></td><td><input type="submit" value="Add it now!"></td></tr>
</table>
</form>
</div>

<div class="dashboardbox">
<div style="font-weight: bold;">Create Album</div>
<div class="cmisdetail cmisservicedetail">CMIS Service: <code>createDocument</code></div>
<form action="album" method="POST">
<input type="hidden" name="action" value="1">
<br>
<table style="width: 350px;">
  <tr><td style="width: 90px;">Where?</td><td><input name="parentpath" type="text" size="30" value="<%= HTMLHelper.formatAttribute(path) %>"></td></tr>
  <tr><td>Title?</td><td><input name="name" type="text" size="30"></td></tr>
  <tr><td></td><td><input type="submit" value="Create it now!"></td></tr>
</table>
</form>
</div>

</div>

<div class="dashboardbox" style="float: right; width: 570px;">
<div style="font-weight: bold;">Recently Updated Documents</div>
<div class="cmisdetail">
<br>Query:<br>
<code><%= HTMLHelper.format(cql) %></code>
</div>
<br>
<% if (recentlyUpdated.isEmpty()) { %>
<div>Nothing found!</div>
<% } %>

<% for (CmisObject doc: recentlyUpdated) { %>
<div class="objectbox">
  <a href="<%= HTMLHelper.encodeUrlWithId(request, "show", doc.getId()) %>"><%= HTMLHelper.format(doc.getName()) %></a><br>
  <%= HTMLHelper.format(doc.getType().getDisplayName()) %><br>
  Modified: <%= HTMLHelper.format(doc.getLastModificationDate()) %>
<%
  List<String> tags = doc.getPropertyValue(IdMapping.getRepositoryPropertyId("cmisbook:tags"));
  if (tags != null) { 
    for(String tag: tags) {
%>
  <%= HTMLHelper.format(tag) %>
<%  } } %>  
</div>
<% } %>

</div>

<div style="clear: both;"></div>

<%@ include file="footer.jsp" %>