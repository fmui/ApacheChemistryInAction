package com.manning;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.chemistry.opencmis.client.bindings.spi.StandardAuthenticationProvider;

//<start id="ne-setup"/>
public class ExampleAuthenticationProvider extends
    StandardAuthenticationProvider {

  private static final long serialVersionUID = 1L;

  @Override
  public Map<String, List<String>> getHTTPHeaders(String url) {

    //<co id="ch12_ap_1" />
    Map<String, List<String>> headers = super.getHTTPHeaders(url); 
    if (headers == null) {
      headers = new HashMap<String, List<String>>();
    }

    //<co id="ch12_ap_2" /> 
    Object exampleUserObject =
      getSession().get("org.example.user");
    if (exampleUserObject instanceof String) {
      headers.put("example-user",
          Collections.singletonList((String) exampleUserObject));
    }

    //<co id="ch12_ap_3" />
    Object exampleSecretObject = 
      getSession().get("org.example.secret");
    if (exampleSecretObject instanceof String) {
      headers.put("example-secret",
          Collections.singletonList((String) exampleSecretObject));
    }

    return headers;
  }
}//<end id="ne-setup"/>