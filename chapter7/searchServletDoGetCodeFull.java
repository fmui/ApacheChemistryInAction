//<start id="ne-setup"/>
protected void doGet(HttpServletRequest request,
    HttpServletResponse response, Session session)
    throws ServletException, IOException, TheBlendException {

String q = getStringParameter(request, "q");

if (q != null) {  //<co id="search-query-for-the-first-time" /> 
  request.setAttribute("q", q);

  QueryStatement stmt = 
  session.createQueryStatement(
    "SELECT cmis:objectId, cmis:name FROM cmis:document " +
    "WHERE cmis:name LIKE ?");
  stmt.setStringLike(1, "%" + q + "%");

  ItemIterable<QueryResult> queryResults = stmt.query(false);

  ItemIterable<QueryResult> page = queryResults.skipTo(0).getPage(10); //<co id="search-get-first-10-results" /> 

  LinkedHashMap<String, String> results = 
    new LinkedHashMap<String, String>(); 

  try {
    for (QueryResult result : page) {
      String docId = 
        result.getPropertyValueByQueryName("cmis:objectId");
      String name = 
        result. getPropertyValueByQueryName("cmis:name");
      results.put(docId, name);
    }
  }
  catch (CmisBaseException cbe) {
    throw new TheBlendException("Could not perform query!");
  }

  request.setAttribute("results", results);
} 

// --- show the search page ---
dispatch("search.jsp", "Search. The Blend.",
  request, response);
}
//<end id="ne-setup"/>

