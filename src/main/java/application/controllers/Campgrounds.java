package application.controllers;

import application.models.Campground;
import application.models.CampgroundRepository;
import application.models.User;
import application.models.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.HashMap;
import java.util.Optional;

@Controller
@RequestMapping("/campgrounds")
public class Campgrounds {

    @Autowired
    private CampgroundRepository campgroundRepository;
    @Autowired
    private UserRepository userRepository;

    @GetMapping("")
    public String index(Model model) {
        Iterable<Campground> allCampgrounds = campgroundRepository.findAll();
        model.addAttribute("allCampgrounds", allCampgrounds);
        return "campgrounds/index";
    }

    @PostMapping("")
    public String addCampground(@ModelAttribute Campground campground, Model model, Principal principal, RedirectAttributes redirAttrs) {
        Optional<User> result = userRepository.findByUsername(principal.getName());
        if (!result.isPresent()) {
            redirAttrs.addFlashAttribute("error", "User not found!");
            return "redirect:/campgrounds";
        }

        User author = result.get();
        campground.setAuthor(author);

        try {
            campgroundRepository.save(campground);
        } catch (Exception e) {
            e.printStackTrace();
            redirAttrs.addFlashAttribute("error", "Something went wrong!");
            return "redirect:/campgrounds";
        }

        redirAttrs.addFlashAttribute("success", "Campground successfully added!");
        return "redirect:/campgrounds/" + campground.getId();
    }

    @GetMapping("/new")
    public String addCampgroundForm(Model model) {
        model.addAttribute("campground", new Campground());
        return "campgrounds/new";
    }

    @GetMapping("/{id}")
    public String showCampground(@PathVariable long id, Model model, Principal principal, RedirectAttributes redirAttrs) {
        Optional<Campground> result = campgroundRepository.findById(id);
        if (!result.isPresent()) {
            redirAttrs.addFlashAttribute("error", "Campground not found!");
            return "redirect:/campgrounds";
        }
        Campground campground = result.get();
        model.addAttribute("campground", campground);
        model.addAttribute("currentUser", principal == null ? "" : principal.getName());
        return "campgrounds/show";
    }

    @GetMapping("/{id}/edit")
    public String editCampgroundForm(@PathVariable long id, Model model, Principal principal, RedirectAttributes redirAttrs) {
        HashMap<String, Object> result = getCampgroundFromDB(id, principal.getName());
        if (result.containsKey("error")) {
            redirAttrs.addFlashAttribute("error", result.get("error"));
            return "redirect:/campgrounds";
        }
        model.addAttribute("campground", result.get("campground"));
        return "campgrounds/edit";
    }

    @PutMapping("/{id}")
    public String editCampground(@ModelAttribute Campground campground, @PathVariable long id, Principal principal, RedirectAttributes redirAttrs) {
        HashMap<String, Object> result = getCampgroundFromDB(id, principal.getName());
        if (result.containsKey("error")) {
            redirAttrs.addFlashAttribute("error", result.get("error"));
            return "redirect:/campgrounds";
        }

        Campground campgroundDB = (Campground) result.get("campground");
        campground.setAuthor(campgroundDB.getAuthor());
        campground.setId(campgroundDB.getId());

        try {
            campgroundRepository.save(campground);
        } catch (Exception e) {
            e.printStackTrace();
            redirAttrs.addFlashAttribute("error", "Something went wrong!");
            return "redirect:/campgrounds";
        }

        redirAttrs.addFlashAttribute("success", "Campground updated!");
        return "redirect:/campgrounds/" + id;
    }

    @DeleteMapping("/{id}")
    public String deleteCampground(@PathVariable long id, Principal principal, RedirectAttributes redirAttrs) {
        HashMap<String, Object> result = getCampgroundFromDB(id, principal.getName());
        if (result.containsKey("error")) {
            redirAttrs.addFlashAttribute("error", result.get("error"));
            return "redirect:/campgrounds";
        }

        campgroundRepository.deleteById(id);

        redirAttrs.addFlashAttribute("success", "Campground is deleted!");
        return "redirect:/campgrounds";
    }

    public HashMap<String, Object> getCampgroundFromDB(Long id, String username) {
        /*
        Get Campground from DB, check ownership.
        If success return map[("campground", Campground)] else map[("error", "error_text")].
         */
        HashMap<String, Object> output = new HashMap<>();
        Optional<Campground> result = campgroundRepository.findById(id);
        if (!result.isPresent()) {
            output.put("error", "Campground is not found!");
            return output;
        }
        Campground campgroundDB = result.get();
        if (!campgroundDB.checkOwnership(username)) {
            output.put("error", "You don't have permissions");
        } else {
            output.put("campground", campgroundDB);
        }
        return output;
    }
}
