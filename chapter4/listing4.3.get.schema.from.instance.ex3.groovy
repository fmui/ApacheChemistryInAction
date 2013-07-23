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
import org.apache.chemistry.opencmis.commons.definitions.*

// obtain the root folder instance object from the session
   Folder rootFolder = session.getRootFolder();
  
// this is how you get its type directly from the instance object
ObjectType typeObj = rootFolder.getType(); //<co id="get-type-from-object" /> 
println("Id of folder's type:" + typeObj.getId());

int DefCount = typeObj.getPropertyDefinitions().entrySet().size();
println("Prop definition total:" + DefCount);

// how to get property definitions directly from the property instance
// by just looking at the defs for the properties that are present
List<Property<?>> props = rootFolder.getProperties();
int propCount = props.size(); //<co id="prop-size-and-def-size" /> 
println("Property count:" + propCount);
for (prop in props) {
   PropertyDefinition<?> propDef = prop.getDefinition(); //<co id="get-propdef-from-prop" /> 
   println("  property:" + prop.getDisplayName() + 
         " id[" + propDef.getId() + "]");
}    
//<end id="ne-setup"/>
