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
<%@ include file="header.jsp"  %>

<div class="monospace" style="text-align: center; margin: 30px;">
<div style="font-size: 15px;">Mix it. Mash it. Blend it. Rock it.</div>
<div style="font-size: 60px;">The Blend.</div>
<div style="font-size: 23px;">Organize your Creativity.</div>
</div>

<div class="monospace" style="text-align: center;">

<form method="POST" action="">
	Your Username:<br>
	<input type="text" name="username"><br>
    Your Password:<br>
	<input type="password" name="password"><br>
	<input type="submit" value="Login">
</form>

</div>

<br><br><br><br><br>

<div style="text-align: center;">

<div style="width: 700px; padding: 10px; margin-left: auto; margin-right: auto; background: #DDDDDD; border:2px dashed gray; text-align: left;">
<div style="font-weight: bold;">Setup Instructions</div>
<ul>
  <li>Create a folder in your repository that will become the application root folder.</li>
  <li>Make sure that the user you want use has read-write permissions.</li>
  <li>Find the class <code>com.manning.cmis.theblend.session.OpenCMISSessionFactory</code> .</li>
  <li>Update the OpenCMIS session parameters.</li>
  <li>Update the path to the application root folder.</li>
  <li>Restart the application.</li>
  <li>Click <a href="install">here</a> to upload demo data.</li>
</ul>
</div>

<br><br><br><br><br>

</div>

<%@ include file="footer.jsp" %>
