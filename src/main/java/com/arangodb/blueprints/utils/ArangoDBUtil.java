//////////////////////////////////////////////////////////////////////////////////////////
//
// Implementation of the Blueprints Interface for ArangoDB by triAGENS GmbH Cologne.
//
// Copyright triAGENS GmbH Cologne.
//
//////////////////////////////////////////////////////////////////////////////////////////

package com.arangodb.blueprints.utils;

/**
 * This class is used to rename attributes of the vertices and edges to support
 * names starting with a '_' character.
 * 
 * @author Achim Brandt (http://www.triagens.de)
 * @author Johannes Gocke (http://www.triagens.de)
 */

public class ArangoDBUtil {

	private ArangoDBUtil() {
		// this is a hlper class
	}

	/**
	 * rename key "_XXXX" to ",XXXX"
	 * 
	 * @param key
	 *            the key to convert
	 * @return String the converted String
	 */

	public static String normalizeKey(String key) {
		if (key.charAt(0) == '_') {
			return "," + key.substring(1);
		}
		return key;
	}

	/**
	 * rename key ",XXXX" to "_XXXX"
	 * 
	 * @param key
	 *            the key to convert
	 * @return String the converted String
	 */

	public static String denormalizeKey(String key) {
		if (key.charAt(0) == ',') {
			return "_" + key.substring(1);
		}
		return key;
	}

}
