package scoop.reportinstance;

import java.util.List;
import java.util.ArrayList;

public class TablePath {
    public List<TablePathEntry> getPathEntries() { return new ArrayList<>(); }
    public void addPathEntry(TablePathEntryType type, String name) {}
    
    public static class TablePathEntry {
        public TablePathEntryType type;
    }
}