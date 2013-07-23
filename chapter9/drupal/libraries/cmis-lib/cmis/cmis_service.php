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
require_once ('cmis_repository_wrapper.php');

// Option Contants for Array Indexing
// -- Generally optional flags that control how much information is returned
// -- Change log token is an anomoly -- but included in URL as parameter
define("OPT_MAX_ITEMS", "maxItems");
define("OPT_SKIP_COUNT", "skipCount");
define("OPT_FILTER", "filter");
define("OPT_INCLUDE_PROPERTY_DEFINITIONS", "includePropertyDefinitions");
define("OPT_INCLUDE_RELATIONSHIPS", "includeRelationships");
define("OPT_INCLUDE_POLICY_IDS", "includePolicyIds");
define("OPT_RENDITION_FILTER", "renditionFilter");
define("OPT_INCLUDE_ACL", "includeACL");
define("OPT_INCLUDE_ALLOWABLE_ACTIONS", "includeAllowableActions");
define("OPT_DEPTH", "depth");
define("OPT_CHANGE_LOG_TOKEN", "changeLogToken");
define("OPT_CHECK_IN_COMMENT", "checkinComment");
define("OPT_CHECK_IN", "checkin");
define("OPT_MAJOR_VERSION", "major");

define("COLLECTION_ROOT_FOLDER","root");
define("COLLECTION_TYPES","types");
define("COLLECTION_CHECKED_OUT","checkedout");
define("COLLECTION_QUERY","query");
define("COLLECTION_UNFILED","unfiled");

define("URI_TEMPLATE_OBJECT_BY_ID","objectbyid");
define("URI_TEMPLATE_OBJECT_BY_PATH","objectbypath");
define("URI_TEMPLATE_TYPE_BY_ID","typebyid");
define("URI_TEMPLATE_QUERY","query");

define("LINK_SELF", "self");
define("LINK_SERVICE","service");
define("LINK_DESCRIBED_BY", "describedby");
define("LINK_VIA","via");
define("LINK_EDIT_MEDIA", "edit-media");
define("LINK_EDIT","edit");
define("LINK_ALTERNATE", "alternate");
define("LINK_FIRST","first");
define("LINK_PREVIOUS", "previous");
define("LINK_NEXT","next");
define("LINK_LAST", "last");
define("LINK_UP","up");
define("LINK_DOWN", "down");
define("LINK_DOWN_TREE","down-tree");
define("LINK_VERSION_HISTORY","version-history");
define("LINK_CURRENT_VERSION", "current-version");


define("LINK_ALLOWABLE_ACTIONS", "http://docs.oasis-open.org/ns/cmis/link/200908/allowableactions");
define("LINK_RELATIONSHIPS","http://docs.oasis-open.org/ns/cmis/link/200908/relationships");
define("LINK_SOURCE","http://docs.oasis-open.org/ns/cmis/link/200908/source");
define("LINK_TARGET","http://docs.oasis-open.org/ns/cmis/link/200908/target");
define("LINK_POLICIES", "http://docs.oasis-open.org/ns/cmis/link/200908/policies");
define("LINK_ACL","http://docs.oasis-open.org/ns/cmis/link/200908/acl");
define("LINK_CHANGES","http://docs.oasis-open.org/ns/cmis/link/200908/changes");
define("LINK_FOLDER_TREE","http://docs.oasis-open.org/ns/cmis/link/200908/foldertree");
define("LINK_ROOT_DESCENDANTS","http://docs.oasis-open.org/ns/cmis/link/200908/rootdescendants");
define("LINK_TYPE_DESCENDANTS","http://docs.oasis-open.org/ns/cmis/link/200908/typedescendants");

define("MIME_ATOM_XML", 'application/atom+xml');
define("MIME_ATOM_XML_ENTRY", 'application/atom+xml;type=entry');
define("MIME_ATOM_XML_FEED", 'application/atom+xml;type=feed');
define("MIME_CMIS_TREE", 'application/cmistree+xml');
define("MIME_CMIS_QUERY", 'application/cmisquery+xml');

// Many Links have a pattern to them based upon objectId -- but can that be depended upon?

class CMISService extends CMISRepositoryWrapper {
	var $_link_cache;
	var $_title_cache;
	var $_objTypeId_cache;
	var $_type_cache;
	var $_changeToken_cache;
	function __construct($url, $username, $password, $options = null, array $addlCurlOptions = array ()) {
		parent :: __construct($url, $username, $password, $options, $addlCurlOptions);
		$this->_link_cache = array ();
		$this->_title_cache = array ();
		$this->_objTypeId_cache = array ();
		$this->_type_cache = array ();
		$this->_changeToken_cache = array ();
	}

	// Utility Methods -- Added Titles
	// Should refactor to allow for single object	
	function cacheObjectInfo($obj) {
		$this->_link_cache[$obj->id] = $obj->links;
		$this->_title_cache[$obj->id] = $obj->properties["cmis:name"]; // Broad Assumption Here?
		$this->_objTypeId_cache[$obj->id] = $obj->properties["cmis:objectTypeId"];
		if (isset($obj->properties["cmis:changeToken"])) {
			$this->_changeToken_cache[$obj->id] = $obj->properties["cmis:changeToken"];
		}
	}

	function getMultiValuedProp($obj,$propName) {
		if (isset($obj->properties[$propName])) {
			return CMISRepositoryWrapper::getAsArray($obj->properties[$propName]);
		}
		return array();
	}
	function cacheFeedInfo($objs) {
		foreach ($objs->objectList as $obj) {
			$this->cacheObjectInfo($obj);
		}
	}

	function cacheTypeFeedInfo($typs) {
		foreach ($typs->objectList as $typ) {
			$this->cacheTypeInfo($typ);
		}
	}

	function cacheTypeInfo($tDef) {
		// TODO: Fix Type Caching with missing properties
		$this->_type_cache[$tDef->id] = $tDef;
	}

	function getPropertyType($typeId, $propertyId) {
		if (isset($this->_type_cache[$typeId])) {
			if ($this->_type_cache[$typeId]->properties) {
				return $this->_type_cache[$typeId]->properties[$propertyId]["cmis:propertyType"];
			}
		}
		$obj = $this->getTypeDefinition($typeId);
		return $obj->properties[$propertyId]["cmis:propertyType"];
	}

	function getObjectType($objectId) {
		if ($this->_objTypeId_cache[$objectId]) {
			return $this->_objTypeId_cache[$objectId];
		}
		$obj = $this->getObject($objectId);
		return $obj->properties["cmis:objectTypeId"];
	}

	function getTitle($objectId) {
		if ($this->_title_cache[$objectId]) {
			return $this->_title_cache[$objectId];
		}
		$obj = $this->getObject($objectId);
		return $obj->properties["cmis:name"];
	}

	function getTypeLink($typeId, $linkName) {
		if ($this->_type_cache[$typeId]->links) {
			return $this->_type_cache[$typeId]->links[$linkName];
		}
		$typ = $this->getTypeDefinition($typeId);
		return $typ->links[$linkName];
	}

	function getLink($objectId, $linkName) {
		if ($this->_link_cache[$objectId][$linkName]) {
			return $this->_link_cache[$objectId][$linkName];
		}
		$obj = $this->getObject($objectId);
		return $obj->links[$linkName];
	}

	// Repository Services
	// TODO: Need to fix this for multiple repositories
	function getRepositories() {
		throw CmisNotImplementedException("getRepositories");
	}

	function getRepositoryInfo() {
		return $this->workspace;
	}

	function getTypeDescendants($typeId = null, $depth, $options = array ()) {
		// TODO: Refactor Type Entries Caching
		$varmap = $options;
		if ($typeId) {
			$hash_values = $options;
			$hash_values[OPT_DEPTH] = $depth;
			$myURL = $this->getTypeLink($typeId, LINK_DOWN_TREE);
			$myURL = CMISRepositoryWrapper :: getOpUrl($myURL, $hash_values);
		} else {
			$myURL = $this->processTemplate($this->workspace->links[LINK_TYPE_DESCENDANTS], $varmap);
		}
		$ret = $this->doGet($myURL);
		$typs = $this->extractTypeFeed($ret->body);
		$this->cacheTypeFeedInfo($typs);
		return $typs;
	}

	function getTypeChildren($typeId = null, $options = array ()) {
		// TODO: Refactor Type Entries Caching
		$varmap = $options;
		if ($typeId) {
			$myURL = $this->getTypeLink($typeId, "down");
			//TODO: Need GenURLQueryString Utility
		} else {
			//TODO: Need right URL
			$myURL = $this->processTemplate($this->workspace->collections['types'], $varmap);
		}
		$ret = $this->doGet($myURL);
		$typs = $this->extractTypeFeed($ret->body);
		$this->cacheTypeFeedInfo($typs);
		return $typs;
	}

	function getTypeDefinition($typeId, $options = array ()) { // Nice to have
		$varmap = $options;
		$varmap["id"] = $typeId;
		$myURL = $this->processTemplate($this->workspace->uritemplates['typebyid'], $varmap);
		$ret = $this->doGet($myURL);
		$obj = $this->extractTypeDef($ret->body);
		$this->cacheTypeInfo($obj);
		return $obj;
	}

	function getObjectTypeDefinition($objectId) { // Nice to have
		$myURL = $this->getLink($objectId, "describedby");
		$ret = $this->doGet($myURL);
		$obj = $this->extractTypeDef($ret->body);
		$this->cacheTypeInfo($obj);
		return $obj;
	}
	//Navigation Services
	function getFolderTree($folderId, $depth, $options = array ()) {
		$hash_values = $options;
		$hash_values[OPT_DEPTH] = $depth;
		$myURL = $this->getLink($folderId, "http://docs.oasis-open.org/ns/cmis/link/200908/foldertree");
		$myURL = CMISRepositoryWrapper :: getOpUrl($myURL, $hash_values);
		$ret = $this->doGet($myURL);
		$objs = $this->extractObjectFeed($ret->body);
		$this->cacheFeedInfo($objs);
		return $objs;
	}

	function getDescendants($folderId, $depth, $options = array ()) { // Nice to have
		$hash_values = $options;
		$hash_values[OPT_DEPTH] = $depth;
		$myURL = $this->getLink($folderId, LINK_DOWN_TREE);
		$myURL = CMISRepositoryWrapper :: getOpUrl($myURL, $hash_values);
		$ret = $this->doGet($myURL);
		$objs = $this->extractObjectFeed($ret->body);
		$this->cacheFeedInfo($objs);
		return $objs;
	}

	function getChildren($folderId, $options = array ()) {
		$myURL = $this->getLink($folderId, LINK_DOWN);
		//TODO: Need GenURLQueryString Utility
		$ret = $this->doGet($myURL);
		$objs = $this->extractObjectFeed($ret->body);
		$this->cacheFeedInfo($objs);
		return $objs;
	}

	function getFolderParent($folderId, $options = array ()) { //yes
		$myURL = $this->getLink($folderId, LINK_UP);
		//TODO: Need GenURLQueryString Utility
		$ret = $this->doGet($myURL);
		$obj = $this->extractObjectEntry($ret->body);
		$this->cacheObjectInfo($obj);
		return $obj;
	}

	function getObjectParents($objectId, $options = array ()) { // yes
		$myURL = $this->getLink($objectId, LINK_UP);
		//TODO: Need GenURLQueryString Utility
		$ret = $this->doGet($myURL);
		$objs = $this->extractObjectFeed($ret->body);
		$this->cacheFeedInfo($objs);
		return $objs;
	}

	function getCheckedOutDocs($options = array ()) {
		$obj_url = $this->workspace->collections[COLLECTION_CHECKED_OUT];
		$ret = $this->doGet($obj_url);
		$objs = $this->extractObjectFeed($ret->body);
		$this->cacheFeedInfo($objs);
		return $objs;
	}

	//Discovery Services

	static function getQueryTemplate() {
		ob_start();
		echo '<?xml version="1.0" encoding="UTF-8" standalone="yes"?>' . "\n";
?>
<cmis:query xmlns:cmis="http://docs.oasis-open.org/ns/cmis/core/200908/"
xmlns:cmism="http://docs.oasis-open.org/ns/cmis/messaging/200908/"
xmlns:atom="http://www.w3.org/2005/Atom"
xmlns:app="http://www.w3.org/2007/app"
xmlns:cmisra="http://docs.oasisopen.org/ns/cmis/restatom/200908/">
<cmis:statement>{q}</cmis:statement>
<cmis:searchAllVersions>{searchAllVersions}</cmis:searchAllVersions>
<cmis:includeAllowableActions>{includeAllowableActions}</cmis:includeAllowableActions>
<cmis:includeRelationships>{includeRelationships}</cmis:includeRelationships>
<cmis:renditionFilter>{renditionFilter}</cmis:renditionFilter>
<cmis:maxItems>{maxItems}</cmis:maxItems>
<cmis:skipCount>{skipCount}</cmis:skipCount>
</cmis:query>
<?php


		return ob_get_clean();
	}
	function query($statement, $options = array ()) {
		static $query_template;
		if (!isset ($query_template)) {
			$query_template = CMISService :: getQueryTemplate();
		}
		$hash_values = $options;
		$hash_values['q'] = $statement;
		$post_value = CMISRepositoryWrapper :: processTemplate($query_template, $hash_values);
		$ret = $this->doPost($this->workspace->collections['query'], $post_value, MIME_CMIS_QUERY);
		$objs = $this->extractObjectFeed($ret->body);
		$this->cacheFeedInfo($objs);
		return $objs;
	}
	function checkURL($url,$functionName=null) {
		if (!$url) {
			throw new CmisNotSupportedException($functionName?$functionName:"UnspecifiedMethod");
		}
	}

	function getContentChanges($options = array()) {
		$myURL =  CMISRepositoryWrapper :: processTemplate($this->workspace->links[LINK_CHANGES],$options);
		$this->checkURL($myURL,"getContentChanges");
		$ret = $this->doGet($myURL);
		$objs = $this->extractObjectFeed($ret->body);
		$this->cacheFeedInfo($objs);
		return $objs;
	}

	//Object Services
	static function getEntryTemplate() {
		ob_start();
		echo '<?xml version="1.0" encoding="UTF-8" standalone="yes"?>' . "\n";
?>
<atom:entry xmlns:cmis="http://docs.oasis-open.org/ns/cmis/core/200908/"
xmlns:cmism="http://docs.oasis-open.org/ns/cmis/messaging/200908/"
xmlns:atom="http://www.w3.org/2005/Atom"
xmlns:app="http://www.w3.org/2007/app"
xmlns:cmisra="http://docs.oasis-open.org/ns/cmis/restatom/200908/">
<atom:title>{title}</atom:title>
{SUMMARY}
{CONTENT}
<cmisra:object><cmis:properties>{PROPERTIES}</cmis:properties></cmisra:object>
</atom:entry>
<?php


		return ob_get_clean();
	}

	static function getPropertyTemplate() {
		ob_start();
?>
		<cmis:property{propertyType} propertyDefinitionId="{propertyId}">
			<cmis:value>{properties}</cmis:value>
		</cmis:property{propertyType}>
<?php


		return ob_get_clean();
	}

	function processPropertyTemplates($objectType, $propMap) {
		static $propTemplate;
		static $propertyTypeMap;
		if (!isset ($propTemplate)) {
			$propTemplate = CMISService :: getPropertyTemplate();
		}
		if (!isset ($propertyTypeMap)) { // Not sure if I need to do this like this
			$propertyTypeMap = array (
				"integer" => "Integer",
				"boolean" => "Boolean",
				"datetime" => "DateTime",
				"decimal" => "Decimal",
				"html" => "Html",
				"id" => "Id",
				"string" => "String",
				"url" => "Url",
				"xml" => "Xml",

				
			);
		}
		$propertyContent = "";
		$hash_values = array ();
		foreach ($propMap as $propId => $propValue) {
			$hash_values['propertyType'] = $propertyTypeMap[$this->getPropertyType($objectType, $propId)];
			$hash_values['propertyId'] = $propId;
			if (is_array($propValue)) {
				$first_one = true;
				$hash_values['properties'] = "";
				foreach ($propValue as $val) {
					//This is a bit of a hack
					if ($first_one) {
						$first_one = false;
					} else {
						$hash_values['properties'] .= "</cmis:value>\n<cmis:value>";
					}
					$hash_values['properties'] .= $val;
				}
			} else {
				$hash_values['properties'] = $propValue;
			}
			//echo "HASH:\n";
			//print_r(array("template" =>$propTemplate, "Hash" => $hash_values));
			$propertyContent .= CMISRepositoryWrapper :: processTemplate($propTemplate, $hash_values);
		}
		return $propertyContent;
	}

	static function getContentEntry($content, $content_type = "application/octet-stream") {
		static $contentTemplate;
		if (!isset ($contentTemplate)) {
			$contentTemplate = CMISService :: getContentTemplate();
		}
		if ($content) {
			return CMISRepositoryWrapper :: processTemplate($contentTemplate, array (
				"content" => base64_encode($content),
				"content_type" => $content_type
			));
		} else {
			return "";
		}
	}

	static function getSummaryTemplate() {
		ob_start();
?>
		<atom:summary>{summary}</atom:summary>
<?php


		return ob_get_clean();
	}

	static function getContentTemplate() {
		ob_start();
?>
		<cmisra:content>
			<cmisra:mediatype>
				{content_type}
			</cmisra:mediatype>
			<cmisra:base64>
				{content}
			</cmisra:base64>
		</cmisra:content>
<?php


		return ob_get_clean();
	}
	static function createAtomEntry($name, $properties) {

	}
	function getObject($objectId, $options = array ()) {
		$varmap = $options;
		$varmap["id"] = $objectId;
		$obj_url = $this->processTemplate($this->workspace->uritemplates['objectbyid'], $varmap);
		$ret = $this->doGet($obj_url);
		$obj = $this->extractObject($ret->body);
		$this->cacheObjectInfo($obj);
		return $obj;
	}

	function getObjectByPath($path, $options = array ()) {
		$varmap = $options;
		$varmap["path"] = $this->handleSpaces($path);
		$obj_url = $this->processTemplate($this->workspace->uritemplates['objectbypath'], $varmap);
		$ret = $this->doGet($obj_url);
		$obj = $this->extractObject($ret->body);
		$this->cacheObjectInfo($obj);
		return $obj;
	}

	function getProperties($objectId, $options = array ()) {
		// May need to set the options array default -- 
		return $this->getObject($objectId, $options);
	}

	function getAllowableActions($objectId, $options = array ()) {
		$myURL = $this->getLink($objectId, LINK_ALLOWABLE_ACTIONS);
		$ret = $this->doGet($myURL);
		$result = $this->extractAllowableActions($ret->body);
		return $result;
	}

	function getRenditions($objectId, $options = array (
		OPT_RENDITION_FILTER => "*"
	)) {
		return getObject($objectId, $options);
	}

	function getContentStream($objectId, $options = array ()) { // Yes
		$myURL = $this->getLink($objectId, "edit-media");
		$ret = $this->doGet($myURL);
		// doRequest stores the last request information in this object
		return $ret->body;
	}
    function legacyPostObject($folderId, $objectName, $objectType, $properties = array (), $content = null, $content_type = "application/octet-stream", $options = array ())
    { // Yes
        $myURL = $this->getLink($folderId, "down");
        // TODO: Need Proper Query String Handling
        // Assumes that the 'down' link does not have a querystring in it
        $myURL = CMISRepositoryWrapper :: getOpUrl($myURL, $options);
        static $entry_template;
        if (!isset ($entry_template))
        {
            $entry_template = CMISService :: getEntryTemplate();
        }
        if (is_array($properties))
        {
            $hash_values = $properties;
        } else
        {
            $hash_values = array ();
        }
        if (!isset ($hash_values["cmis:objectTypeId"]))
        {
            $hash_values["cmis:objectTypeId"] = $objectType;
        }
        $properties_xml = $this->processPropertyTemplates($hash_values["cmis:objectTypeId"], $hash_values);
        if (is_array($options))
        {
            $hash_values = $options;
        } else
        {
            $hash_values = array ();
        }
        $hash_values["PROPERTIES"] = $properties_xml;
        $hash_values["SUMMARY"] = CMISService :: getSummaryTemplate();
        if ($content)
        {
            $hash_values["CONTENT"] = CMISService :: getContentEntry($content, $content_type);
        }
        if (!isset ($hash_values['title']))
        {
            $hash_values['title'] = $objectName;
        }
        if (!isset ($hash_values['summary']))
        {
            $hash_values['summary'] = $objectName;
        }
        $post_value = CMISRepositoryWrapper :: processTemplate($entry_template, $hash_values);
        $ret = $this->doPost($myURL, $post_value, MIME_ATOM_XML_ENTRY);
        // print "DO_POST\n";
        // print_r($ret);
        $obj = $this->extractObject($ret->body);
        $this->cacheObjectInfo($obj);
        return $obj;
    }
	function postObject($folderId, $objectName, $objectType, $properties = array (), $content = null, $content_type = "application/octet-stream", $options = array ()) {
		$myURL = $this->getLink($folderId, "down");
		// TODO: Need Proper Query String Handling
		// Assumes that the 'down' link does not have a querystring in it
		if (is_array($properties)) {
			$hash_values = $properties;
		} else {
			$hash_values = array ();
		}
		if (!isset ($hash_values["cmis:objectTypeId"])) {
			$hash_values["cmis:objectTypeId"] = $objectType;
		}
		if (!isset ($hash_values['title'])) {
			$hash_values['title'] = $objectName;
		}
		if (!isset ($hash_values['summary'])) {
			$hash_values['summary'] = $objectName;
		}
		$this->postEntry($myURL, $hash_values);
	}
	function postEntry($url, $properties = array (), $content = null, $content_type = "application/octet-stream", $options = array ()) {
		// TODO: Fix Hack HERE -- get type if it is there otherwise retrieve it --
		$objectType ="";
		if (isset($properties['cmis:objectTypeId'])) {
			$objType = $properties['cmis:objectTypeId'];
		} else if (isset($properties["cmis:objectId"])) {
			$objType=$this->getObjectType($properties["cmis:objectId"]);			
		}
		$myURL = CMISRepositoryWrapper :: getOpUrl($url, $options);
		//DEBUG
		print("DEBUG: postEntry: myURL = " . $myURL);
		static $entry_template;
		if (!isset ($entry_template)) {
			$entry_template = CMISService :: getEntryTemplate();
		}
		print("DEBUG: postEntry: entry_template = " . $entry_template);		
		$properties_xml = $this->processPropertyTemplates($objType, $properties);
		print("DEBUG: postEntry: properties_xml = " . $properties_xml);		
		if (is_array($options)) {
			$hash_values = $options;
		} else {
			$hash_values = array ();
		}
		$hash_values["PROPERTIES"] = $properties_xml;
		$hash_values["SUMMARY"] = CMISService :: getSummaryTemplate();
		if ($content) {
			$hash_values["CONTENT"] = CMISService :: getContentEntry($content, $content_type);
		}
		print("DEBUG: postEntry: hash_values = " . print_r($hash_values,true));		
		$post_value = CMISRepositoryWrapper :: processTemplate($entry_template, $hash_values);
		print("DEBUG: postEntry: post_value = " . $post_value);		
		$ret = $this->doPost($myURL, $post_value, MIME_ATOM_XML_ENTRY);
		$obj = $this->extractObject($ret->body);
		$this->cacheObjectInfo($obj);
		return $obj;
	}

	function createDocument($folderId, $fileName, $properties = array (), $content = null, $content_type = "application/octet-stream", $options = array ()) { // Yes
		return $this->postObject($folderId, $fileName, "cmis:document", $properties, $content, $content_type, $options);
	}

	function createDocumentFromSource() { //Yes?
		throw new CmisNotSupportedException("createDocumentFromSource is not supported by the AtomPub binding!");
	}

	function createFolder($folderId, $folderName, $properties = array (), $options = array ()) { // Yes
		return $this->legacyPostObject($folderId, $folderName, "cmis:folder", $properties, null, null, $options);
	}

	function createRelationship() { // Not in first Release
		throw CmisNotImplementedException("createRelationship");
	}

	function createPolicy() { // Not in first Release
		throw CmisNotImplementedException("createPolicy");
	}

	function updateProperties($objectId, $properties = array (), $options = array ()) { // Yes
		$varmap = $options;
		$varmap["id"] = $objectId;
		$objectName = $this->getTitle($objectId);
		$objectType = $this->getObjectType($objectId);
		$obj_url = $this->getLink($objectId, "edit");
		$obj_url = CMISRepositoryWrapper :: getOpUrl($obj_url, $options);
		static $entry_template;
		if (!isset ($entry_template)) {
			$entry_template = CMISService :: getEntryTemplate();
		}
		if (is_array($properties)) {
			$hash_values = $properties;
		} else {
			$hash_values = array ();
		}
		if ($this->_changeToken_cache[$objectId] != null) {
			$properties['cmis:changeToken'] = $this->_changeToken_cache[$objectId];
		}
		
		$properties_xml = $this->processPropertyTemplates($objectType, $hash_values);
		if (is_array($options)) {
			$hash_values = $options;
		} else {
			$hash_values = array ();
		}
		$hash_values["PROPERTIES"] = $properties_xml;
		$hash_values["SUMMARY"] = CMISService :: getSummaryTemplate();
		if (!isset ($hash_values['title'])) {
			$hash_values['title'] = $objectName;
		}
		if (!isset ($hash_values['summary'])) {
			$hash_values['summary'] = $objectName;
		}
		$put_value = CMISRepositoryWrapper :: processTemplate($entry_template, $hash_values);
		// print $put_value; // RRM DEBUG
		$ret = $this->doPut($obj_url, $put_value, MIME_ATOM_XML_ENTRY);
		$obj = $this->extractObject($ret->body);
		$this->cacheObjectInfo($obj);
		return $obj;
	}

	function moveObject($objectId, $targetFolderId, $sourceFolderId, $options = array ()) { //yes
		$options['sourceFolderId'] = $sourceFolderId;
		return $this->postObject($targetFolderId, $this->getTitle($objectId), $this->getObjectType($objectId), array (
			"cmis:objectId" => $objectId
		), null, null, $options);
	}

	function deleteObject($objectId, $options = array ()) { //Yes
		$varmap = $options;
		$varmap["id"] = $objectId;
		$obj_url = $this->getLink($objectId, "edit");
		$ret = $this->doDelete($obj_url);
		return;
	}

	function deleteTree($folderId, $options = array ()) { // Nice to have
		$hash_values = $options;
		$myURL = $this->getLink($folderId, LINK_DOWN_TREE);
		$myURL = CMISRepositoryWrapper :: getOpUrl($myURL, $hash_values);
		$ret = $this->doDelete($myURL);
		//List of problem objects
		$objs = $this->extractObjectFeed($ret->body);
		$this->cacheFeedInfo($objs);
		return $objs;
	}

	function setContentStream($objectId, $content, $content_type, $options = array ()) { //Yes
		$myURL = $this->getLink($objectId, "edit-media");
		$ret = $this->doPut($myURL, $content, $content_type);
	}

	function deleteContentStream($objectId, $options = array ()) { //yes
		$myURL = $this->getLink($objectId, "edit-media");
		$ret = $this->doDelete($myURL);
		return;
	}

	//Versioning Services
	function getPropertiesOfLatestVersion($objectId, $major = false, $options = array ()) {
		return $this->getObjectOfLatestVersion($objectId, $major, $options);
	}

	function getObjectOfLatestVersion($objectId, $major = false, $options = array ()) {
		return $this->getObject($objectId, $options); // Won't be able to handle major/minor distinction
		// Need to add this -- "current-version"
		/*
		 * Headers: CMIS-filter, CMIS-returnVersion (enumReturnVersion) 
		 * HTTP Arguments: filter, returnVersion 
		 * Enum returnVersion: This, Latest, Major
		 */
	}

	function getAllVersions() {
		throw CmisNotImplementedException("getAllVersions");
	}

	function checkOut($objectId,$options = array()) {
		$myURL = $this->workspace->collections[COLLECTION_CHECKED_OUT];
		$myURL = CMISRepositoryWrapper :: getOpUrl($myURL, $options);
		$ret = $this->postEntry($myURL,  array ("cmis:objectId" => $objectId));
		$obj = $this->extractObject($ret->body);
		$this->cacheObjectInfo($obj);
		return $obj;
	}

	function checkIn($objectId,$options = array()) {
		$myURL = $this->workspace->collections[COLLECTION_CHECKED_OUT];
		$myURL = CMISRepositoryWrapper :: getOpUrl($myURL, $options);
		$ret = $this->postEntry($myURL,  array ("cmis:objectId" => $objectId));
		$obj = $this->extractObject($ret->body);
		$this->cacheObjectInfo($obj);
		return $obj;
	}

	function cancelCheckOut($objectId,$options = array()) {
		// TODO: Look at links "via" and "working-copy"
		$varmap = $options;
		$varmap["id"] = $objectId;
		$via = $this->getLink($objectId,"via");
		print("DEBUG: cancelCheckOut VIA="+$via);
		if (!$via) {
			throw new CmisInvalidArgumentException("Not a WORKING COPY!");
		}
		$obj_url = $this->getLink($objectId, "edit");
		$ret = $this->doDelete($obj_url);
		return;
	}

	function deleteAllVersions() {
		throw CmisNotImplementedException("deleteAllVersions");
	}

	//Relationship Services
	function getObjectRelationships() {
		// get stripped down version of object (for the links) and then get the relationships?
		// Low priority -- can get all information when getting object
		throw CmisNotImplementedException("getObjectRelationships");
	}

	//Multi-Filing ServicesRelation
	function addObjectToFolder($objectId, $targetFolderId, $options = array ()) { // Probably
		return $this->postObject($targetFolderId, $this->getTitle($objectId), $this->getObjectType($objectId), array (
			"cmis:objectId" => $objectId
		), null, null, $options);
	}

	function removeObjectFromFolder($objectId, $targetFolderId, $options = array ()) { //Probably
		$hash_values = $options;
		$myURL = $this->workspace->collections['unfiled'];
		$myURL = CMISRepositoryWrapper :: getOpUrl($myURL, $hash_values);
		$ret = $this->postEntry($myURL,  array ("cmis:objectId" => $objectId),null,null,array("removeFrom" => $targetFolderId));
		$obj = $this->extractObject($ret->body);
		$this->cacheObjectInfo($obj);
		return $obj;
	}

	//Policy Services
	function getAppliedPolicies() {
		throw CmisNotImplementedException("getAppliedPolicies");
	}

	function applyPolicy() {
		throw CmisNotImplementedException("applyPolicy");
	}

	function removePolicy() {
		throw CmisNotImplementedException("removePolicy");
	}

	//ACL Services
	function getACL() {
		throw CmisNotImplementedException("getACL");
	}

	function applyACL() {
		throw CmisNotImplementedException("applyACL");
	}
}