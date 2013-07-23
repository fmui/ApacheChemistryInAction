package com.manning;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.MessageContext.Scope;

import org.apache.chemistry.opencmis.commons.server.CallContext;
import org.apache.chemistry.opencmis.server.impl.webservices.AbstractService;

import com.sun.xml.ws.api.handler.MessageHandler;
import com.sun.xml.ws.api.handler.MessageHandlerContext;

//<start id="ne-setup"/>
public class MyAuthHandler implements 
  MessageHandler<MessageHandlerContext> {

  public Set<QName> getHeaders() {
    return null;
  }

  public void close(MessageContext context) {
  }

  public boolean handleFault(MessageHandlerContext context) {
    return true;
  }

  public boolean handleMessage(MessageHandlerContext context) {
    Boolean outboundProperty = (Boolean) context
        .get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
    if (outboundProperty.booleanValue()) {
      return true;
    }

    Map<String, String> callContextMap = 
      new HashMap<String, String>();

    String user = ...
    String password = ...

    callContextMap.put(CallContext.USERNAME, user);
    callContextMap.put(CallContext.PASSWORD, password);

    context.put(AbstractService.CALL_CONTEXT_MAP, 
      callContextMap); //<co id="ch14_co_authhandler1"/>

    context.setScope(AbstractService.CALL_CONTEXT_MAP, 
      Scope.APPLICATION);

    return true;
  }
}//<end id="ne-setup"/>