package com.example.help_bridge.controller;

import com.example.help_bridge.dto.request.AssignVolunteerRequest;
import com.example.help_bridge.dto.request.CompleteAssignmentRequest;
import com.example.help_bridge.dto.request.ReturnAssignmentRequest;
import com.example.help_bridge.dto.response.AssignVolunteerResponse;
import com.example.help_bridge.dto.response.CompleteAssignmentResponse;
import com.example.help_bridge.dto.response.FundraiserAssignmentResponse;
import com.example.help_bridge.dto.response.ReturnAssignmentResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// TODO When using Thymeleaf will be converted to @Controller
@RestController
@RequestMapping("/api")
@Validated
public class FundraiserAssignmentController {


    @PatchMapping("/fundraiser-assignments/{assignmentId}/complete")
    public ResponseEntity<CompleteAssignmentResponse> completeAssignment(
            @PathVariable(value = "assignmentId") Long assignmentId,
            @RequestBody @Valid CompleteAssignmentRequest completeAssignmentRequest
            ){
        return ResponseEntity.ok(null);
    }

    @PatchMapping("/fundraiser-assignments/{assignmentId}/return")
    public ResponseEntity<ReturnAssignmentResponse> returnAssignment(
            @PathVariable(value = "assignmentId") Long assignmentId,
            @RequestBody @Valid ReturnAssignmentRequest returnAssignmentRequest
    ){
        return ResponseEntity.ok(null);
    }

    @PostMapping("/fundraisers/{fundraiserId}/assignments")
    public ResponseEntity<AssignVolunteerResponse> assignVolunteerToFund(
            @PathVariable(value = "fundraiserId") Long fundraiserId,
            @RequestBody @Valid AssignVolunteerRequest assignVolunteerRequest
    ){
        return ResponseEntity.ok(null);
    }

    @GetMapping("/fundraisers/{fundraiserId}/assignments")
    public ResponseEntity<List<FundraiserAssignmentResponse>> fundraiserAssignments(
            @PathVariable(value = "fundraiserId") Long fundraiserId
    ){
        return ResponseEntity.ok(null);
    }

    @GetMapping("/volunteers/{volunteerId}/assignments")
    public ResponseEntity<List<FundraiserAssignmentResponse>> volunteersAssignments(
            @PathVariable(value = "volunteerId") Long volunteerId
    ){
        return ResponseEntity.ok(null);
    }




}
