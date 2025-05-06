package org.lrdm;

import com.opencsv.*;
import com.opencsv.exceptions.CsvException;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.BoxAndWhiskerRenderer;
import org.jfree.data.statistics.DefaultBoxAndWhiskerCategoryDataset;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MeanSquaredErrorBoxPlot {
    private static final String ROW_KEY = "Mean Squared Error";

    public static void display(Map<Integer, List<Double>> meanSquaredErrorMap) {
        JFrame f = new JFrame("BoxPlot");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        DefaultBoxAndWhiskerCategoryDataset dataset = new DefaultBoxAndWhiskerCategoryDataset();
        for (Map.Entry<Integer, List<Double>> entry : meanSquaredErrorMap.entrySet()) {
            Integer key = entry.getKey();
            List<Double> value = entry.getValue();
            dataset.add(value, ROW_KEY, key.toString());
        }

        JFreeChart chart = ChartFactory.createBoxAndWhiskerChart("Box and Whisker Chart", ROW_KEY, "MSE", dataset, false);
        f.add(new ChartPanel(chart) {
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(320, 480);
            }
        });
        f.pack();
        f.setLocationRelativeTo(null);
        f.setVisible(true);
    }

    public static void writeDataLineByLine(Map<Integer, List<Double>> meanSquaredErrorMap) {
        File file = new File("mean-squared-errors.csv");
        try {
            FileWriter outputfile = new FileWriter(file);

            CSVWriter writer = new CSVWriter(outputfile);

            String[] header = {"Scenario", "# Mirrors", "miLinkActive", "maxLinkActive", "Run", "usesLatency", "Mean squared error"};
            writer.writeNext(header);

            for (Map.Entry<Integer, List<Double>> entry : meanSquaredErrorMap.entrySet()) {
                Integer key = entry.getKey();
                List<Double> value = entry.getValue();

                for (int i = 0; i < value.size(); i++) {
                    String[] row = {key.toString(), "?", "5", "10", String.valueOf(i + 1), "yes", String.valueOf(value.get(i))};
                    writer.writeNext(row);
                }

            }

            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

