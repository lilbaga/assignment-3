package util;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** Tiny table helper: prints to console, saves as Markdown (.md) and CSV (.csv). */
public class Table {
    private final String title;
    private final List<String> headers;
    private final List<List<String>> rows = new ArrayList<>();

    public Table(String title, String... headers) {
        this.title = title;
        this.headers = Arrays.asList(headers);
    }

    public void addRow(Object... cells) {
        List<String> r = new ArrayList<>();
        for (Object c : cells) r.add(String.valueOf(c));
        rows.add(r);
    }

    public String toMarkdown() {
        StringBuilder sb = new StringBuilder();
        sb.append("**").append(title).append("**\n\n");
        sb.append("| ").append(String.join(" | ", headers)).append(" |\n");
        sb.append("|");
        for (int i = 0; i < headers.size(); i++) sb.append("---|");
        sb.append("\n");
        for (List<String> r : rows) sb.append("| ").append(String.join(" | ", r)).append(" |\n");
        return sb.toString();
    }

    public String toCsv() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.join(",", headers)).append("\n");
        for (List<String> r : rows) sb.append(String.join(",", r)).append("\n");
        return sb.toString();
    }

    public void print() {
        System.out.println();
        System.out.println(toMarkdown());
    }

    public void save(Path dir, String baseName) throws IOException {
        Files.createDirectories(dir);
        try (PrintWriter w = new PrintWriter(Files.newBufferedWriter(dir.resolve(baseName + ".md")))) {
            w.print(toMarkdown());
        }
        try (PrintWriter w = new PrintWriter(Files.newBufferedWriter(dir.resolve(baseName + ".csv")))) {
            w.print(toCsv());
        }
    }
}
