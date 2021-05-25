package engine;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class QuizController {

    @Autowired
    private QuizRepository quizRepository;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("register")
    public void registerUser(@Valid @RequestBody User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        user.setPassword(new BCryptPasswordEncoder().encode(user.getPassword()));
        userRepository.save(user);
    }

    @PostMapping("quizzes")
    public Quiz createQuiz(@Valid @RequestBody Quiz quiz,
                           @AuthenticationPrincipal User user) {
        quiz.setUser(user);
        return quizRepository.save(quiz);
    }

    @GetMapping("quizzes/{id}")
    public Quiz getQuiz(@PathVariable int id) {
        return quizRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @GetMapping("quizzes")
    public List<Quiz> getQuizzes() {
        return quizRepository.findAll();
    }

    @PostMapping("quizzes/{id}/solve")
    public String solveQuiz(@PathVariable int id, @RequestBody Map<String, int[]> answer) {
        Quiz quiz = quizRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        int[] quizAnswer = quiz.getAnswer();
        if (quizAnswer == null) {
            quizAnswer = new int[]{};
        }
        return Arrays.equals(answer.get("answer"), quizAnswer)
                ? "{\"success\":true,\"feedback\":\"Congratulations, you're right!\"}"
                : "{\"success\":false,\"feedback\":\"Wrong answer! Please, try again.\"}";
    }

    @DeleteMapping("quizzes/{id}")
    public ResponseEntity<?> deleteQuiz(@PathVariable int id,
                                        @AuthenticationPrincipal User user) {
        Quiz quiz = quizRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (quiz.getUser().getId() == user.getId()) {
            quizRepository.delete(quiz);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
    }
}
