
package com.jobportal.controller;

import java.security.Principal;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.jobportal.entity.CandidateResume;
import com.jobportal.service.CandidateResumeService;

@Controller
@RequestMapping("/candidate/resume")
public class CandidateResumeController {

    private final CandidateResumeService resumeService;

    public CandidateResumeController(
            CandidateResumeService resumeService) {

        this.resumeService = resumeService;
    }

    // 1. SHOW RESUME UPLOAD PAGE
    @GetMapping
    public String resumePage(
            Principal principal,
            Model model) {

        boolean hasResume = resumeService
                .hasResume(principal.getName());

        model.addAttribute("hasResume", hasResume);

        if (hasResume) {

            CandidateResume resume = resumeService
                    .getCandidateResume(principal.getName());

            model.addAttribute("resume", resume);
        }

        return "candidate-resume";
    }

    // 2. UPLOAD OR REPLACE PDF RESUME
    @PostMapping("/upload")
    public String uploadResume(
            @RequestParam("file") MultipartFile file,
            Principal principal,
            RedirectAttributes redirectAttributes) {

        try {

            resumeService.uploadResume(
                    principal.getName(),
                    file
            );

            redirectAttributes.addFlashAttribute(
                    "success",
                    "Resume uploaded successfully!"
            );

        } catch (IllegalArgumentException ex) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    ex.getMessage()
            );
        }

        return "redirect:/candidate/resume";
    }

    // 3. DOWNLOAD OWN RESUME WITH ORIGINAL FILENAME
    @GetMapping("/download")
    public ResponseEntity<byte[]> downloadResume(
            Principal principal) {

        CandidateResume resume = resumeService
                .getCandidateResume(principal.getName());

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + resume.getFileName() + "\""
                )
                .header(
                        HttpHeaders.CACHE_CONTROL,
                        "no-store"
                )
                .body(resume.getFileData());
    }

    // 4. DELETE OWN RESUME
    @PostMapping("/delete")
    public String deleteResume(
            Principal principal,
            RedirectAttributes redirectAttributes) {

        resumeService.deleteResume(
                principal.getName()
        );

        redirectAttributes.addFlashAttribute(
                "success",
                "Resume deleted successfully!"
        );

        return "redirect:/candidate/resume";
    }
}
