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
<%@ include file="header.jsp" %>

<%
	Folder folder = (Folder) request.getAttribute("folder");
    Folder parent = (Folder) request.getAttribute("parent");

    long total = (Long) request.getAttribute("total");
	String totalStr  = "";
    if (total > -1) {
		totalStr = "This folder has ";
		if (total == 0) {
			totalStr += "no children.";
		} else if (total == 1) {
			totalStr += "one child.";
		} else {
			totalStr += total + " children.";
		}
	}
	
	int skip = (Integer) request.getAttribute("skip");

	boolean hasMore = (Boolean) request.getAttribute("hasMore");

	List<ObjectType> docTypes = (List<ObjectType>) request.getAttribute("docTypes");
	List<ObjectType> folderTypes = (List<ObjectType>) request.getAttribute("folderTypes");
%>

<h1><%= HTMLHelper.format(folder.getName()) %></h1>

<div class="cmisdetail cmisservicedetail">CMIS Service: <code>getChildren</code></div>

<div style="padding-bottom:5px;"><%= totalStr %></div>

<table class="objectlist">
<tr>
  <th style="width: 350px">Name<span class="cmisdetail" style="font-size: small;"><br>cmis:name</span></th>
  <th style="width: 150px">Type<span class="cmisdetail" style="font-size: small;"><br>display name of type definition</span></th>
  <th style="width: 150px">MIME Type<span class="cmisdetail" style="font-size: small;"><br>cmis:contentStreamMimeType</span></th>
  <th style="width: 100px; text-align: right;">Size (bytes)<span class="cmisdetail" style="font-size: small;"><br>cmis:contentStreamLength</span></th>
  <th></th>
  <th></th>
  <th></th>
</tr>

<% if (parent != null ) { %>
<tr>
  <td><a href="<%= HTMLHelper.encodeUrlWithId(request, "browse", parent.getId()) %>">..</a></td>
  <td><%= HTMLHelper.format(parent.getType().getDisplayName()) %></td>
  <td></td>
  <td></td> 
  <td></td>
  <td></td>   
  <td></td>   
</tr>
<% } %>

<% for (CmisObject child: (List<CmisObject>) request.getAttribute("page")) { %>
<tr>

<%	if (child instanceof Document) { 
 	Document doc = (Document) child;
%>
  <td>
    <a href="<%= HTMLHelper.encodeUrlWithId(request, "show", child.getId()) %>"><%= HTMLHelper.format(child.getName()) %></a>
    <% List<String> tags = (List<String>) child.getPropertyValue(IdMapping.getRepositoryPropertyId("cmisbook:tags"));
       if (tags != null && !tags.isEmpty()) { %>
    <div style="font-size: small;">
      Tags: <% for(String tag: tags) { %><a href="<%= HTMLHelper.encodeUrlWithTag(request, tag) %>"><%= HTMLHelper.format(tag) %></a> <% } %>
    </div>
    <% } %>
  </td>
  <td><%= HTMLHelper.format(child.getType().getDisplayName()) %></td>
  <td><%= HTMLHelper.format(doc.getContentStreamMimeType()) %></td>
  <td style="text-align: right;"><% if(doc.getContentStreamLength() > 0) { %><%= HTMLHelper.format(doc.getContentStreamLength()) %><% } %></td>

<%	} else if (child instanceof Folder) { %>
  <td><a href="<%= HTMLHelper.encodeUrlWithId(request, "browse", child.getId()) %>"><%= HTMLHelper.format(child.getName()) %></a></td>
  <td><%= HTMLHelper.format(child.getType().getDisplayName()) %></td>
  <td></td>
  <td></td>

<%	} else { %>
  <td><%= HTMLHelper.format(child.getName()) %></td>
  <td><%= HTMLHelper.format(child.getType().getDisplayName()) %></td>
  <td></td>
  <td></td>  
<% } %>
  <td>
  <% if(child.getAllowableActions().getAllowableActions().contains(Action.CAN_UPDATE_PROPERTIES)) { %>
  <a class="actionlink" href="<%= HTMLHelper.encodeUrlWithId(request, "rename", child.getId()) %>"><img class="actionlink" alt="Rename" src="images/rename.png">Rename</a>
  <% } %>
  </td>

  <td>
  <% if(child.getAllowableActions().getAllowableActions().contains(Action.CAN_MOVE_OBJECT)) { %>
  <a class="actionlink" href="<%= HTMLHelper.encodeUrlWithId(request, "move", child.getId()) %>"><img class="actionlink" alt="Move" src="images/move.png">Move</a>
  <% } %>
  </td>
  
  <td>
  <% if(child.getAllowableActions().getAllowableActions().contains(Action.CAN_DELETE_OBJECT) || child.getAllowableActions().getAllowableActions().contains(Action.CAN_DELETE_TREE)) { %>
  <a class="actionlink" href="<%= HTMLHelper.encodeUrlWithId(request, "delete", child.getId()) %>"><img class="actionlink" alt="Delete" src="images/delete.png">Delete</a>
  <% } %>
  </td>
</tr>

<% } %>

<tr>
  <form method="POST">
  <input type="hidden" name="parent" value="<%= HTMLHelper.formatAttribute(folder.getId()) %>" >
  <td>
    <div class="cmisdetail cmisservicedetail">CMIS Service: <code>createFolder</code></div>
    <input type="text" size="20" name="name">
  </td>
  <td><select name="typeid">  
  <% for(ObjectType type: folderTypes) { %>
    <option value="<%= HTMLHelper.formatAttribute(type.getId()) %>"<%= ("cmis:folder".equals(type.getId()) ? " selected" : "") %>>
    <%= HTMLHelper.format(type.getDisplayName()) %>
    </option>
  <% } %>
  </select></td>
  <td></td>
  <td></td>
  <td colspan="3"><input type="button" value="Create Folder" onclick="form.submit()"></td>
  </form>
</tr>

<tr>
  <form method="POST" enctype="multipart/form-data" action="<%= HTMLHelper.encodeUrl(request, "add") %>">
  <input type="hidden" name="parentid" value="<%= HTMLHelper.formatAttribute(folder.getId()) %>" >
  <td>
    <div class="cmisdetail cmisservicedetail">CMIS Service: <code>createDocument</code></div>
    <input type="file" size="20" name="content">
  </td>
  <td><select name="typeid">  
  <% for(ObjectType type: docTypes) { %>
    <option value="<%= HTMLHelper.formatAttribute(type.getId()) %>"<%= ("cmis:document".equals(type.getId()) ? " selected" : "") %>>
    <%= HTMLHelper.format(type.getDisplayName()) %>
    </option>
  <% } %>
  </select></td>
  <td></td>
  <td></td>
  <td colspan="3"><input type="button" value="Create Document" onclick="form.submit()"></td>
  </form>
</tr>

<tr>
  <form method="POST" action="album">
  <input type="hidden" name="action" value="1">
  <input type="hidden" name="parent" value="<%= HTMLHelper.formatAttribute(folder.getId()) %>" >
  <td>
    <div class="cmisdetail cmisservicedetail">CMIS Service: <code>createDocument</code></div>
    <input type="text" size="20" name="name">
  </td>
  <td>Album</td>
  <td></td>
  <td></td>
  <td colspan="3"><input type="button" value="Create Album" onclick="form.submit()"></td>
  </form>
</tr>

</table>

<div style="padding-top: 20px; text-align: center;">
<% if (skip > 0) { %>
<a href="<%= HTMLHelper.encodeUrlWithId(request, "browse", folder.getId()) + "&skip=" + (skip - 1) %>">&lt; Previous Page</a> -
<% } %>

Page <%= (skip + 1) %>

<% if (hasMore) { %>
 - <a href="<%= HTMLHelper.encodeUrlWithId(request, "browse", folder.getId()) + "&skip=" + (skip + 1) %>">Next Page &gt;</a>
<% } %>
</div>

<%@ include file="footer.jsp" %>