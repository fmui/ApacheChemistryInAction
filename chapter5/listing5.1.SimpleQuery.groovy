//Copyright 2012 Manning Publications Co.
//
//   Licensed under the Apache License, Version 2.0 (the "License");
//   you may not use this file except in compliance with the License.
//   You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
//   Unless required by applicable law or agreed to in writing, software
//   distributed under the License is distributed on an "AS IS" BASIS,
//   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//   See the License for the specific language governing permissions and
//   limitations under the License.
//
// <start id="ne-setup"/>
import org.apache.chemistry.opencmis.commons.*
import org.apache.chemistry.opencmis.commons.data.*
import org.apache.chemistry.opencmis.client.api.*

RepositoryInfo info = session.getRepositoryInfo();

RepositoryCapabilities caps = 
  session.getRepositoryInfo().getCapabilities();
println("Query capability=" + caps.getQueryCapability());//<co id="check-query-capability" />

String query = "SELECT * FROM cmis:document";   //<co id="define-query-same-as-before" />
boolean searchAllVersions = false;
int count = 1;
ItemIterable<QueryResult> queryResult = 
    session.query(query, searchAllVersions);  //<co id="session-query-discussion" />
for (qr in queryResult) {

  println("--------------------------");
  println("");
  println(count + ": "
    + qr.getPropertyByQueryName("cmis:objectTypeId")
      .getFirstValue() + " , "  //<co id="get-first-value" />
    + qr.getPropertyByQueryName("cmis:name")
      .getFirstValue() + " , "
    + qr.getPropertyByQueryName("cmis:createdBy")
      .getFirstValue() + " , "
    + qr.getPropertyByQueryName("cmis:objectId")
      .getFirstValue() + " , "
    + qr.getPropertyByQueryName("cmis:contentStreamFileName")
      .getFirstValue() + " , "
    + qr.getPropertyById("cmis:contentStreamLength") //<co id="get-prop-by-id" />
      .getFirstValue());

    // limit the output to 5 results
    if (count++ >= 5) break;  
    
} 
//<end id="ne-setup"/>
