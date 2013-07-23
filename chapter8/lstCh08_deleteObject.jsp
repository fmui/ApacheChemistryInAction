//<start id="ne-setup"/>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ page import="org.apache.chemistry.opencmis.client.api.*" %>
<%@ page import="org.apache.chemistry.opencmis.commons.enums.*" %>
<%@ page import="java.util.*" %>
<%@ include file="header.jsp"  %>

<h1>Delete object</h1> //<co id="ch8_fig_jsp_delete0" />

  <form method="POST" action="delete">
    Object path:
    <input type="text" size="20" name="path"><br>
    <input type="submit" value="delete">
  </form>

<%@ include file="footer.jsp" %>
//<end id="ne-setup"/>

