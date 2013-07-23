//<start id="ne-setup"/>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ page import="org.apache.chemistry.opencmis.client.api.*" %>
<%@ page import="org.apache.chemistry.opencmis.commons.enums.*" %>
<%@ page import="java.util.*" %>
<%@ include file="header.jsp"  %>
 
  <h1>Rename object</h1> //<co id="ch8_fig_jsp_rename0" />

  <form method="POST" action="rename"> 
    Object path:
    <input type="text" size="20" name="path"><br>
    New name: <input type="text" size="20" name="name"><br>
    <input type="submit" value="rename">
  </form> 

<%@ include file="footer.jsp" %>
//<end id="ne-setup"/>

