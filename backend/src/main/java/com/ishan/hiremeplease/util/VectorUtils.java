package com.ishan.hiremeplease.util;

import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.stream.Collectors;

@Component
public class VectorUtils {
    public String embeddingToString(float[] embedding){
        if(embedding == null) return "[]";
        StringBuilder sb = new StringBuilder("[");

        for(int i = 0; i < embedding.length; i++){
            sb.append(embedding[i]);
            if(i < embedding.length - 1){
                sb.append(", ");
            }
        }
        return sb.append("]").toString();
    }

    public double cosineSimilarity(float[] a, float[] b){
        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;

        for(int i = 0; i< a.length; i++){
            dotProduct += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }

        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }
}
