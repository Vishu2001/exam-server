package com.exam.examserver.controller;

import com.exam.examserver.dto.ApiResponse;
import com.exam.examserver.dto.CategoryDTO;
import com.exam.examserver.dto.QuestionDTO;
import com.exam.examserver.entity.exam.Question;
import com.exam.examserver.entity.exam.Quiz;
import com.exam.examserver.mapper.QuestionMapper;
import com.exam.examserver.service.QuestionService;
import com.exam.examserver.service.QuizService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/v1/question")
@CrossOrigin("*")
public class QuestionController {

    @Autowired
    private QuestionService questionService;

    @Autowired
    private QuizService quizService;

    // Add a question
    @PostMapping(value = "/")
    public ResponseEntity<?> addQuestion(@Valid @RequestBody QuestionDTO dto) {
        Question saved = questionService.addQuestion(dto);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(saved.getQuestionId())
                .toUri();

        ApiResponse<Question> response = new ApiResponse<>("Question Added successfully", saved);

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(location);

        return ResponseEntity.status(HttpStatus.CREATED).headers(headers).body(response);
    }

    //Partial update
    @PatchMapping(value = "/{questionId}")
    public ResponseEntity<?> patchQuestion (@PathVariable("questionId")Long questionId,
                                            @RequestBody Map<String,Object> updates){
        QuestionDTO questionDTO = questionService.patchQuestion(questionId, updates);
        ApiResponse<QuestionDTO> response = new ApiResponse<>("Question updated successfully", questionDTO);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    //get all question of a quiz
    @GetMapping(value = "/quiz/{qId}")
    public ResponseEntity<?> getQuestionsOfQuiz(@PathVariable("qId") Long qId) {
        // 1) ensure quiz exists (will throw ResourceNotFoundException -> handled by GlobalExceptionHandler)
        Quiz quiz = this.quizService.getQuiz(qId);

        // 2) load questions via repository/service (avoids lazy loading issues)
        Set<Question> questions = this.questionService.getQuestionsOfQuiz(quiz);

        // 3) convert to list and shuffle
        List<Question> list = new ArrayList<>(questions);
        Collections.shuffle(list);

        // 4) parse numberOfQuestions safely
        int limit = list.size(); // default: return all
        String numStr = quiz.getNumberOfQuestions();
        if (numStr != null) {
            try {
                int requested = Integer.parseInt(numStr);
                if (requested >= 0) {
                    limit = Math.min(limit, requested);
                }
            } catch (NumberFormatException ignored) {
                // keep default limit (all). Optionally log the invalid number.
            }
        }

        // 5) trim list if needed
        if (list.size() > limit) {
            list = list.subList(0, limit);
        }
        ApiResponse<?> response = new ApiResponse<>("Questions from the given Quiz Id successfully fetched",list);
        // 6) return
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    //Get a question
    @GetMapping("/{questionId}")
    public ResponseEntity<?> getQuestion(@PathVariable("questionId") Long questionId){

        Question question = questionService.getQuestion(questionId);
        ApiResponse<Question> response = new ApiResponse<>("Question fetched Successfully",question);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    //delete a question
    @DeleteMapping("/{questionId}")
    public ResponseEntity<?> deleteAndReturn(@PathVariable ("questionId") Long questionId){
        QuestionDTO dto = questionService.deleteAndReturn(questionId);
        ApiResponse<QuestionDTO> response = new ApiResponse<>("Question deleted successfully",dto);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    //soft delete question
    @DeleteMapping(path = "/soft-delete/" , params ={"cid","qId","questionId"} )
    public ResponseEntity<?> softDeleteQuiz(
            @RequestParam("cid") Long cid,
            @RequestParam("qId") Long qId,
            @RequestParam("questionId") Long questionId
    ){
        QuestionDTO questionDTO = questionService.softDeleteQuestion(questionId);
        ApiResponse<?> response = new ApiResponse<>("Soft delete question successful",questionDTO);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping(value = "/list-questions")
    public ResponseEntity<?> getAllQuestionsByQIdAndCid(
            @RequestParam(value = "page",defaultValue = "0") int page,
            @RequestParam(value = "size",defaultValue = "5") int size,
            @RequestParam("qId") Long qId,
            @RequestParam("cid") Long cid
    ){
        Page<QuestionDTO> questionList = questionService.getAllQuestionsByQIdAndCid(page,size,qId,cid);
        ApiResponse<?> response = new ApiResponse<>("All questions fetched successfully",questionList);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
