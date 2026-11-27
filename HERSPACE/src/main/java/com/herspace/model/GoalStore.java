package com.herspace.model;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Keeps the list of goals for the whole application run and
 * saves it to a text file (data/goals.csv) so goals are still
 * there the next time the application starts.
 *
 * One goal per line, fields separated by a tab:
 * title, description, target, unit, current, startDate, deadline
 */
public class GoalStore {

    private static final Path FILE = Paths.get("data", "goals.csv");

    private static GoalStore instance;

    private final List<Goal> goals = new ArrayList<>();

    private GoalStore() {
        load();
    }

    // Returns the single shared store
    public static GoalStore getInstance() {
        if (instance == null) {
            instance = new GoalStore();
        }
        return instance;
    }

    public List<Goal> getGoals() {
        return goals;
    }

    public void add(Goal goal) {
        goals.add(goal);
        save();
    }

    public void remove(Goal goal) {
        goals.remove(goal);
        save();
    }

    // Call after a goal has been changed
    public void save() {

        List<String> lines = goals.stream()
                .map(GoalStore::toLine)
                .collect(Collectors.toList());

        try {
            if (FILE.getParent() != null) {
                Files.createDirectories(FILE.getParent());
            }
            Files.write(FILE, lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            // The page keeps working with the data in memory
            System.err.println("Unable to save goals: " + e.getMessage());
        }
    }

    // Reads the goals file; a missing or damaged line is skipped
    private void load() {

        if (!Files.exists(FILE)) {
            return;
        }

        try {
            for (String line : Files.readAllLines(FILE, StandardCharsets.UTF_8)) {
                Goal goal = fromLine(line);
                if (goal != null) {
                    goals.add(goal);
                }
            }
        } catch (IOException e) {
            System.err.println("Unable to load goals: " + e.getMessage());
        }
    }

    private static String toLine(Goal g) {
        return String.join("\t",
                escape(g.getTitle()),
                escape(g.getDescription()),
                String.valueOf(g.getTarget()),
                escape(g.getUnit()),
                String.valueOf(g.getCurrent()),
                g.getStartDate().toString(),
                g.getDeadline().toString());
    }

    private static Goal fromLine(String line) {
        try {
            String[] p = line.split("\t", -1);
            if (p.length != 7) {
                return null;
            }
            double target = Double.parseDouble(p[2]);
            double current = Double.parseDouble(p[4]);
            if (p[0].trim().isEmpty() || target <= 0 || current < 0) {
                return null;
            }
            return new Goal(unescape(p[0]), unescape(p[1]), target,
                    unescape(p[3]), current,
                    LocalDate.parse(p[5]), LocalDate.parse(p[6]));
        } catch (RuntimeException e) {
            // NumberFormatException or DateTimeParseException
            return null;
        }
    }

    // Keeps tabs and line breaks inside text from breaking the file format
    private static String escape(String text) {
        return text.replace("\\", "\\\\")
                .replace("\t", "\\t")
                .replace("\r", "")
                .replace("\n", "\\n");
    }

    private static String unescape(String text) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '\\' && i + 1 < text.length()) {
                char next = text.charAt(++i);
                sb.append(next == 't' ? '\t' : next == 'n' ? '\n' : next);
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}
