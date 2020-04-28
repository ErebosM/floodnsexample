import ch.ethz.systems.floodns.core.Aftermath;
import ch.ethz.systems.floodns.core.Network;
import ch.ethz.systems.floodns.core.Simulator;
import ch.ethz.systems.floodns.ext.allocator.SimpleMmfAllocator;
import ch.ethz.systems.floodns.ext.basicsim.schedule.ScheduleEntry;
import ch.ethz.systems.floodns.ext.basicsim.schedule.TrafficSchedule;
import ch.ethz.systems.floodns.ext.basicsim.topology.FileToTopologyConverter;
import ch.ethz.systems.floodns.ext.basicsim.topology.Topology;
import ch.ethz.systems.floodns.ext.logger.file.FileLoggerFactory;
import ch.ethz.systems.floodns.ext.routing.EcmpRoutingStrategy;
import ch.ethz.systems.floodns.ext.basicsim.schedule.Schedule;
import ch.ethz.systems.floodns.ext.routing.KspRoutingStrategy;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Random;

public class KSP_v3 {

    public static void main(String[] args) {
        for (int k = 1; k <= 20; k++) {
            if(k != 1 && k != 2 && k != 4 && k != 10 && k!= 20) {
                continue;
            }
            for (int curr = 1; curr <= 10; curr++) {
                // Random number generators
                Random routingRandom = new Random(4839252);

                // Fat-tree topology
                Topology topology = FileToTopologyConverter.convert(
                        "/home/manuelgr/OneDrive/Master Thesis/Simulators/FloodNS/topologies/satellite_constellation_" + curr + ".properties",
                        20, // 20 flow units / time unit ("bit/ns")
                        true
                );
                Network network = topology.getNetwork();

                // Create simulator
                Simulator simulator = new Simulator();
                FileLoggerFactory loggerFactory = new FileLoggerFactory(simulator, "temp/ksp" + k + "/sat_const_" + curr);
                Aftermath aftermath = new SimpleMmfAllocator(simulator, network);
                simulator.setup(network, aftermath, loggerFactory);

                // Routing
                KspRoutingStrategy routingStrategy = new KspRoutingStrategy(simulator, topology, routingRandom, k);
                //EcmpRoutingStrategy routingStrategy = new EcmpRoutingStrategy(simulator, topology, routingRandom);

                // Traffic
                //TrafficSchedule trafficSchedule = new TrafficSchedule(simulator, network, routingStrategy);
                //trafficSchedule.addConnectionStartEvent(1600, 1601, 100000, 0); // "0 -> 1 send 100000 bit starting at t=0
                Schedule schedule = new Schedule("/home/manuelgr/OneDrive/Master Thesis/Simulators/FloodNS/topologies/trafficSchedule.properties", topology, (long) 1e9);
                simulator.insertEvents(schedule.getConnectionStartEvents(simulator, routingStrategy));

                // Run the simulator
                simulator.run((long) 1e9); // 1e9 time units ("ns")
                loggerFactory.runCommandOnLogFolder("python external/analyze.py");

                // Simulation log files are now viewable in: demo_logs
                // Simulation statistical results are now viewable in: demo_logs/analysis
            }
        }
    }

}
