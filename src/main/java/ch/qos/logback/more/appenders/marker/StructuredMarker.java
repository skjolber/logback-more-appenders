package ch.qos.logback.more.appenders.marker;

import java.util.Map;

import org.slf4j.Marker;

/**
 * Interface for projection of marker data into map.
 * 
 * @param <T> map value type
 */

public interface StructuredMarker<T> extends Marker {

	void populate(Map<String, T> map);
	
}
