import ch.ethz.systems.floodns.core.Aftermath;
import ch.ethz.systems.floodns.core.Network;
import ch.ethz.systems.floodns.core.Simulator;
import ch.ethz.systems.floodns.ext.allocator.SimpleMmfAllocator;
import ch.ethz.systems.floodns.ext.basicsim.topology.FileToTopologyConverter;
import ch.ethz.systems.floodns.ext.basicsim.topology.Topology;
import ch.ethz.systems.floodns.ext.logger.file.FileLoggerFactory;
import ch.ethz.systems.floodns.ext.basicsim.schedule.Schedule;
import ch.ethz.systems.floodns.ext.routing.KspMultiPathRoutingStrategy;

public class KSP_withoutISL {
        public static void main(String[] args) {
                final int DURATION = Integer.parseInt(args[0]);
                final String RESULTS_FOLDER = args[1];
                final int KSP_K = Integer.parseInt(args[2]);
                final String PATHFILE = args[3];
                final long TBIT_TRAFFIC = (long) Double.parseDouble(args[4]);
                final int ISL_CAPACITY = Integer.parseInt(args[5]);

                String folderPath = "/home/manuelgr/master_thesis/Simulators/FloodNS/results_paper/" + RESULTS_FOLDER;
                String pathfile = "/home/manuelgr/master_thesis/Simulators/FloodNS/results_paper/" + PATHFILE;
                String constpath = "";
                if (RESULTS_FOLDER.contains("kuiper")) {
                        constpath = "/home/manuelgr/master_thesis/Simulators/FloodNS/results_paper/kuiper";
                } else {
                        constpath = "/home/manuelgr/master_thesis/Simulators/FloodNS/results_paper/starlink";
                }

                // Random number generators
                // Random routingRandom = new Random(4839252);

                // Fat-tree topology
                int special = 0;
                if (RESULTS_FOLDER.contains("hyb")) {
                        if (RESULTS_FOLDER.contains("kuiper")) {
                                special = 1156;
                        } else {
                                special = 1584;
                        }
                }

                Topology topology;
                if (RESULTS_FOLDER.contains("hyb")) {
                        topology = FileToTopologyConverter.convert(
                                        constpath + "/hyb_satellite_constellation.properties", 20, ISL_CAPACITY, true,
                                        special);
                } else if (RESULTS_FOLDER.contains("alt")) {
                        topology = FileToTopologyConverter.convert(
                                        constpath + "/alt_satellite_constellation.properties", 20, ISL_CAPACITY, true,
                                        special);
                } else {
                        topology = FileToTopologyConverter.convert(
                                        constpath + "/sat_satellite_constellation.properties", 20, ISL_CAPACITY, true,
                                        special);
                }
                Network network = topology.getNetwork();

                // Create simulator
                Simulator simulator = new Simulator();
                FileLoggerFactory loggerFactory = new FileLoggerFactory(simulator, folderPath);
                Aftermath aftermath = new SimpleMmfAllocator(simulator, network);
                simulator.setup(network, aftermath, loggerFactory);

                // Routing
                KspMultiPathRoutingStrategy routingStrategy = new KspMultiPathRoutingStrategy(simulator, topology,
                                KSP_K, pathfile, true);

                // Traffic
                Schedule schedule;
                if (RESULTS_FOLDER.contains("hyb")) {
                        schedule = new Schedule(constpath + "/hyb_trafficSchedule.properties", topology,
                                        (long) DURATION * (long) 1e9, TBIT_TRAFFIC);
                } else if (RESULTS_FOLDER.contains("alt")) {
                        schedule = new Schedule(constpath + "/alt_trafficSchedule.properties", topology,
                                        (long) DURATION * (long) 1e9, TBIT_TRAFFIC);
                } else {
                        schedule = new Schedule(constpath + "/sat_trafficSchedule.properties", topology,
                                        (long) DURATION * (long) 1e9, TBIT_TRAFFIC);
                }

                simulator.insertEvents(schedule.getConnectionStartEvents(simulator, routingStrategy));

                // Run the simulator
                simulator.run((long) DURATION * (long) 1e9); // 5e9 time units ("ns")
                loggerFactory.runCommandOnLogFolder("python3 /home/manuelgr/floodnsexample/external/analyze.py");

        }

}
