import ch.ethz.systems.floodns.core.Aftermath;
import ch.ethz.systems.floodns.core.Network;
import ch.ethz.systems.floodns.core.Simulator;
import ch.ethz.systems.floodns.ext.allocator.SimpleMmfAllocator;
import ch.ethz.systems.floodns.ext.basicsim.topology.FileToTopologyConverter;
import ch.ethz.systems.floodns.ext.basicsim.topology.Topology;
import ch.ethz.systems.floodns.ext.logger.file.FileLoggerFactory;
import ch.ethz.systems.floodns.ext.basicsim.schedule.Schedule;
import ch.ethz.systems.floodns.ext.routing.KspMultiPathRoutingStrategy;

public class KSP_v3 {
    public static void main(String[] args) {

        final int DURATION = Integer.parseInt(args[0]);
        final String RESULTS_FOLDER_NAME = args[1];
        final String FOLDER_NAME = args[2];
        final int KSP_K = Integer.parseInt(args[3]);
        final int UPDOWN_CAPACITY = Integer.parseInt(args[4]);
        final int ISL_CAPACITY = Integer.parseInt(args[5]);

        String folderPath = "/home/manuelgr/master_thesis/Simulators/FloodNS/" + RESULTS_FOLDER_NAME + "/"
                + FOLDER_NAME;

        Topology topology = FileToTopologyConverter.convert(folderPath + "/topo/satellite_constellation.properties",
                UPDOWN_CAPACITY, ISL_CAPACITY, true);
        Network network = topology.getNetwork();

        // Create simulator
        Simulator simulator = new Simulator();
        FileLoggerFactory loggerFactory = new FileLoggerFactory(simulator, folderPath);
        Aftermath aftermath = new SimpleMmfAllocator(simulator, network);
        simulator.setup(network, aftermath, loggerFactory);

        // Routing
        KspMultiPathRoutingStrategy routingStrategy = new KspMultiPathRoutingStrategy(simulator, topology, KSP_K);

        // Traffic
        Schedule schedule = new Schedule(folderPath + "/topo/trafficSchedule.properties", topology,
                (long) DURATION * (long) 1e9);
        simulator.insertEvents(schedule.getConnectionStartEvents(simulator, routingStrategy));

        // Run the simulator
        simulator.run((long) DURATION * (long) 1e9);
        loggerFactory.runCommandOnLogFolder("python3 /home/manuelgr/floodnsexample/external/analyze.py");

    }

}
