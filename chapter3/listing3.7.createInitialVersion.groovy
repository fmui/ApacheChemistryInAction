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

//<start id="createInitialVersion"/> 
cmis = new scripts.CMIS(session)
 
def someFolder = session.getObjectByPath('/my first folder')
             
def f = new File('/users/jpotts/Documents/sample/potts_contract.docx')//<co id="sampleDoc" />

def someDoc = cmis.createDocumentFromFile(someFolder,
                                          f,
                                          "cmisbook:officeDocument",//<co id="versionableType" />
                                          VersioningState.MAJOR)//<co id="versioningState" />

println("Doc created!")
println("Id:" + someDoc.id)
println("Name:" + someDoc.name)
println("Length:" + someDoc.contentStreamLength)
println("Version:" + someDoc.versionLabel)//<co id="versionInfo" />
println("Is Latest?" + someDoc.latestVersion)
println("Is Major?" + someDoc.majorVersion)
//<end id="createInitialVersion"/>
