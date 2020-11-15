package application.controllers;

import application.models.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.HashMap;
import java.util.Optional;

@Controller
@RequestMapping("/campgrounds/{id}/comments")
public class Comments {

    @Autowired
    private CampgroundRepository campgroundRepository;
    @Autowired
    private CommentRepository commentRepository;
    @Autowired
    private UserRepository userRepository;

    @GetMapping("/new")
    public String addCommentForm(@PathVariable long id, Model model, RedirectAttributes redirAttrs) {
        Optional<Campground> foundCampground = campgroundRepository.findById(id);
        if (!foundCampground.isPresent()) {
            redirAttrs.addFlashAttribute("error", "Campground is not found!");
            return "redirect:/campgrounds";
        }
        model.addAttribute("comment", new Comment());
        model.addAttribute("campground", foundCampground.get());
        return "comments/new";
    }

    @PostMapping("")
    public String addComment(@ModelAttribute Comment comment, Model model, @PathVariable long id, Principal principal, RedirectAttributes redirAttrs) {
        Optional<User> foundUser = userRepository.findByUsername(principal.getName());
        Optional<Campground> foundCampground = campgroundRepository.findById(id);
        if (!foundUser.isPresent() || !foundCampground.isPresent()) {
            redirAttrs.addFlashAttribute("error", "User or campground not found!");
            return "redirect:/campgrounds";
        }

        comment.setCampground(foundCampground.get());
        comment.setAuthor(foundUser.get());
        comment.setCurrentDate();

        try {
            commentRepository.save(comment);
        } catch (Exception e) {
            e.printStackTrace();
            redirAttrs.addFlashAttribute("error", "Something went wrong!");
            return "redirect:/campgrounds/" + id;
        }

        redirAttrs.addFlashAttribute("success", "Comment successfully added!");
        return "redirect:/campgrounds/" + id;
    }

    @GetMapping("/{commentId}/edit")
    public String editCommentForm(Model model, @PathVariable long id, @PathVariable long commentId, Principal principal, RedirectAttributes redirAttrs) {
        HashMap<String, Object> result = getCommentFromDB(commentId, principal.getName());
        if (result.containsKey("error")) {
            redirAttrs.addFlashAttribute("error", result.get("error"));
            return "redirect:/campgrounds/" + id;
        }
        model.addAttribute("comment", result.get("comment"));
        model.addAttribute("campgroundId", id);
        return "comments/edit";
    }

    @PutMapping("/{commentId}")
    public String editComment(@ModelAttribute Comment comment, @PathVariable long id, @PathVariable long commentId, Principal principal, RedirectAttributes redirAttrs) {
        HashMap<String, Object> result = getCommentFromDB(commentId, principal.getName());
        if (result.containsKey("error")) {
            redirAttrs.addFlashAttribute("error", result.get("error"));
            return "redirect:/campgrounds/" + id;
        }
        Comment commentDB = (Comment) result.get("comment");

        comment.setId(commentDB.getId());
        comment.setCampground(commentDB.getCampground());
        comment.setAuthor(commentDB.getAuthor());
        comment.setCurrentDate();

        try {
            commentRepository.save(comment);
        } catch (Exception e) {
            e.printStackTrace();
            redirAttrs.addFlashAttribute("error", "Something went wrong!");
            return "redirect:/campgrounds/" + id;
        }

        redirAttrs.addFlashAttribute("success", "Comment updated!");
        return "redirect:/campgrounds/" + id;
    }

    @DeleteMapping("/{commentId}")
    public String deleteComment(@PathVariable long commentId, @PathVariable long id, RedirectAttributes redirAttrs, Principal principal) {
        HashMap<String, Object> result = getCommentFromDB(commentId, principal.getName());
        if (result.containsKey("error")) {
            redirAttrs.addFlashAttribute("error", result.get("error"));
            return "redirect:/campgrounds/" + id;
        }

        commentRepository.deleteById(commentId);

        redirAttrs.addFlashAttribute("success", "Comment is deleted!");
        return "redirect:/campgrounds/" + id;
    }

    public HashMap<String, Object> getCommentFromDB(Long id, String username) {
        /*
        Get Comment from DB, check ownership.
        If success return map[("comment", Comment)] else map[("error", "error_text")].
         */
        HashMap<String, Object> output = new HashMap<>();
        Optional<Comment> result = commentRepository.findById(id);
        if (!result.isPresent()) {
            output.put("error", "Comment is not found!");
            return output;
        }
        Comment commentDB = result.get();
        if (!commentDB.checkOwnership(username)) {
            output.put("error", "You don't have permissions");
        } else {
            output.put("comment", commentDB);
        }
        return output;
    }

}
