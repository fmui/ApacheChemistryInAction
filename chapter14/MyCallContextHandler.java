package com.manning;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.chemistry.opencmis.commons.server.CallContext;
import org.apache.chemistry.opencmis.server.shared.CallContextHandler;

//<start id="ne-setup"/>
public class MyCallContextHandler implements CallContextHandler {

  @Override
  public Map<String, String> 
    getCallContextMap(HttpServletRequest request) {

    String user = ...
    String password = ...

    Map<String, String> callContextMap = 
      new HashMap<String, String>();

    callContextMap.put(CallContext.USERNAME, user);
    callContextMap.put(CallContext.PASSWORD, password);

    return callContextMap;
  }
}//<end id="ne-setup"/>