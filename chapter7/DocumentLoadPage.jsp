//<start id="ne-setup"/>
<%
  Document doc = (Document) request.getAttribute("document"); //<co id="doc-obj-here" /> 
  Set<Action> allowableActions = 
     doc.getAllowableActions().getAllowableActions(); //<co id="extract-allowable-actions" /> 
  String thumbnailStreamId = (String) request.getAttribute("thumbnail");
%>

<h1><%= doc.getName() %></h1>

<% if (thumbnailStreamId != null) { %>
  <img src="download?id=<%= doc.getId() %>&stream=
     <%= thumbnailStreamId %>">
<% } %>

<% if (doc.getContentStreamLength() > 0) { %>
  <% if (allowableActions.contains(Action.CAN_GET_CONTENT_STREAM)){ %> //<co id="check-user-access" /> 
    <a href="download?id=<%= doc.getId() %>">download</a>
  <% } %>
<% } %>


<h2>Paths</h2>

<ul>
<% for (String path: doc.getPaths()) { %>
  <li><%= path %></li>
<% } %>
</ul>


<h2>Allowable Actions</h2>

<ul>
<% for (Action action: allowableActions) { %>
  <li><%= action.value() %></li>
<% } %>
</ul>


<h2>Properties</h2>

<table>
<% for (Property<?> prop: doc.getProperties()) { %>
<tr>
  <td><%= prop.getDefinition().getDisplayName() %></td>
  <td>
    <% if (prop.isMultiValued()) { %> //<co id="is-multivalue-prop" /> 
      <ul>
        <% if (prop.getValues() != null) { %>
          <% for(Object value: prop.getValues()) { %>
            <li><%= value %></li>
          <% } %> 
        <% } %>
      </ul>
    <% } else { %>
      <%= prop.getValue() %>
    <% } %>
</tr>
<% } %>
</table>
//<end id="ne-setup"/>

