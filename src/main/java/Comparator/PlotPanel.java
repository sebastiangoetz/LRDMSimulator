package Comparator;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;

public class PlotPanel extends JPanel {

    private final int padding = 40;
    private final int labelPadding = 30;

    private final ArrayList<ArrayList<Integer>> valueLists;
    private final int[] thresholds;
    private final ArrayList<String> titles;

    private final int diagramHeight = 200;

    public PlotPanel(ArrayList<ArrayList<Integer>> valueLists, int[] thresholds, ArrayList<String> titles) {
        this.valueLists = valueLists;
        this.thresholds = thresholds;
        this.titles = titles;

        int totalHeight = valueLists.size() * diagramHeight + (valueLists.size() + 1) * padding;
        setPreferredSize(new Dimension(800, totalHeight));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        int w = getWidth();

        for (int i = 0; i < valueLists.size(); i++) {
            int yOffset = padding + i * (diagramHeight + padding);
            drawDiagram(g2, valueLists.get(i), new int[]{thresholds[i],thresholds[i+3],thresholds[i+6]}, yOffset, w - 2 * padding, i);
        }
    }

    private void drawDiagram(Graphics2D g2, ArrayList<Integer> values, int[] threshold,
                             int yOffset, int width, int index) {
        if (values.isEmpty()) return;
        g2.setColor(Color.BLACK);
        String title = (titles != null && index < titles.size()) ? titles.get(index) : "Diagram " + (index + 1);
        FontMetrics metrics = g2.getFontMetrics();
        int titleWidth = metrics.stringWidth(title);
        g2.drawString(title, 40 + (width - titleWidth)/2, yOffset + metrics.getHeight());

        yOffset += metrics.getHeight() + 5;

        int x0 = 40 + labelPadding;
        int y0 = yOffset + 200 - labelPadding;
        int xMax = 40 + width - padding;
        int yMax = yOffset + padding;

        g2.setColor(Color.BLACK);
        g2.drawLine(x0, y0, xMax, y0);
        g2.drawLine(x0, y0, x0, yMax);

        drawSeries(g2, values, yOffset, width);
        drawThreshold(g2, values, threshold, yOffset, width);
        int xTickCount = Math.min(10, values.size() - 1);
        int tickSize = 6;
        for (int i = 0; i <= xTickCount; i++) {
            double fraction = (double) i / xTickCount;
            int x = x0 + (int)((xMax - x0) * fraction);
            int value = (int)((values.size() - 1) * fraction);
            g2.drawLine(x, y0, x, y0 + tickSize);

            String label = String.valueOf(value);
            int labelWidth = metrics.stringWidth(label);
            g2.drawString(label, x - labelWidth / 2, y0 + tickSize + 15);
        }
        int yTickCount = 10;
        double maxVal = values.stream().mapToDouble(v -> v).max().orElse(1);
        double minVal = 0;
        for (int i = 0; i <= yTickCount; i++) {
            int y = y0 - (y0 - yMax) * i / yTickCount;
            double val = minVal + (maxVal - minVal) * i / yTickCount;
            g2.drawLine(x0 - tickSize, y, x0, y);

            String label = String.format("%.0f", val);
            g2.drawString(label, x0 - tickSize - 35, y + metrics.getHeight() / 3);
        }
    }

    private void drawSeries(Graphics2D g2, ArrayList<Integer> values,
                            int yOffset, int width) {

        if (values.isEmpty()) return;
        g2.setColor(Color.BLUE);

        int x0 = 40 + labelPadding;
        int y0 = yOffset + 200 - labelPadding;
        int xMax = 40 + width - padding;
        int yMax = yOffset + padding;

        double maxVal = values.stream().mapToDouble(v -> v).max().orElse(1);
        double minVal = 0;

        int n = values.size();
        double xScale = (double)(xMax - x0) / (n - 1);
        double yScale = (double)(y0 - yMax) / (maxVal - minVal + 1e-9);

        for (int i = 1; i < n; i++) {
            int x1 = x0 + (int)((i - 1) * xScale);
            int x2 = x0 + (int)(i * xScale);
            int y1 = y0 - (int)((values.get(i - 1) - minVal) * yScale);
            int y2 = y0 - (int)((values.get(i) - minVal) * yScale);
            g2.drawLine(x1, y1, x2, y2);
        }
    }

    private void drawThreshold(Graphics2D g2, ArrayList<Integer> values, int[] threshold,
                               int yOffset, int width) {
        System.out.println(Arrays.toString(threshold));

        g2.setColor(Color.RED);
        g2.setStroke(new BasicStroke(2f));

        int x0 = 40 + labelPadding;
        int y0 = yOffset + 200 - labelPadding;
        int xMax = 40 + width - padding;
        int yMax = yOffset + padding;
        int n =  values.size();
        double maxVal = values.stream().mapToDouble(v -> v).max().orElse(1);
        double minVal = 0;
        double yScale = (double)(y0 - yMax) / (maxVal - minVal + 1e-9);
        int p1End = Math.min(100, n);
        int xEnd1 = x0 + (xMax - x0) * p1End / n;

        int y1 = y0 - (int)((threshold[0] - minVal) * yScale);
        g2.drawLine(x0, y1, xEnd1, y1);
        if (n > 100) {
            int p2End = Math.min(300, n);

            int xStart2 = x0 + (xMax - x0) * 100 / n;
            int xEnd2 = x0 + (xMax - x0) * p2End / n;

            int y2 = y0 - (int)((threshold[1] - minVal) * yScale);
            g2.drawLine(xStart2, y2, xEnd2, y2);
        }

        if (n > 300) {
            int xStart3 = x0 + (xMax - x0) * 300 / n;

            int y3 = y0 - (int)((threshold[2] - minVal) * yScale);
            g2.drawLine(xStart3, y3, xMax, y3);
        }
    }
}
