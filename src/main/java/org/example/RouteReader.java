
package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.domain.RouteSolution;

import java.io.File;
import java.io.IOException;

public class RouteReader {
    public static RouteSolution read(File file) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(file, RouteSolution.class);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
