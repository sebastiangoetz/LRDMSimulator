package Comparator;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class RunDetailFrame extends JFrame {

    public RunDetailFrame(POMDPEvaluator eval) {
        setTitle("Run " + eval.getRunId() + " Details");
        setSize(800, 1000);
        setLocationRelativeTo(null);
        setVisible(true);
        ArrayList<ArrayList<Integer>> valueLists = new ArrayList<>();
        valueLists.add(eval.getActiveLinksSuccessRateList());
        valueLists.add(eval.getBandwidthSuccessRateList());
        valueLists.add(eval.getTimeToWriteSuccessRateList());
        int[] thresholds = eval.getThresholdList();
        ArrayList<String> titles = new ArrayList<>();
        titles.add("Active Links");
        titles.add("Bandwidth");
        titles.add("Time To Write");
        PlotPanel plotPanel = new PlotPanel(valueLists, thresholds, titles);
        JScrollPane scrollPane = new JScrollPane(plotPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        add(plotPanel);
    }
}
