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
<%@ include file="header.jsp" %>

<%
	CmisObject cmisObject = (CmisObject) request.getAttribute("object");
	List<Document> tracks = (List<Document>) request.getAttribute("tracks");
%>

<h1><%= HTMLHelper.format(cmisObject.getName()) %></h1>

<div class="cmisdetail cmisservicedetail">CMIS Service: <code>updateProperties</code></div>

<form method="POST">
<input type="hidden" name="action" value="2">
<input type="hidden" name="id" value="<%= HTMLHelper.formatAttribute(cmisObject.getId()) %>">
<table class="objectlist">
<tr>
  <th style="width: 50px">Track</th>
  <th style="width: 600px">Title</th>
  <th style="width: 100px"></th>
</tr>  
<% int pos = 0;
   for (Document track: tracks) { %>
<input type="hidden" name="id_<%= pos %>" value="<%= HTMLHelper.formatAttribute(track.getId()) %>">
<tr>
  <td><input type="text" size="2" name="pos_<%= pos %>" value="<%= pos + 1 %>"></td>
  <td><a href="<%= HTMLHelper.encodeUrlWithId(request, "show", track.getId()) %>"><%= HTMLHelper.format(track.getName()) %></a></td>
  <td><input type="checkbox" id="remove_<%= pos %>" name="remove_<%= pos %>" value="1"> <label for="remove_<%= pos %>">remove</label></td>
</tr>
<% pos++; 
	} %>
</table>
<br>
<input type="submit" value="Update Album">
</form>

<br><br>

<div class="cmisdetail cmisservicedetail">CMIS Service: <code>updateProperties</code></div>

<form method="POST">
<input type="hidden" name="action" value="3">
<input type="hidden" name="id" value="<%= HTMLHelper.formatAttribute(cmisObject.getId()) %>">
<table>
<tr>
  <td><input type="radio" id="trid" name="what" value="id" checked> <label for="trid">Track Id:</label></td>
  <td><input type="text" name="trackid" size="50" value=""></td>
</tr>
<tr>
  <td><input type="radio" id="trpath" name="what" value="path"> <label for="trpath">Track Path:</label></td>
  <td><input type="text" name="trackpath" size="50"></td>
</tr>
</table>
<br>
<input type="submit" value="Add Track">
</form>

<%@ include file="footer.jsp" %>