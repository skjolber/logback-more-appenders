package ch.qos.logback.more.appenders.marker;

import java.util.Iterator;
import java.util.Map;

import org.slf4j.Marker;

public class StructuredMarkerUtil {

	public static <T> void mapInto(Map<String, T> map, StructuredMarker<T> mapMarker) {
		mapMarker.populate(map);
		
		if(mapMarker.hasReferences()) {
			Iterator<Marker> iterator = mapMarker.iterator();
			while(iterator.hasNext()) {
				Marker next = iterator.next();
				
				if(next instanceof StructuredMarker) {
					@SuppressWarnings("unchecked")
					StructuredMarker<T> child = (StructuredMarker<T>)next;
					child.populate(map);
				}
			}
		}
	}
}
