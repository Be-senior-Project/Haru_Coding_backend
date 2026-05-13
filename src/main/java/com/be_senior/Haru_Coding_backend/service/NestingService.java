package com.be_senior.Haru_Coding_backend.service;

import com.be_senior.Haru_Coding_backend.DTO.NestingRequestDto;
import com.be_senior.Haru_Coding_backend.DTO.NestingResponseDto;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service
public class NestingService {

    public NestingResponseDto format(NestingRequestDto request) {
        int maxWidth = request.calcMaxWidth();
        String[] lines = request.getCode().split("\n", -1);
        List<String> result = new ArrayList<>();
        List<NestingResponseDto.ChangeInfo> changes = new ArrayList<>();

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            int depth = calcDepth(line, request.getIndentSize());
            int indentSize = calcIndentSize(depth);
            String normalized = normalizeLine(line, depth, indentSize);

            if (normalized.length() <= maxWidth) {
                result.add(normalized);
                continue;
            }

            String formatted = tryFormatLine(normalized, maxWidth, indentSize);

            if (!formatted.equals(normalized)) {
                String type = detectType(normalized.stripLeading());
                int indentOffset = countLeadingSpaces(normalized) + getKeywordLength(type);
                changes.add(new NestingResponseDto.ChangeInfo(i + 1, type, indentOffset));
            }

            result.add(formatted);
        }

        String formattedCode = String.join("\n", result);
        return new NestingResponseDto("success", formattedCode, changes);
    }

    // depth에 따라 indent 크기 결정
    private int calcIndentSize(int depth) {
        if (depth <= 1) return 4;
        if (depth == 2) return 3;
        return 2;
    }

    // 현재 라인의 depth 계산 (기존 indentSize 기준)
    private int calcDepth(String line, int baseIndentSize) {
        int spaces = countLeadingSpaces(line);
        return spaces / baseIndentSize;
    }

    // depth에 맞게 들여쓰기 재조정
    private String normalizeLine(String line, int depth, int indentSize) {
        String trimmed = line.stripLeading();
        int totalIndent = 0;
        for (int i = 1; i <= depth; i++) {
            totalIndent += calcIndentSize(i);
        }
        return " ".repeat(totalIndent) + trimmed;
    }

    private String tryFormatLine(String line, int maxWidth, int indentSize) {
        String trimmed = line.stripLeading();
        String lead = line.substring(0, line.length() - trimmed.length());

        if (trimmed.matches("if\\s*\\(.*\\)\\s*\\{.*")) {
            return formatIfStatement(line, lead, trimmed, indentSize);
        }

        if (trimmed.matches("for\\s*\\(.*\\)\\s*\\{.*")) {
            return formatForStatement(line, lead, trimmed, indentSize);
        }

        return line;
    }

    private String formatIfStatement(String original, String lead, String trimmed, int indentSize) {
        int openParen = trimmed.indexOf('(');
        if (openParen < 0) return original;

        int closeParen = findMatchingCloseParen(trimmed, openParen);
        if (closeParen < 0) return original;

        String condition = trimmed.substring(openParen + 1, closeParen);
        String suffix = trimmed.substring(closeParen + 1).stripLeading();

        List<String> parts = splitAtLogicalOperators(condition);
        if (parts.size() <= 1) return original;

        String keyword = trimmed.substring(0, openParen).stripTrailing();
        String alignPad = lead + " ".repeat(indentSize);

        List<String> formatted = new ArrayList<>();
        formatted.add(lead + keyword + " (" + parts.get(0));
        for (int i = 1; i < parts.size(); i++) {
            formatted.add(alignPad + parts.get(i));
        }

        String lastLine = formatted.remove(formatted.size() - 1);
        formatted.add(lastLine + ") " + suffix);

        return String.join("\n", formatted);
    }

    private String formatForStatement(String original, String lead, String trimmed, int indentSize) {
        int openParen = trimmed.indexOf('(');
        if (openParen < 0) return original;

        int closeParen = findMatchingCloseParen(trimmed, openParen);
        if (closeParen < 0) return original;

        String inner = trimmed.substring(openParen + 1, closeParen);
        String suffix = trimmed.substring(closeParen + 1).stripLeading();

        List<String> segs = splitAtSemicolons(inner);
        if (segs.size() != 3) return original;

        String alignPad = lead + " ".repeat(indentSize);

        return lead + "for (" + segs.get(0).trim() + ";\n" +
                alignPad + segs.get(1).trim() + ";\n" +
                alignPad + segs.get(2).trim() + ") " + suffix;
    }

    private List<String> splitAtLogicalOperators(String condition) {
        List<String> parts = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        int depth = 0;
        int i = 0;

        while (i < condition.length()) {
            char ch = condition.charAt(i);
            if (ch == '(' || ch == '[') depth++;
            else if (ch == ')' || ch == ']') depth--;

            if (depth == 0 && i + 1 < condition.length()) {
                String two = condition.substring(i, i + 2);
                if (two.equals("&&") || two.equals("||")) {
                    parts.add(current.toString().stripTrailing());
                    current = new StringBuilder(two + " ");
                    i += 2;
                    while (i < condition.length() && condition.charAt(i) == ' ') i++;
                    continue;
                }
            }

            current.append(ch);
            i++;
        }

        if (!current.toString().isBlank()) {
            parts.add(current.toString().stripTrailing());
        }

        return parts;
    }

    private List<String> splitAtSemicolons(String s) {
        List<String> parts = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        int depth = 0;

        for (char ch : s.toCharArray()) {
            if (ch == '(' || ch == '[') depth++;
            else if (ch == ')' || ch == ']') depth--;

            if (ch == ';' && depth == 0) {
                parts.add(current.toString());
                current = new StringBuilder();
            } else {
                current.append(ch);
            }
        }

        if (!current.toString().isBlank()) {
            parts.add(current.toString());
        }

        return parts;
    }

    private int findMatchingCloseParen(String s, int openIdx) {
        int depth = 0;
        for (int i = openIdx; i < s.length(); i++) {
            if (s.charAt(i) == '(') depth++;
            else if (s.charAt(i) == ')') {
                depth--;
                if (depth == 0) return i;
            }
        }
        return -1;
    }

    private int countLeadingSpaces(String line) {
        int count = 0;
        for (char ch : line.toCharArray()) {
            if (ch == ' ') count++;
            else break;
        }
        return count;
    }

    private String detectType(String trimmed) {
        if (trimmed.startsWith("if")) return "if_wrap";
        if (trimmed.startsWith("for")) return "for_wrap";
        if (trimmed.startsWith("while")) return "while_wrap";
        return "unknown";
    }

    private int getKeywordLength(String type) {
        return switch (type) {
            case "if_wrap" -> "if (".length();
            case "for_wrap" -> "for (".length();
            case "while_wrap" -> "while (".length();
            default -> 0;
        };
    }
}
