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
<%@ page import="org.apache.chemistry.opencmis.client.api.*" %>
<%@ page import="java.util.List" %>
<%@ include file="header.jsp" %>

<%
	String message = (String) request.getAttribute("message");
	Throwable ex = (Throwable) request.getAttribute("exception");
%>

<div class="monospace" style="font-size: 60px;">Error.</div>
<div style="font-size: 16px; color: #FF3333"><%= message %></div>

<% if (ex != null) { %>
  <br><div style="font-size: 16px;">Exception: <%= ex.getMessage() %></div>
  <pre><% ex.printStackTrace(new java.io.PrintWriter(out)); %></pre>
<% } %>

<div>
<br><input type="button" value="Back" onclick="window.history.back()" />
</div>

<%@ include file="footer.jsp" %>