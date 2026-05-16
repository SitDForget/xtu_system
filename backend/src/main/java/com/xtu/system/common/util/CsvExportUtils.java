package com.xtu.system.common.util;

import java.nio.charset.StandardCharsets;
import java.util.List;

public final class CsvExportUtils {

    private static final byte[] UTF8_BOM = new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};

    private CsvExportUtils() {
    }

    public static byte[] buildCsv(List<String> headers, List<List<String>> rows) {
        StringBuilder builder = new StringBuilder();
        appendRow(builder, headers);
        for (List<String> row : rows) {
            appendRow(builder, row);
        }

        byte[] content = builder.toString().getBytes(StandardCharsets.UTF_8);
        byte[] output = new byte[UTF8_BOM.length + content.length];
        System.arraycopy(UTF8_BOM, 0, output, 0, UTF8_BOM.length);
        System.arraycopy(content, 0, output, UTF8_BOM.length, content.length);
        return output;
    }

    private static void appendRow(StringBuilder builder, List<String> columns) {
        for (int i = 0; i < columns.size(); i++) {
            if (i > 0) {
                builder.append(',');
            }
            builder.append(escape(columns.get(i)));
        }
        builder.append('\n');
    }

    private static String escape(String value) {
        String normalized = value == null ? "" : value;
        boolean needsQuote = normalized.contains(",") || normalized.contains("\"") || normalized.contains("\n") || normalized.contains("\r");
        if (!needsQuote) {
            return normalized;
        }
        return "\"" + normalized.replace("\"", "\"\"") + "\"";
    }
}
