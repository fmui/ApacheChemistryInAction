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

//<start id="checkinNewVersion"/> 
def someDoc = session.
		getObjectByPath("/my first folder/potts_contract.docx")

println("id:" + someDoc.id)
println("name:" + someDoc.name)

if (!someDoc.latestVersion) {//<co id="checkDoc" />
    someDoc = someDoc.getObjectOfLatestVersion(false)
}

println("Version:" + someDoc.versionLabel)
println("Is Major?" + someDoc.majorVersion)

def pwcId
if (someDoc.versionSeriesCheckedOut) {
    pwcId = someDoc.versionSeriesCheckedOutId//<co id="idOfPwc" />
} else {
    pwcId = someDoc.checkOut()//<co id="docNeededCheckout" />
    someDoc.refresh()
}
def pwc = session.getObject(pwcId)

println("Checked out?" + someDoc.versionSeriesCheckedOut)
println("Checked out by:" +
		  someDoc.versionSeriesCheckedOutBy)//<co id="whoCheckedOut" />

def f = new File('/users/jpotts/Desktop/potts_contract.docx')//<co id="modifiedDoc" />

def name = f.getName()

def mimetype = someDoc.contentStreamMimeType

def contentStream = session.getObjectFactory().createContentStream(name,
                                          f.size(),
                                          mimetype,
                                          new FileInputStream(f))

def newDocId = pwc.checkIn(false,//<co id="checkin" />
			   null,
			   contentStream,
			   "Made a minor change")

println("Checked in new version")

def newDoc = session.getObject(newDocId)
newDoc.refresh()
println("Version:" + newDoc.versionLabel)
println("Is Latest?" + newDoc.latestVersion)
println("Is Major?" + newDoc.majorVersion)
//<end id="checkinNewVersion"/> 
