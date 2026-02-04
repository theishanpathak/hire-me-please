package com.ishan.hiremeplease.util;

import com.ishan.hiremeplease.dto.ChunkData;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
public class DocumentChunker {
    private static final Set<String> COMMON_HEADERS = Set.of(
            "requirements", "required qualifications", "required skills",
            "responsibilities", "what you'll do", "about the role",
            "nice to have", "preferred qualifications", "preferred skills",
            "bonus points", "benefits", "about us", "about the company",
            "qualifications", "skills", "experience", "education",
            "job description", "overview", "summary", "we offer", "pay transparency", "pay range"
    );

    public List<ChunkData> chunkByLines(String document){
        String[] lines = document.split("\n");
        List<ChunkData> chunks = new ArrayList<>();

        String currentSection = "Overview";

        for(int i = 0; i < lines.length; i++){
            String line = lines[i].trim();

            //skip empty lines
            if(line.isEmpty()) continue;

            if(isHeader(line)){
                currentSection = cleanHeaderName(line);
                continue;
            }

            chunks.add(new ChunkData(
                    currentSection,
                    line,
                    i
            ));
        }

        return chunks;
    }


    /**
     * Determines if a given line of text should be treated as a section header.
     * Uses a combination of keyword matching, length constraints, and formatting cues.
     */
    private boolean isHeader(String line){
        if(line == null) return false;


        //Clean up and prepare normalized versions for comparison
        String trimmed = line.trim();
        String normalized = trimmed.toLowerCase();

        //We count words and char length once to avoid calculating it in the loop
        String[] words = normalized.split("\\s+");
        int wordCount = words.length;
        int charCount = normalized.length();

        //Guard clauses to keep the length within normal header length
        if(normalized.length() < 3 || normalized.length() > 50) return false;
        if(wordCount > 7) return false;


        // Keyword Check:
        // We iterate through known headers. If the keyword is a single word (like "Skills"),
        // we require an exact match to avoid flagging sentences that just mention the word.
        for(String header : COMMON_HEADERS){
            boolean isSingleWordHeader = !header.trim().contains(" ");

            if(isSingleWordHeader){
                if(normalized.equals(header)) return true;
            }else{
                if(normalized.contains(header)) return true;
            }
        }

        // All caps lines and not starting with bullets
        if (trimmed.equals(trimmed.toUpperCase()) &&
                charCount < 30 &&
                !trimmed.startsWith("-") &&
                !trimmed.startsWith("•")) {
            //see if it contains at least one actual letter and not just numbers
            if (trimmed.matches(".*[A-Z].*")) return true;
            return true;
        }

        //checking the colon rule
        if(normalized.endsWith(":") && normalized.length() < 30) return true;

        return false;
    }



    private String cleanHeaderName(String header) {
        return header.replace(":", "")
                .replace("#", "")
                .trim();
    }


}
