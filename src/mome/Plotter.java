package mome;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class Plotter {

    /** Apply point-only styling: tiny red PF dots, larger blue ND dots; no lines. */
    private static void styleAsDotsNoLines(JFreeChart chart) {
        XYPlot plot = (XYPlot) chart.getPlot();

        XYLineAndShapeRenderer r = new XYLineAndShapeRenderer(false, true); // no lines, shapes only

        // Series 0: True PF (red, small dots)
        r.setSeriesShape(0, new java.awt.geom.Ellipse2D.Double(-1, -1, 2, 2));   // ~2px
        r.setSeriesPaint(0, Color.RED);

        // Series 1: Found ND (blue, bigger dots)
        r.setSeriesShape(1, new java.awt.geom.Ellipse2D.Double(-3, -3, 6, 6));   // ~6px
        r.setSeriesPaint(1, new Color(0, 102, 204));

        r.setUseOutlinePaint(false);
        plot.setRenderer(r);

        // Let axes autoscale so off-front results are obvious
        plot.getDomainAxis().setAutoRange(true);
        plot.getRangeAxis().setAutoRange(true);
    }

    /** Show a scatter plot overlay of True PF vs Approx ND. */
    public static void showZDTOverlay(String title, double[][] truePF, double[][] approx) {
        XYSeries sTrue = new XYSeries("True PF");
        for (double[] p : truePF) sTrue.add(p[0], p[1]);

        XYSeries sApprox = new XYSeries("Found ND");
        for (double[] p : approx) sApprox.add(p[0], p[1]);

        XYSeriesCollection ds = new XYSeriesCollection();
        ds.addSeries(sTrue);
        ds.addSeries(sApprox);

        JFreeChart chart = ChartFactory.createScatterPlot(
                title, "f1", "f2", ds,
                PlotOrientation.VERTICAL, true, true, false
        );

        styleAsDotsNoLines(chart);

        JFrame frame = new JFrame(title);
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.setContentPane(new ChartPanel(chart));
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    /** Save the same overlay as a PNG file. */
    public static void saveZDTOverlayPNG(String path, String title, double[][] truePF, double[][] approx, int w, int h) {
        XYSeries sTrue = new XYSeries("True PF");
        for (double[] p : truePF) sTrue.add(p[0], p[1]);

        XYSeries sApprox = new XYSeries("Found ND");
        for (double[] p : approx) sApprox.add(p[0], p[1]);

        XYSeriesCollection ds = new XYSeriesCollection();
        ds.addSeries(sTrue);
        ds.addSeries(sApprox);

        JFreeChart chart = ChartFactory.createScatterPlot(
                title, "f1", "f2", ds,
                PlotOrientation.VERTICAL, true, true, false
        );

        styleAsDotsNoLines(chart);

        try {
            ChartUtils.saveChartAsPNG(new File(path), chart, w, h);
        } catch (Exception e) {
            throw new RuntimeException("Failed to save PNG: " + path, e);
        }
    }
}
