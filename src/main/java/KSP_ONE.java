import ch.ethz.systems.floodns.core.*;
import ch.ethz.systems.floodns.ext.allocator.SimpleMmfAllocator;
import ch.ethz.systems.floodns.ext.basicsim.topology.FileToTopologyConverter;
import ch.ethz.systems.floodns.ext.basicsim.topology.Topology;
import ch.ethz.systems.floodns.ext.logger.file.FileLoggerFactory;
import ch.ethz.systems.floodns.ext.basicsim.schedule.Schedule;
import ch.ethz.systems.floodns.ext.routing.KspRoutingStrategy;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class KSP_ONE {
    public static final int DURATION = 60;
    public static final int KSP_K = 1;

    private static class AddEvent extends Event {
        private final int from;
        private final int to;

        AddEvent(Simulator simulator, int priority, long timeFromNow, int from, int to) {
            super(simulator, priority, timeFromNow);
            this.from = from;
            this.to = to;
        }

        @Override
        protected void trigger() {
            simulator.addNewLink(from, to, 20);
            System.out.println("Added Link from " + from + " to " + to);
        }

    }

    private static class UpdateEvent extends Event {
        private final int from;
        private final int to;
        private final double newLatency;

        UpdateEvent(Simulator simulator, int priority, long timeFromNow, int from, int to, double newLatency) {
            super(simulator, priority, timeFromNow);
            this.from = from;
            this.to = to;
            this.newLatency = newLatency;
        }

        @Override
        protected void trigger() {
            List<Link> linksBetween = simulator.getNetwork().getPresentLinksBetween(from, to);
            if (linksBetween.size() != 1) {
                System.out.println("ERROR: linksBetween is != 1. It is " + linksBetween.size());
            }
            simulator.updateExistingLink(linksBetween.get(0), newLatency);
            System.out.println("Updated Link from " + from + " to " + to + " with latency " + newLatency);
        }

    }

    private static class RemoveEvent extends Event {
        private final int from;
        private final int to;

        RemoveEvent(Simulator simulator, int priority, long timeFromNow, int from, int to) {
            super(simulator, priority, timeFromNow);
            this.from = from;
            this.to = to;
        }

        @Override
        protected void trigger() {
            List<Link> linksBetween = simulator.getNetwork().getPresentLinksBetween(from, to);
            if (linksBetween.size() != 1) {
                System.out.println("ERROR: linksBetween is != 1. It is " + linksBetween.size());
            }
            simulator.removeExistingLink(linksBetween.get(0));
            System.out.println("Removed Link from " + from + " to " + to);
        }

    }

    private static class TopologyUpdateEvent extends Event {
        private final Topology topology;
        private final Random routingRandom;

        TopologyUpdateEvent(Simulator simulator, int priority, long timeFromNow, Topology topology,
                Random routingRandom) {
            super(simulator, priority, timeFromNow);
            this.topology = topology;
            this.routingRandom = routingRandom;
        }

        @Override
        protected void trigger() {
            simulator.getNetwork().finalizeFlows();
            KspRoutingStrategy routingStrategy = new KspRoutingStrategy(simulator, topology, routingRandom, KSP_K);
            for (Connection connection : simulator.getActiveConnections()) {
                routingStrategy.assignStartFlows(connection);
            }
        }

    }

    public static List<Event> getAddEvents(Simulator simulator, String folderPath) {
        List<Event> events = new ArrayList<>();

        for (int i = 1; i < DURATION; i++) {
            try (BufferedReader br = new BufferedReader(
                    new FileReader("/home/manuelgr/OneDrive/Master Thesis/Simulators/FloodNS/topologies" + folderPath
                            + "/add_" + i + ".properties"))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] split_line = line.split("-");
                    AddEvent event = new AddEvent(simulator, 10, (long) i * (long) 1e9, Integer.parseInt(split_line[0]),
                            Integer.parseInt(split_line[1]));
                    events.add(event);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return events;
    }

    public static List<Event> getUpdateEvents(Simulator simulator, String folderPath) {
        List<Event> events = new ArrayList<>();

        for (int i = 1; i < DURATION; i++) {
            try (BufferedReader br = new BufferedReader(
                    new FileReader("/home/manuelgr/OneDrive/Master Thesis/Simulators/FloodNS/topologies" + folderPath
                            + "/update_" + i + ".properties"))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] split_line = line.split("-");
                    UpdateEvent event = new UpdateEvent(simulator, 10, (long) i * (long) 1e9,
                            Integer.parseInt(split_line[0]), Integer.parseInt(split_line[1]),
                            Double.parseDouble(split_line[2]));
                    events.add(event);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return events;
    }

    public static List<Event> getRemoveEvents(Simulator simulator, String folderPath) {
        List<Event> events = new ArrayList<>();

        for (int i = 1; i < DURATION; i++) {
            try (BufferedReader br = new BufferedReader(
                    new FileReader("/home/manuelgr/OneDrive/Master Thesis/Simulators/FloodNS/topologies" + folderPath
                            + "/remove_" + i + ".properties"))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] split_line = line.split("-");
                    RemoveEvent event = new RemoveEvent(simulator, 10, (long) i * (long) 1e9,
                            Integer.parseInt(split_line[0]), Integer.parseInt(split_line[1]));
                    events.add(event);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return events;
    }

    public static List<Event> getTopologyUpdateEvents(Simulator simulator, Topology topology, Random routingRandom) {
        List<Event> events = new ArrayList<>();

        for (int i = 1; i < DURATION; i++) {
            TopologyUpdateEvent event = new TopologyUpdateEvent(simulator, 0, (long) i * (long) 1e9, topology,
                    routingRandom);
            events.add(event);
        }

        return events;
    }

    public static void main(String[] args) {
        for (int i = 50; i <= 50; i += 5) {
            String folderPath = "/topo_" + i + "Tbit_" + DURATION + "s";

            // Random number generators
            Random routingRandom = new Random(4839252);

            // Fat-tree topology
            Topology topology = FileToTopologyConverter.convert(
                    "/home/manuelgr/OneDrive/Master Thesis/Simulators/FloodNS/topologies" + folderPath
                            + "/satellite_constellation.properties",
                    20, // 20 flow units / time unit ("bit/ns")
                    100, // 100 flow units / time unit ("bit/ns")
                    true);
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

            simulator.insertEvents(getAddEvents(simulator, folderPath));
            simulator.insertEvents(getUpdateEvents(simulator, folderPath));
            simulator.insertEvents(getRemoveEvents(simulator, folderPath));

            simulator.insertEvents(getTopologyUpdateEvents(simulator, topology, routingRandom));

            // Run the simulator
            simulator.run((long) DURATION * (long) 1e9); // 5e9 time units ("ns")
            loggerFactory.runCommandOnLogFolder("python external/analyze.py");
        }

    }
}
