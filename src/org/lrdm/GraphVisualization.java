package org.lrdm;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.swing_viewer.DefaultView;
import org.graphstream.ui.swing_viewer.SwingViewer;
import org.graphstream.ui.view.View;
import org.graphstream.ui.view.Viewer;
import org.knowm.xchart.QuickChart;
import org.knowm.xchart.XChartPanel;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYSeries;
import org.knowm.xchart.style.markers.SeriesMarkers;
import org.knowm.xchart.style.theme.MatlabTheme;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**Graphical visualisation of the simulator using the GraphStream library.
 *
 * @author Sebastian Götz <sebastian.goetz1@tu-dresden.de>
 */
public class GraphVisualization implements VisualizationStrategy {
    private static final int WIDTH = Math.round(Toolkit.getDefaultToolkit().getScreenSize().width * 0.9f);
    private static final int HEIGHT = Math.round(Toolkit.getDefaultToolkit().getScreenSize().height * 0.9f);
    private static final String UI_CLASS = "ui.class";
    private static final String BANDWIDTH = "% Bandwidth";
    private static final String ACTIVE_LINKS = "% Active Links";
    private static final String TTW = "% Time to Write";
    private static final String TIMESTEP = "Timestep";

    private static final String RATIO = "% Ratio";

    private static final String TIME_STEP = "Timestep";
    private Graph graph;
    private JLabel simTimeLabel;
    private XYChart bandwidthChart;
    private XYChart activeLinksChart;
    private XYChart timeToWriteChart;

    private XYChart ratioChart;
    private JPanel chartPanel;
    private JPanel linkChartPanel;
    private JPanel ttwChartPanel;

    private JPanel ratioChartPanel;

    @Override
    public void init(Network network) {
        graph = new SingleGraph("Runtime View");
        String css = loadGraphCSS();
        graph.setAttribute("ui.stylesheet", css);
        Viewer viewer = new SwingViewer(graph, Viewer.ThreadingModel.GRAPH_IN_ANOTHER_THREAD);
        View view = viewer.addDefaultView(false);
        viewer.enableAutoLayout();
        if(view instanceof DefaultView dv) {
            createUI(dv);
        }
        for (Mirror m : network.getMirrors()) {
            Node n = graph.addNode(String.valueOf(m.getID()));
            n.setAttribute(UI_CLASS, "starting");
        }
        for (Link l : network.getLinks()) {
            String sid = String.valueOf(l.getSource().getID());
            String tid = String.valueOf(l.getTarget().getID());
            Edge e = graph.addEdge("s" + sid + "t" + tid, sid, tid);
            e.setAttribute(UI_CLASS, "inactive");
        }
    }

    private void createUI(DefaultView dv) {
        JFrame frame = new JFrame();
        JPanel panel = new JPanel();

        GridBagLayout gl = new GridBagLayout();
        GridBagConstraints gc = new GridBagConstraints();
        panel.setLayout(gl);

        simTimeLabel = new JLabel();
        simTimeLabel.setText("Simulation Time: 0");
        simTimeLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gc.gridx=0;
        gc.gridy=0;
        gc.gridwidth=1;
        gc.gridheight=1;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gl.setConstraints(simTimeLabel, gc);
        panel.add(simTimeLabel);

        gc = new GridBagConstraints();
        gc.gridx=0;
        gc.gridy=1;
        gc.gridwidth=1;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gl.setConstraints(dv, gc);
        dv.setMinimumSize(new Dimension(WIDTH,HEIGHT/2));
        dv.setPreferredSize(new Dimension(WIDTH, HEIGHT/2));
        panel.add(dv);

        bandwidthChart = QuickChart.getChart("Bandwidth over Time",TIMESTEP,BANDWIDTH,BANDWIDTH, List.of(0), List.of(0));
        bandwidthChart.getStyler().setTheme(new MatlabTheme());
        bandwidthChart.getStyler().setLegendVisible(false);
        XYSeries targetBW = bandwidthChart.addSeries("Target",List.of(0),List.of(0));
        targetBW.setMarker(SeriesMarkers.NONE);

        chartPanel = new XChartPanel<>(bandwidthChart);
        gc = new GridBagConstraints();
        gc.gridx=0;
        gc.gridy=2;
        gc.gridwidth=1;
        gl.setConstraints(chartPanel, gc);
        chartPanel.setMinimumSize(new Dimension(WIDTH,HEIGHT/6));
        chartPanel.setMaximumSize(new Dimension(WIDTH,HEIGHT/6));
        panel.add(chartPanel);

        activeLinksChart = QuickChart.getChart("Active Links", TIMESTEP, ACTIVE_LINKS, ACTIVE_LINKS, List.of(0), List.of(0));
        activeLinksChart.getStyler().setTheme(new MatlabTheme());
        activeLinksChart.getStyler().setLegendVisible(false);
        XYSeries targetAL = activeLinksChart.addSeries("Target Active Links",List.of(0),List.of(0));
        targetAL.setMarker(SeriesMarkers.NONE);
        linkChartPanel = new XChartPanel<>(activeLinksChart);
        gc = new GridBagConstraints();
        gc.gridx=0;
        gc.gridy=3;
        gc.gridwidth=1;
        gl.setConstraints(linkChartPanel, gc);
        linkChartPanel.setMinimumSize(new Dimension(WIDTH,HEIGHT/6));
        linkChartPanel.setMaximumSize(new Dimension(WIDTH,HEIGHT/6));
        panel.add(linkChartPanel);

        timeToWriteChart = QuickChart.getChart("Time To Write", TIMESTEP, TTW, TTW, List.of(0), List.of(0));
        timeToWriteChart.getStyler().setTheme(new MatlabTheme());
        timeToWriteChart.getStyler().setLegendVisible(false);
        XYSeries targetTTW = timeToWriteChart.addSeries("Target Time To Write",List.of(0),List.of(0));
        targetTTW.setMarker(SeriesMarkers.NONE);
        ttwChartPanel = new XChartPanel<>(timeToWriteChart);
        gc = new GridBagConstraints();
        gc.gridx=0;
        gc.gridy=4;
        gc.gridwidth=1;
        gl.setConstraints(ttwChartPanel, gc);
        ttwChartPanel.setMinimumSize(new Dimension(WIDTH,HEIGHT/6));
        ttwChartPanel.setMaximumSize(new Dimension(WIDTH,HEIGHT/6));
        ttwChartPanel.setPreferredSize(new Dimension(WIDTH,HEIGHT/6));
        panel.add(ttwChartPanel);

        ratioChart = new XYChart(600, 400);
        ratioChart.setTitle("DirtyFlags Ratio");
        ratioChart.setXAxisTitle(TIME_STEP);
        ratioChart.setYAxisTitle(RATIO);
        ratioChart.getStyler().setTheme(new MatlabTheme());
        ratioChart.getStyler().setLegendVisible(false);
        targetTTW.setMarker(SeriesMarkers.NONE);
        ratioChartPanel = new XChartPanel<>(ratioChart);
        gc = new GridBagConstraints();
        gc.gridx=0;
        gc.gridy=5;
        gc.gridwidth=1;
        gl.setConstraints(ratioChartPanel, gc);
        ratioChartPanel.setMinimumSize(new Dimension(WIDTH,HEIGHT/6));
        ratioChartPanel.setMaximumSize(new Dimension(WIDTH,HEIGHT/6));
        panel.add(ratioChartPanel);

        JScrollPane scrollPane = new JScrollPane(panel, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setMinimumSize(new Dimension(WIDTH, HEIGHT));
        panel.setBackground(Color.WHITE);

        frame.add(scrollPane);
        frame.setTitle("Timed RDM Simulator");

        frame.setSize(WIDTH,HEIGHT);
        frame.setVisible(true);
        frame.setBackground(Color.WHITE);

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
    }

    @Override
    public void updateGraph(Network network, long timeStep) {
        updateTimeStep(network, timeStep);
        updateMirrors(network);
        removeVanishedMirrors(network);
        removeVanishedLinks(network);
        updateLinks(network);
    }

    private void updateTimeStep(Network network, long timeStep) {
        simTimeLabel.setText("Simulation Time: "+timeStep);

        List<Integer> timeSteps = new ArrayList<>(network.getBandwidthHistory().keySet());
        List<Integer> bandwidthTS = new ArrayList<>(network.getBandwidthHistory().values());
        List<Integer> bandwidthGoalTS = Collections.nCopies(network.getBandwidthHistory().size(),40);
        List<Integer> activeLinksTS = new ArrayList<>(network.getActiveLinksHistory().values());
        List<Integer> activeLinksGoalTS = Collections.nCopies(network.getActiveLinksHistory().size(),35);
        List<Integer> ttwTS = new ArrayList<>(network.getTtwHistory().values());
        List<Integer> ttwGoalTS = Collections.nCopies(network.getTtwHistory().size(), 45);
        Map<DirtyFlag, Map<Integer, Integer>> dirtyFlags = network.getDirtyFlagHistory();


        bandwidthChart.updateXYSeries(BANDWIDTH, timeSteps, bandwidthTS, null);
        bandwidthChart.updateXYSeries("Target", timeSteps, bandwidthGoalTS,null);
        chartPanel.repaint();

        activeLinksChart.updateXYSeries(ACTIVE_LINKS, timeSteps, activeLinksTS, null);
        activeLinksChart.updateXYSeries("Target Active Links", timeSteps, activeLinksGoalTS,null);
        linkChartPanel.repaint();

        timeToWriteChart.updateXYSeries(TTW, timeSteps, ttwTS, null);
        timeToWriteChart.updateXYSeries("Target Time To Write", timeSteps, ttwGoalTS,null);
        ttwChartPanel.repaint();

        Map<String, XYSeries> seriesMap = ratioChart.getSeriesMap();
        for(Map.Entry<DirtyFlag, Map<Integer, Integer>> entry : dirtyFlags.entrySet()){
            if(seriesMap.containsKey(entry.getKey().toString())){
                ratioChart.updateXYSeries(entry.getKey().toString(), new ArrayList<>(entry.getValue().keySet()),
                        new ArrayList<>(entry.getValue().values()), null);
            }
            else{
                ratioChart.addSeries(entry.getKey().toString(), new ArrayList<>(entry.getValue().keySet()),
                        new ArrayList<>(entry.getValue().values())).setMarker(SeriesMarkers.NONE);
            }
        }
        ratioChartPanel.repaint();
    }

    private void updateLinks(Network network) {
        for (Link l : network.getLinks()) {
            if (l.getState() != Link.State.CLOSED) {
                Optional<Edge> e = graph.edges().filter(edge -> edge.getSourceNode().getId().equals(Integer.toString(l.getSource().getID())) &&
                        edge.getTargetNode().getId().equals(Integer.toString(l.getTarget().getID()))).findAny();

                Edge edge = null;
                if (e.isEmpty() && (l.getSource().getState() != Mirror.State.STOPPING
                            && l.getSource().getState() != Mirror.State.STOPPED
                            && l.getTarget().getState() != Mirror.State.STOPPING
                            && l.getTarget().getState() != Mirror.State.STOPPED)) {
                        edge = graph.addEdge(Integer.toString(l.getID()), Integer.toString(l.getSource().getID()), Integer.toString(l.getTarget().getID()));
                }
                if (e.isPresent()) {
                    edge = e.get();
                }
                updateUIClassOfEdge(l, edge);
            }
        }
    }

    private static void updateUIClassOfEdge(Link l, Edge addedEdge) {
        if (addedEdge != null) {
            if (l.isActive()) {
                addedEdge.setAttribute(UI_CLASS, "active");
            } else {
                addedEdge.setAttribute(UI_CLASS, "inactive");
            }
        }
    }

    private void removeVanishedLinks(Network network) {
        Set<Edge> edgesToRemove = graph.edges().filter(e -> {
            boolean exists = false;
            if(e != null) {
                for (Link l : network.getLinks()) {
                    if (e.getSourceNode().getId().equals(Integer.toString(l.getSource().getID())) &&
                            e.getTargetNode().getId().equals(Integer.toString(l.getTarget().getID())))
                        exists = true;
                }
            }
            return !exists;
        }).collect(Collectors.toSet());
        edgesToRemove.forEach(e -> graph.removeEdge(e));
    }

    private void removeVanishedMirrors(Network network) {
        for (int i = 0; i < graph.getNodeCount(); i++) {
                Node n = graph.getNode(i);
                boolean exists = false;
                for (Mirror m : network.getMirrors()) {
                    if (m.getID() == Integer.parseInt(n.getId()))
                        exists = true;
                }
                if (!exists)
                    graph.removeNode(i);
            }
    }

    private void updateMirrors(Network network) {
        for (Mirror m : network.getMirrors()) {
            Node n = graph.getNode(String.valueOf(m.getID()));
            if (n == null)
                n = graph.addNode(String.valueOf(m.getID()));
            n.setAttribute("ui.label", m.getID());
            String invalid = "notinvalid";
            if(m.getData() != null) {
                String label = m.getID() + ": " + m.getData().getDirtyFlag().toString();
                n.setAttribute("ui.label", label);
                if (m.getData().getInvalid()) {
                    invalid = "invalid";
                } else {
                    invalid = "notinvalid";
                }
            }

            switch (m.getState()) {
                case HASDATA -> n.setAttribute(UI_CLASS, invalid, "hasdata");
                case READY -> n.setAttribute(UI_CLASS, invalid, "running");
                case STOPPING -> n.setAttribute(UI_CLASS, invalid,"stopping");
                default -> n.setAttribute(UI_CLASS,invalid,"starting");
            }
        }
    }

    private String loadGraphCSS() {
        try(InputStream in = getClass().getResourceAsStream("/graph.css");
            BufferedReader br = new BufferedReader(new InputStreamReader(in))) {
            StringBuilder ret = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                ret.append(line);
            }
            return ret.toString();
        } catch (IOException e) {
            Logger.getLogger(this.getClass().getName()).log(Level.WARNING, "Could not read css file for visualization.");
        }
        return "";
    }
}
