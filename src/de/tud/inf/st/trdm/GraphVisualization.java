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

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GraphVisualization implements VisualizationStrategy {
    private static final String UI_CLASS = "ui.class";
    private static final String BANDWIDTH = "Bandwidth";
    private Graph graph;
    private JLabel simTimeLabel;
    private XYChart bandwidthChart;
    private JPanel chartPanel;

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

        gc.gridx=0;
        gc.gridy=1;
        gc.gridwidth=1;
        gl.setConstraints(dv, gc);
        dv.setMinimumSize(new Dimension(600,400));
        panel.add(dv);

        bandwidthChart = QuickChart.getChart("Bandwidth over Time","Timestep",BANDWIDTH,BANDWIDTH, List.of(0), List.of(0));
        bandwidthChart.getStyler().setLegendVisible(false);
        chartPanel = new XChartPanel<>(bandwidthChart);
        gc.gridx=0;
        gc.gridy=2;
        gc.gridwidth=1;
        gl.setConstraints(chartPanel, gc);
        chartPanel.setMinimumSize(new Dimension(600,400));
        chartPanel.setMaximumSize(new Dimension(600,400));
        panel.add(chartPanel);
        frame.setSize(600,850);
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

        bandwidthChart.updateXYSeries(BANDWIDTH, timeSteps, bandwidthTS, null);
        chartPanel.repaint();
    }

    private void updateLinks(Network network) {
        for (Link l : network.getLinks()) {
            if (l.getState() != Link.State.CLOSED) {
                String sid = String.valueOf(l.getSource().getID());
                String tid = String.valueOf(l.getTarget().getID());
                String eid = "s" + sid + "t" + tid;
                Edge e = graph.getEdge(eid);
                if (e == null && (l.getSource().getState() != Mirror.State.STOPPING
                            && l.getSource().getState() != Mirror.State.STOPPED
                            && l.getTarget().getState() != Mirror.State.STOPPING
                            && l.getTarget().getState() != Mirror.State.STOPPED)) {
                        e = graph.addEdge(eid, sid, tid);
                }
                if (e != null) {
                    if (l.isActive()) {
                        e.setAttribute(UI_CLASS, "active");
                    } else {
                        e.setAttribute(UI_CLASS, "inactive");
                    }
                }
            }
        }
    }

    private void removeVanishedLinks(Network network) {
        for(int i = 0; i < graph.getEdgeCount(); i++) {
            Edge e = graph.getEdge(i);
            boolean exists = false;
            for(Link l : network.getLinks()) {
                String sid = String.valueOf(l.getSource().getID());
                String tid = String.valueOf(l.getTarget().getID());
                String eid = "s" + sid + "t" + tid;
                if(eid.equals(e.getId())) exists = true;
            }
            if(!exists) graph.removeEdge(e);
        }
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
            switch (m.getState()) {
                case HASDATA -> n.setAttribute(UI_CLASS, "hasdata");
                case READY -> n.setAttribute(UI_CLASS, "running");
                case STOPPING -> n.setAttribute(UI_CLASS, "stopping");
                default -> n.setAttribute(UI_CLASS,"starting");
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
