package org.example.solver;

import org.example.domain.Customer;
import org.example.domain.RouteSolution;
import org.example.domain.Standstill;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
import org.optaplanner.core.api.score.stream.*;

public class RouteConstraintProvider implements ConstraintProvider {

    @Override
    public Constraint[] defineConstraints(ConstraintFactory factory) {
        return new Constraint[]{
                vehicleCapacity(factory),
                totalDistance(factory)
        };
    }

    private Constraint vehicleCapacity(ConstraintFactory factory) {
        return factory.from(Customer.class)
                .groupBy(Customer::getPreviousStandstill, ConstraintCollectors.sum(Customer::getDemand))
                .filter((standstill, totalDemand) ->
                        standstill instanceof org.example.domain.Vehicle &&
                        totalDemand > ((org.example.domain.Vehicle) standstill).getCapacity())
                .penalize("Over capacity", HardSoftScore.ONE_HARD,
                        (standstill, totalDemand) ->
                                totalDemand - ((org.example.domain.Vehicle) standstill).getCapacity());
    }

    private Constraint totalDistance(ConstraintFactory factory) {
        return factory.from(Customer.class)
                .filter(customer -> customer.getPreviousStandstill() != null)
                .penalize("Total distance", HardSoftScore.ONE_SOFT, customer -> {
                    Standstill from = customer.getPreviousStandstill();
                    double dx = from.getLocation().getLatitude() - customer.getLocation().getLatitude();
                    double dy = from.getLocation().getLongitude() - customer.getLocation().getLongitude();
                    return (int) (Math.sqrt(dx * dx + dy * dy) * 100000); // approximate
                });
    }
}
