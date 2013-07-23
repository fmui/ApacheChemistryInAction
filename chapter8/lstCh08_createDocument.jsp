//<start id="ne-setup"/>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ page import="org.apache.chemistry.opencmis.client.api.*" %>
<%@ page import="org.apache.chemistry.opencmis.commons.enums.*" %>
<%@ page import="java.util.*" %>
<%@ include file="header.jsp"  %>

   <h1>Create new document</h1> //<co id="ch8_fig_html_createdoc0" />

   <form method="POST" action="add" enctype="multipart/form-data"> //<co id="ch8_fig_html_createdoc1" />
     Path to the parent folder:
     <input type="text" size="20" name="path"><br>
     Document name: 
     <input type="text" size="20" name="name"><br>
     Document type: 
     <input type="text" size="20" name="type" value="cmis:document"><br>
     File:
     <input name="content" type="file"><br> //<co id="ch8_fig_html_createdoc2" />
     <input type="submit" value="create">
   </form>

<%@ include file="footer.jsp" %>
//<end id="ne-setup"/>

