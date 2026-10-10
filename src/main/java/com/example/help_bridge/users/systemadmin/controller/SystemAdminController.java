package com.example.help_bridge.users.systemadmin.controller;

import io.swagger.v3.oas.annotations.Parameter;
import com.example.help_bridge.users.systemadmin.dto.request.SystemAdminCreateRequest;
import com.example.help_bridge.users.systemadmin.dto.request.SystemAdminUpdateRequest;
import com.example.help_bridge.users.systemadmin.dto.response.SystemAdminResponse;
import com.example.help_bridge.users.systemadmin.service.SystemAdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/system-admins")
@Tag(name = "System admins", description = "Управління системними адміністраторами")
public class SystemAdminController {

    private final SystemAdminService service;

    public SystemAdminController(SystemAdminService service) {
        this.service = service;
    }

    @Operation(summary = "Отримати всіх адміністраторів")
    @ApiResponse(responseCode = "200", description = "Список адміністраторів")
    @GetMapping
    public ResponseEntity<List<SystemAdminResponse>> getAllSystemAdmins() {
        return ResponseEntity.ok(service.getAllSystemAdmins());
    }

    @Operation(summary = "Пошук адміністраторів за прізвищем")
    @ApiResponse(responseCode = "200", description = "Результати пошуку")
    @GetMapping("/search")
    public ResponseEntity<List<SystemAdminResponse>> search(
            @Parameter(description = "Прізвище", example = "Іваненко")
            @RequestParam("lastName") String lastName) {
        return ResponseEntity.ok(service.searchByLastName(lastName));
    }

    @Operation(summary = "Отримати адміністратора за id")
    @ApiResponse(responseCode = "200", description = "Адміністратора знайдено")
    @ApiResponse(responseCode = "404", description = "Адміністратора не знайдено",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    @GetMapping("/{id}")
    public ResponseEntity<SystemAdminResponse> getSystemAdminById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getSystemAdminById(id));
    }

    @Operation(summary = "Створити системного адміністратора")
    @ApiResponse(responseCode = "201", description = "Адміністратора створено")
    @ApiResponse(responseCode = "400", description = "Помилка валідації",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "409", description = "Адміністратор з таким email вже існує",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    @PostMapping
    public ResponseEntity<SystemAdminResponse> createSystemAdmin(@RequestBody @Valid SystemAdminCreateRequest request) {
        SystemAdminResponse response = service.createSystemAdmin(request);
        return ResponseEntity
                .created(URI.create("/api/system-admins/" + response.id()))
                .body(response);
    }

    @Operation(summary = "Оновити ім'я та прізвище адміністратора")
    @ApiResponse(responseCode = "200", description = "Дані оновлено")
    @PutMapping("/{id}")
    public ResponseEntity<SystemAdminResponse> updateSystemAdmin(
            @PathVariable Long id,
            @RequestBody @Valid SystemAdminUpdateRequest request
    ) {
        return ResponseEntity.ok(service.updateSystemAdmin(id, request));
    }

    @Operation(summary = "Видалити адміністратора")
    @ApiResponse(responseCode = "204", description = "Адміністратора видалено")
    @ApiResponse(responseCode = "409", description = "Адміністратор має акти верифікації",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSystemAdminById(@PathVariable Long id) {
        service.deleteSystemAdminById(id);
        return ResponseEntity.noContent().build();
    }
}