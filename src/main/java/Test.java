import ch.ethz.systems.floodns.core.*;
import ch.ethz.systems.floodns.ext.allocator.SimpleMmfAllocator;
import ch.ethz.systems.floodns.ext.basicsim.schedule.TrafficSchedule;
import ch.ethz.systems.floodns.ext.basicsim.topology.FileToTopologyConverter;
import ch.ethz.systems.floodns.ext.basicsim.topology.Topology;
import ch.ethz.systems.floodns.ext.logger.file.FileLoggerFactory;
import ch.ethz.systems.floodns.ext.routing.EcmpRoutingStrategy;
import ch.ethz.systems.floodns.ext.routing.KspRoutingStrategy;

import java.util.Random;

public class Test {

    public static void main(String[] args) {
        class RemoveEvent extends Event {

            private long time;

            RemoveEvent(Simulator simulator, long timeFromNow) {
                super(simulator, 0, timeFromNow);
                this.time = timeFromNow;
            }

            @Override
            protected void trigger() {

                this.simulator.removeExistingLink(4);
         }

        }

        class AddEvent extends Event {

            private long time;

            AddEvent(Simulator simulator, long timeFromNow) {
                super(simulator, 0, timeFromNow);
                this.time = timeFromNow;
            }

            @Override
            protected void trigger() {

                Link link = this.simulator.addNewLink(0, 3, 10);
                System.out.println(link);
                System.out.println(link.getLinkId());
            }

        }


        // Random number generators
        Random routingRandom = new Random(1234567);

        // Fat-tree topology
        Topology topology = FileToTopologyConverter.convert(
                "test_data/11simple_but_advanced.properties",
                10 // 10 flow units / time unit ("bit/ns")
        );
        Network network = topology.getNetwork();

        // Create simulator
        Simulator simulator = new Simulator();
        FileLoggerFactory loggerFactory = new FileLoggerFactory(simulator, "demo_logs");
        Aftermath aftermath = new SimpleMmfAllocator(simulator, network);
        simulator.setup(network, aftermath, loggerFactory);

        // Routing
        KspRoutingStrategy routingStrategy = new KspRoutingStrategy(simulator, topology, routingRandom, 1);

        // Traffic
        TrafficSchedule trafficSchedule = new TrafficSchedule(simulator, network, routingStrategy);
        trafficSchedule.addConnectionStartEvent(0, 3, 100000, 0); // "0 -> 1 send 100000 bit starting at t=0"
        simulator.insertEvents(trafficSchedule.getConnectionStartEvents());

        // Run the simulator
        simulator.insertEvents(new AddEvent(simulator, 0));
        simulator.insertEvents(new RemoveEvent(simulator, 1));

        simulator.run((long) 1e9); // 10e9 time units ("ns")
        loggerFactory.runCommandOnLogFolder("python external/analyze.py");

        // Simulation log files are now viewable in: demo_logs
        // Simulation statistical results are now viewable in: demo_logs/analysis

    }

}
