package com.modularmc.registrate.internal.util;

import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

@SuppressWarnings("null")
public final class DebugMarkers {

    private static final String PREFIX = "REGISTRATE.";

    public static final Marker REGISTER = marker("REGISTER");
    public static final Marker DATA = marker("DATA");

    private DebugMarkers() {}

    private static Marker marker(String name) {
        return MarkerManager.getMarker(PREFIX + name);
    }
}
