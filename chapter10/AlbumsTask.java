//<start id="albums-task" />
public class AlbumsTask
  extends AsyncTask<Void, Void, CmisResult<List<Document>>> {
  
    private static final String QUERY_ALL_ALBUMS =
      "SELECT * FROM cmis:document" .
      " where cmis:objectTypeId = 'cmisbook:album'";

...

   @Override
   protected CmisResult<List<Document>> doInBackground(Void... arg0) {
        List<Document> listAlbums = null;
        Exception exception = null;

        // Try to execute a CMIS Query
        // to retrieve all albums from the Server.
        try {
            ItemIterable<QueryResult> results = 
              session.query(QUERY_ALL_ALBUMS, false);
            listAlbums = new ArrayList<Document>(
              (int) results.getTotalNumItems());
            Document album = null;

            // Create a list of Albums (Document object)
            // based on the result.
            for (QueryResult result : results) {
                album = (Document) session.getObject(
                  session.createObjectId((String) result.
                    getPropertyById(PropertyIds.OBJECT_ID).
                    getFirstValue()));
                listAlbums.add(album);
            }
        } catch (Exception e) {
            exception = e;
        }
        return new CmisResult<List<Document>>(exception, listAlbums);
    }

    @Override
    protected void onPostExecute(CmisResult<List<Document>> results) {//<co id="on-post-execute" />
        // In case of exception, displays
        // informations for debugging purpose.
        if (results.hasException()) {
            Toast.makeText(
              activity,
              results.getException().getMessage(),
              Toast.LENGTH_LONG).show();//<co id="show-exception-message" />
            Log.e(TAG, Log.getStackTraceString(results.getException()));
        } else if (activity instanceof AlbumsActivity) {
            // Display albums inside the listview.
            ((AlbumsActivity) activity).listAlbums(results.getData());
        }
    }
...

}
//<end id="albums-task" />