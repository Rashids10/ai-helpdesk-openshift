package com.rashid.ai_helpdesk.config;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.IntStream;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

@Component
public class KnowledgeBaseImporter implements CommandLineRunner {

    private static final String KNOWLEDGE_BASE_LOCATION = "classpath:docs/rag-knowledge-base.md";
    private static final String KNOWLEDGE_BASE_ID = "rag-knowledge-base";
    private static final String VECTOR_TABLE = "public.vector_store";

    private final VectorStore vectorStore;
    private final ResourceLoader resourceLoader;
    private final JdbcTemplate jdbcTemplate;

    public KnowledgeBaseImporter(VectorStore vectorStore, ResourceLoader resourceLoader, JdbcTemplate jdbcTemplate) {
        this.vectorStore = vectorStore;
        this.resourceLoader = resourceLoader;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) throws Exception {
        Resource resource = resourceLoader.getResource(KNOWLEDGE_BASE_LOCATION);

        if (!resource.exists()) {
            throw new IllegalStateException("Knowledge base file not found: " + KNOWLEDGE_BASE_LOCATION);
        }

        String markdown = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);

        if (markdown.isBlank()) {
            throw new IllegalStateException("Knowledge base file is empty: " + KNOWLEDGE_BASE_LOCATION);
        }

        List<Document> documents = toDocuments(markdown);

        if (documentsAlreadyImported(documents)) {
            System.out.println("Knowledge base already imported: " + documents.size() + " documents.");
            return;
        }

        vectorStore.add(documents);

        System.out.println("Knowledge base imported: " + documents.size() + " documents.");
    }

    private List<Document> toDocuments(String markdown) {
        List<String> sections = Arrays.stream(markdown.split("(?m)^---\\s*$"))
                .map(String::trim)
                .filter(section -> !section.isBlank())
                .toList();

        return IntStream.range(0, sections.size())
                .mapToObj(index -> toDocument(sections.get(index), index + 1))
                .toList();
    }

    private Document toDocument(String section, int sectionNumber) {
        String title = extractTitle(section, sectionNumber);
        String contentHash = sha256(section);
        String id = KNOWLEDGE_BASE_ID + ":" + sectionNumber + ":" + slugify(title);

        Map<String, Object> metadata = Map.of(
                "knowledgeBaseId", KNOWLEDGE_BASE_ID,
                "source", KNOWLEDGE_BASE_LOCATION,
                "section", sectionNumber,
                "title", title,
                "contentHash", contentHash);

        return new Document(id, section, metadata);
    }

    private boolean documentsAlreadyImported(List<Document> documents) {
        try {
            return documents.stream().allMatch(document -> Boolean.TRUE.equals(jdbcTemplate.queryForObject("""
                    SELECT EXISTS (
                        SELECT 1
                        FROM %s
                        WHERE id = ?
                        AND metadata::jsonb ->> 'contentHash' = ?
                    )
                    """.formatted(VECTOR_TABLE), Boolean.class,
                    document.getId(),
                    document.getMetadata().get("contentHash"))));
        }
        catch (DataAccessException ex) {
            return false;
        }
    }

    private String extractTitle(String section, int sectionNumber) {
        return section.lines()
                .filter(line -> line.startsWith("## "))
                .map(line -> line.substring(3).trim())
                .findFirst()
                .orElse("section-" + sectionNumber);
    }

    private String slugify(String value) {
        String slug = value.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");
        return slug.isBlank() ? "section" : slug;
    }

    private String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        }
        catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is not available", ex);
        }
    }

}
