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
	List<ObjectType> docTypes = (List<ObjectType>) request.getAttribute("docTypes");
%>

<h1>Add Something New</h1>

<div class="cmisdetail cmisservicedetail">CMIS Service: <code>createDocument</code></div>

<form method="POST" enctype="multipart/form-data">
<table>
<tr>
  <td>Path:</td>
  <td><input name="parentpath" type="text" size="40" value="<%= HTMLHelper.formatAttribute(path) %>"></td>
</tr>
<tr>
  <td>File:</td>
  <td><input name="content" type="file"></td>
</tr>
<tr>
  <td>Type:</td>
  <td><select name="typeid">  
  <% for(ObjectType type: docTypes) { %>
    <option value="<%= HTMLHelper.formatAttribute(type.getId()) %>"<%= ("cmis:document".equals(type.getId()) ? " selected" : "") %>>
    <%= HTMLHelper.format(type.getDisplayName()) %>
    </option>
  <% } %>
  </select></td>
</tr>
<tr>
  <td></td>
  <td><input type="submit" value="Upload"> <input type="button" value="Cancel" onclick="window.history.back()" /></td>
</tr>
</table>
</form>

<%@ include file="footer.jsp" %>