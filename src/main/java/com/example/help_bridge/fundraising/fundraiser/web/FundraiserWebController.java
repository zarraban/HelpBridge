package com.example.help_bridge.fundraising.fundraiser.web;

import com.example.help_bridge.fundraising.fundraiser.service.FundraiserService;
import com.example.help_bridge.fundraising.request.service.RequestService;
import com.example.help_bridge.fundraising.request.exception.RequestNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@Controller
@RequestMapping("/fundraisers")
@PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'FUND_REPRESENTATIVE')")
@RequiredArgsConstructor
public class FundraiserWebController {
    private final FundraiserService fundraisers;
    private final RequestService requests;

    @GetMapping("/{id}")
    public String details(@PathVariable Long id, Model model) {
        var fundraiser = fundraisers.getFundraiserById(id);
        model.addAttribute("fundraiser", fundraiser);
        model.addAttribute("evidences", fundraiser.evidences().stream().map(EvidenceLink::from).toList());
        if (fundraiser.requestId() != null) {
            try {
                model.addAttribute("request", requests.getRequestById(fundraiser.requestId()));
                model.addAttribute("assistanceTypes", requests.getAssistanceTypes());
            } catch (RequestNotFoundException exception) {
                model.addAttribute("requestUnavailable", true);
            }
        }
        return "fundraisers/details";
    }


    @GetMapping
    public String list(@RequestParam(defaultValue = "createdAt") String sortBy,
                       @RequestParam(defaultValue = "asc") String sort,
                       @RequestParam(defaultValue = "1") Long page,
                       @RequestParam(defaultValue = "5") Long size,
                       Model model, RedirectAttributes redirect) {
        var result = fundraisers.getFundraisers(sortBy, sort, page, size);
        int lastPage = Math.max(1, result.getTotalPages());
        if (page > lastPage) {
            redirect.addFlashAttribute("message", "Сторінка більше не містить зборів. Показано актуальний список.");
            return "redirect:" + UriComponentsBuilder.fromPath("/fundraisers")
                    .queryParam("sortBy", sortBy).queryParam("sort", sort)
                    .queryParam("size", size).queryParam("page", lastPage).build().encode().toUriString();
        }
        model.addAttribute("fundraisers", result);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sort", sort.toLowerCase(java.util.Locale.ROOT));
        model.addAttribute("page", page);
        model.addAttribute("size", size);
        model.addAttribute("pageSizes", List.of(5L, 10L, 20L, 50L, 100L));
        return "fundraisers/list";
    }
}
