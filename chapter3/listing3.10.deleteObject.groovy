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
import org.apache.chemistry.opencmis.commons.*
import org.apache.chemistry.opencmis.commons.data.*
import org.apache.chemistry.opencmis.client.api.*
//<start id="deleteDocument"/> 
import org.apache.chemistry.opencmis.commons.enums.*
import org.apache.chemistry.opencmis.
         commons.exceptions.CmisObjectNotFoundException;

def targetPath = "/my first folder/potts_contract.docx"
def someDoc
try {
    someDoc = session.
        getObjectByPath(targetPath)
} catch (CmisObjectNotFoundException confe) {//<co id="catchException" />
    println("Could not find document to delete: " + targetPath)
    return
}

println("id:" + someDoc.id)
println("name:" + someDoc.name)

if (!someDoc.latestVersion) {
    someDoc = someDoc.getObjectOfLatestVersion(false)
}

someDoc.delete(true)//<co id="deleteDoc" />
//<end id="deleteDocument"/>