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

//<co id="java-or-groovy" />
println("");
println("Abbreviated repository info:");
println("  Name: " + info.getName());
println("  ID: " + info.getId());
println("  Product name: " + info.getProductName());
println("  Product version: " + info.getProductVersion());
println("  Version supported: " + info.getCmisVersionSupported());

RepositoryCapabilities caps = 
  session.getRepositoryInfo().getCapabilities();
println("");
println("Brief capabilities report:");
println("  Query: " + caps.getQueryCapability());
println("  GetDescendants: " + caps.isGetDescendantsSupported());
println("  GetFolderTree: " + caps.isGetFolderTreeSupported());
//<end id="ne-setup"/>