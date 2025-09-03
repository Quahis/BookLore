package com.adityachandel.booklore.util;

import com.adityachandel.booklore.model.MetadataClearFlags;
import com.adityachandel.booklore.model.dto.BookMetadata;
import com.adityachandel.booklore.model.entity.AuthorEntity;
import com.adityachandel.booklore.model.entity.BookMetadataEntity;
import com.adityachandel.booklore.model.entity.CategoryEntity;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.BooleanUtils.isTrue;

@Slf4j
public class MetadataChangeDetector {

    public static boolean isDifferent(BookMetadata newMeta, BookMetadataEntity existingMeta, MetadataClearFlags clear) {
        if (clear == null) return true;

        List<String> changes = new ArrayList<>();

        compare(changes, "title", clear.isTitle(), newMeta.getTitle(), existingMeta.getTitle(), () -> !isTrue(existingMeta.getTitleLocked()), newMeta.getTitleLocked(), existingMeta.getTitleLocked());
        compare(changes, "subtitle", clear.isSubtitle(), newMeta.getSubtitle(), existingMeta.getSubtitle(), () -> !isTrue(existingMeta.getSubtitleLocked()), newMeta.getSubtitleLocked(), existingMeta.getSubtitleLocked());
        compare(changes, "publisher", clear.isPublisher(), newMeta.getPublisher(), existingMeta.getPublisher(), () -> !isTrue(existingMeta.getPublisherLocked()), newMeta.getPublisherLocked(), existingMeta.getPublisherLocked());
        compare(changes, "publishedDate", clear.isPublishedDate(), newMeta.getPublishedDate(), existingMeta.getPublishedDate(), () -> !isTrue(existingMeta.getPublishedDateLocked()), newMeta.getPublishedDateLocked(), existingMeta.getPublishedDateLocked());
        compare(changes, "description", clear.isDescription(), newMeta.getDescription(), existingMeta.getDescription(), () -> !isTrue(existingMeta.getDescriptionLocked()), newMeta.getDescriptionLocked(), existingMeta.getDescriptionLocked());
        compare(changes, "seriesName", clear.isSeriesName(), newMeta.getSeriesName(), existingMeta.getSeriesName(), () -> !isTrue(existingMeta.getSeriesNameLocked()), newMeta.getSeriesNameLocked(), existingMeta.getSeriesNameLocked());
        compare(changes, "seriesNumber", clear.isSeriesNumber(), newMeta.getSeriesNumber(), existingMeta.getSeriesNumber(), () -> !isTrue(existingMeta.getSeriesNumberLocked()), newMeta.getSeriesNumberLocked(), existingMeta.getSeriesNumberLocked());
        compare(changes, "seriesTotal", clear.isSeriesTotal(), newMeta.getSeriesTotal(), existingMeta.getSeriesTotal(), () -> !isTrue(existingMeta.getSeriesTotalLocked()), newMeta.getSeriesTotalLocked(), existingMeta.getSeriesTotalLocked());
        compare(changes, "isbn13", clear.isIsbn13(), newMeta.getIsbn13(), existingMeta.getIsbn13(), () -> !isTrue(existingMeta.getIsbn13Locked()), newMeta.getIsbn13Locked(), existingMeta.getIsbn13Locked());
        compare(changes, "isbn10", clear.isIsbn10(), newMeta.getIsbn10(), existingMeta.getIsbn10(), () -> !isTrue(existingMeta.getIsbn10Locked()), newMeta.getIsbn10Locked(), existingMeta.getIsbn10Locked());
        compare(changes, "asin", clear.isAsin(), newMeta.getAsin(), existingMeta.getAsin(), () -> !isTrue(existingMeta.getAsinLocked()), newMeta.getAsinLocked(), existingMeta.getAsinLocked());
        compare(changes, "goodreadsId", clear.isGoodreadsId(), newMeta.getGoodreadsId(), existingMeta.getGoodreadsId(), () -> !isTrue(existingMeta.getGoodreadsIdLocked()), newMeta.getGoodreadsIdLocked(), existingMeta.getGoodreadsIdLocked());
        compare(changes, "comicvineId", clear.isComicvineId(), newMeta.getComicvineId(), existingMeta.getComicvineId(), () -> !isTrue(existingMeta.getComicvineIdLocked()), newMeta.getComicvineIdLocked(), existingMeta.getComicvineIdLocked());
        compare(changes, "hardcoverId", clear.isHardcoverId(), newMeta.getHardcoverId(), existingMeta.getHardcoverId(), () -> !isTrue(existingMeta.getHardcoverIdLocked()), newMeta.getHardcoverIdLocked(), existingMeta.getHardcoverIdLocked());
        compare(changes, "googleId", clear.isGoogleId(), newMeta.getGoogleId(), existingMeta.getGoogleId(), () -> !isTrue(existingMeta.getGoogleIdLocked()), newMeta.getGoogleIdLocked(), existingMeta.getGoogleIdLocked());
        compare(changes, "pageCount", clear.isPageCount(), newMeta.getPageCount(), existingMeta.getPageCount(), () -> !isTrue(existingMeta.getPageCountLocked()), newMeta.getPageCountLocked(), existingMeta.getPageCountLocked());
        compare(changes, "language", clear.isLanguage(), newMeta.getLanguage(), existingMeta.getLanguage(), () -> !isTrue(existingMeta.getLanguageLocked()), newMeta.getLanguageLocked(), existingMeta.getLanguageLocked());
        compare(changes, "personalRating", clear.isPersonalRating(), newMeta.getPersonalRating(), existingMeta.getPersonalRating(), () -> !isTrue(existingMeta.getPersonalRatingLocked()), newMeta.getPersonalRatingLocked(), existingMeta.getPersonalRatingLocked());
        compare(changes, "amazonRating", clear.isAmazonRating(), newMeta.getAmazonRating(), existingMeta.getAmazonRating(), () -> !isTrue(existingMeta.getAmazonRatingLocked()), newMeta.getAmazonRatingLocked(), existingMeta.getAmazonRatingLocked());
        compare(changes, "amazonReviewCount", clear.isAmazonReviewCount(), newMeta.getAmazonReviewCount(), existingMeta.getAmazonReviewCount(), () -> !isTrue(existingMeta.getAmazonReviewCountLocked()), newMeta.getAmazonReviewCountLocked(), existingMeta.getAmazonReviewCountLocked());
        compare(changes, "goodreadsRating", clear.isGoodreadsRating(), newMeta.getGoodreadsRating(), existingMeta.getGoodreadsRating(), () -> !isTrue(existingMeta.getGoodreadsRatingLocked()), newMeta.getGoodreadsRatingLocked(), existingMeta.getGoodreadsRatingLocked());
        compare(changes, "goodreadsReviewCount", clear.isGoodreadsReviewCount(), newMeta.getGoodreadsReviewCount(), existingMeta.getGoodreadsReviewCount(), () -> !isTrue(existingMeta.getGoodreadsReviewCountLocked()), newMeta.getGoodreadsReviewCountLocked(), existingMeta.getGoodreadsReviewCountLocked());
        compare(changes, "hardcoverRating", clear.isHardcoverRating(), newMeta.getHardcoverRating(), existingMeta.getHardcoverRating(), () -> !isTrue(existingMeta.getHardcoverRatingLocked()), newMeta.getHardcoverRatingLocked(), existingMeta.getHardcoverRatingLocked());
        compare(changes, "hardcoverReviewCount", clear.isHardcoverReviewCount(), newMeta.getHardcoverReviewCount(), existingMeta.getHardcoverReviewCount(), () -> !isTrue(existingMeta.getHardcoverReviewCountLocked()), newMeta.getHardcoverReviewCountLocked(), existingMeta.getHardcoverReviewCountLocked());
        compare(changes, "authors", clear.isAuthors(), newMeta.getAuthors(), toNameSet(existingMeta.getAuthors()), () -> !isTrue(existingMeta.getAuthorsLocked()), newMeta.getAuthorsLocked(), existingMeta.getAuthorsLocked());
        compare(changes, "categories", clear.isCategories(), newMeta.getCategories(), toNameSet(existingMeta.getCategories()), () -> !isTrue(existingMeta.getCategoriesLocked()), newMeta.getCategoriesLocked(), existingMeta.getCategoriesLocked());

        Boolean coverLockedNew = newMeta.getCoverLocked();
        Boolean coverLockedExisting = existingMeta.getCoverLocked();
        if (differsLock(coverLockedNew, coverLockedExisting)) {
            changes.add("cover lock: [" + isTrue(coverLockedExisting) + "] → [" + isTrue(coverLockedNew) + "]");
        }

        if (!changes.isEmpty()) {
            /*changes.forEach(change -> log.info("Metadata change: {}", change));*/
            return true;
        }

        return false;
    }

    public static boolean hasValueChanges(BookMetadata newMeta, BookMetadataEntity existingMeta, MetadataClearFlags clear) {
        List<String> diffs = new ArrayList<>();
        compareValue(diffs, "title", clear.isTitle(), newMeta.getTitle(), existingMeta.getTitle(), () -> !isTrue(existingMeta.getTitleLocked()));
        compareValue(diffs, "subtitle", clear.isSubtitle(), newMeta.getSubtitle(), existingMeta.getSubtitle(), () -> !isTrue(existingMeta.getSubtitleLocked()));
        compareValue(diffs, "publisher", clear.isPublisher(), newMeta.getPublisher(), existingMeta.getPublisher(), () -> !isTrue(existingMeta.getPublisherLocked()));
        compareValue(diffs, "publishedDate", clear.isPublishedDate(), newMeta.getPublishedDate(), existingMeta.getPublishedDate(), () -> !isTrue(existingMeta.getPublishedDateLocked()));
        compareValue(diffs, "description", clear.isDescription(), newMeta.getDescription(), existingMeta.getDescription(), () -> !isTrue(existingMeta.getDescriptionLocked()));
        compareValue(diffs, "seriesName", clear.isSeriesName(), newMeta.getSeriesName(), existingMeta.getSeriesName(), () -> !isTrue(existingMeta.getSeriesNameLocked()));
        compareValue(diffs, "seriesNumber", clear.isSeriesNumber(), newMeta.getSeriesNumber(), existingMeta.getSeriesNumber(), () -> !isTrue(existingMeta.getSeriesNumberLocked()));
        compareValue(diffs, "seriesTotal", clear.isSeriesTotal(), newMeta.getSeriesTotal(), existingMeta.getSeriesTotal(), () -> !isTrue(existingMeta.getSeriesTotalLocked()));
        compareValue(diffs, "isbn13", clear.isIsbn13(), newMeta.getIsbn13(), existingMeta.getIsbn13(), () -> !isTrue(existingMeta.getIsbn13Locked()));
        compareValue(diffs, "isbn10", clear.isIsbn10(), newMeta.getIsbn10(), existingMeta.getIsbn10(), () -> !isTrue(existingMeta.getIsbn10Locked()));
        compareValue(diffs, "asin", clear.isAsin(), newMeta.getAsin(), existingMeta.getAsin(), () -> !isTrue(existingMeta.getAsinLocked()));
        compareValue(diffs, "goodreadsId", clear.isGoodreadsId(), newMeta.getGoodreadsId(), existingMeta.getGoodreadsId(), () -> !isTrue(existingMeta.getGoodreadsIdLocked()));
        compareValue(diffs, "comicvineId", clear.isComicvineId(), newMeta.getComicvineId(), existingMeta.getComicvineId(), () -> !isTrue(existingMeta.getComicvineIdLocked()));
        compareValue(diffs, "hardcoverId", clear.isHardcoverId(), newMeta.getHardcoverId(), existingMeta.getHardcoverId(), () -> !isTrue(existingMeta.getHardcoverIdLocked()));
        compareValue(diffs, "googleId", clear.isGoogleId(), newMeta.getGoogleId(), existingMeta.getGoogleId(), () -> !isTrue(existingMeta.getGoogleIdLocked()));
        compareValue(diffs, "pageCount", clear.isPageCount(), newMeta.getPageCount(), existingMeta.getPageCount(), () -> !isTrue(existingMeta.getPageCountLocked()));
        compareValue(diffs, "language", clear.isLanguage(), newMeta.getLanguage(), existingMeta.getLanguage(), () -> !isTrue(existingMeta.getLanguageLocked()));
        compareValue(diffs, "personalRating", clear.isPersonalRating(), newMeta.getPersonalRating(), existingMeta.getPersonalRating(), () -> !isTrue(existingMeta.getPersonalRatingLocked()));
        compareValue(diffs, "amazonRating", clear.isAmazonRating(), newMeta.getAmazonRating(), existingMeta.getAmazonRating(), () -> !isTrue(existingMeta.getAmazonRatingLocked()));
        compareValue(diffs, "amazonReviewCount", clear.isAmazonReviewCount(), newMeta.getAmazonReviewCount(), existingMeta.getAmazonReviewCount(), () -> !isTrue(existingMeta.getAmazonReviewCountLocked()));
        compareValue(diffs, "goodreadsRating", clear.isGoodreadsRating(), newMeta.getGoodreadsRating(), existingMeta.getGoodreadsRating(), () -> !isTrue(existingMeta.getGoodreadsRatingLocked()));
        compareValue(diffs, "goodreadsReviewCount", clear.isGoodreadsReviewCount(), newMeta.getGoodreadsReviewCount(), existingMeta.getGoodreadsReviewCount(), () -> !isTrue(existingMeta.getGoodreadsReviewCountLocked()));
        compareValue(diffs, "hardcoverRating", clear.isHardcoverRating(), newMeta.getHardcoverRating(), existingMeta.getHardcoverRating(), () -> !isTrue(existingMeta.getHardcoverRatingLocked()));
        compareValue(diffs, "hardcoverReviewCount", clear.isHardcoverReviewCount(), newMeta.getHardcoverReviewCount(), existingMeta.getHardcoverReviewCount(), () -> !isTrue(existingMeta.getHardcoverReviewCountLocked()));
        compareValue(diffs, "authors", clear.isAuthors(), newMeta.getAuthors(), toNameSet(existingMeta.getAuthors()), () -> !isTrue(existingMeta.getAuthorsLocked()));
        compareValue(diffs, "categories", clear.isCategories(), newMeta.getCategories(), toNameSet(existingMeta.getCategories()), () -> !isTrue(existingMeta.getCategoriesLocked()));
        return !diffs.isEmpty();
    }

    public static boolean hasLockChanges(BookMetadata newMeta, BookMetadataEntity existingMeta) {
        if (differsLock(newMeta.getTitleLocked(), existingMeta.getTitleLocked())) return true;
        if (differsLock(newMeta.getSubtitleLocked(), existingMeta.getSubtitleLocked())) return true;
        if (differsLock(newMeta.getPublisherLocked(), existingMeta.getPublisherLocked())) return true;
        if (differsLock(newMeta.getPublishedDateLocked(), existingMeta.getPublishedDateLocked())) return true;
        if (differsLock(newMeta.getDescriptionLocked(), existingMeta.getDescriptionLocked())) return true;
        if (differsLock(newMeta.getSeriesNameLocked(), existingMeta.getSeriesNameLocked())) return true;
        if (differsLock(newMeta.getSeriesNumberLocked(), existingMeta.getSeriesNumberLocked())) return true;
        if (differsLock(newMeta.getSeriesTotalLocked(), existingMeta.getSeriesTotalLocked())) return true;
        if (differsLock(newMeta.getIsbn13Locked(), existingMeta.getIsbn13Locked())) return true;
        if (differsLock(newMeta.getIsbn10Locked(), existingMeta.getIsbn10Locked())) return true;
        if (differsLock(newMeta.getAsinLocked(), existingMeta.getAsinLocked())) return true;
        if (differsLock(newMeta.getGoodreadsIdLocked(), existingMeta.getGoodreadsIdLocked())) return true;
        if (differsLock(newMeta.getComicvineIdLocked(), existingMeta.getComicvineIdLocked())) return true;
        if (differsLock(newMeta.getHardcoverIdLocked(), existingMeta.getHardcoverIdLocked())) return true;
        if (differsLock(newMeta.getGoogleIdLocked(), existingMeta.getGoogleIdLocked())) return true;
        if (differsLock(newMeta.getPageCountLocked(), existingMeta.getPageCountLocked())) return true;
        if (differsLock(newMeta.getLanguageLocked(), existingMeta.getLanguageLocked())) return true;
        if (differsLock(newMeta.getPersonalRatingLocked(), existingMeta.getPersonalRatingLocked())) return true;
        if (differsLock(newMeta.getAmazonRatingLocked(), existingMeta.getAmazonRatingLocked())) return true;
        if (differsLock(newMeta.getAmazonReviewCountLocked(), existingMeta.getAmazonReviewCountLocked())) return true;
        if (differsLock(newMeta.getGoodreadsRatingLocked(), existingMeta.getGoodreadsRatingLocked())) return true;
        if (differsLock(newMeta.getGoodreadsReviewCountLocked(), existingMeta.getGoodreadsReviewCountLocked())) return true;
        if (differsLock(newMeta.getHardcoverRatingLocked(), existingMeta.getHardcoverRatingLocked())) return true;
        if (differsLock(newMeta.getHardcoverReviewCountLocked(), existingMeta.getHardcoverReviewCountLocked())) return true;
        if (differsLock(newMeta.getCoverLocked(), existingMeta.getCoverLocked())) return true;
        if (differsLock(newMeta.getAuthorsLocked(), existingMeta.getAuthorsLocked())) return true;
        if (differsLock(newMeta.getCategoriesLocked(), existingMeta.getCategoriesLocked())) return true;
        if (differsLock(newMeta.getReviewsLocked(), existingMeta.getReviewsLocked())) return true;
        return false;
    }

    private static void compare(List<String> diffs, String field, boolean shouldClear, Object newVal, Object oldVal, Supplier<Boolean> isUnlocked, Boolean newLock, Boolean oldLock) {
        boolean valueChanged = differs(shouldClear, newVal, oldVal, isUnlocked);
        boolean lockChanged = differsLock(newLock, oldLock);

        if (valueChanged || lockChanged) {
            StringBuilder sb = new StringBuilder();
            sb.append(field);
            if (valueChanged) sb.append(" value: [").append(safe(oldVal)).append("] → [").append(safe(newVal)).append("]");
            if (lockChanged) sb.append(" lock: [").append(isTrue(oldLock)).append("] → [").append(isTrue(newLock)).append("]");
            diffs.add(sb.toString());
        }
    }

    private static <T> void compareValue(List<String> diffs,
                                         String field,
                                         boolean shouldClear,
                                         T newVal,
                                         T oldVal,
                                         Supplier<Boolean> isUnlocked) {
        if (differs(shouldClear, newVal, oldVal, isUnlocked)) {
            diffs.add(field + " changed");
        }
    }

    private static boolean differs(boolean shouldClear, Object newVal, Object oldVal, Supplier<Boolean> isUnlocked) {
        if (!isUnlocked.get()) return false;

        Object normNew = normalize(newVal);
        Object normOld = normalize(oldVal);

        // Ignore transitions from null to empty string or empty set
        if (normOld == null && isEffectivelyEmpty(normNew)) return false;
        if (shouldClear) return normOld != null;

        return !Objects.equals(normNew, normOld);
    }

    private static boolean isEffectivelyEmpty(Object value) {
        if (value == null) return true;
        if (value instanceof String s) return s.isBlank();
        if (value instanceof Collection<?> c) return c.isEmpty();
        return false;
    }

    private static boolean differsLock(Boolean dtoLock, Boolean entityLock) {
        return !Objects.equals(Boolean.TRUE.equals(dtoLock), Boolean.TRUE.equals(entityLock));
    }

    private static String safe(Object val) {
        if (val == null) return "null";
        if (val instanceof Set<?> set) return set.stream().map(String::valueOf).sorted().collect(Collectors.joining(", ", "[", "]"));
        return val.toString().strip();
    }

    private static Object normalize(Object value) {
        if (value instanceof String s) return s.strip();
        return value;
    }

    private static Set<String> toNameSet(Set<?> entities) {
        if (entities == null) {
            return Collections.emptySet();
        }
        return entities.stream()
            .map(e -> {
                if (e instanceof AuthorEntity author) {
                    return author.getName();
                }
                if (e instanceof CategoryEntity category) {
                    return category.getName();
                }
                return e.toString();
            })
            .collect(Collectors.toSet());
    }
}
