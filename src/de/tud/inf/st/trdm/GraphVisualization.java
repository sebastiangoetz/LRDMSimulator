package de.tud.inf.st.trdm;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;

import java.io.*;
import java.util.Properties;

public class GraphVisualization implements VisualizationStrategy {
    private Graph graph;
    @Override
    public void init(Network network) {
        graph = new SingleGraph("Runtime View");
        String css = loadGraphCSS();
        graph.setAttribute("ui.stylesheet", css);
        for (Mirror m : network.getMirrors()) {
            Node n = graph.addNode(m.getID() + "");
            n.setAttribute("ui.class", "starting");
        }
        for (Link l : network.getLinks()) {
            String sid = l.getSource().getID() + "";
            String tid = l.getTarget().getID() + "";
            Edge e = graph.addEdge("s" + sid + "t" + tid, sid, tid);
            e.setAttribute("ui.class", "inactive");
        }
        graph.display();
    }

    @Override
    public void updateGraph(Network network) {
            for (Mirror m : network.getMirrors()) {
                Node n = graph.getNode(m.getID() + "");
                if (n == null)
                    n = graph.addNode(m.getID() + "");
                n.setAttribute("ui.label", m.getID());
                switch (m.getState()) {
                    case starting:
                        n.setAttribute("ui.class", "starting");
                        break;
                    case ready:
                        n.setAttribute("ui.class", "running");
                        break;
                    case stopping:
                        n.setAttribute("ui.class", "stopping");
                        break;
                    default:
                        break;
                }
            }
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
            //remove links
            for(int i = 0; i < graph.getEdgeCount(); i++) {
                Edge e = graph.getEdge(i);
                boolean exists = false;
                for(Link l : network.getLinks()) {
                    String sid = l.getSource().getID() + "";
                    String tid = l.getTarget().getID() + "";
                    String eid = "s" + sid + "t" + tid;
                    if(eid.equals(e.getId())) exists = true;
                }
                if(!exists) graph.removeEdge(e);
            }
            //add or update links
            for (Link l : network.getLinks()) {
                if (l.getState() != Link.State.closed) {
                    String sid = l.getSource().getID() + "";
                    String tid = l.getTarget().getID() + "";
                    String eid = "s" + sid + "t" + tid;
                    Edge e = graph.getEdge(eid);
                    if (e == null) {
                        if (l.getSource().getState() != Mirror.State.stopping
                                && l.getSource().getState() != Mirror.State.stopped
                                && l.getTarget().getState() != Mirror.State.stopping
                                && l.getTarget().getState() != Mirror.State.stopped)
                            e = graph.addEdge(eid, sid, tid);
                    }
                    if (e != null)
                        if (l.isActive())
                            e.setAttribute("ui.class", "active");
                        else
                            e.setAttribute("ui.class", "inactive");
                }
            }
    }

    private String loadGraphCSS() {
        try {
            StringBuffer ret = new StringBuffer();
            BufferedReader br = new BufferedReader(new FileReader(new File("resources/graph.css")));
            String line;
            while ((line = br.readLine()) != null) {
                ret.append(line);
            }
            return ret.toString();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }
}
