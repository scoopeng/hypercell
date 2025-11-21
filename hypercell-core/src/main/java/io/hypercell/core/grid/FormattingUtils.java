package io.hypercell.core.grid;

public class FormattingUtils {
    public static boolean isExcelDateFormat(String format) {
        if (format == null) return false;
        // Basic check for now
        return format.contains("d") || format.contains("m") || format.contains("y") || format.contains("h") || format.contains("s");
    }
    
    public static boolean isDateFormat(String format) {
        return isExcelDateFormat(format);
    }
}
