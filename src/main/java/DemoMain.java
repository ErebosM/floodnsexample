import ch.ethz.systems.floodns.core.Aftermath;
import ch.ethz.systems.floodns.core.Network;
import ch.ethz.systems.floodns.core.Simulator;
import ch.ethz.systems.floodns.ext.allocator.SimpleMmfAllocator;
import ch.ethz.systems.floodns.ext.basicsim.schedule.TrafficSchedule;
import ch.ethz.systems.floodns.ext.basicsim.topology.FileToTopologyConverter;
import ch.ethz.systems.floodns.ext.basicsim.topology.Topology;
import ch.ethz.systems.floodns.ext.logger.file.FileLoggerFactory;
import ch.ethz.systems.floodns.ext.routing.EcmpRoutingStrategy;

import java.util.Random;

public class DemoMain {

    public static void main(String[] args) {

        // Random number generators
        Random routingRandom = new Random(1234567);

        // Fat-tree topology
        Topology topology = FileToTopologyConverter.convert(
                "test_data/1_to_1.properties",
                10 // 10 flow units / time unit ("bit/ns")
        );
        Network network = topology.getNetwork();

        // Create simulator
        Simulator simulator = new Simulator();
        FileLoggerFactory loggerFactory = new FileLoggerFactory(simulator, "demo_logs");
        Aftermath aftermath = new SimpleMmfAllocator(simulator, network);
        simulator.setup(network, aftermath, loggerFactory);

        // Routing
        EcmpRoutingStrategy routingStrategy = new EcmpRoutingStrategy(simulator, topology, routingRandom);

        // Traffic
        TrafficSchedule trafficSchedule = new TrafficSchedule(simulator, network, routingStrategy);
        trafficSchedule.addConnectionStartEvent(0, 1, 100000, 0); // "0 -> 1 send 100000 bit starting at t=0
        simulator.insertEvents(trafficSchedule.getConnectionStartEvents());

        // Run the simulator
        simulator.run((long) 10e9); // 10e9 time units ("ns")
        loggerFactory.runCommandOnLogFolder("python external/analyze.py");

        // Simulation log files are now viewable in: demo_logs
        // Simulation statistical results are now viewable in: demo_logs/analysis

    }

}
