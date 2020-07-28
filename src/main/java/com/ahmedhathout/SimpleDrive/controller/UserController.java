package com.ahmedhathout.SimpleDrive.controller;

import com.ahmedhathout.SimpleDrive.entities.ConfirmationToken;
import com.ahmedhathout.SimpleDrive.entities.User;
import com.ahmedhathout.SimpleDrive.repositories.ConfirmationTokenRepository;
import com.ahmedhathout.SimpleDrive.services.EmailService;
import com.ahmedhathout.SimpleDrive.services.UserService;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.ServletException;
import javax.validation.Valid;

@Log4j2
@AllArgsConstructor
@Controller
@Validated
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private ConfirmationTokenRepository confirmationTokenRepository;

    @Autowired
    private EmailService emailService;

    @GetMapping("/signup")
    public String signUp(Model model) {
        model.addAttribute("user", new User());
        return "signup";
    }

    // TODO Try to make this method as succinct as possible
    @PostMapping("/signup")
    public ModelAndView createNewUser(@ModelAttribute @Valid @NonNull User user,
                                      ModelAndView modelAndView) throws ServletException {

        confirmationTokenRepository.findAll().forEach(confirmationToken -> {
            if (confirmationToken.isExpired()) {
                userService.deleteUser(confirmationToken.getUser());
                confirmationTokenRepository.delete(confirmationToken);
            }
        });

        System.out.println(user);
        userService.createNewUser(user);
        //After successfully Creating user

        ConfirmationToken confirmationToken = ConfirmationToken.builder()
                .user(user)
                .build();

        confirmationTokenRepository.save(confirmationToken);

        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();

        simpleMailMessage.setFrom("noreply@simpledrive.com"); // Looks like it does not matter what this from has
        simpleMailMessage.setTo(user.getEmail());
        simpleMailMessage.setSubject("Simple Drive Registration");
        simpleMailMessage.setText("To confirm your account, please click here : "
                + "https://simpledrive2020.herokuapp.com/signup/confirm-account?email=" + user.getEmail() + "&token=" +
                confirmationToken.getConfirmationToken());

        emailService.sendEmail(simpleMailMessage);

        modelAndView.setViewName("signup");
        modelAndView.addObject("successMessage", "A confirmation email has been sent to "  + user.getEmail() + ". Follow the link there");

        return modelAndView;
    }

    @GetMapping("/signup/confirm-account")
    public String confrimAccount(RedirectAttributes redirectAttributes,
                                 @RequestParam String token) {

        confirmationTokenRepository
                .findByConfirmationToken(token)
                .ifPresentOrElse(
                    // if
                    confirmationToken -> {
                        // Token found. Activating account.
                        User user = userService.findUserByEmail(confirmationToken.getUser().getEmail());
                        user.setEnabled(true);
                        userService.saveUser(user);
                        confirmationTokenRepository.delete(confirmationToken);
                        redirectAttributes.addFlashAttribute("success_signup_message", "Your email has been verified. Please sign in");
                        log.debug("account confirmed");
                    },
                    // else
                    () -> {
                        // Could not find token. Redirect to the login page with failure message.
                        redirectAttributes.addFlashAttribute("failure_signup_message", "This link is invalid. If you had used it before already, just sign in now.");
                        log.debug("Could not confirm account");
                    }
                );

        return "redirect:/login";
    }

    @GetMapping("logout")
    public String logout() {
        return "redirect:/";
    }
}
