package org.example.controller;

import org.example.model.Email;
import org.example.repository.EmailRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@Controller
public class EmailController {

    private final EmailRepository emailRepository;

    public EmailController(EmailRepository emailRepository) {
        this.emailRepository = emailRepository;
    }

    /**
     * GET /  — list all emails, newest first.
     */
    @GetMapping("/")
    public String listEmails(Model model) {
        model.addAttribute("emails", emailRepository.findAll());
        return "emails";
    }

    /**
     * GET /email/{id}  — single email detail view.
     */
    @GetMapping("/email/{id}")
    public String emailDetail(@PathVariable UUID id, Model model) {
        Email email = emailRepository.findById(id);
        model.addAttribute("email", email);
        return "email-detail";
    }
}
