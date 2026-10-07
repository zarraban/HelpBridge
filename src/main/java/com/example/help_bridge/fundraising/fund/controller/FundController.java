package com.example.help_bridge.fundraising.fund.controller;

import com.example.help_bridge.fundraising.fund.dto.request.FundCreateRequest;
import com.example.help_bridge.fundraising.fund.dto.request.FundDescriptUpdateRequest;
import com.example.help_bridge.fundraising.fund.dto.request.FundStatusUpdateRequest;
import com.example.help_bridge.fundraising.fund.dto.request.FundUpdateRequest;
import com.example.help_bridge.fundraising.fund.dto.response.FundResponse;
import com.example.help_bridge.fundraising.fund.service.FundService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/funds")
@Tag(name = "Funds", description = "Управління благодійними фондами")
public class FundController {

    private final FundService service;

    public FundController(FundService service) {
        this.service = service;
    }

    @Operation(summary = "Отримати всі фонди")
    @ApiResponse(responseCode = "200", description = "Список фондів")
    @GetMapping
    public ResponseEntity<List<FundResponse>> getAllFunds() {
        return ResponseEntity.ok(service.getAllFunds());
    }

    @Operation(summary = "Отримати фонд за id")
    @ApiResponse(responseCode = "200", description = "Фонд знайдено")
    @ApiResponse(responseCode = "404", description = "Фонд не знайдено",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    @GetMapping("/{id}")
    public ResponseEntity<FundResponse> getFundById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(service.getFundById(id));
    }

    @Operation(summary = "Створити фонд")
    @ApiResponse(responseCode = "201", description = "Фонд створено")
    @ApiResponse(responseCode = "400", description = "Помилка валідації",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "409", description = "Фонд з такими даними вже існує",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    @PostMapping
    public ResponseEntity<FundResponse> createFund(@RequestBody @Valid FundCreateRequest request) {
        FundResponse response = service.createFund(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @Operation(summary = "Повністю оновити дані фонду")
    @ApiResponse(responseCode = "200", description = "Фонд оновлено")
    @ApiResponse(responseCode = "404", description = "Фонд не знайдено",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    @PutMapping("/{id}")
    public ResponseEntity<FundResponse> updateFund(
            @PathVariable("id") Long id,
            @RequestBody @Valid FundUpdateRequest request
    ) {
        return ResponseEntity.ok(service.updateFund(id, request));
    }

    @Operation(summary = "Змінити статус фонду (схвалення/відхилення адміністратором)")
    @ApiResponse(responseCode = "200", description = "Статус змінено")
    @ApiResponse(responseCode = "404", description = "Фонд або адміністратор не знайдені",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "409", description = "Недопустимий перехід статусу",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    @PatchMapping("/{id}/status")
    public ResponseEntity<FundResponse> updateFundStatus(
            @PathVariable("id") Long id,
            @RequestBody @Valid FundStatusUpdateRequest request
    ) {
        return ResponseEntity.ok(service.updateFundStatus(id, request));
    }

    @Operation(summary = "Оновити опис фонду")
    @ApiResponse(responseCode = "200", description = "Опис оновлено")
    @PatchMapping("/{id}/description")
    public ResponseEntity<FundResponse> updateFundDescription(
            @PathVariable("id") Long id,
            @RequestBody @Valid FundDescriptUpdateRequest request
    ) {
        return ResponseEntity.ok(service.updateFundDescription(id, request));
    }

    @Operation(summary = "Видалити фонд")
    @ApiResponse(responseCode = "204", description = "Фонд видалено")
    @ApiResponse(responseCode = "409", description = "У фонду є запити, видалення неможливе",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFundById(@PathVariable("id") Long id) {
        service.deleteFundById(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Видалити представника з фонду")
    @ApiResponse(responseCode = "204", description = "Представника видалено")
    @DeleteMapping("/{fundId}/representatives/{representativeId}")
    public ResponseEntity<Void> removeRepresentative(@PathVariable("fundId") Long fundId,
                                                     @PathVariable("representativeId") Long representativeId) {
        service.removeRepresentative(fundId, representativeId);
        return ResponseEntity.noContent().build();
    }
}