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
<%@page import="org.apache.chemistry.opencmis.commons.enums.Action"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ page import="com.manning.cmis.theblend.util.HTMLHelper" %>
<%@ page import="com.manning.cmis.theblend.session.IdMapping" %>
<%@ page import="org.apache.chemistry.opencmis.client.api.*" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Set" %>
<%@ include file="header.jsp" %>

<%
	Document doc = (Document) request.getAttribute("document");
	request.setAttribute("contentUrl", HTMLHelper.encodeUrlWithId(request, "download", doc.getId()));
	
	String artworkId = (String) request.getAttribute("artwork");
	List<Document> versions = (List<Document>) request.getAttribute("versions");
	
	Set<Action> allowableActions = doc.getAllowableActions().getAllowableActions();
%>

<h1><%= HTMLHelper.format(doc.getName()) %></h1>

<div class="cmisdetail cmisservicedetail">CMIS Service: <code>getObject</code></div>

<div style="width: 600px; float:left;">

<!-- artwork -->
<% if (artworkId != null) { %>
<div>
<div class="cmisdetail cmisservicedetail">CMIS Service: <code>getContentStream</code></div>
<img style="max-width: 600px;" src="<%= HTMLHelper.encodeUrlWithId(request, "download", artworkId) %>">
</div>
<% } %>

<!-- content -->
<div>
<%
	if (doc.getType().getId().equals(IdMapping.getRepositoryTypeId("cmisbook:album"))) {
		%><jsp:include page="albumview.jsp" /><%
	} else if (doc.getContentStreamLength() > 0 && doc.getContentStreamMimeType() != null) {
		%><div class="cmisdetail cmisservicedetail">CMIS Service: <code>getContentStream</code></div><%
		if (doc.getContentStreamMimeType().toLowerCase().startsWith("audio/")) {  
			%>
			<jsp:include page="audioplayer.jsp">
    			<jsp:param name="url" value="${contentUrl}" />
				<jsp:param name="format" value="<%= (doc.getContentStreamMimeType().toLowerCase().endsWith(\"ogg\") ? \"oga\" : \"mp3\") %>" />
			</jsp:include>			
			<%
  		} else if (doc.getContentStreamMimeType().toLowerCase().startsWith("video/")) {
			%>
			<jsp:include page="videoplayer.jsp">
    			<jsp:param name="url" value="${contentUrl}" />
				<jsp:param name="format" value="m4v" />
			</jsp:include>			
			<%
    	} else if (doc.getContentStreamMimeType().toLowerCase().startsWith("image/")) {
    		%>
			<img style="max-width: 600px;" src="<%= HTMLHelper.encodeUrlWithId(request, "download", doc.getId()) %>">
    		<%
    	} else if (doc.getContentStreamMimeType().toLowerCase().startsWith("text/") ||
      		doc.getContentStreamMimeType().toLowerCase().startsWith("application/pdf")) {
			%>
			<iframe src="<%= HTMLHelper.encodeUrlWithId(request, "download", doc.getId()) %>" style="width: 600px; height: 300px;">
			iframes are not supported.
			</iframe>
			<%
  		} else {
			%>
			[Content view not available for this MIME type.]		
			<%
  		}
%>
<% } else {  %>
[No Content.]
<% } %>
</div>

<br>

<!-- properties table -->
<table class="proplist">
<% for (Property<?> prop: doc.getProperties()) { %>
  <tr>
    <td style="width: 200px">
      <span style="font-weight: bold;"><%= HTMLHelper.format(prop.getDefinition().getDisplayName()) %></span><br>
      <span class="cmisdetail" style="font-size: smaller;"><%= HTMLHelper.format(prop.getId()) %></span>
    </td>
    <td class="cmisdetail" style="width: 80px"><%= HTMLHelper.format(prop.getDefinition().getPropertyType()) %></td>
    <td style="width: 320px;"><%= HTMLHelper.formatList(prop.getValues()) %></td>
  </tr>
<% } %>
</table>

</div>

<div style="width: 370px; float:right;">

<!-- action links -->
<div class="showbox">

<% if(allowableActions.contains(Action.CAN_GET_CONTENT_STREAM)) { %>
<a class="actionlink" href="${contentUrl}"><img class="actionlink" alt="Open" src="images/open.png">Open</a><br>
<a class="actionlink" href="${contentUrl}&save=true"><img class="actionlink" alt="Download" src="images/download.png">Download</a><br>
<% } %>

<% if(allowableActions.contains(Action.CAN_UPDATE_PROPERTIES)) { %>
<a class="actionlink" href="<%= HTMLHelper.encodeUrlWithId(request, "rename", doc.getId()) %>"><img class="actionlink" alt="Rename" src="images/rename.png">Rename</a><br>
<% } %>

<% if(allowableActions.contains(Action.CAN_MOVE_OBJECT)) { %>
<a class="actionlink" href="<%= HTMLHelper.encodeUrlWithId(request, "move", doc.getId()) %>"><img class="actionlink" alt="Move" src="images/move.png">Move</a><br>
<% } %>

<% if(allowableActions.contains(Action.CAN_DELETE_OBJECT)) { %>
<a class="actionlink" href="<%= HTMLHelper.encodeUrlWithId(request, "delete", doc.getId()) %>"><img class="actionlink" alt="Delete" src="images/delete.png">Delete</a><br>
<% } %>

<% if(allowableActions.contains(Action.CAN_CHECK_OUT)) { %>
<a class="actionlink" href="<%= HTMLHelper.encodeUrlWithId(request, "addversion", doc.getId()) %>"><img class="actionlink" alt="Add new version" src="images/addversion.png">Add New Version</a><br>
<% } %>

<% if (doc.getType().getPropertyDefinitions().containsKey(IdMapping.getRepositoryPropertyId("cmisbook:artwork")) && allowableActions.contains(Action.CAN_UPDATE_PROPERTIES)) { %>
<a class="actionlink" href="<%= HTMLHelper.encodeUrlWithId(request, "artwork", doc.getId()) %>"><img class="actionlink" alt="Artwork" src="images/artwork.png">Change Artwork</a><br>
<% } %>

</div>

<!-- paths -->
<br>
<div class="showbox">
<div style="font-weight: bold;">Document Path(s)</div>
<div class="cmisdetail cmisservicedetail">CMIS Service: <code>getObjectParents</code></div>
<div style="padding-top: 10px; padding-left: 10px;">
<% for (String path: doc.getPaths()) {
  int x = path.lastIndexOf('/');
  String folderPath = path.substring(0, x);
  String docName = path.substring(x+1);
	%>
   <a style="color: #666666;" href="<%= HTMLHelper.encodeUrlWithPath(request, folderPath) %>"><%= HTMLHelper.format(folderPath) %></a>/<%= HTMLHelper.format(docName) %><br>
<% } %>
</div>
</div>

<!-- tags -->
<% if (doc.getType().getPropertyDefinitions().containsKey(IdMapping.getRepositoryPropertyId("cmisbook:tags"))) { %>
<br>
<div class="showbox">
<div style="font-weight: bold;">Tags</div>
<div class="cmisdetail cmisservicedetail">CMIS Service: <code>updateProperties</code></div>
<div style="padding-top: 10px; padding-left: 10px;">
<%
  List<String> tags = (List<String>) doc.getPropertyValue(IdMapping.getRepositoryPropertyId("cmisbook:tags"));
  if (tags != null) { 
    for(String tag: tags) {
%>
  <div style="padding-top: 2px; padding-bottom: 2px;">
  <form method="POST">
  <input type="hidden" name="id" value="<%= HTMLHelper.formatAttribute(doc.getId()) %>">
  <input type="hidden" name="removetag" value="<%= HTMLHelper.formatAttribute(tag) %>">
  <a href="<%= HTMLHelper.encodeUrlWithTag(request, tag) %>"><%= HTMLHelper.format(tag) %></a>
  <% if(allowableActions.contains(Action.CAN_UPDATE_PROPERTIES)) { %>
    <input type="image" src="images/minus.png" alt="remove" title="Remove Tag">
  <% } %>
  </form>
  </div>
<%  } } %>
<% if(allowableActions.contains(Action.CAN_UPDATE_PROPERTIES)) { %>
  <form method="POST">
  <input type="hidden" name="id" value="<%= HTMLHelper.formatAttribute(doc.getId()) %>">
  <input type="text" name="addtag" size="20">
  <input type="image" src="images/plus.png" alt="add" title="Add Tag">
  </form>
<% } %>
</div>  
</div>
<% } %>

<!-- version history -->
<% if (versions != null) { %>
<br>
<div class="showbox">
<div style="font-weight: bold;">Version History</div>
<div class="cmisdetail cmisservicedetail">CMIS Service: <code>getAllVersions</code></div>
<table style="border-spacing: 10px;">
<% for (Document version: versions) { %>
  <tr style="vertical-align: top;">
    <td><%= HTMLHelper.format(version.getVersionLabel()) %></td>
    <td>
      <a href="<%= HTMLHelper.encodeUrlWithId(request, "show", version.getId()) %>"><%= HTMLHelper.format(version.getName()) %></a>
      <br>
      Created: <%= HTMLHelper.format(version.getCreationDate()) %>
      <br>
      <% if(version.getId().equals(doc.getId())) { %><span style="font-size: smaller; color: #008800;"> (THIS) </span><% } %>
      <% if(version.getId().equals(doc.getVersionSeriesCheckedOutId())) { %><span class="cmisdetail" style="font-size: smaller;"> (PWC) </span><% } %>
      <% if(Boolean.TRUE.equals(version.isLatestVersion())) { %><span class="cmisdetail" style="font-size: smaller;"> (LATEST&nbsp;VERSION) </span><% } %>
      <% if(Boolean.TRUE.equals(version.isMajorVersion())) { %><span class="cmisdetail" style="font-size: smaller;"> 
        <% if(Boolean.TRUE.equals(version.isLatestMajorVersion())) { %> (LATEST&nbsp;MAJOR&nbsp;VERSION)<% } else { %> (MAJOR&nbsp;VERSION)<% } %> </span>
      <% } %>
    </td>
  </tr>
<% } %>
</table>
</div>
<% } %>

</div>

<div style="clear: both;"></div>

<%@ include file="footer.jsp" %>