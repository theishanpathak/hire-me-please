package com.ishan.hiremeplease.util;

import com.ishan.hiremeplease.dto.ChunkData;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class MarkdownChunker {
    public List<ChunkData> chunkMarkdown(String markdown){
        String[] lines = markdown.split("\n");
        List<ChunkData> chunks = new ArrayList<>();

        String currentSection = "Header Info";

        for(int i = 0; i < lines.length; i++){
            String line = lines[i].trim();
            if(line.isEmpty()) continue;

            if(line.matches("^#{1,3}\\s+.*")){
                currentSection = cleanMarkdownHeader(line);
                chunks.add(new ChunkData(
                        currentSection,
                        currentSection, // The text is the name of the section
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

    private String cleanMarkdownHeader(String header) {
        return header.replaceAll("^#+\\s*", "").trim();
    }
}
