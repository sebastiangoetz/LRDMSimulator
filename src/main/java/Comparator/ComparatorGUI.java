package Comparator;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class ComparatorGUI extends JFrame {

    private final DefaultTableModel model;
    private List<POMDPEvaluator> currentEvaluators;


    public ComparatorGUI() {
        setTitle("POMDP Run Comparison");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        model = new DefaultTableModel(
                new Object[]{"Scenario","Run", "DecisionAccuracy", "BeliefAccuracy", "ActiveLinks", "Bandwidth", "TimeToWrite"},
                0
        );

        JTable table = new JTable(model);
        add(new JScrollPane(table), BorderLayout.CENTER);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        table.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) {

                int selectedRow = table.getSelectedRow();

                if (selectedRow >= 0 && currentEvaluators != null
                        && selectedRow < currentEvaluators.size()) {

                    POMDPEvaluator evaluator = currentEvaluators.get(selectedRow);

                    new RunDetailFrame(evaluator);
                }
            }
        });

    }

    public void updateResults(List<POMDPEvaluator> evaluators) {
        this.currentEvaluators = evaluators;
        SwingUtilities.invokeLater(() -> {
            model.setRowCount(0);
            for (POMDPEvaluator e : evaluators) {
                String h = String.valueOf(e.getRunId());
                String s = getScenarioName(e);
                String beliefa = String.valueOf(e.averageBeliefAccuracy());
                if (h.equals("100")){
                    h="basic optimizer";
                    beliefa = String.valueOf(1.0);
                }
                if (h.equals("101")){h="original POMDP";}
                model.addRow(new Object[]{
                        s,
                        h,
                        e.getPercentageAgreement(),
                        beliefa,
                        e.getActiveLinksPenalty(),
                        e.getBandwidthPenalty(),
                        e.getTimeToWritePenalty()
                });
            }
        });
    }

    private static String getScenarioName(POMDPEvaluator e) {
        String s = String.valueOf(e.getSimId());
        if(s.equals("0")){
            s="Cost-focused";
        }
        if(s.equals("1")){s="Reliability-focused";}
        if(s.equals("2")){s="Performance-focused";}
        if(s.equals("3")){s="Dense Network";}
        if(s.equals("4")){s="Sparse Network";}
        if(s.equals("5")){s="Stable Network";}
        if(s.equals("6")){s="Unstable Network";}
        if(s.equals("7")){s="Dynamic Priority";}
        if(s.equals("8")){s="Stress Test";}
        if(s.equals("9")){s="Original Scenario";}
        return s;
    }
}

