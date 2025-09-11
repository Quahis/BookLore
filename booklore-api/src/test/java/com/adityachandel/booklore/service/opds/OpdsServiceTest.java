package com.adityachandel.booklore.service.opds;

import com.adityachandel.booklore.config.security.service.AuthenticationService;
import com.adityachandel.booklore.config.security.userdetails.OpdsUserDetails;
import com.adityachandel.booklore.mapper.custom.BookLoreUserTransformer;
import com.adityachandel.booklore.model.dto.*;
import com.adityachandel.booklore.model.entity.BookLoreUserEntity;
import com.adityachandel.booklore.repository.UserRepository;
import com.adityachandel.booklore.service.BookQueryService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;

@ExtendWith(MockitoExtension.class)
class OpdsServiceTest {

    @Mock
    private BookQueryService bookQueryService;
    @Mock
    private AuthenticationService authenticationService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private BookLoreUserTransformer bookLoreUserTransformer;
    @Mock
    private com.adityachandel.booklore.service.library.LibraryService libraryService;
    @Mock
    private com.adityachandel.booklore.repository.ShelfRepository shelfRepository;
    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private OpdsService service;

    @Test
    void generateCatalogFeed_defaultsToV1_callsGetAllBooksAndReturnsFeed() {
        OpdsUserDetails details = mock(OpdsUserDetails.class);
        when(authenticationService.getOpdsUser()).thenReturn(details);
        when(details.getOpdsUser()).thenReturn(mock(OpdsUser.class));

        when(request.getHeader("Accept")).thenReturn(null);
        when(request.getRequestURI()).thenReturn("/opds/catalog");
        when(bookQueryService.getAllBooks(true)).thenReturn(List.of()); // minimal stub

        String feed = service.generateCatalogFeed(request);

        assertNotNull(feed);
        assertTrue(feed.contains("<title>Booklore Catalog</title>"));
        verify(bookQueryService).getAllBooks(true);
    }

    @Test
    void generateSearchResults_withQuery_usesSearchBooksByMetadata_and_producesFeed() {
        OpdsUserDetails details = mock(OpdsUserDetails.class);
        when(authenticationService.getOpdsUser()).thenReturn(details);
        when(details.getOpdsUser()).thenReturn(mock(OpdsUser.class));

        // build a minimal Book mock so feed generation won't NPE
        Book book = mock(Book.class);
        BookMetadata metadata = mock(BookMetadata.class);
        when(book.getMetadata()).thenReturn(metadata);
        when(metadata.getTitle()).thenReturn("Searchable Title");
        when(metadata.getAuthors()).thenReturn(Set.of("Author One"));
        when(book.getId()).thenReturn(11L);
        when(book.getAddedOn()).thenReturn(null);
        when(book.getBookType()).thenReturn(null); // will map to default mime type

        when(bookQueryService.searchBooksByMetadata("query")).thenReturn(List.of(book));
        when(request.getHeader("Accept")).thenReturn(null);
        when(request.getRequestURI()).thenReturn("/opds/search");

        String feed = service.generateSearchResults(request, "query");

        assertNotNull(feed);
        assertTrue(feed.contains("Searchable Title"));
        verify(bookQueryService).searchBooksByMetadata("query");
    }

    @Test
    void generateCatalogFeed_opdsV2Admin_callsGetAllBooks_and_returnsV2Json() {
        OpdsUserDetails details = mock(OpdsUserDetails.class);
        when(authenticationService.getOpdsUser()).thenReturn(details);
        when(details.getOpdsUser()).thenReturn(null);

        OpdsUserV2 opdsUserV2 = mock(OpdsUserV2.class);
        when(details.getOpdsUserV2()).thenReturn(opdsUserV2);
        when(opdsUserV2.getUserId()).thenReturn(9L);

        // entity with deep-stubbed permissions for the initial permission check
        BookLoreUserEntity entity = mock(BookLoreUserEntity.class, RETURNS_DEEP_STUBS);
        when(entity.getPermissions().isPermissionAccessOpds()).thenReturn(true);
        when(userRepository.findById(9L)).thenReturn(Optional.of(entity));

        // transformer returns DTO indicating admin user
        BookLoreUser userDto = mock(BookLoreUser.class);
        BookLoreUser.UserPermissions dtoPerms = mock(BookLoreUser.UserPermissions.class);
        when(dtoPerms.isAdmin()).thenReturn(true);
        when(userDto.getPermissions()).thenReturn(dtoPerms);
        when(bookLoreUserTransformer.toDTO(entity)).thenReturn(userDto);

        // Accept header drives v2 selection; do not stub request.getRequestURI() (unused here)
        when(request.getHeader("Accept")).thenReturn("application/opds+json;version=2.0");
        // stub the backend call exercised by getAllowedBooksPage for admin + no query
        when(bookQueryService.getAllBooksPage(true, 1, 50)).thenReturn(new org.springframework.data.domain.PageImpl<>(List.of()));

        String feed = service.generateCatalogFeed(request);

        assertNotNull(feed);
        assertTrue(feed.trim().startsWith("{"));
        assertTrue(feed.contains("\"publications\""));
        verify(userRepository).findById(9L);
        verify(bookQueryService).getAllBooksPage(true, 1, 50);
    }


    @Test
    void generateSearchResults_opdsV2_callsSearch_and_returnsV2Json() {
        // Setup v2 user (admin) so getAllowedBooks will call searchBooksByMetadata(query)
        OpdsUserDetails details = mock(OpdsUserDetails.class);
        when(authenticationService.getOpdsUser()).thenReturn(details);
        when(details.getOpdsUser()).thenReturn(null);

        OpdsUserV2 opdsUserV2 = mock(OpdsUserV2.class);
        when(details.getOpdsUserV2()).thenReturn(opdsUserV2);
        when(opdsUserV2.getUserId()).thenReturn(9L);

        BookLoreUserEntity entity = mock(BookLoreUserEntity.class, RETURNS_DEEP_STUBS);
        when(entity.getPermissions().isPermissionAccessOpds()).thenReturn(true);
        when(userRepository.findById(9L)).thenReturn(Optional.of(entity));

        BookLoreUser userDto = mock(BookLoreUser.class);
        BookLoreUser.UserPermissions dtoPerms = mock(BookLoreUser.UserPermissions.class);
        when(dtoPerms.isAdmin()).thenReturn(true);
        when(userDto.getPermissions()).thenReturn(dtoPerms);
        when(bookLoreUserTransformer.toDTO(entity)).thenReturn(userDto);

        // Accept header drives v2 selection; do not stub request.getRequestURI()
        when(request.getHeader("Accept")).thenReturn("application/opds+json;version=2.0");

        // getAllowedBooksPage will invoke searchBooksByMetadataPage for admin + query
        when(bookQueryService.searchBooksByMetadataPage("query", 1, 50)).thenReturn(new org.springframework.data.domain.PageImpl<>(List.of(mock(Book.class))));

        String feed = service.generateSearchResults(request, "query");

        assertNotNull(feed);
        assertTrue(feed.trim().startsWith("{"));
        assertTrue(feed.contains("\"publications\""));
        verify(userRepository).findById(9L);
        verify(bookQueryService).searchBooksByMetadataPage("query", 1, 50);
    }

    @Test
    void generateCatalogFeed_opdsV2NonAdmin_callsLibraryScopedQueryMethods() {
        OpdsUserDetails details = mock(OpdsUserDetails.class);
        when(authenticationService.getOpdsUser()).thenReturn(details);
        when(details.getOpdsUser()).thenReturn(null);

        OpdsUserV2 opdsUserV2 = mock(OpdsUserV2.class);
        when(details.getOpdsUserV2()).thenReturn(opdsUserV2);
        when(opdsUserV2.getUserId()).thenReturn(21L);

        BookLoreUserEntity entity = mock(BookLoreUserEntity.class, RETURNS_DEEP_STUBS);
        when(entity.getPermissions().isPermissionAccessOpds()).thenReturn(true); // allow access
        when(userRepository.findById(21L)).thenReturn(Optional.of(entity));

        // DTO: non-admin with assigned libraries
        BookLoreUser userDto = mock(BookLoreUser.class);
        BookLoreUser.UserPermissions dtoPerms = mock(BookLoreUser.UserPermissions.class);
        when(dtoPerms.isAdmin()).thenReturn(false);
        when(userDto.getPermissions()).thenReturn(dtoPerms);

        Library lib = mock(Library.class);
        when(lib.getId()).thenReturn(7L);
        when(userDto.getAssignedLibraries()).thenReturn(List.of(lib));

        when(bookLoreUserTransformer.toDTO(entity)).thenReturn(userDto);

        when(request.getHeader("Accept")).thenReturn(null); // v1 default; feed version not relevant for allowed-books logic
        when(request.getRequestURI()).thenReturn("/opds/catalog");
        when(bookQueryService.getAllBooksByLibraryIds(Set.of(7L), true)).thenReturn(List.of());

        String feed = service.generateCatalogFeed(request);

        assertNotNull(feed);
        verify(bookQueryService).getAllBooksByLibraryIds(Set.of(7L), true);
        verify(userRepository).findById(21L);
    }

    @Test
    void generateCatalogFeed_opdsV2UserMissingBookLoreUser_throwsAccessDenied() {
        OpdsUserDetails details = mock(OpdsUserDetails.class);
        when(authenticationService.getOpdsUser()).thenReturn(details);
        when(details.getOpdsUser()).thenReturn(null);

        OpdsUserV2 opdsUserV2 = mock(OpdsUserV2.class);
        when(details.getOpdsUserV2()).thenReturn(opdsUserV2);
        when(opdsUserV2.getUserId()).thenReturn(42L);

        when(userRepository.findById(42L)).thenReturn(Optional.empty());

        AccessDeniedException ex = assertThrows(AccessDeniedException.class, () -> service.generateCatalogFeed(request));
        assertTrue(ex.getMessage().contains("User not found"));
        verify(userRepository).findById(42L);
    }

    @Test
    void generateSearchDescription_opdsV2_returnsOpenSearchXml() {
        when(request.getHeader("Accept")).thenReturn("application/opds+json;version=2.0");
        String desc = service.generateSearchDescription(request);
        assertNotNull(desc);
        assertTrue(desc.contains("<OpenSearchDescription"));
        assertTrue(desc.contains("application/opds+json"));
    }

    @Test
    void generateOpdsV2LibrariesNavigation_admin_listsAllLibraries() {
        OpdsUserDetails details = mock(OpdsUserDetails.class);
        when(authenticationService.getOpdsUser()).thenReturn(details);
        when(details.getOpdsUserV2()).thenReturn(mock(OpdsUserV2.class));
        when(details.getOpdsUserV2().getUserId()).thenReturn(1L);

        BookLoreUserEntity entity = mock(BookLoreUserEntity.class);
        when(userRepository.findById(1L)).thenReturn(Optional.of(entity));

        BookLoreUser dto = mock(BookLoreUser.class);
        BookLoreUser.UserPermissions perms = mock(BookLoreUser.UserPermissions.class);
        when(perms.isAdmin()).thenReturn(true);
        when(dto.getPermissions()).thenReturn(perms);
        when(bookLoreUserTransformer.toDTO(entity)).thenReturn(dto);

        Library lib1 = Library.builder().id(10L).name("AllLib1").build();
        Library lib2 = Library.builder().id(11L).name("AllLib2").build();
        when(libraryService.getAllLibraries()).thenReturn(List.of(lib1, lib2));

        String json = service.generateOpdsV2LibrariesNavigation(request);

        assertNotNull(json);
        assertTrue(json.contains("AllLib1"));
        assertTrue(json.contains("AllLib2"));
        assertTrue(json.contains("/api/v2/opds/catalog?libraryId=10"));
        assertTrue(json.contains("/api/v2/opds/catalog?libraryId=11"));
    }

    @Test
    void generateOpdsV2LibrariesNavigation_nonAdmin_listsAssignedLibraries() {
        OpdsUserDetails details = mock(OpdsUserDetails.class);
        when(authenticationService.getOpdsUser()).thenReturn(details);
        when(details.getOpdsUserV2()).thenReturn(mock(OpdsUserV2.class));
        when(details.getOpdsUserV2().getUserId()).thenReturn(2L);

        BookLoreUserEntity entity = mock(BookLoreUserEntity.class);
        when(userRepository.findById(2L)).thenReturn(Optional.of(entity));

        BookLoreUser dto = mock(BookLoreUser.class);
        BookLoreUser.UserPermissions perms = mock(BookLoreUser.UserPermissions.class);
        when(perms.isAdmin()).thenReturn(false);
        when(dto.getPermissions()).thenReturn(perms);

        Library assigned = Library.builder().id(20L).name("AssignedLib").build();
        when(dto.getAssignedLibraries()).thenReturn(List.of(assigned));
        when(bookLoreUserTransformer.toDTO(entity)).thenReturn(dto);

        String json = service.generateOpdsV2LibrariesNavigation(request);

        assertNotNull(json);
        assertTrue(json.contains("AssignedLib"));
        assertTrue(json.contains("/api/v2/opds/catalog?libraryId=20"));
    }

    @Test
    void generateOpdsV2LibrariesNavigation_withoutV2User_hasNoLibraryItems() {
        OpdsUserDetails details = mock(OpdsUserDetails.class);
        when(authenticationService.getOpdsUser()).thenReturn(details);
        when(details.getOpdsUserV2()).thenReturn(null); // no OPDS v2 principal

        String json = service.generateOpdsV2LibrariesNavigation(request);

        assertNotNull(json);
        assertTrue(json.contains("\"title\":\"Libraries\""));
        assertFalse(json.contains("/api/v2/opds/catalog?libraryId="));
    }
}
