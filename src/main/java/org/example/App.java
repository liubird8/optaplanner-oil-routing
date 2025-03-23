package org.example;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import org.example.domain.*;
import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.api.solver.SolverFactory;

import java.io.*;
import java.util.*;

public class App {

    public static void main(String[] args) throws IOException {
        JsonReader reader = new JsonReader(new FileReader("src/main/resources/my_final_jsprit_input.json"));
        JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();

        JsonObject vehicleJson = json.getAsJsonArray("vehicles").get(0).getAsJsonObject();
        JsonObject startAddress = vehicleJson.getAsJsonObject("start_address");

        Location depot = new Location(
                startAddress.get("location_id").getAsString(),
                startAddress.get("lat").getAsDouble(),
                startAddress.get("lon").getAsDouble()
        );

        Vehicle vehicle = new Vehicle();
        vehicle.setId(vehicleJson.get("vehicle_id").getAsString());
        vehicle.setCapacity(vehicleJson.getAsJsonArray("capacity").get(0).getAsInt());
        vehicle.setStartLocation(depot);

        List<Vehicle> vehicleList = Collections.singletonList(vehicle);

        List<Customer> customerList = new ArrayList<>();
        JsonArray services = json.getAsJsonArray("services");

        for (JsonElement element : services) {
            JsonObject obj = element.getAsJsonObject();
            JsonObject addr = obj.getAsJsonObject("address");

            Location loc = new Location(
                    addr.get("location_id").getAsString(),
                    addr.get("lat").getAsDouble(),
                    addr.get("lon").getAsDouble()
            );

            Customer c = new Customer();
            c.setId(obj.get("id").getAsString());
            c.setLocation(loc);
            c.setDemand(obj.getAsJsonArray("size").get(0).getAsInt());

            customerList.add(c);
        }

        RouteSolution problem = new RouteSolution();
        problem.setVehicleList(vehicleList);
        problem.setCustomerList(customerList);

        SolverFactory<RouteSolution> solverFactory = SolverFactory.createFromXmlResource("solverConfig.xml");
        Solver<RouteSolution> solver = solverFactory.buildSolver();

        RouteSolution solution = solver.solve(problem);

        List<Map<String, Object>> routeList = new ArrayList<>();

        for (Vehicle v : solution.getVehicleList()) {
            System.out.println("車輛：" + v.getId() + " 實際型別是：" + v.getClass().getName());
            Customer current = v.getNextCustomer();
            System.out.println("→ v.getNextCustomer() 型別是：" + (current != null ? current.getClass().getName() : "null"));
            int order = 1;
            double totalDistance = 0.0;
            Standstill previous = v;

            while (current != null && order < 999) {
                System.out.printf("  第 %d 站 → 商家 %s (%f, %f)｜previous 類型：%s%n",
                        order,
                        current.getId(),
                        current.getLocation().getLatitude(),
                        current.getLocation().getLongitude(),
                        current.getPreviousStandstill() != null ? current.getPreviousStandstill().getClass().getName() : "null");

                double dx = current.getLocation().getLatitude() - previous.getLocation().getLatitude();
                double dy = current.getLocation().getLongitude() - previous.getLocation().getLongitude();
                totalDistance += Math.sqrt(dx * dx + dy * dy) * 111.0;

                Map<String, Object> point = new LinkedHashMap<>();
                point.put("order", order++);
                point.put("id", current.getId());
                point.put("lat", current.getLocation().getLatitude());
                point.put("lon", current.getLocation().getLongitude());
                routeList.add(point);

                previous = current;
                current = current.getNextCustomer();
            }

            System.out.printf("🚛 總距離（大約）：%.2f km%n", totalDistance);
        }

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        FileWriter fw = new FileWriter("best_route.json");
        gson.toJson(routeList, fw);
        fw.close();
        System.out.println("✅ 已輸出最佳路線到 best_route.json");
    }
}
