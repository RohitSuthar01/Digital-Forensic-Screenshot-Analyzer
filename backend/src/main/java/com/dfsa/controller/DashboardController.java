package com.dfsa.controller;

import com.dfsa.dto.DashboardStatsDTO;
import com.dfsa.service.DashboardService;
import com.dfsa.security.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @GetMapping
    public ResponseEntity<DashboardStatsDTO> getDashboardStats(@AuthenticationPrincipal UserDetailsImpl principal) {
        boolean admin = principal.getAuthorities().stream().anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
        return ResponseEntity.ok(dashboardService.getDashboardStats(principal.getId(), admin));
    }
}
