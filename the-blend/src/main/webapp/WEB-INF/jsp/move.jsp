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
	String path = (String) request.getAttribute("path");
	CmisObject cmisObject = (CmisObject) request.getAttribute("object");
	List<Folder> parents = (List<Folder>) request.getAttribute("parents");
%>

<h1>Move <%= HTMLHelper.format(cmisObject.getName()) %></h1>

<div class="cmisdetail cmisservicedetail">CMIS Services: <code>moveObject</code></div>

<form method="POST">
<input type="hidden" name="id" value="<%= HTMLHelper.formatAttribute(cmisObject.getId()) %>">
<table>
<tr>
  <td>Move from:</td>
  <td>
  <% 
  int count = 0;
  for(Folder parent: parents) { 
  %>
    <input type="radio" id="source<%= count %>" name="sourceid" value="<%= HTMLHelper.formatAttribute(parent.getId()) %>"<%= (count == 0 ? " checked" : "") %>>
    <label for="source<%= count %>"><%= HTMLHelper.format(parent.getPath()) %></label><br>
  <%
  count++;
  } 
  %>
  </td>
</tr>
<tr>
  <td>Move to:</td>
  <td><input name="targetpath" type="text" size="40" value="<%= HTMLHelper.formatAttribute(parents.get(0).getPath()) %>"></td>
</tr>
<tr>
  <td></td>
  <td><br><input type="submit" value="Move"> <input type="button" value="Cancel" onclick="window.history.back()" /></td>
</tr>
</table>
</form>

<%@ include file="footer.jsp" %>