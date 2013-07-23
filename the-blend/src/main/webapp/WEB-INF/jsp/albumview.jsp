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
<%@ page import="com.manning.cmis.theblend.util.HTMLHelper" %>
<%@ page import="org.apache.chemistry.opencmis.client.api.*" %>
<%@ page import="java.util.List" %>

<%
	Document doc = (Document) request.getAttribute("document");
	List<Document> tracks = (List<Document>) request.getAttribute("tracks");
%>

<div class="objectbox">
<table>
<tr>
  <th style="width: 20px">Track</th>
  <th style="width: 500px">Title</th>
</tr>  
<% int pos = 0;
   for (Document track: tracks) { %>
<tr>
  <td style="text-align: right;"><%= ++pos %></td>
  <td><a href="<%= HTMLHelper.encodeUrlWithId(request, "show", track.getId()) %>"><%= HTMLHelper.format(track.getName()) %></a></td>
</tr>
<% } %>
</table>

<div style="text-align: right;">
<a class="actionlink" href="<%= HTMLHelper.encodeUrlWithId(request, "album", doc.getId()) %>">Edit</a><br>
</div>
</div>