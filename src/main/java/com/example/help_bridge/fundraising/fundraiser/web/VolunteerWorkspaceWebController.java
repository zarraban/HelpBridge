package com.example.help_bridge.fundraising.fundraiser.web;

import com.example.help_bridge.fundraising.fundraiser.entity.AssignmentStatus;
import com.example.help_bridge.fundraising.fundraiser.service.VolunteerWorkspaceService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
@PreAuthorize("hasRole('VOLUNTEER')")
@RequiredArgsConstructor
public class VolunteerWorkspaceWebController {
    private final VolunteerWorkspaceService workspace;

    @GetMapping("/my/assignments")
    public String assignments(Model model) {
        var all = workspace.getMyAssignments();
        model.addAttribute("activeAssignments", all.stream().filter(a -> a.status() == AssignmentStatus.ACTIVE).toList());
        model.addAttribute("pastAssignments", all.stream().filter(a -> a.status() != AssignmentStatus.ACTIVE).toList());
        return "volunteer-workspace/assignments";
    }

    @GetMapping("/my/fundraisers/{id}")
    public String details(@PathVariable Long id, Model model) {
        var details = workspace.getMyFundraiser(id);
        model.addAttribute("fundraiser", details.fundraiser());
        model.addAttribute("request", details.request());
        model.addAttribute("assignments", details.assignments());
        model.addAttribute("evidences", details.fundraiser().evidences().stream().map(EvidenceLink::from).toList());
        return "volunteer-workspace/details";
    }
}
