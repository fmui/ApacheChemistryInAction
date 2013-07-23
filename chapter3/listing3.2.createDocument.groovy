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
import org.apache.chemistry.opencmis.commons.enums.*
import org.apache.chemistry.opencmis.client.api.*

//<start id="createDocument"/>  
def someFolder = session.getObjectByPath('/my first folder')//<co id="getFolder" />

// create a map of properties
def props = ['cmis:objectTypeId': 'cmis:document',//<co id="documentType" />
             'cmis:name' : 'my test doc']

def someDoc = someFolder.createDocument(props, null, null)//<co id="createDocumentMethod" />

println("Doc created!")
println("id:" + someDoc.id)
println("name:" + someDoc.name)
//<end id="createDocument"/>
