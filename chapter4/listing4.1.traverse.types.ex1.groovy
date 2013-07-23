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
import org.apache.chemistry.opencmis.client.api.*
import org.apache.chemistry.opencmis.commons.enums.*

      boolean includePropertyDefinitions = true;
      for (t in session.getTypeDescendants(
            null,                      // start at the top of the tree
            -1,                        // infinite depth recursion
            includePropertyDefinitions  // include prop defs 
            )) {
         printTypes(t, "");
      }
   

   static void printTypes(Tree<ObjectType> tree, String tab) { //<co id="get-type-descendants" /> 
      ObjectType objType =  tree.getItem();
      println(tab + "TYPE:" + objType.getDisplayName() + 
            " (" + objType.getDescription() + ")");
      // Print some of the common attributes for this type
      print(tab + "   Id:" + objType.getId()); //<co id="common-attributes" />
      print(" Fileable:" + objType.isFileable());
      print(" Queryable:" + objType.isQueryable());
      
      if (objType instanceof DocumentType) { //<co id="document-only-attributes" />
         print(" [DOC Attrs->] Versionable:" + 
            ((DocumentType)objType).isVersionable());        
         print(" Content:" + 
            ((DocumentType)objType).getContentStreamAllowed());
      }
      println("");  // end the line
      for (t in tree.getChildren()) {
         // there are more - call self for next level
         printTypes(t, tab + "  ");  
      }
   }

   //<end id="ne-setup"/>
