package com.fyre.controllers;

import com.fyre.models.Contest;
import com.fyre.models.Problem;
import com.fyre.models.User;
import com.fyre.services.*;
import com.fyre.util.RankRecord;
import com.fyre.validators.CommonValidators;
import com.fyre.validators.ContestValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class ContestController {

    @Autowired
    private ContestService contestService;

    @Autowired
    private ProblemService problemService;

    @Autowired
    private UserService userService;

    @Autowired
    private SubmissionService submissionService;

    @Autowired
    private StandingService standingService;

    @InitBinder
    public void initBinder ( WebDataBinder binder ) {
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
    }
    // menampilkan daftar kontes yang ada baik yang masih berjalan atau yang sudah berakhir
    @GetMapping("/contests")
    public String getContestsPage(Model model) {
        // kirim ke thymeleaf
        model.addAttribute("contestsList", contestService.getAllContests());
        return "contest/index";
    }

    // menambahkan lomba baru
    // yang bisa upload adalah yang memiliki role ADMIN
    @GetMapping("/contests/new")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public String getContestCreationPage(Model model) {
        // kirim ke thymeleaf
        model.addAttribute("contest", new Contest());
        return "contest/new";
    }

    // Menampilkan profile penyelenggara lomba
    @GetMapping("/contests/creator/{username}")
    @PreAuthorize("hasRole('ROLE_ADMIN') and #username == authentication.principal.username")
    public String getUserOwnContestsPage(@PathVariable("username") String username, Model model) {
        List<Contest> contestsList = contestService.getContestsCreatedByUsername(username);
        // kirim ke thymeleaf
        model.addAttribute("contestsList", contestsList);
        return "contest/creator";
    }

    //  Handling Error saat membuat kontes
    @PostMapping("/contests/new")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public String postNewContest(@ModelAttribute("contest") Contest contest,
                                 BindingResult result,
                                 RedirectAttributes redirectAttributes) {
        ContestValidator.validate(contest, result);
        //  Ketika gagal akan dikembalikan ke form untuk membuat kontes kembali
        if(result.hasErrors()) {
            return "contest/new";
        }
        contestService.createNewContest(contest);
        //  Ketika berhasil akan memunculkan pesan "Contest created successfully"
        // kirim ke thymeleaf
        redirectAttributes.addFlashAttribute("alert", "Contest created successfully");
        redirectAttributes.addFlashAttribute("alertType", "success");
        return "redirect:/contests";
    }


    //    Halaman untuk mengedit lomba, apabila terjadi perbaikan
    @GetMapping("/contests/edit/{id}")
    //    Tentu saja halaman hanya untuk role ADMIN
    @PreAuthorize("hasRole('ROLE_ADMIN') and #username == authentication.principal.username")
    public String getEditContestPage(@PathVariable("id") long contestId,
                                     @RequestParam("username") String username,
                                     Model model,
                                     RedirectAttributes redirectAttributes) {
        Contest contest = contestService.getById(contestId);
        //Handling Error jika kontes tidak ada
        if(null == contest) {
            redirectAttributes.addFlashAttribute("alert", "The requested contest not exits");
            redirectAttributes.addFlashAttribute("alertType", "primary");
            return "redirect:/contests";
        }
        //    Terdapat error handling ketika yang mengakses halaman ini bukan ADMIN terkait,
        //    akan dikembalikan pesan "Access Denied"
        if(!contest.getCreatorUsername().equalsIgnoreCase(username)) {
            throw new AccessDeniedException("You don't have any permission to access this page");
        }
        // kirim ke thymeleaf
        model.addAttribute("contest", contest);
        model.addAttribute("editContestForm", contest);
        return "contest/edit";
    }

    // Menampilkan Lomba berdasarkan Id
    @GetMapping("/contests/{id}")
    public String getContestDetailsPage(@PathVariable("id") long contestId,
                                        Model model,
                                        RedirectAttributes redirectAttributes) {
        Contest contest = contestService.getById(contestId);
    // Ketika halaman invalid atau tidak ada lomba akan dikirimkan pesan ke thymeleaf (view)
        if(null == contest) {
            redirectAttributes.addFlashAttribute("alert", "Contest not found");
            redirectAttributes.addFlashAttribute("alertType", "primary");
            return "redirect:/contests";
        }
    // Pesan yang ditampilkan jika kontes belum dimulai
        if(contest.getContestStatus() == Contest.Status.NOT_STARTED)
            throw new AccessDeniedException("Contest not started yet");
        model.addAttribute("contest", contest);
        return "contest/details";
    }

    //Menambah problems yang akan dipecahkan oleh peserta / user
    @PostMapping("/contests/{id}/addProblem")
    @PreAuthorize("hasRole('ROLE_ADMIN') and #username == authentication.principal.username")
    public String postAddProblemToContest(@PathVariable("id") long contestId,
                                          @RequestParam("username") String username,
                                          @RequestParam("problemId") long problemId,
                                          Model model,
                                          RedirectAttributes redirectAttributes) {
        Contest contest = contestService.getById(contestId);
        if(null == contest) {
        //  Pesan Error ketika alamat kontes invalid atau kontes tidak ada
            redirectAttributes.addFlashAttribute("alert", "The requested contest not found");
            redirectAttributes.addFlashAttribute("alertType", "primary");
            return "redirect:/contests";
        }
        // Pesan Error jika user yang mengunjungi laman bukan user yang punya hak otoritas
        if(!contest.getCreatorUsername().equalsIgnoreCase(username))
            throw new AccessDeniedException("You don't have any permission to access this page");

        Problem problem = problemService.getProblemById(problemId);
        if(null == problem) {
            model.addAttribute("contest", contest);
            model.addAttribute("editContestForm", contest);
            model.addAttribute("addProblemError", "This problem not found");
            return "contest/edit";
        }
        // melihat problems yang sudah ada dari belakang layar
        List<Problem> existingProblemsInContest = contest.getProblems();
        for(Problem p : existingProblemsInContest) {
            if (p.getId() == problemId) {
                model.addAttribute("contest", contest);
                model.addAttribute("editContestForm", contest);
                // Pesan Error jika problem sudah ada dalam kontes
                model.addAttribute("addProblemError", "Same problem cannot be added twice in the same contest");
                return "contest/edit";
            }
        }

        contestService.addProblemToContest(contestId, problemId);
        // jika proses menambah problem berhasil, maka akan muncul pesan berhasil dan akan dikembalikan ke halaman edit kontes
        redirectAttributes.addFlashAttribute("alert", "Problem added successfully");
        redirectAttributes.addFlashAttribute("alertType", "success");
        return "redirect:/contests/edit/{id}?username=" + username;
    }

    @PostMapping("/contests/update/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN') and #username == authentication.principal.username")
    public String postUpdateContest(@PathVariable("id") long contestId,
                                    @RequestParam("username") String username,
                                    @ModelAttribute("editContestForm") Contest updatedContest,
                                    BindingResult result,
                                    Model model,
                                    RedirectAttributes redirectAttributes) {
        Contest existingContest = contestService.getById(contestId);
        if(null == existingContest) {
            redirectAttributes.addFlashAttribute("alert", "The requested contest not found");
            redirectAttributes.addFlashAttribute("alertType", "primary");
            return "redirect:/contests";
        }
        if(!existingContest.getCreatorUsername().equalsIgnoreCase(username))
            throw new AccessDeniedException("You don't have any permission to access this page");

        ContestValidator.validate(updatedContest, result);

        if(result.hasErrors()) {
            model.addAttribute("contest", existingContest);
            return "contest/edit";
        }

        contestService.updateContestWithExisting(updatedContest, existingContest);

        redirectAttributes.addFlashAttribute("alert", "Contest updated successfully");
        redirectAttributes.addFlashAttribute("alertType", "success");
        return "redirect:/contests/edit/{id}?username=" + username;
    }

    @PostMapping("/contests/{id}/register")
    @PreAuthorize("isAuthenticated() and #username == authentication.principal.username")
    public String registerInContest(@PathVariable("id") long contestId,
                                    @RequestParam("username") String username,
                                    RedirectAttributes redirectAttributes) {
        Contest contest = contestService.getById(contestId);
        if(null == contest) {
            redirectAttributes.addFlashAttribute("alert", "Contest not found");
            redirectAttributes.addFlashAttribute("alertType", "primary");
            return "redirect:/contests";
        }
        User user = userService.getByUserName(username);
        if(null == user) {
            redirectAttributes.addFlashAttribute("alert", "User doesn't exist");
            redirectAttributes.addFlashAttribute("alertType", "primary");
            return "redirect:/contests";
        }
        contestService.registerUser(user, contest);

        redirectAttributes.addFlashAttribute("alert", "You have been registered successfully");
        redirectAttributes.addFlashAttribute("alertType", "success");
        return "redirect:/contests";
    }

    @GetMapping("/contests/{id}/problems/{problemIndex}")
    public String getProblemDetailsPage(@PathVariable("id") long contestId,
                                        @PathVariable("problemIndex") String problemIndex,
                                        Model model,
                                        RedirectAttributes redirectAttributes) {
        Contest contest = contestService.getById(contestId);
        if(null == contest) {
            redirectAttributes.addFlashAttribute("alert", "Contest not found");
            redirectAttributes.addFlashAttribute("alertType", "primary");
            return "redirect:/contests";
        }
        if(contest.getContestStatus() == Contest.Status.NOT_STARTED)
            throw new AccessDeniedException("Contest not started yet");

        Problem problem = contestService.getProblemIndexInContest(contestId, problemIndex);
        if(null == problem) {
            redirectAttributes.addFlashAttribute("alert", "Problem not found");
            redirectAttributes.addFlashAttribute("alertType", "primary");
            return "redirect:/contests/{id}";
        }

        model.addAttribute("problem", problem);
        model.addAttribute("contest", contest);
        return "problem/details";
    }

    @PostMapping("/contests/submitProblem")
    @PreAuthorize("isAuthenticated()")
    public String submitProblem(@RequestParam("problemIndex") String problemIndex,
                                @RequestParam("contestId") long contestId,
                                @RequestParam("outputFile") MultipartFile file,
                                RedirectAttributes redirectAttributes) {

        if(null == file ||
                CommonValidators.isBlank(file.getOriginalFilename())) {
            redirectAttributes.addFlashAttribute("outputFileSubmitErrors", "You should choose a file");
            return "redirect:/contests/" + contestId + "/problems/" + problemIndex;
        }

        String username = userService.getAuthenticatedUsername();
        Contest contest = contestService.getById(contestId);
        if(null == contest) {
            redirectAttributes.addFlashAttribute("alert", "Contest not found");
            redirectAttributes.addFlashAttribute("alertType", "primary");
            return "redirect:/contests";
        }

        if(contest.getContestStatus() != Contest.Status.FINISHED &&
                !contestService.isRegisteredInContest(contestId, username)) {
            redirectAttributes.addFlashAttribute("alert", "Please register in contest first");
            redirectAttributes.addFlashAttribute("alertType", "primary");
            return "redirect:/contests";
        }

        Problem problem = contestService.getProblemIndexInContest(contestId, problemIndex);
        boolean result = false;
        switch (contest.getContestStatus()) {
            case NOT_STARTED:
                throw new AccessDeniedException("Contest not started yet");
            case RUNNING:
                if(null == problem) {
                    redirectAttributes.addFlashAttribute("alert", "Problem not found");
                    redirectAttributes.addFlashAttribute("alertType", "primary");
                    return "redirect:/contests" + contestId;
                }
                int numberOfTries = submissionService.getTriesCountForProblem(contest.getId(), problem.getIndex(), username);
                if(numberOfTries >= 2) {
                    redirectAttributes.addFlashAttribute("submissionExceededError", "You don't have any tries for this problem");
                    return "redirect:/contests/" + contestId + "/problems/" + problemIndex;
                }
                result = submissionService.submit(contest, problem, username, file, true);
                redirectAttributes.addFlashAttribute("submissionStatus", result);
                return "redirect:/contests/" + contestId + "/problems/" + problemIndex;
            case FINISHED:
                result = submissionService.submit(contest, problem, username, file, false);
                redirectAttributes.addFlashAttribute("submissionStatus", result);
                return "redirect:/contests/" + contestId + "/problems/" + problemIndex;
        }

        return "redirect:/contests/" + contestId + "/problems/" + problemIndex;

    }

    @GetMapping("/contests/{id}/standings")
    public String getContestStandingPage(@PathVariable("id") long contestId,
                                         Model model,
                                         RedirectAttributes redirectAttributes) {
        Contest contest = contestService.getById(contestId);
        if(null == contest) {
            redirectAttributes.addFlashAttribute("alert", "Contest not found");
            redirectAttributes.addFlashAttribute("alertType", "primary");
            return "redirect:/contests/{id}";
        }
        if(!contest.getContestStatus().equals(Contest.Status.FINISHED))
            throw new AccessDeniedException("Standing will be available when contest ends");

        List<RankRecord> rankList = standingService.getContestStanding(contestId);

        model.addAttribute("contest", contest);
        model.addAttribute("rankList", rankList);
        return "contest/standings";
    }
}
