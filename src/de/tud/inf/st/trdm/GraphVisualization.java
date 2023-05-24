package de.tud.inf.st.trdm;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;

import java.io.*;

public class GraphVisualization implements VisualizationStrategy {
    private static final String UI_CLASS = "ui.class";
    private Graph graph;
    @Override
    public void init(Network network) {
        graph = new SingleGraph("Runtime View");
        String css = loadGraphCSS();
        graph.setAttribute("ui.stylesheet", css);
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
        graph.display();
    }

    @Override
    public void updateGraph(Network network) {
        updateMirrors(network);
        removeVanishedMirrors(network);
        removeVanishedLinks(network);
        updateLinks(network);
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
                case STARTING:
                    n.setAttribute(UI_CLASS, "starting");
                    break;
                case READY:
                    n.setAttribute(UI_CLASS, "running");
                    break;
                case STOPPING:
                    n.setAttribute(UI_CLASS, "stopping");
                    break;
                default:
                    break;
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
            e.printStackTrace();
        }
        return "";
    }
}
