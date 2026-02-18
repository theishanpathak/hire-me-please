package com.ishan.hiremeplease.util;

import com.ishan.hiremeplease.dto.ChunkData;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
public class DocumentChunker {
    private static final Set<String> COMMON_HEADERS = Set.of(
            // Role & Purpose
            "requirements", "required qualifications", "required skills", "required", "what you will do",
            "responsibilities", "what youll do", "about the role", "role description", "mission",
            "the opportunity", "position summary", "key purpose", "scope of work", "overview", "summary",
            "primary responsibilities", "core responsibilities", "essential functions", "your impact", "what to expect",

            // Requirements & Skills
            "minimum qualifications", "basic qualifications", "key requirements", "what you bring", "who you are",
            "technical skills", "hard skills", "soft skills", "competencies", "professional experience",
            "background", "eligibility", "must haves", "prerequisites", "qualifications", "skills", "experience", "education",

            // Preferences & Bonus
            "nice to have", "preferred qualifications", "preferred skills", "desired skills", "bonus", "pluses",
            "great to have", "extra credit", "ideally", "additional qualifications", "highly desired", "stand out",
            "bonus points", "preferred",

            // Company & Culture
            "about us", "about the company", "who we are", "our mission", "our values", "culture", "why join us",
            "life at", "the team", "diversity and inclusion", "equal opportunity", "commitment to diversity",

            // Compensation & Perks
            "benefits", "perks", "total rewards", "compensation", "salary", "stipend", "equity", "pay transparency",
            "pay range", "rewards", "rewards & benefits at", "vacation", "healthcare", "insurance",
            "what's in it for you", "employee benefits", "we offer",

            // Logistics & Process
            "how to apply", "interview process", "hiring process", "location", "remote work",
            "working conditions", "physical requirements", "travel", "contract details",
            "additional notes", "looking for", "what we're looking for", "first year will include"
    );

    public List<ChunkData> chunkByLines(String document){
        if (document == null || document.isBlank()) return new ArrayList<>();

        String[] lines = document.split("\n");
        List<ChunkData> chunks = new ArrayList<>();
        String currentSection = "Overview";

        for(int i = 0; i < lines.length; i++){
            String line = lines[i].trim();

            //skip empty lines
            if(line.isEmpty()) continue;

            if(isHeader(line)){
                currentSection = cleanHeaderName(line);
                chunks.add(new ChunkData(
                        currentSection,
                        currentSection, // The actual text of the header
                        i + 1
                ));
                continue;
            }

            chunks.add(new ChunkData(
                    currentSection,
                    line,
                    i+1
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
        String normalized = trimmed.toLowerCase().replaceAll("[^a-z0-9\\s]", "").trim();

        //We count words and char length once to avoid calculating it in the loop
        String[] words = normalized.split("\\s+");
        int wordCount = words.length;
        int charCount = trimmed.length();

        //immediate rejections
        if (charCount < 3 || charCount > 60) return false;
        if (wordCount > 8) return false;
        // Rejects bullet points
        if (trimmed.startsWith("-") || trimmed.startsWith("•") || trimmed.startsWith("*")) return false;
        // Rejects Salary ranges, Dates, and Years (looks for 3+ consecutive digits)
        if (trimmed.matches(".*\\d{3,}.*") || trimmed.startsWith("$")) return false;
        // Rejects contact info
        if (trimmed.contains("@") || trimmed.contains("http")) return false;

        //Structural cues
        // Ends with colon
        if (trimmed.endsWith(":") && wordCount <= 4) return true;

        // All Caps Rule (Strengthened)
        if (trimmed.equals(trimmed.toUpperCase()) && charCount < 35) {
            // Must contain at least two consecutive letters to be a word, not a symbol/number
            if (trimmed.matches(".*[A-Z]{2,}.*")) return true;
        }


        for (String header : COMMON_HEADERS) {
            // Match exact normalized string or common multi-word headers
            if (normalized.equals(header)) return true;

            // Handle variations like "Healthcare Benefits" or "Our Requirements"
            // by checking if the line is short and contains the core keyword
            if (wordCount <= 3 && (normalized.startsWith(header) || normalized.endsWith(header))) {
                // Additional check to ensure we don't match short common words in wrong context
                if (header.length() > 4) return true;
            }
        }
        return false;
    }



    private String cleanHeaderName(String header) {
        String cleaned = header.replaceAll("[#:*]", "").trim();
        return cleaned.substring(0, 1).toUpperCase() + cleaned.substring(1).toLowerCase();
    }


}
