package de.tud.inf.st.trdm;

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

//Id's mit Versionsnummer beschriften oder außen anzeigen
// Diagramm mit Ratio wird nicht benötigt
// INVALID-Flag nicht als Zustand ( sondern Boolean + Versionsnummer)
//Border verändern, und nicht blau färben
public class GraphVisualization implements VisualizationStrategy {
    private static final String UI_CLASS = "ui.class";
    private static final String BANDWIDTH = "Bandwidth";
    private static final String ACTIVE_LINKS = "% Active Links";

    private static final String RATIO = "% Ratio";

    private static final String TIME_STEP = "Timestep";
    private Graph graph;
    private JLabel simTimeLabel;
    private XYChart bandwidthChart;
    private XYChart activeLinksChart;

    private XYChart ratioChart;
    private JPanel chartPanel;
    private JPanel linkChartPanel;

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

        frame.add(panel);
        frame.setTitle("Timed RDM Simulator");

        simTimeLabel = new JLabel();
        simTimeLabel.setText("Simulation Time: 0");
        simTimeLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gc.gridx=0;
        gc.gridy=0;
        gc.gridwidth=2;
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
        dv.setMinimumSize(new Dimension(600,400));
        panel.add(dv);

        bandwidthChart = QuickChart.getChart("Bandwidth over Time",TIME_STEP,BANDWIDTH,BANDWIDTH, List.of(0), List.of(0));
        bandwidthChart.getStyler().setTheme(new MatlabTheme());
        bandwidthChart.getStyler().setLegendVisible(false);
        chartPanel = new XChartPanel<>(bandwidthChart);
        gc = new GridBagConstraints();
        gc.gridx=0;
        gc.gridy=2;
        gc.gridwidth=1;
        gl.setConstraints(chartPanel, gc);
        chartPanel.setMinimumSize(new Dimension(600,200));
        chartPanel.setMaximumSize(new Dimension(600,200));
        panel.add(chartPanel);

        activeLinksChart = QuickChart.getChart("Active Links", TIME_STEP, ACTIVE_LINKS, ACTIVE_LINKS, List.of(0), List.of(0));
        activeLinksChart.getStyler().setTheme(new MatlabTheme());
        activeLinksChart.getStyler().setLegendVisible(false);
        linkChartPanel = new XChartPanel<>(activeLinksChart);
        gc = new GridBagConstraints();
        gc.gridx=0;
        gc.gridy=3;
        gc.gridwidth=1;
        gl.setConstraints(linkChartPanel, gc);
        linkChartPanel.setMinimumSize(new Dimension(600,200));
        linkChartPanel.setMaximumSize(new Dimension(600,200));
        panel.add(linkChartPanel);

        ratioChart = QuickChart.getChart("Ratio of newest Package", TIME_STEP, RATIO, RATIO, List.of(0), List.of(0));
        ratioChart.getStyler().setTheme(new MatlabTheme());
        ratioChart.getStyler().setLegendVisible(false);
        ratioChartPanel = new XChartPanel<>(ratioChart);
        gc = new GridBagConstraints();
        gc.gridx=0;
        gc.gridy=4;
        gc.gridwidth=1;
        gl.setConstraints(ratioChartPanel, gc);
        ratioChartPanel.setMinimumSize(new Dimension(600,200));
        ratioChartPanel.setMaximumSize(new Dimension(600,200));
        panel.add(ratioChartPanel);

        frame.setResizable(false);
        frame.setSize(600,1050);
        frame.setVisible(true);

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
        List<Integer> activeLinksTS = new ArrayList<>(network.getActiveLinksHistory().values());

        bandwidthChart.updateXYSeries(BANDWIDTH, timeSteps, bandwidthTS, null);
        chartPanel.repaint();

        activeLinksChart.updateXYSeries(ACTIVE_LINKS, timeSteps, activeLinksTS, null);
        linkChartPanel.repaint();

        ratioChart.updateXYSeries(RATIO, timeSteps, activeLinksTS, null);
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
        try(BufferedReader br = new BufferedReader(new FileReader("resources/graph.css"))) {
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
