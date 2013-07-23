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
//<start id="ne-setup"/>  
import org.apache.chemistry.opencmis.commons.*
import org.apache.chemistry.opencmis.commons.data.*
import org.apache.chemistry.opencmis.commons.enums.*
import org.apache.chemistry.opencmis.client.api.*

// obtain the root folder object
Folder rootFolder = session.getRootFolder();
foundCount = 0;

for (t in rootFolder.getChildren()) {
  // until we find an object that is a doc type or subtype
  if (t instanceof Document) {
    println("name:" + t.getName());
    foundCount += 1;
    List<Property<?>> props = t.getProperties();
    
    // list all of the system properties that is those 
    // that begin with the cmis: prefix we listed earlier
    for (p in props) {
      if (p.getId().startsWith("cmis:")) {
        println("  " + p.getDefinition().getId()
            + "=" + p.getValuesAsString());
          }
        }
  }
  if (foundCount > 0) {
    break;   // we can stop after the first one is found
  }
}
//<end id="ne-setup"/>