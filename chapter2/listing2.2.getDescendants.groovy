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

RepositoryInfo info = session.getRepositoryInfo();
RepositoryCapabilities caps = 
  session.getRepositoryInfo().getCapabilities();

//<co id="get-root-folder" />
Folder rootFolder = session.getRootFolder();

if (!caps.isGetDescendantsSupported()) {
    println("\n Warning: getDescendants " +
            "not supported in this repository");
} else {
    println("\ngetDescendants " +
            "is supported on this repository.");

    println("\nDescendants of " + 
      rootFolder.getName() + " : ");
    for (t in rootFolder.getDescendants(-1)) {
        printTree(t , "");
    }
}  

private static void printTree(Tree<FileableCmisObject> tree, 
       String tab) {
    println(tab + "Descendant "+ tree.getItem().getName());
    for (t in tree.getChildren()) {
        printTree(t, tab + "  ");
    }
}
//<end id="ne-setup"/>