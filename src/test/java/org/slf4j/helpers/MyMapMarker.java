package org.slf4j.helpers;

import java.util.Iterator;
import java.util.Map;

import org.slf4j.Marker;
import org.slf4j.helpers.BasicMarker;

import ch.qos.logback.more.appenders.marker.StructuredMarker;

public class MyMapMarker<T> extends BasicMarker implements StructuredMarker<T> {

	private final Map<String, T> map;
	
	public MyMapMarker(String name, Map<String, T> map) {
		super(name);
		
		this.map = map;
	}

	@Override
	public void populate(Map<String, T> map) {
		map.putAll(this.map);
		
		if(hasReferences()) {
			Iterator<Marker> iterator = iterator();
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
