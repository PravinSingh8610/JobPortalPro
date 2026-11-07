
package com.jobportal.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

import com.jobportal.entity.Company;
import com.jobportal.service.CompanyService;

@Controller
@RequestMapping("/recruiter/company")
public class CompanyProfileController {

    private final CompanyService companyService;

    public CompanyProfileController(CompanyService companyService) {
        this.companyService = companyService;
    }

    // Open company profile page
    @GetMapping("/profile")
    public String showCompanyProfile(
            Principal principal, Model model) {

        try {
            Company company = companyService
                    .getCompanyProfile(principal.getName());

            model.addAttribute("company", company);

        } catch (RuntimeException e) {
            model.addAttribute("company", new Company());
        }

        return "company-profile";
    }

    // Save or update company profile
    @PostMapping("/profile/update")
    public String updateCompanyProfile(
            @ModelAttribute("company") Company company,
            Principal principal,
            RedirectAttributes redirectAttributes) {

        try {
            companyService.saveOrUpdateCompany(
                    principal.getName(), company);

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Company profile updated successfully!");

        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage", e.getMessage());
        }

        return "redirect:/recruiter/company/profile";
    }
}
