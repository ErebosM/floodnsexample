import ch.ethz.systems.floodns.core.Aftermath;
import ch.ethz.systems.floodns.core.Network;
import ch.ethz.systems.floodns.core.Simulator;
import ch.ethz.systems.floodns.ext.allocator.SimpleMmfAllocator;
import ch.ethz.systems.floodns.ext.logger.file.FileLoggerFactory;
import ch.ethz.systems.floodns.ext.routing.EcmpRoutingStrategy;
import ch.ethz.systems.floodns.ext.topology.FileToTopologyConverter;
import ch.ethz.systems.floodns.ext.topology.Topology;
import ch.ethz.systems.floodns.ext.topology.TopologyServerExtender;
import ch.ethz.systems.floodns.ext.traffic.StartTrafficSchedule;
import ch.ethz.systems.floodns.ext.traffic.flowsize.FlowSizeDistribution;
import ch.ethz.systems.floodns.ext.traffic.flowsize.UniformFSD;
import ch.ethz.systems.floodns.ext.traffic.pair.AllToAllServerFractionPD;
import ch.ethz.systems.floodns.ext.traffic.pair.PairDistribution;

import java.util.Random;

public class DemoMain {

    public static void main(String[] args) {

        // Random number generators
        Random routingRandom = new Random(4839252);
        Random trafficRandom = new Random(3275892);

        // Fat-tree topology
        TopologyServerExtender.extendRegular(
                "test_topologies/demo/fat_tree_k4.properties",
                "test_topologies/demo/fat_tree_k4_s2.properties",
                2 // 2 servers per ToR
        );
        Topology topology = FileToTopologyConverter.convert(
                "test_topologies/demo/fat_tree_k4_s2.properties",
                10 // 10 flow units / time unit
        );
        Network network = topology.getNetwork();

        // Create simulator
        Simulator simulator = new Simulator();
        FileLoggerFactory loggerFactory = new FileLoggerFactory(simulator, "temp/demo");
        Aftermath aftermath = new SimpleMmfAllocator(simulator, network);
        simulator.setup(network, aftermath, loggerFactory);

        // Routing
        EcmpRoutingStrategy routingStrategy = new EcmpRoutingStrategy(simulator, topology, routingRandom);

        // Traffic
        PairDistribution pairDistribution = new AllToAllServerFractionPD(trafficRandom, topology, 1.0, true);
        FlowSizeDistribution flowSizeDistribution = new UniformFSD(100000); // 100000 flow units for each flow
        StartTrafficSchedule trafficSchedule = new StartTrafficSchedule(simulator, topology.getNetwork(), routingStrategy, pairDistribution, flowSizeDistribution);
        trafficSchedule.generate(400); // Generate 400 connection start events

        // Insert initial events and run simulator
        simulator.insertEvents(trafficSchedule.getConnectionStartEvents());
        simulator.run((long) 10e9); // 10e9 time units
        loggerFactory.runCommandOnLogFolder("python external/analyze.py");

        // Simulation log files are now viewable in: temp/demo
        // Simulation statistical results are now viewable in: temp/demo/analysis

    }

}
