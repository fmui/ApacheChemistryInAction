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
%>

<h1>Rename <%= HTMLHelper.format(cmisObject.getName()) %></h1>

<div class="cmisdetail cmisservicedetail">CMIS Service: <code>updateProperties</code></div>

<form method="POST">
New Name: 
<input type="text" name="newname" size="50" value="<%= HTMLHelper.formatAttribute(cmisObject.getName()) %>">
<input type="hidden" name="id" value="<%= HTMLHelper.formatAttribute(cmisObject.getId()) %>">
<br><br>
<input type="submit" value="Rename"> <input type="button" value="Cancel" onclick="window.history.back()" />
</form>

<%@ include file="footer.jsp" %>