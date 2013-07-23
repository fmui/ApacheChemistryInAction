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
import org.apache.chemistry.opencmis.client.api.*
import org.apache.chemistry.opencmis.commons.enums.*
import org.apache.chemistry.opencmis.commons.definitions.* 

ObjectType complex = session.getTypeDefinition("cmisbook:audio"); // <co id="get-type-directly" />
printPropDefsForTypeWithWithContraints(complex, "");

static void printPropDefsForTypeWithWithContraints(ObjectType type,
      String tab) {
   Map<String, PropertyDefinition<?>> mapDefs = type
         .getPropertyDefinitions();
   for (key in mapDefs.keySet()) {
      print(tab + "     " + key + "->");
      PropertyDefinition defn = mapDefs.get(key);
      print(" Id:[" + defn.getId() + "]");
      print(" dataType:[" + defn.getPropertyType() + "]");
      println(" updateable:["+defn.getUpdatability()+"]");
      
      // show min max constraint test on integer type
      if (defn.getPropertyType().equals(PropertyType.INTEGER)) { // <co id="type-specific-constraints"/>
         PropertyIntegerDefinition propDefInt = 
               (PropertyIntegerDefinition) defn;
         if (propDefInt.getMaxValue() != null) {
            println("      Max value:" 
              + propDefInt.getMaxValue());
         }
      }

      // list default value if present
      if (defn.getDefaultValue() != null) {
         println("       default value:["
               + defn.getDefaultValue().get(0) + "]"); // <co id="assume-single-value"/>
      }

      // list choices if present
      if (defn.getChoices().size() > 0) {
         // there are choices on this property
         print("       choice present: values:[");
         List<Choice> choices = defn.getChoices();
         Cardinality card = defn.getCardinality();
         for (choice in choices) {
            if (card.equals(Cardinality.SINGLE)) {
               print(choice.getValue().get(0) + " "); // <co id="discuss-choice-hierarchy"/>
            } else {
               // code to iterate through all values in 
               // choice.getValue() if this was a 
               // multivalued choice.
            }
         }
         println("]");
      }
   }
}
//<end id="ne-setup"/>