package com.example.help_bridge.fundraiser.controller;

import com.example.help_bridge.fundraiser.dto.request.AssignVolunteerRequest;
import com.example.help_bridge.fundraiser.dto.request.CompleteAssignmentRequest;
import com.example.help_bridge.fundraiser.dto.request.ReturnAssignmentRequest;
import com.example.help_bridge.fundraiser.dto.response.AssignVolunteerResponse;
import com.example.help_bridge.fundraiser.dto.response.CompleteAssignmentResponse;
import com.example.help_bridge.fundraiser.dto.response.FundraiserAssignmentResponse;
import com.example.help_bridge.fundraiser.dto.response.ReturnAssignmentResponse;
import com.example.help_bridge.fundraiser.service.FundraiserAssignmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// TODO When using Thymeleaf will be converted to @Controller
@RestController
@RequestMapping("/api")
@Validated
public class FundraiserAssignmentController {

    private final FundraiserAssignmentService assignmentService;

    public FundraiserAssignmentController(FundraiserAssignmentService assignmentService){
        this.assignmentService = assignmentService;
    }

    @PatchMapping("/fundraiser-assignments/{assignmentId}/complete")
    public ResponseEntity<CompleteAssignmentResponse> completeAssignment(
            @PathVariable(value = "assignmentId") Long assignmentId,
            @RequestBody @Valid CompleteAssignmentRequest completeAssignmentRequest
            ){
        return ResponseEntity.ok(assignmentService.completeAssignment(assignmentId, completeAssignmentRequest));
    }

    @PatchMapping("/fundraiser-assignments/{assignmentId}/return")
    public ResponseEntity<ReturnAssignmentResponse> returnAssignment(
            @PathVariable(value = "assignmentId") Long assignmentId,
            @RequestBody @Valid ReturnAssignmentRequest returnAssignmentRequest
    ){
        return ResponseEntity.ok(assignmentService.returnAssignment(assignmentId, returnAssignmentRequest));
    }

    @PostMapping("/fundraisers/{fundraiserId}/assignments")
    public ResponseEntity<AssignVolunteerResponse> assignVolunteerToFund(
            @PathVariable(value = "fundraiserId") Long fundraiserId,
            @RequestBody @Valid AssignVolunteerRequest assignVolunteerRequest
    ){
        return ResponseEntity.ok(assignmentService.assignVolunteerToFund(fundraiserId, assignVolunteerRequest));
    }

    @GetMapping("/fundraisers/{fundraiserId}/assignments")
    public ResponseEntity<List<FundraiserAssignmentResponse>> fundraiserAssignments(
            @PathVariable(value = "fundraiserId") Long fundraiserId
    ){
        return ResponseEntity.ok(assignmentService.getFundraiserAssignments(fundraiserId));
    }

    @GetMapping("/volunteers/{volunteerId}/assignments")
    public ResponseEntity<List<FundraiserAssignmentResponse>> volunteersAssignments(
            @PathVariable(value = "volunteerId") Long volunteerId
    ){
        return ResponseEntity.ok(assignmentService.getVolunteerAssignments(volunteerId));
    }

}
