import ch.ethz.systems.floodns.core.Aftermath;
import ch.ethz.systems.floodns.core.Network;
import ch.ethz.systems.floodns.core.Simulator;
import ch.ethz.systems.floodns.ext.allocator.SimpleMmfAllocator;
import ch.ethz.systems.floodns.ext.logger.file.FileLoggerFactory;
import ch.ethz.systems.floodns.ext.routing.EcmpRoutingStrategy;
import ch.ethz.systems.floodns.ext.topology.FileToTopologyConverter;
import ch.ethz.systems.floodns.ext.topology.Topology;
import ch.ethz.systems.floodns.ext.topology.TopologyServerExtender;
import ch.ethz.systems.floodns.ext.traffic.PoissonTrafficSchedule;
import ch.ethz.systems.floodns.ext.traffic.StartTrafficSchedule;
import ch.ethz.systems.floodns.ext.traffic.flowsize.FlowSizeDistribution;
import ch.ethz.systems.floodns.ext.traffic.flowsize.UniformFSD;
import ch.ethz.systems.floodns.ext.traffic.pair.AllToAllServerFractionPD;
import ch.ethz.systems.floodns.ext.traffic.pair.PairDistribution;

import java.util.Random;

public class DemoMain {

    public static void main(String[] args) {

        for (int numFlows : new int[]{1, 200, 400, 600, 800, 1000, 1500, 2000, 2500, 3000, 3500}) {

            // Random number generators
            Random routingRandom = new Random(4839252);
            Random trafficRandom = new Random(3275892);
            Random poissonRandom = new Random(8925892);

            // Fat-tree topology
            TopologyServerExtender.extendRegular(
                    "test_topologies/fat_tree/fat_tree_k6.properties",
                    "test_topologies/demo/fat_tree_k6_s3.properties",
                    3 // 2 servers per ToR
            );
            Topology topology = FileToTopologyConverter.convert(
                    "test_topologies/demo/fat_tree_k6_s3.properties",
                    1 // 10 flow units / time unit
            );
            Network network = topology.getNetwork();

            // Create simulator
            Simulator simulator = new Simulator();
            FileLoggerFactory loggerFactory = new FileLoggerFactory(simulator, "temp/demo_" + numFlows);
            Aftermath aftermath = new SimpleMmfAllocator(simulator, network);
            simulator.setup(network, aftermath, loggerFactory);

            // Routing
            EcmpRoutingStrategy routingStrategy = new EcmpRoutingStrategy(simulator, topology, routingRandom);

            // Traffic
            PairDistribution pairDistribution = new AllToAllServerFractionPD(trafficRandom, topology, 1.0, true);
            FlowSizeDistribution flowSizeDistribution = new UniformFSD(1700 * 1000 * 8L); // 100000 flow units for each flow
            PoissonTrafficSchedule trafficSchedule = new PoissonTrafficSchedule(simulator, poissonRandom, topology.getNetwork(), routingStrategy, pairDistribution, flowSizeDistribution);
            trafficSchedule.generate((long) 10e9, numFlows); // Generate 400 connection start events

            // Insert initial events and run simulator
            simulator.insertEvents(trafficSchedule.getConnectionStartEvents());
            simulator.run((long) 10e9); // 10e9 time units
            loggerFactory.runCommandOnLogFolder("python external/analyze.py");

            // Simulation log files are now viewable in: temp/demo
            // Simulation statistical results are now viewable in: temp/demo/analysis

        }

    }

}
