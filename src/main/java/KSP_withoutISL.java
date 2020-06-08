import ch.ethz.systems.floodns.core.Aftermath;
import ch.ethz.systems.floodns.core.Network;
import ch.ethz.systems.floodns.core.Simulator;
import ch.ethz.systems.floodns.ext.allocator.SimpleMmfAllocator;
import ch.ethz.systems.floodns.ext.basicsim.topology.FileToTopologyConverter;
import ch.ethz.systems.floodns.ext.basicsim.topology.Topology;
import ch.ethz.systems.floodns.ext.logger.file.FileLoggerFactory;
import ch.ethz.systems.floodns.ext.basicsim.schedule.Schedule;
import ch.ethz.systems.floodns.ext.routing.KspMultiPathRoutingStrategy;
import ch.ethz.systems.floodns.ext.routing.KspRoutingStrategy;

import java.util.Random;

public class KSP_withoutISL {
        public static final int DURATION = 1;
        public static final int KSP_K = 1;

        public static void main(String[] args) {
                final int DURATION = Integer.parseInt(args[0]);
                final String RESULTS_FOLDER = args[1];
                final int KSP_K = Integer.parseInt(args[2]);

                String folderPath = "/home/manuelgr/master_thesis/Simulators/FloodNS/results/" + RESULTS_FOLDER;

                // Random number generators
                Random routingRandom = new Random(4839252);

                // Fat-tree topology
                int special = 0;
                if (RESULTS_FOLDER.contains("hyb")) {
                        special = 1584;
                }
                Topology topology = FileToTopologyConverter.convert(
                                folderPath + "/topo/satellite_constellation.properties", 20, 100, true, special);
                Network network = topology.getNetwork();

                // Create simulator
                Simulator simulator = new Simulator();
                FileLoggerFactory loggerFactory = new FileLoggerFactory(simulator, folderPath);
                Aftermath aftermath = new SimpleMmfAllocator(simulator, network);
                simulator.setup(network, aftermath, loggerFactory);

                // Routing
                KspMultiPathRoutingStrategy routingStrategy = new KspMultiPathRoutingStrategy(simulator, topology,
                                routingRandom, KSP_K, folderPath + "/topo/trafficSchedule.properties");

                // Traffic
                Schedule schedule = new Schedule(folderPath + "/topo/trafficSchedule.properties", topology,
                                (long) DURATION * (long) 1e9);

                simulator.insertEvents(schedule.getConnectionStartEvents(simulator, routingStrategy));

                // Run the simulator
                simulator.run((long) DURATION * (long) 1e9); // 5e9 time units ("ns")
                loggerFactory.runCommandOnLogFolder("python3 /home/manuelgr/floodnsexample/external/analyze.py");

        }

}
