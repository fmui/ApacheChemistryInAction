<?php
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
# 
# http://www.apache.org/licenses/LICENSE-2.0
# 
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.

define("HTTP_OK", 200);
define("HTTP_CREATED", 201);
define("HTTP_ACCEPTED", 202);
define("HTTP_NONAUTHORITATIVE_INFORMATION", 203);
define("HTTP_NO_CONTENT", 204);
define("HTTP_RESET_CONTENT", 205);
define("HTTP_PARTIAL_CONTENT", 206);
define("HTTP_MULTIPLE_CHOICES", 300);
define("HTTP_BAD_REQUEST", 400); // invalidArgument, filterNotValid
define("HTTP_UNAUTHORIZED", 401);
define("HTTP_FORBIDDEN", 403); // permissionDenied, streamNotSupported
define("HTTP_NOT_FOUND", 404); // objectNotFound
define("HTTP_METHOD_NOT_ALLOWED", 405); // notSupported
define("HTTP_NOT_ACCEPTABLE", 406);
define("HTTP_PROXY_AUTHENTICATION_REQUIRED", 407);
define("xHTTP_REQUEST_TIMEOUT", 408); //Had to change this b/c HTTP_REQUEST_TIMEOUT conflicts with definition in Drupal 7
define("HTTP_CONFLICT", 409); // constraint, contentAlreadyExists, versioning, updateConflict, nameConstraintViolation
define("HTTP_UNSUPPORTED_MEDIA_TYPE", 415);
define("HTTP_UNPROCESSABLE_ENTITY", 422);
define("HTTP_INTERNAL_SERVER_ERROR", 500); // runtime, storage

class CmisInvalidArgumentException extends Exception {}
class CmisObjectNotFoundException extends Exception {}
class CmisPermissionDeniedException extends Exception {}
class CmisNotSupportedException extends Exception {}
class CmisNotImplementedException extends Exception {}
class CmisConstraintException extends Exception {}
class CmisRuntimeException extends Exception {}

class CMISRepositoryWrapper
{
    // Handles --
    //   Workspace -- but only endpoints with a single repo
    //   Entry -- but only for objects
    //   Feeds -- but only for non-hierarchical feeds
    // Does not handle --
    //   -- Hierarchical Feeds
    //   -- Types
    //   -- Others?
    // Only Handles Basic Auth
    // Very Little Error Checking
    // Does not work against pre CMIS 1.0 Repos
    
    var $url;
    var $username;
    var $password;
    var $authenticated;
    var $workspace;
    var $last_request;
    var $do_not_urlencode;
    protected $_addlCurlOptions = array();
    
    static $namespaces = array (
        "cmis" => "http://docs.oasis-open.org/ns/cmis/core/200908/",
        "cmisra" => "http://docs.oasis-open.org/ns/cmis/restatom/200908/",
        "atom" => "http://www.w3.org/2005/Atom",
        "app" => "http://www.w3.org/2007/app",
        
    );

    function __construct($url, $username = null, $password = null, $options = null, array $addlCurlOptions = array())
    {
        if (is_array($options) && $options["config:do_not_urlencode"]) {
            $this->do_not_urlencode=true;
        }
        $this->_addlCurlOptions = $addlCurlOptions; // additional cURL options
        
        $this->connect($url, $username, $password, $options);
    }
    static function getAsArray($prop) {
    	if ($prop == null) {
			return array();
		} elseif (!is_array($prop)) {
			return array($prop);
		} else {
			return($prop);
		}
		    	
    }

    static function getOpUrl($url, $options = null)
    {
        if (is_array($options) && (count($options) > 0))
        {
            $needs_question = strstr($url, "?") === false;
            return $url . ($needs_question ? "?" : "&") . http_build_query($options);
        } else
        {
            return $url;
        }
    }
    
    function convertStatusCode($code, $message)
    {
        switch ($code) {
            case HTTP_BAD_REQUEST:
                return new CmisInvalidArgumentException($message, $code);
            case HTTP_NOT_FOUND:
                return new CmisObjectNotFoundException($message, $code);
            case HTTP_FORBIDDEN:
                return new CmisPermissionDeniedException($message, $code);
            case HTTP_METHOD_NOT_ALLOWED:
                return new CmisNotSupportedException($message, $code);
            case HTTP_CONFLICT:
                return new CmisConstraintException($message, $code);
            default:
                return new CmisRuntimeException($message, $code);
            }
    }

    function connect($url, $username, $password, $options)
    {
        // TODO: Make this work with cookies
        $this->url = $url;
        $this->username = $username;
        $this->password = $password;
        $this->auth_options = $options;
        $this->authenticated = false;
        $retval = $this->doGet($this->url);
        if ($retval->code == HTTP_OK || $retval->code == HTTP_CREATED)
        {
            $this->authenticated = true;
            $this->workspace = CMISRepositoryWrapper :: extractWorkspace($retval->body);
        }
    }

    function doGet($url)
    {
        $retval = $this->doRequest($url);
        if ($retval->code != HTTP_OK)
        {
            throw $this->convertStatusCode($retval->code, $retval->body);
        }
        return $retval;
    }

    function doDelete($url)
    {
        $retval = $this->doRequest($url, "DELETE");
        if ($retval->code != HTTP_NO_CONTENT)
        {
            throw $this->convertStatusCode($retval->code, $retval->body);
        }
        return $retval;
    }

    function doPost($url, $content, $contentType, $charset = null)
    {
        $retval = $this->doRequest($url, "POST", $content, $contentType);
        if ($retval->code != HTTP_CREATED)
        {
            throw $this->convertStatusCode($retval->code, $retval->body);
        }
        return $retval;
    }

    function doPut($url, $content, $contentType, $charset = null)
    {
        $retval = $this->doRequest($url, "PUT", $content, $contentType);
        if (($retval->code < HTTP_OK) || ($retval->code >= HTTP_MULTIPLE_CHOICES))
        {
            throw $this->convertStatusCode($retval->code, $retval->body);
        }
        return $retval;
    }

    function doRequest($url, $method = "GET", $content = null, $contentType = null, $charset = null)
    {
        // Process the HTTP request
        // 'til now only the GET request has been tested
        // Does not URL encode any inputs yet
        if (is_array($this->auth_options))
        {
            $url = CMISRepositoryWrapper :: getOpUrl($url, $this->auth_options);
        }
        $session = curl_init($url);
        curl_setopt($session, CURLOPT_HEADER, false);
        curl_setopt($session, CURLOPT_RETURNTRANSFER, true);
        if ($this->username)
        {
            curl_setopt($session, CURLOPT_USERPWD, $this->username . ":" . $this->password);
        }
        curl_setopt($session, CURLOPT_CUSTOMREQUEST, $method);
        if ($contentType)
        {
            curl_setopt($session, CURLOPT_HTTPHEADER, array (
                "Content-Type: " . $contentType
            ));
        }
        if ($content)
        {
            curl_setopt($session, CURLOPT_POSTFIELDS, $content);
        }
        if ($method == "POST")
        {
            curl_setopt($session, CURLOPT_POST, true);
        }
        
        // apply addl. cURL options
        // WARNING: this may override previously set options
        if (count($this->_addlCurlOptions)) {
            foreach ($this->_addlCurlOptions as $key => $value) {
                curl_setopt($session, $key, $value);
            }
        }
        
        
        //TODO: Make this storage optional
        $retval = new stdClass();
        $retval->url = $url;
        $retval->method = $method;
        $retval->content_sent = $content;
        $retval->content_type_sent = $contentType;
        $retval->body = curl_exec($session);
        $retval->code = curl_getinfo($session, CURLINFO_HTTP_CODE);
        $retval->content_type = curl_getinfo($session, CURLINFO_CONTENT_TYPE);
        $retval->content_length = curl_getinfo($session, CURLINFO_CONTENT_LENGTH_DOWNLOAD);
        curl_close($session);
        $this->last_request = $retval;
        return $retval;
    }

    function getLastRequest()
    {
        return $this->last_request;
    }

    function getLastRequestBody()
    {
        return $this->last_request->body;
    }

    function getLastRequestCode()
    {
        return $this->last_request->code;
    }

    function getLastRequestContentType()
    {
        return $this->last_request->content_type;
    }

    function getLastRequestContentLength()
    {
        return $this->last_request->content_length;
    }

    function getLastRequestURL()
    {
        return $this->last_request->url;
    }

    function getLastRequestMethod()
    {
        return $this->last_request->method;
    }

    function getLastRequestContentTypeSent()
    {
        return $this->last_request->content_type_sent;
    }

    function getLastRequestContentSent()
    {
        return $this->last_request->content_sent;
    }

    // Static Utility Functions
    static function processTemplate($template, $values = array ())
    {
        // Fill in the blanks -- 
        $retval = $template;
        if (is_array($values))
        {
            foreach ($values as $name => $value)
            {
                $retval = str_replace("{" . $name . "}", $value, $retval);
            }
        }
        // Fill in any unpoupated variables with ""
        return preg_replace("/{[a-zA-Z0-9_]+}/", "", $retval);

    }

    static function doXQuery($xmldata, $xquery)
    {
        $doc = new DOMDocument();
        $doc->loadXML($xmldata);
        return CMISRepositoryWrapper :: doXQueryFromNode($doc, $xquery);
    }

    static function doXQueryFromNode($xmlnode, $xquery)
    {
        // Perform an XQUERY on a NODE
        // Register the 4 CMIS namespaces
        //THis may be a hopeless HACK!
        //TODO: Review
        if (!($xmlnode instanceof DOMDocument)) {
            $xdoc=new DOMDocument();
            $xnode = $xdoc->importNode($xmlnode,true);
            $xdoc->appendChild($xnode);
            $xpath = new DomXPath($xdoc);
        } else {
        	$xpath = new DomXPath($xmlnode);
        }
        foreach (CMISRepositoryWrapper :: $namespaces as $nspre => $nsuri)
        {
            $xpath->registerNamespace($nspre, $nsuri);
        }
        return $xpath->query($xquery);

    }
    static function getLinksArray($xmlnode)
    {
        // Gets the links of an object or a workspace
        // Distinguishes between the two "down" links
        //  -- the children link is put into the associative array with the "down" index
        //  -- the descendants link is put into the associative array with the "down-tree" index
        //  These links are distinquished by the mime type attribute, but these are probably the only two links that share the same rel ..
        //    so this was done as a one off
        $links = array ();
        $link_nodes = $xmlnode->getElementsByTagName("link");
        foreach ($link_nodes as $ln)
        {
            if ($ln->attributes->getNamedItem("rel")->nodeValue == "down" && $ln->attributes->getNamedItem("type")->nodeValue == "application/cmistree+xml")
            {
                //Descendents and Childredn share same "rel" but different document type
                $links["down-tree"] = $ln->attributes->getNamedItem("href")->nodeValue;
            } else
            {
                $links[$ln->attributes->getNamedItem("rel")->nodeValue] = $ln->attributes->getNamedItem("href")->nodeValue;
            }
        }
        return $links;
    }
	static function extractAllowableActions($xmldata)
    {
        $doc = new DOMDocument();
        $doc->loadXML($xmldata);
        return CMISRepositoryWrapper :: extractAllowableActionsFromNode($doc);
    }
    static function extractAllowableActionsFromNode($xmlnode)
    {
        $result = array();
        $allowableActions = $xmlnode->getElementsByTagName("allowableActions");
        if ($allowableActions->length > 0) {
            foreach($allowableActions->item(0)->childNodes as $action)
            {
                if (isset($action->localName)) {
                    $result[$action->localName] = (preg_match("/^true$/i", $action->nodeValue) > 0);
                }
            }
        }
        return $result;
    }
    static function extractObject($xmldata)
    {
        $doc = new DOMDocument();
        $doc->loadXML($xmldata);
        return CMISRepositoryWrapper :: extractObjectFromNode($doc);

    }
    static function extractObjectFromNode($xmlnode)
    {
        // Extracts the contents of an Object and organizes them into:
        //  -- Links
        //  -- Properties
        //  -- the Object ID
        // RRM -- NEED TO ADD ALLOWABLEACTIONS
        $retval = new stdClass();
        $retval->links = CMISRepositoryWrapper :: getLinksArray($xmlnode);
        $retval->properties = array ();
        $prop_nodes = $xmlnode->getElementsByTagName("object")->item(0)->getElementsByTagName("properties")->item(0)->childNodes;
        foreach ($prop_nodes as $pn)
        {
            if ($pn->attributes)
            {
                $propDefId = $pn->attributes->getNamedItem("propertyDefinitionId");
                // TODO: Maybe use ->length=0 to even detect null values
                if (!is_null($propDefId) && $pn->getElementsByTagName("value") && $pn->getElementsByTagName("value")->item(0))
                {
                	if ($pn->getElementsByTagName("value")->length > 1) {
                		$retval->properties[$propDefId->nodeValue] = array();
                		for ($idx=0;$idx < $pn->getElementsByTagName("value")->length;$idx++) {
                			$retval->properties[$propDefId->nodeValue][$idx] = $pn->getElementsByTagName("value")->item($idx)->nodeValue;
                		}
                	} else {
                		$retval->properties[$propDefId->nodeValue] = $pn->getElementsByTagName("value")->item(0)->nodeValue;
                	}
                }
            }
        }
        $retval->uuid = $xmlnode->getElementsByTagName("id")->item(0)->nodeValue;
        $retval->id = $retval->properties["cmis:objectId"];
        //TODO: RRM FIX THIS
        $children_node = $xmlnode->getElementsByTagName("children");
        if (is_object($children_node)) {
        	    $children_feed_c = $children_node->item(0);
        }
        if (is_object($children_feed_c)) {
			$children_feed_l = $children_feed_c->getElementsByTagName("feed");
        }
        if (isset($children_feed_l) && is_object($children_feed_l) && is_object($children_feed_l->item(0))) {
        	$children_feed = $children_feed_l->item(0);
			$children_doc = new DOMDocument();
			$xnode = $children_doc->importNode($children_feed,true); // Avoid Wrong Document Error
			$children_doc->appendChild($xnode);
	        $retval->children = CMISRepositoryWrapper :: extractObjectFeedFromNode($children_doc);
        }
		$retval->allowableActions = CMISRepositoryWrapper :: extractAllowableActionsFromNode($xmlnode);
        return $retval;
    }
    
    function handleSpaces($path)
    {
        return $this->do_not_urlencode ? $path : rawurlencode($path);
    }

    static function extractTypeDef($xmldata)
    {
        $doc = new DOMDocument();
        $doc->loadXML($xmldata);
        return CMISRepositoryWrapper :: extractTypeDefFromNode($doc);

    }
    static function extractTypeDefFromNode($xmlnode)
    {
        // Extracts the contents of an Object and organizes them into:
        //  -- Links
        //  -- Properties
        //  -- the Object ID
        // RRM -- NEED TO ADD ALLOWABLEACTIONS
        $retval = new stdClass();
        $retval->links = CMISRepositoryWrapper :: getLinksArray($xmlnode);
        $retval->properties = array ();
        $retval->attributes = array ();
        $result = CMISRepositoryWrapper :: doXQueryFromNode($xmlnode, "//cmisra:type/*");
        foreach ($result as $node)
        {
            if ((substr($node->nodeName, 0, 13) == "cmis:property") && (substr($node->nodeName, -10) == "Definition"))
            {
                $id = $node->getElementsByTagName("id")->item(0)->nodeValue;
                $cardinality = $node->getElementsByTagName("cardinality")->item(0)->nodeValue;
                $propertyType = $node->getElementsByTagName("propertyType")->item(0)->nodeValue;
                // Stop Gap for now
                $retval->properties[$id] = array (
                    "cmis:propertyType" => $propertyType,
                    "cmis:cardinality" => $cardinality,
                    
                );
            } else
            {
                $retval->attributes[$node->nodeName] = $node->nodeValue;
            }
            $retval->id = $retval->attributes["cmis:id"];
        }
        //TODO: RRM FIX THIS
        $children_node = $xmlnode->getElementsByTagName("children");
        if (is_object($children_node)) {
        	    $children_feed_c = $children_node->item(0);
        }
        if (is_object($children_feed_c)) {
			$children_feed_l = $children_feed_c->getElementsByTagName("feed");
        }
        if (isset($childern_feed_l) && is_object($children_feed_l) && is_object($children_feed_l->item(0))) {
        	$children_feed = $children_feed_l->item(0);
			$children_doc = new DOMDocument();
			$xnode = $children_doc->importNode($children_feed,true); // Avoid Wrong Document Error
			$children_doc->appendChild($xnode);
	        $retval->children = CMISRepositoryWrapper :: extractTypeFeedFromNode($children_doc);
        }

        /*
         * 
        
        
        
        		$prop_nodes = $xmlnode->getElementsByTagName("object")->item(0)->getElementsByTagName("properties")->item(0)->childNodes;
        		foreach ($prop_nodes as $pn) {
        			if ($pn->attributes) {
        				$retval->properties[$pn->attributes->getNamedItem("propertyDefinitionId")->nodeValue] = $pn->getElementsByTagName("value")->item(0)->nodeValue;
        			}
        		}
                $retval->uuid=$xmlnode->getElementsByTagName("id")->item(0)->nodeValue;
                $retval->id=$retval->properties["cmis:objectId"];
         */
        return $retval;
    }

    static function extractObjectFeed($xmldata)
    {
        //Assumes only one workspace for now
        $doc = new DOMDocument();
        $doc->loadXML($xmldata);
        return CMISRepositoryWrapper :: extractObjectFeedFromNode($doc);
    }
    static function extractObjectFeedFromNode($xmlnode)
    {
        // Process a feed and extract the objects
        //   Does not handle hierarchy
        //   Provides two arrays 
        //   -- one sequential array (a list)
        //   -- one hash table indexed by objectID
        //   and a property "numItems" that holds the total number of items available.
        $retval = new stdClass();
        // extract total number of items
        $numItemsNode = CMISRepositoryWrapper::doXQueryFromNode($xmlnode, "/atom:feed/cmisra:numItems");
        $retval->numItems = $numItemsNode->length ? (int) $numItemsNode->item(0)->nodeValue : -1; // set to negative value if info is not available
                
        $retval->objectList = array ();
        $retval->objectsById = array ();
        $result = CMISRepositoryWrapper :: doXQueryFromNode($xmlnode, "/atom:feed/atom:entry");
        foreach ($result as $node)
        {
            $obj = CMISRepositoryWrapper :: extractObjectFromNode($node);
            $retval->objectsById[$obj->id] = $obj;
            $retval->objectList[] = & $retval->objectsById[$obj->id];
        }
        return $retval;
    }

    static function extractTypeFeed($xmldata)
    {
        //Assumes only one workspace for now
        $doc = new DOMDocument();
        $doc->loadXML($xmldata);
        return CMISRepositoryWrapper :: extractTypeFeedFromNode($doc);
    }
    static function extractTypeFeedFromNode($xmlnode)
    {
        // Process a feed and extract the objects
        //   Does not handle hierarchy
        //   Provides two arrays 
        //   -- one sequential array (a list)
        //   -- one hash table indexed by objectID
        $retval = new stdClass();
        $retval->objectList = array ();
        $retval->objectsById = array ();
        $result = CMISRepositoryWrapper :: doXQueryFromNode($xmlnode, "/atom:feed/atom:entry");
        foreach ($result as $node)
        {
            $obj = CMISRepositoryWrapper :: extractTypeDefFromNode($node);
            $retval->objectsById[$obj->id] = $obj;
            $retval->objectList[] = & $retval->objectsById[$obj->id];
        }
        return $retval;
    }

    static function extractWorkspace($xmldata)
    {
        //Assumes only one workspace for now
        $doc = new DOMDocument();
        $doc->loadXML($xmldata);
        return CMISRepositoryWrapper :: extractWorkspaceFromNode($doc);
    }
    static function extractWorkspaceFromNode($xmlnode)
    {
        // Assumes only one workspace for now
        // Load up the workspace object with arrays of
        //  links
        //  URI Templates
        //  Collections
        //  Capabilities
        //  General Repository Information
        $retval = new stdClass();
        $retval->links = CMISRepositoryWrapper :: getLinksArray($xmlnode);
        $retval->uritemplates = array ();
        $retval->collections = array ();
        $retval->capabilities = array ();
        $retval->repositoryInfo = array ();
        $retval->permissions = array();
        $retval->permissionsMapping = array();
        $result = CMISRepositoryWrapper :: doXQueryFromNode($xmlnode, "//cmisra:uritemplate");
        foreach ($result as $node)
        {
            $retval->uritemplates[$node->getElementsByTagName("type")->item(0)->nodeValue] = $node->getElementsByTagName("template")->item(0)->nodeValue;
        }
        $result = CMISRepositoryWrapper :: doXQueryFromNode($xmlnode, "//app:collection");
        foreach ($result as $node)
        {
            $retval->collections[$node->getElementsByTagName("collectionType")->item(0)->nodeValue] = $node->attributes->getNamedItem("href")->nodeValue;
        }
        $result = CMISRepositoryWrapper :: doXQueryFromNode($xmlnode, "//cmis:capabilities/*");
        foreach ($result as $node)
        {
            $retval->capabilities[$node->nodeName] = $node->nodeValue;
        }
        $result = CMISRepositoryWrapper :: doXQueryFromNode($xmlnode, "//cmisra:repositoryInfo/*[name()!='cmis:capabilities' and name()!='cmis:aclCapability']");
        foreach ($result as $node)
        {
            $retval->repositoryInfo[$node->nodeName] = $node->nodeValue;
        }
        $result = CMISRepositoryWrapper :: doXQueryFromNode($xmlnode, "//cmis:aclCapability/cmis:permissions");
        foreach ($result as $node)
        {
            $retval->permissions[$node->getElementsByTagName("permission")->item(0)->nodeValue] = $node->getElementsByTagName("description")->item(0)->nodeValue;
        }
        $result = CMISRepositoryWrapper :: doXQueryFromNode($xmlnode, "//cmis:aclCapability/cmis:mapping");
        foreach ($result as $node)
        {
            $key = $node->getElementsByTagName("key")->item(0)->nodeValue;
            $values = array();
            foreach ($node->getElementsByTagName("permission") as $value)
            {
                array_push($values, $value->nodeValue);
            }
            $retval->permissionsMapping[$key] = $values;
        }
        $result = CMISRepositoryWrapper :: doXQueryFromNode($xmlnode, "//cmis:aclCapability/*[name()!='cmis:permissions' and name()!='cmis:mapping']");
        foreach ($result as $node)
        {
            $retval->repositoryInfo[$node->nodeName] = $node->nodeValue;
        }

        return $retval;
    }
}
