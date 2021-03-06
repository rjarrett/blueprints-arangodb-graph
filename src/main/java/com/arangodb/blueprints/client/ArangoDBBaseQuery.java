//////////////////////////////////////////////////////////////////////////////////////////
//
// Implementation of a simple graph client for the ArangoDB.
//
// Copyright triAGENS GmbH Cologne.
//
//////////////////////////////////////////////////////////////////////////////////////////

package com.arangodb.blueprints.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import com.arangodb.CursorResult;
import com.arangodb.util.AqlQueryOptions;

/**
 * The ArangoDB base query class
 * 
 * @author Achim Brandt (http://www.triagens.de)
 * @author Johannes Gocke (http://www.triagens.de)
 * @author Guido Schwab (http://www.triagens.de)
 * @author Jan Steemann (http://www.triagens.de)
 */

public class ArangoDBBaseQuery {

	/**
	 * the ArangoDB graph
	 */
	protected ArangoDBSimpleGraph graph;

	/**
	 * the ArangoDB client
	 */
	protected ArangoDBSimpleGraphClient client;

	/**
	 * query type
	 */
	protected QueryType queryType;

	protected ArangoDBSimpleVertex startVertex;
	protected ArangoDBPropertyFilter propertyFilter;
	protected List<String> labelsFilter;
	protected Direction direction;
	protected Long limit;
	protected boolean count;

	/**
	 * Direction
	 * 
	 */

	public enum Direction {
		// direction into a element
		IN,
		// direction out of a element
		OUT,
		// direction in and out of a element
		ALL
	};

	public enum QueryType {
		GRAPH_VERTICES,
		GRAPH_EDGES,
		GRAPH_NEIGHBORS
	}

	/**
	 * Constructor
	 * 
	 * @param graph
	 *            the graph of the query
	 * @param client
	 *            the request client of the query
	 * @param queryType
	 *            a query type
	 * @throws ArangoDBException
	 */
	public ArangoDBBaseQuery(ArangoDBSimpleGraph graph, ArangoDBSimpleGraphClient client, QueryType queryType)
			throws ArangoDBException {
		this.graph = graph;
		this.client = client;
		this.queryType = queryType;
	}

	/**
	 * Executes the query and returns a result cursor
	 * 
	 * @return CursorResult<Map> the result cursor
	 * 
	 * @throws ArangoDBException
	 *             if the query could not be executed
	 */
	@SuppressWarnings("rawtypes")
	public CursorResult<Map> getCursorResult() throws ArangoDBException {

		Map<String, Object> options = new HashMap<String, Object>();
		options.put("includeData", true);
		options.put("direction", getDirectionString());

		Map<String, Object> bindVars = new HashMap<String, Object>();
		bindVars.put("graphName", graph.getName());
		bindVars.put("options", options);
		bindVars.put("vertexExample", getVertexExample());

		StringBuilder sb = new StringBuilder();
		String prefix = "i.";
		String returnExp = " return i";

		switch (queryType) {
		case GRAPH_VERTICES:
			sb.append("for i in GRAPH_VERTICES(@graphName , @vertexExample, @options)");
			break;
		case GRAPH_EDGES:
			sb.append("for i in GRAPH_EDGES(@graphName , @vertexExample, @options)");
			break;
		case GRAPH_NEIGHBORS:
		default:
			sb.append("for i in GRAPH_EDGES(@graphName , @vertexExample, @options)");
			returnExp = " return DOCUMENT(" + getDocumentByDirection() + ")";
			break;
		}

		List<String> andFilter = new ArrayList<String>();

		if (propertyFilter == null) {
			propertyFilter = new ArangoDBPropertyFilter();
		}
		propertyFilter.addProperties(prefix, andFilter, bindVars);

		if (CollectionUtils.isNotEmpty(labelsFilter)) {
			List<String> orFilter = new ArrayList<String>();
			int tmpCount = 0;
			for (String label : labelsFilter) {
				orFilter.add(prefix + "label == @label" + tmpCount);
				bindVars.put("label" + tmpCount++, label);
			}
			if (CollectionUtils.isNotEmpty(orFilter)) {
				andFilter.add("(" + StringUtils.join(orFilter, " OR ") + ")");
			}
		}

		if (CollectionUtils.isNotEmpty(andFilter)) {
			sb.append(" FILTER ");
			sb.append(StringUtils.join(andFilter, " AND "));
		}

		if (limit != null) {
			sb.append(" LIMIT " + limit.toString());
		}

		sb.append(returnExp);

		String query = sb.toString();
		AqlQueryOptions aqlQueryOptions = new AqlQueryOptions();
		aqlQueryOptions.setBatchSize(client.getConfiguration().getBatchSize());
		aqlQueryOptions.setCount(count);

		return client.executeAqlQuery(query, bindVars, aqlQueryOptions);
	}

	private Object getVertexExample() {
		if (startVertex != null) {
			return startVertex.getDocumentId();
		} else {
			return new HashMap<String, String>();
		}
	}

	private String getDirectionString() {
		if (direction != null) {
			if (direction == Direction.IN) {
				return "inbound";
			} else if (direction == Direction.OUT) {
				return "outbound";
			}
		}

		return "any";
	}

	private String getDocumentByDirection() {
		if (direction != null) {
			if (direction == Direction.IN) {
				return "i._from";
			} else if (direction == Direction.OUT) {
				return "i._to";
			}
		}

		return "i._to == @vertexExample ? i._from : i._to";
	}

	public ArangoDBSimpleVertex getStartVertex() {
		return startVertex;
	}

	public ArangoDBBaseQuery setStartVertex(ArangoDBSimpleVertex startVertex) {
		this.startVertex = startVertex;
		return this;
	}

	public ArangoDBPropertyFilter getPropertyFilter() {
		return propertyFilter;
	}

	public ArangoDBBaseQuery setPropertyFilter(ArangoDBPropertyFilter propertyFilter) {
		this.propertyFilter = propertyFilter;
		return this;
	}

	public List<String> getLabelsFilter() {
		return labelsFilter;
	}

	public ArangoDBBaseQuery setLabelsFilter(List<String> labelsFilter) {
		this.labelsFilter = labelsFilter;
		return this;
	}

	public Direction getDirection() {
		return direction;
	}

	public ArangoDBBaseQuery setDirection(Direction direction) {
		this.direction = direction;
		return this;
	}

	public Long getLimit() {
		return limit;
	}

	public ArangoDBBaseQuery setLimit(Long limit) {
		this.limit = limit;
		return this;
	}

	public boolean isCount() {
		return count;
	}

	public ArangoDBBaseQuery setCount(boolean count) {
		this.count = count;
		return this;
	}

}
