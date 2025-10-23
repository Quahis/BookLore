package com.adityachandel.booklore.controller;


import com.adityachandel.booklore.model.dto.OpdsUserV2;
import com.adityachandel.booklore.model.dto.request.OpdsUserV2CreateRequest;
import com.adityachandel.booklore.service.OpdsUserV2Service;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v2/opds-users")
@RequiredArgsConstructor
@Tag(name = "OPDS Users", description = "Endpoints for managing OPDS users")
public class OpdsUserV2Controller {

    private final OpdsUserV2Service service;

    @Operation(summary = "Get all OPDS users", description = "Retrieve a list of all OPDS users.")
    @ApiResponse(responseCode = "200", description = "List of OPDS users returned successfully")
    @GetMapping
    @PreAuthorize("@securityUtil.isAdmin() or @securityUtil.canAccessOpds()")
    public List<OpdsUserV2> getUsers() {
        return service.getOpdsUsers();
    }

    @Operation(summary = "Create OPDS user", description = "Create a new OPDS user.")
    @ApiResponse(responseCode = "200", description = "OPDS user created successfully")
    @PostMapping
    @PreAuthorize("@securityUtil.isAdmin() or @securityUtil.canAccessOpds()")
    public OpdsUserV2 createUser(
            @Parameter(description = "OPDS user creation request") @RequestBody OpdsUserV2CreateRequest createRequest) {
        return service.createOpdsUser(createRequest);
    }

    @Operation(summary = "Delete OPDS user", description = "Delete an OPDS user by ID.")
    @ApiResponse(responseCode = "204", description = "OPDS user deleted successfully")
    @DeleteMapping("/{id}")
    @PreAuthorize("@securityUtil.isAdmin() or @securityUtil.canAccessOpds()")
    public void deleteUser(
            @Parameter(description = "ID of the OPDS user to delete") @PathVariable Long id) {
        service.deleteOpdsUser(id);
    }
}
