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
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ page import="org.apache.chemistry.opencmis.client.api.*" %>
<%@ page import="org.apache.chemistry.opencmis.commons.enums.*" %>
<%@ page import="java.util.*" %>
<%@ include file="header.jsp"  %>

<div class="monospace" style="text-align: center;">

<% 
Session cmisSession = (Session) request.
                                  getSession().
                                  getAttribute("session");
if(cmisSession != null) {%>
    <ul>
		<li><%=cmisSession.getRepositoryInfo().getVendorName() %></li>
    	<li><%=cmisSession.getRepositoryInfo().getProductName() %></li>
		<li><%=cmisSession.getRepositoryInfo().getProductVersion() %></li>    	
    </ul>
<% 
}
%>

</div>

<br><br><br><br><br>

<%@ include file="footer.jsp" %>