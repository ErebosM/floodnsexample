import ch.ethz.systems.floodns.core.Aftermath;
import ch.ethz.systems.floodns.core.Network;
import ch.ethz.systems.floodns.core.Simulator;
import ch.ethz.systems.floodns.ext.allocator.SimpleMmfAllocator;
import ch.ethz.systems.floodns.ext.basicsim.topology.FileToTopologyConverter;
import ch.ethz.systems.floodns.ext.basicsim.topology.Topology;
import ch.ethz.systems.floodns.ext.logger.file.FileLoggerFactory;
import ch.ethz.systems.floodns.ext.basicsim.schedule.Schedule;
import ch.ethz.systems.floodns.ext.routing.KspRoutingStrategy;

import java.util.Random;

public class KSP_withoutISL {
    public static final int DURATION = 1;
    public static final int KSP_K = 1;

    public static void main(String[] args) {
        for (int i = 20; i <= 20; i += 5) {
            String folderPath = "/topo_" + i + "Tbit_" + DURATION + "s_withoutISL";

            // Random number generators
            Random routingRandom = new Random(4839252);

            // Fat-tree topology
            Topology topology = FileToTopologyConverter.convert(
                    "/home/manuelgr/OneDrive/Master Thesis/Simulators/FloodNS/topologies" + folderPath
                            + "/satellite_constellation.properties",
                    20,
                    20, // 100 flow units / time unit ("bit/ns")
                    true); // 20 flow units / time unit ("bit/ns");
            Network network = topology.getNetwork();

            // Create simulator
            Simulator simulator = new Simulator();
            FileLoggerFactory loggerFactory = new FileLoggerFactory(simulator, "temp" + folderPath);
            Aftermath aftermath = new SimpleMmfAllocator(simulator, network);
            simulator.setup(network, aftermath, loggerFactory);

            // Routing
            KspRoutingStrategy routingStrategy = new KspRoutingStrategy(simulator, topology, routingRandom, KSP_K);

            // Traffic
            Schedule schedule = new Schedule("/home/manuelgr/OneDrive/Master Thesis/Simulators/FloodNS/topologies"
                    + folderPath + "/trafficSchedule.properties", topology, (long) DURATION * (long) 1e9);
            simulator.insertEvents(schedule.getConnectionStartEvents(simulator, routingStrategy));

            // Run the simulator
            simulator.run((long) DURATION * (long) 1e9); // 5e9 time units ("ns")
            loggerFactory.runCommandOnLogFolder("python external/analyze.py");
        }

    }

}
