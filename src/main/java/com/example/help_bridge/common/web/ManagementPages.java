package com.example.help_bridge.common.web;

import org.springframework.ui.Model;
import java.util.List;

public final class ManagementPages {
    private ManagementPages() {}

    public record Field(String name, String label, String type) {}
    public record Row(Long id, List<String> cells) {}

    public static Field field(String name, String label) {
        return new Field(name, label, "text");
    }

    public static Field field(String name, String label, String type) {
        return new Field(name, label, type);
    }

    public static String form(Model model, String title, String action, String back, Field... fields) {
        model.addAttribute("title", title);
        model.addAttribute("action", action);
        model.addAttribute("back", back);
        model.addAttribute("fields", List.of(fields));
        return "management/form";
    }

    public static String list(Model model, String title, String path, List<String> columns, List<Row> rows) {
        model.addAttribute("title", title);
        model.addAttribute("path", path);
        model.addAttribute("columns", columns);
        model.addAttribute("rows", rows);
        return "management/list";
    }

    public static String delete(Model model, String name, String action, String back) {
        model.addAttribute("name", name);
        model.addAttribute("action", action);
        model.addAttribute("back", back);
        return "management/delete";
    }
}
