package scoop.ai.ml.models;

import weka.core.Attribute;
import java.util.Map;
import java.io.IOException;

public class ModelContext {
    public Attribute[] attributes;
    public Clusterer clusterer;
    public Map<String, Object> clusterIDMap;
    
    public static ModelContext deserialize(byte[] bytes) throws IOException, ClassNotFoundException { return new ModelContext(); }
    
    public static class Clusterer {
        public int clusterInstance(weka.core.DenseInstance instance) { return 0; }
    }
}
