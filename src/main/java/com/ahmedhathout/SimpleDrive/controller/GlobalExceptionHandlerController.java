package com.ahmedhathout.SimpleDrive.controller;

import com.ahmedhathout.SimpleDrive.exceptions.IllegalFileAccessException;
import com.ahmedhathout.SimpleDrive.exceptions.UserAlreadyExistsException;
import lombok.extern.log4j.Log4j2;
import org.apache.tomcat.util.http.fileupload.impl.FileSizeLimitExceededException;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.ConstraintViolationException;

@ControllerAdvice
@Log4j2
public class GlobalExceptionHandlerController {

    @ExceptionHandler(value = IllegalFileAccessException.class)
    public String illegalFileAccessHandler(IllegalFileAccessException ex,
                                           Model model) {

        model.addAttribute("errorMessage", ex.getMessage());
        return "accessdenied";
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public String illegalArgumentHandler(ConstraintViolationException ex,
                                         Model model) {

        model.addAttribute("errorMessage", ex.getMessage());
        return "illegalargument";
    }

    // TODO the error message is not sent for some reason
    @ExceptionHandler(UserAlreadyExistsException.class)
    public String userAlreadyExistsHandler(UserAlreadyExistsException ex,
                                           RedirectAttributes redirectAttributes) {

        redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        return "redirect:/signup";
    }

    @ExceptionHandler(FileSizeLimitExceededException.class)
    public String SizeLimitExceededHandler(FileSizeLimitExceededException ex,
                                           RedirectAttributes redirectAttributes) {

        redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        return "redirect:/";
    }

    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler({Throwable.class})
    @ResponseBody
    public String generalExceptionHandler(Throwable th) {
        log.error("Error caught by general handler");
        th.printStackTrace();
        return "Oops, Seems like something went wrong. Please try again";
    }
}
