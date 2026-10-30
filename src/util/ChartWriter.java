package util;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import javax.imageio.ImageIO;

/**
 * Minimal dependency-free line chart renderer (Java2D -> PNG).
 * Optional log-scale on both axes (useful because sizes and times span several orders of magnitude).
 */
public final class ChartWriter {
    private ChartWriter() {}

    private static final Color[] COLORS = {
        new Color(0x1f77b4), new Color(0xd62728), new Color(0x2ca02c),
        new Color(0xff7f0e), new Color(0x9467bd), new Color(0x8c564b)
    };

    public static void lineChart(File out, String title, String xLabel, String yLabel,
                                 double[] xs, Map<String, double[]> series, boolean logScale) throws IOException {
        System.setProperty("java.awt.headless", "true");
        final int W = 960, H = 620, L = 100, R = 40, T = 70, B = 90;
        BufferedImage img = new BufferedImage(W, H, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, W, H);

        double xMin = Double.MAX_VALUE, xMax = -Double.MAX_VALUE, yMin = Double.MAX_VALUE, yMax = -Double.MAX_VALUE;
        for (double x : xs) { xMin = Math.min(xMin, x); xMax = Math.max(xMax, x); }
        for (double[] ys : series.values())
            for (double y : ys) { yMin = Math.min(yMin, Math.max(y, 1e-3)); yMax = Math.max(yMax, y); }
        if (!logScale) yMin = 0;

        double fx0 = logScale ? Math.log10(xMin) : xMin, fx1 = logScale ? Math.log10(xMax) : xMax;
        double fy0, fy1;
        if (logScale) {
            fy0 = Math.floor(Math.log10(yMin));
            fy1 = Math.ceil(Math.log10(yMax));
            if (fy1 == fy0) fy1 = fy0 + 1;
        } else {
            fy0 = 0;
            fy1 = yMax * 1.1;
        }
        int pw = W - L - R, ph = H - T - B;

        // title and axis labels
        g.setColor(Color.BLACK);
        g.setFont(new Font("SansSerif", Font.BOLD, 20));
        FontMetrics fm = g.getFontMetrics();
        g.drawString(title, (W - fm.stringWidth(title)) / 2, 35);
        g.setFont(new Font("SansSerif", Font.PLAIN, 15));
        fm = g.getFontMetrics();
        g.drawString(xLabel + (logScale ? " (log scale)" : ""), L + (pw - fm.stringWidth(xLabel)) / 2, H - 25);
        Graphics2D gr = (Graphics2D) g.create();
        gr.rotate(-Math.PI / 2);
        gr.drawString(yLabel + (logScale ? " (log scale)" : ""), -(T + ph / 2 + fm.stringWidth(yLabel) / 2), 25);
        gr.dispose();

        // grid + y ticks
        g.setFont(new Font("SansSerif", Font.PLAIN, 12));
        fm = g.getFontMetrics();
        int yTicks = logScale ? (int) (fy1 - fy0) : 5;
        for (int i = 0; i <= yTicks; i++) {
            double f = fy0 + (fy1 - fy0) * i / yTicks;
            int py = T + ph - (int) ((f - fy0) / (fy1 - fy0) * ph);
            g.setColor(new Color(0xdddddd));
            g.drawLine(L, py, L + pw, py);
            g.setColor(Color.BLACK);
            double val = logScale ? Math.pow(10, f) : f;
            String s = logScale ? fmt(val) : fmt(val);
            g.drawString(s, L - 8 - fm.stringWidth(s), py + 4);
        }
        // x ticks at data points
        for (double x : xs) {
            double f = logScale ? Math.log10(x) : x;
            int px = L + (int) ((f - fx0) / (fx1 - fx0) * pw);
            g.setColor(new Color(0xdddddd));
            g.drawLine(px, T, px, T + ph);
            g.setColor(Color.BLACK);
            String s = fmt(x);
            g.drawString(s, px - fm.stringWidth(s) / 2, T + ph + 20);
        }
        g.setColor(Color.BLACK);
        g.setStroke(new BasicStroke(1.5f));
        g.drawRect(L, T, pw, ph);

        // series
        int si = 0;
        int legendX = L + 15, legendY = T + 20;
        for (Map.Entry<String, double[]> en : series.entrySet()) {
            Color c = COLORS[si % COLORS.length];
            g.setColor(c);
            g.setStroke(new BasicStroke(2.5f));
            double[] ys = en.getValue();
            int prevX = 0, prevY = 0;
            for (int i = 0; i < xs.length; i++) {
                double fx = logScale ? Math.log10(xs[i]) : xs[i];
                double fy = logScale ? Math.log10(Math.max(ys[i], 1e-3)) : ys[i];
                int px = L + (int) ((fx - fx0) / (fx1 - fx0) * pw);
                int py = T + ph - (int) ((fy - fy0) / (fy1 - fy0) * ph);
                if (i > 0) g.drawLine(prevX, prevY, px, py);
                g.fillOval(px - 5, py - 5, 10, 10);
                prevX = px;
                prevY = py;
            }
            g.fillRect(legendX, legendY + si * 22 - 10, 14, 14);
            g.setColor(Color.BLACK);
            g.setFont(new Font("SansSerif", Font.PLAIN, 14));
            g.drawString(en.getKey(), legendX + 22, legendY + si * 22 + 2);
            si++;
        }
        g.dispose();
        out.getParentFile().mkdirs();
        ImageIO.write(img, "png", out);
    }

    private static String fmt(double v) {
        if (v == Math.rint(v) && Math.abs(v) < 1e9) return String.valueOf((long) v);
        if (Math.abs(v) >= 100) return String.format("%.0f", v);
        if (Math.abs(v) >= 1) return String.format("%.1f", v);
        return String.format("%.3f", v);
    }
}
