package com.example.help_bridge.fundraising.request.controller;

import com.example.help_bridge.fundraising.request.dto.request.RequestDto.CreateRequestRequest;
import com.example.help_bridge.fundraising.request.dto.request.RequestDto.RequestResponse;
import com.example.help_bridge.fundraising.request.dto.request.RequestDto.UpdateRequestRequest;
import com.example.help_bridge.fundraising.request.entity.RequestStatus;
import com.example.help_bridge.fundraising.request.service.RequestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/requests")
@Tag(name = "Requests", description = "Запити на допомогу")
public class RequestController {

    private final RequestService requestService;

    public RequestController(RequestService requestService) {
        this.requestService = requestService;
    }

    @Operation(summary = "Створити запит на допомогу")
    @ApiResponse(responseCode = "201", description = "Запит створено")
    @ApiResponse(responseCode = "400", description = "Помилка валідації",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "404", description = "Користувача не знайдено",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    @PostMapping
    public ResponseEntity<RequestResponse> createRequest(@RequestBody @Valid CreateRequestRequest request) {
        RequestResponse created = requestService.createRequest(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.id())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @Operation(summary = "Отримати запити (фільтр за статусом, закладом або автором)")
    @ApiResponse(responseCode = "200", description = "Список запитів")
    @GetMapping
    public ResponseEntity<List<RequestResponse>> getRequests(
            @Parameter(description = "Фільтр за статусом") @RequestParam(required = false) RequestStatus status,
            @Parameter(description = "Пошук за назвою закладу", example = "Київська міська лікарня №1")
            @RequestParam(required = false)
            @Pattern(regexp = ".*\\S.*", message = "Institution name must not be blank")
            String institutionName,
            @Parameter(description = "Id автора запиту", example = "1") @RequestParam(required = false) Long requesterId) {

        if (institutionName != null) {
            return ResponseEntity.ok(requestService.searchByInstitution(institutionName));
        }
        if (requesterId != null) {
            return ResponseEntity.ok(requestService.getRequestsByUser(requesterId));
        }
        return ResponseEntity.ok(requestService.getAllRequests(status));
    }

    @Operation(summary = "Отримати спільний пул запитів")
    @ApiResponse(responseCode = "200", description = "Список запитів зі спільного пулу")
    @GetMapping("/shared-pool")
    public ResponseEntity<List<RequestResponse>> getSharedPool() {
        return ResponseEntity.ok(requestService.getSharedPool());
    }

    @Operation(summary = "Отримати запит за id")
    @ApiResponse(responseCode = "200", description = "Запит знайдено")
    @ApiResponse(responseCode = "404", description = "Запит не знайдено",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    @GetMapping("/{id}")
    public ResponseEntity<RequestResponse> getRequestById(@PathVariable Long id) {
        return ResponseEntity.ok(requestService.getRequestById(id));
    }

    @Operation(summary = "Оновити запит")
    @ApiResponse(responseCode = "200", description = "Запит оновлено")
    @ApiResponse(responseCode = "409", description = "Запит у стані, що не дозволяє редагування",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    @PutMapping("/{id}")
    public ResponseEntity<RequestResponse> updateRequest(
            @PathVariable Long id,
            @RequestBody @Valid UpdateRequestRequest request) {
        return ResponseEntity.ok(requestService.updateRequest(id, request));
    }

    @Operation(summary = "Видалити запит")
    @ApiResponse(responseCode = "204", description = "Запит видалено")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRequest(@PathVariable Long id) {
        requestService.deleteRequest(id);
        return ResponseEntity.noContent().build();
    }
}