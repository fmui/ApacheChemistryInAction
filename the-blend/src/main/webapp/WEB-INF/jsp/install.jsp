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
<%@ page import="com.manning.cmis.theblend.util.HTMLHelper"%>
<%@ page import="org.apache.chemistry.opencmis.client.api.*"%>
<%@ page import="java.util.List" %>
<%@ include file="header.jsp"  %>

<h1>Installation</h1>

Please login. This user must have write permissions.<br>
Make sure that the session parameters are set correctly. The application root folder will be created if it doesn't exist.<br>
The installation may take a moment!<br>
<br>

<table>
<form method="POST" action="">
	<tr><td>Username:</td><td><input type="text" name="username"></td></tr>
    <tr><td>Password:</td><td><input type="password" name="password"></td></tr>
	<tr><td></td><td><input type="submit" value="Install"> <input type="button" value="Cancel" onclick="window.history.back()" /></td></tr>
</form>
</table>


<%@ include file="footer.jsp" %>