package com.adityachandel.booklore.model.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GoogleBooksApiResponse {
    private List<Item> items;

    @Data
    public static class Item {
        private String id;
        private VolumeInfo volumeInfo;

        @Data
        public static class VolumeInfo {
            private String title;
            private String subtitle;
            private List<String> authors;
            private String publisher;
            private String publishedDate;
            private String description;
            private List<IndustryIdentifier> industryIdentifiers;
            private Integer pageCount;
            private ImageLinks imageLinks;
            private String language;
            private List<String> categories;
        }

        @Data
        public static class IndustryIdentifier {
            private String type;
            private String identifier;
        }

        @Data
        public static class ImageLinks {
            private String thumbnail;
        }
    }
}
