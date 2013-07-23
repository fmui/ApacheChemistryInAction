//<start id="first-open-cmis-activity" />
public class FirstOpenCMISActivity extends Activity {

@Override
public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_opencmis);
    new FirstOpenCMISAsyncTask().execute();
}

private class FirstOpenCMISAsyncTask
  extends AsyncTask<Void, Void, String> {

@Override
protected String doInBackground(Void... arg0) {

    // Initiates a Session Factory
    SessionFactory sessionFactory = SessionFactoryImpl.newInstance();

    // Initiates connection session parameters.
    Map<String, String> parameter = new HashMap<String, String>();
    parameter.put(SessionParameter.USER, "admin");
    parameter.put(SessionParameter.PASSWORD, "admin");
    parameter.put(
      SessionParameter.ATOMPUB_URL,
      "http://192.168.1.36:8081/inmemory/atom/");//<co id="local-ip" />
    parameter.put(
      SessionParameter.BINDING_TYPE,
      BindingType.ATOMPUB.value());

    // Retrieves repository information and create the session object.
    Repository repository = sessionFactory.getRepositories(parameter)
      .get(0);
    parameter.put(SessionParameter.REPOSITORY_ID, repository.getId());
    Session session = sessionFactory.createSession(parameter);

    // Retrieves media folder and list all its children.
    String listChildren = "";
    Folder mediaFolder = (Folder) session.getObjectByPath("/media");
    ItemIterable<CmisObject> children = mediaFolder.getChildren();
    for (CmisObject o : children) {
        listChildren += o.getName() +
          " - " +
          o.getType().getDisplayName() +
          " - " +
          o.getCreatedBy() +
          "\b\n";
    }

    return listChildren;
}

@Override
protected void onPostExecute(String result) {
   TextView tv = (TextView) (FirstOpenCMISActivity.this)
     .findViewById(R.id.opencmis_text);
   tv.setText(result);
}}}
//<end id="first-open-cmis-activity" />