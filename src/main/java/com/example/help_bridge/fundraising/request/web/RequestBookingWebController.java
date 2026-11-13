package com.example.help_bridge.fundraising.request.web;

import com.example.help_bridge.fundraising.fund.exception.FundNotFoundException;
import com.example.help_bridge.fundraising.fund.repository.FundRepository;
import com.example.help_bridge.fundraising.request.exception.FundNotApprovedException;
import com.example.help_bridge.fundraising.request.exception.InvalidRequestStateException;
import com.example.help_bridge.fundraising.request.exception.RequestNotFoundException;
import com.example.help_bridge.fundraising.request.security.RequestAccessPolicy;
import com.example.help_bridge.fundraising.request.service.RequestBookingService;
import com.example.help_bridge.fundraising.request.service.RequestService;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/requests/{id}/book")
@PreAuthorize("hasRole('FUND_REPRESENTATIVE')")
public class RequestBookingWebController {
    private final RequestAccessPolicy access;
    private final RequestService requests;
    private final RequestBookingService booking;
    private final FundRepository funds;

    public RequestBookingWebController(RequestAccessPolicy access, RequestService requests,
                                       RequestBookingService booking, FundRepository funds) {
        this.access = access;
        this.requests = requests;
        this.booking = booking;
        this.funds = funds;
    }

    @GetMapping
    public String confirmation(@PathVariable Long id, Model model) {
        var viewer = access.currentViewer();
        var request = requests.getRequestById(id);
        if (!viewer.permissions(request).book()) {
            throw new InvalidRequestStateException("Request or fund is not available for booking");
        }
        var fund = funds.findById(viewer.fundId()).orElseThrow(() -> new FundNotFoundException(viewer.fundId()));
        model.addAttribute("request", request);
        model.addAttribute("fundName", fund.getFundName());
        return "requests/booking-confirmation";
    }

    @PostMapping
    public String book(@PathVariable Long id, RedirectAttributes redirect) {
        var viewer = access.currentViewer();
        try {
            booking.bookRequest(id, viewer.fundId());
            redirect.addFlashAttribute("message", "Заявку заброньовано. Ваш фонд узяв її в роботу.");
            return "redirect:/requests/" + id;
        } catch (RequestNotFoundException e) {
            redirect.addFlashAttribute("error", "Заявку видалено або її не знайдено. Список оновлено.");
        } catch (FundNotApprovedException | FundNotFoundException e) {
            redirect.addFlashAttribute("error", "Бронювання недоступне: ваш фонд має бути схвалений та доступний у системі.");
        } catch (InvalidRequestStateException | ObjectOptimisticLockingFailureException e) {
            redirect.addFlashAttribute("error", "Заявка вже не доступна для бронювання: її міг узяти в роботу інший фонд. Перевірте оновлений список.");
        }
        return "redirect:/requests";
    }
}
