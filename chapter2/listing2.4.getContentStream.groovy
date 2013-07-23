//Copyright 2012 Manning Publications Co.
//
//  Licensed under the Apache License, Version 2.0 (the "License");
//  you may not use this file except in compliance with the License.
//  You may obtain a copy of the License at
//
//    http://www.apache.org/licenses/LICENSE-2.0
//
//  Unless required by applicable law or agreed to in writing, software
//  distributed under the License is distributed on an "AS IS" BASIS,
//  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
//  implied.
//  See the License for the specific language governing permissions and
//  limitations under the License.
//
//<start id="ne-setup"/> 
import org.apache.chemistry.opencmis.commons.*
import org.apache.chemistry.opencmis.commons.data.*
import org.apache.chemistry.opencmis.commons.enums.*
import org.apache.chemistry.opencmis.client.api.*

// obtain the root folder object
Folder rootFolder = session.getRootFolder();
count = 0

// iterate through the children
for (t in rootFolder.getChildren()) {
   if (t.getBaseTypeId().equals(BaseTypeId.CMIS_DOCUMENT)) {  //<co id="verify-obj-is-document" />
      count +=1;
      println("name:" + t.getName());  
      Document d = (Document) t;
      String mimeType = d.getContentStreamMimeType();
      if ((mimeType != null) && (d.getContentStreamLength() > 0)) {
         if (mimeType.startsWith("text")) {  //<co id="verify_stream_is_text" />
            println("Name of doc:" + d.getName());  //<co id="note_name_and_filename" />
            println("FileName:" + 
              d.getContentStreamFileName());
            println("Stream length:" + 
              d.getContentStreamLength());
            String fullStream = 
              getContentAsString(d.getContentStream());                 
            println("\nFirst line of stream:\n->" + 
              fullStream.substring(0, fullStream.indexOf("\n")));
         }
      }
   }
   if (count > 0) {
      break; // we can stop after the first one is found
   }
}

//<co id="helper_from_chemistry" />
private static String getContentAsString(ContentStream stream)
    throws IOException {
    StringBuilder sb = new StringBuilder();
    Reader reader = new InputStreamReader(stream.getStream(),"UTF-8");
 
    try {
        final char[] buffer = new char[4 * 1024];
        int b;
        while (true) {
            b = reader.read(buffer, 0, buffer.length);
            if (b > 0) {
                sb.append(buffer, 0, b);
            } else if (b == -1) {
                break;
            }
        }
    } finally {
        reader.close();
    } 
    return sb.toString();
 }
//<end id="ne-setup"/>

// Helper method to get the contents of a stream taken from the 
// "OpenCMIS Client API Developer's Guide" on
// http://chemistry.apache.org/java/developing/guide.html