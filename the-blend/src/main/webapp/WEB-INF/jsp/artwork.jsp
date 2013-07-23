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
	String artworkId = (String) request.getAttribute("artwork");
%>

<h1>Change Artwork of <%= HTMLHelper.format(cmisObject.getName()) %></h1>

<!-- artwork -->
<% if (artworkId != null) { %>
<div>
Current Artwork:<br>
<div class="cmisdetail cmisservicedetail">CMIS Service: <code>getContentStream</code></div>
<img src="<%= HTMLHelper.encodeUrlWithId(request, "download", artworkId) %>">
<br><br>
</div>
<% } %>

<div class="cmisdetail cmisservicedetail">CMIS Service: <code>updateProperties</code></div>

<form method="POST">
<input type="hidden" name="id" value="<%= HTMLHelper.formatAttribute(cmisObject.getId()) %>">
<table>
<tr>
  <td><input type="radio" id="awid" name="what" value="id" checked> <label for="awid">Artwork Id:</label></td>
  <td><input type="text" name="artworkid" size="50" value="<%= HTMLHelper.formatAttribute(artworkId) %>"></td>
</tr>
<tr>
  <td><input type="radio" id="awpath" name="what" value="path"> <label for="awpath">Artwork Path:</label></td>
  <td><input type="text" name="artworkpath" size="50"></td>
</tr>
<tr>
  <td><input type="radio" id="awrm" name="what" value="remove"> <label for="awrm">Remove Artwork</label></td>
  <td></td>
</tr>
</table>
<br>
<input type="submit" value="Update"> <input type="button" value="Cancel" onclick="window.history.back()" />
</form>

<%@ include file="footer.jsp" %>