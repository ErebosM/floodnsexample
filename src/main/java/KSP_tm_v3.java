import ch.ethz.systems.floodns.core.Aftermath;
import ch.ethz.systems.floodns.core.Network;
import ch.ethz.systems.floodns.core.Simulator;
import ch.ethz.systems.floodns.ext.allocator.SimpleMmfAllocator;
import ch.ethz.systems.floodns.ext.basicsim.topology.FileToTopologyConverter;
import ch.ethz.systems.floodns.ext.basicsim.topology.Topology;
import ch.ethz.systems.floodns.ext.logger.file.FileLoggerFactory;
import ch.ethz.systems.floodns.ext.basicsim.schedule.Schedule;
import ch.ethz.systems.floodns.ext.routing.KspMultiPathRoutingStrategy;

public class KSP_tm_v3 {
        public static void main(String[] args) {

                final int DURATION = Integer.parseInt(args[0]);
                final String RESULTS_FOLDER_NAME = args[1];
                final String FOLDER_NAME = args[2];
                final int KSP_K = Integer.parseInt(args[3]);
                final double UPDOWN_CAPACITY = Double.parseDouble(args[4]);
                final double ISL_CAPACITY = Double.parseDouble(args[5]);
                final int UPPER_SAT_ID = Integer.parseInt(args[6]);
                final int NUM_CITIES = Integer.parseInt(args[7]);
                final String PATH_FILE = args[8];
                final String ROUTING_TYPE = args[9];
                final String GRAPH_DIR = args[10];
                final int CURR_ITERATION = Integer.parseInt(args[11]);

                String folderPath = "/home/manuelgr/master_thesis/Simulators/FloodNS/" + RESULTS_FOLDER_NAME + "/"
                                + FOLDER_NAME;
                String path_file = "/home/manuelgr/master_thesis/Simulators/FloodNS/" + PATH_FILE;

                Topology topology = FileToTopologyConverter.convert(
                                GRAPH_DIR + "/satellite_constellation_" + CURR_ITERATION + ".properties",
                                UPDOWN_CAPACITY, ISL_CAPACITY, true, UPPER_SAT_ID);
                Network network = topology.getNetwork();

                // Create simulator
                Simulator simulator = new Simulator(1e-4);
                FileLoggerFactory loggerFactory = new FileLoggerFactory(simulator, folderPath);
                Aftermath aftermath = new SimpleMmfAllocator(simulator, network);
                simulator.setup(network, aftermath, loggerFactory);

                // Routing
                KspMultiPathRoutingStrategy routingStrategy = new KspMultiPathRoutingStrategy(simulator, topology,
                                KSP_K, UPPER_SAT_ID, NUM_CITIES, path_file, ROUTING_TYPE, folderPath);

                // Traffic
                Schedule schedule = new Schedule(GRAPH_DIR + "/trafficSchedule.properties", topology, 1);
                simulator.insertEvents(schedule.getConnectionStartEvents(simulator, routingStrategy));

                // Run the simulator
                simulator.run(1);
                loggerFactory.runCommandOnLogFolder("python3 /home/manuelgr/floodnsexample/external/analyze.py",
                                UPPER_SAT_ID + " " + NUM_CITIES);
        }

}
