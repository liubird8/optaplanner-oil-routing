
package org.example;

import org.example.domain.*;
import org.optaplanner.core.api.solver.SolverFactory;
import org.optaplanner.core.api.solver.Solver;

import java.io.File;
import java.io.FileWriter;
import java.util.*;

public class App {
    public static void main(String[] args) {
        SolverFactory<RouteSolution> solverFactory = SolverFactory.createFromXmlResource("solverConfig.xml");
        Solver<RouteSolution> solver = solverFactory.buildSolver();

        RouteSolution problem = RouteReader.read(new File("data.json"));
        RouteSolution solution = solver.solve(problem);

        List<Map<String, Object>> routeList = new ArrayList<>();

        for (Vehicle vehicle : solution.getVehicleList()) {
            System.out.println("車輛：" + vehicle.getId());

            Standstill standstill = vehicle;
            int order = 1;
            double totalDistance = 0.0;

            while (true) {
                Customer next = null;
                for (Customer customer : solution.getCustomerList()) {
                    if (customer.getPreviousStandstill() == standstill) {
                        next = customer;
                        break;
                    }
                }
                if (next == null) break;

                System.out.printf("  第 %d 站 → 商家 %s (%f, %f)%n",
                        order,
                        next.getId(),
                        next.getLocation().getLatitude(),
                        next.getLocation().getLongitude()
                );

                if (next.getPreviousStandstill() != null) {
                    double dx = next.getLocation().getLatitude() - next.getPreviousStandstill().getLocation().getLatitude();
                    double dy = next.getLocation().getLongitude() - next.getPreviousStandstill().getLocation().getLongitude();
                    totalDistance += Math.sqrt(dx * dx + dy * dy) * 111.0;
                }

                Map<String, Object> point = new LinkedHashMap<>();
                point.put("order", order++);
                point.put("id", next.getId());
                point.put("lat", next.getLocation().getLatitude());
                point.put("lon", next.getLocation().getLongitude());
                routeList.add(point);

                standstill = next;
            }

            System.out.printf("🚛 總距離（大約）：%.2f km%n", totalDistance);
        }

        try (FileWriter fw = new FileWriter("best_route.json")) {
            fw.write(new com.fasterxml.jackson.databind.ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(routeList));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
