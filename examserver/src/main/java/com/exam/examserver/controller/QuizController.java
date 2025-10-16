package com.exam.examserver.controller;

import com.exam.examserver.dto.ApiResponse;
import com.exam.examserver.dto.QuizDTO;
import com.exam.examserver.entity.exam.Category;
import com.exam.examserver.entity.exam.Quiz;
import com.exam.examserver.mapper.QuizMapper;
import com.exam.examserver.service.CategoryService;
import com.exam.examserver.service.QuizService;
import com.exam.examserver.service.QuizServiceImpl;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("v1/quiz")
@CrossOrigin("*")
public class QuizController {

    @Autowired
    private QuizService quizService;

    @Autowired
    private CategoryService categoryService;

    // Add a quiz
    @PostMapping(value = "/",consumes = "application/json",produces = "application/json")
    public ResponseEntity<ApiResponse<QuizDTO>> addQuiz (@RequestBody QuizDTO quizDTO){

        // Convert  dto->entity
        Quiz quiz = QuizMapper.toEntity(quizDTO);

        if(quizDTO.getCategoryId() != null){
            Category category = categoryService.getCategory(quizDTO.getCategoryId());
        }

        //Save and return DTO (201)
        Quiz saved = quizService.addQuiz(quiz);
        QuizDTO result = QuizMapper.toDto(saved);

        ApiResponse<QuizDTO> response = new ApiResponse<>("Quiz Added successfully", result);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    //Update a quiz (Supports partial update too)
    @PatchMapping(value = "/{qId}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<QuizDTO>> patchQuiz(
            @PathVariable ("qId") Long qId,
            @RequestBody Map<String,Object> updates){

        QuizDTO updated = quizService.patchQuiz(qId,updates);
        ApiResponse<QuizDTO> response = new ApiResponse<>("Quiz patched successfully",updated);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    // PATCH /api/quizzes?c={cid}&q={qid}
    @PatchMapping(value = "/quizzes",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<QuizDTO>> patchQuizOptimised(
            @RequestParam("cid") Long cid,
            @RequestParam("qId") Long qId,
            @RequestBody Map<String , Object> updates){
        System.out.println("Controller: patch called with cid=" + cid + " qId=" + qId + " keys=" + updates.keySet());
        QuizDTO updated = quizService.patchQuizOptimised(cid, qId, updates);
        ApiResponse<QuizDTO> response = new ApiResponse<>("Quiz patched successfully",updated);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }


    //get all quiz
//    @GetMapping(value = "/")
//    public ResponseEntity<ApiResponse<Set<Quiz>>> getAllQuiz(){
//        Set<Quiz> allQuiz =  quizService.getQuizzes();
//
//        ApiResponse<Set<Quiz>> response = new ApiResponse<>("All quizzes fetched successfully",allQuiz);
//        return ResponseEntity.status(HttpStatus.OK).body(response);
//    }

    //Get a quiz
    @GetMapping(value = "/{qId}")
    public ResponseEntity<ApiResponse<Quiz>> getQuiz(@PathVariable ("qId") Long qId){
        Quiz quiz = quizService.getQuiz(qId);
        ApiResponse<Quiz> response = new ApiResponse<>("Single quiz fetched successfully",quiz);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

//    //get a quiz by its category id and quiz id
//    @GetMapping("/quizzes")
//    public ResponseEntity<ApiResponse<Quiz>> getQuizByCidAndQId(
//            @RequestParam("cid") Long cid,
//            @RequestParam("qId") Long qId){
//        Quiz quiz = quizService.getQuizByCidAndQid(qId,cid);
//        ApiResponse<Quiz> response = new ApiResponse<>("Single Quiz fetched Successfully",quiz);
//        return ResponseEntity.status(HttpStatus.OK).body(response);
//    }

    //Delete a quiz
    @DeleteMapping(value = "/{qId}")
    public ResponseEntity<?> deleteAndReturn (@PathVariable("qId") Long qId){
        QuizDTO dto = quizService.deleteAndReturnQuiz(qId);
        ApiResponse<QuizDTO> response = new ApiResponse<>("Quiz Deleted Successfully",dto);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    //Get all quizzes of a Category using its cId
    @GetMapping(value = "/category/{cId}")
    public ResponseEntity<?> getAllQuizzesOfaCategory(@PathVariable("cId") Long cId){
        List<QuizDTO> allQuiz = quizService.getAllQuizzesOfaCategory(cId);
        ApiResponse<List<QuizDTO>> response = new ApiResponse<>("Quizzes of cid: "+cId+" fetched successfully",allQuiz);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    //Get all active quizzes
    @GetMapping(value = "/active")
    public  ResponseEntity<?> getActiveQuizzes(){
        List<Quiz> allActiveQuizzes = quizService.getActiveQuizzes();
        ApiResponse<List<Quiz>> response = new ApiResponse<>("All active quizzes fetched successfully",allActiveQuizzes);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    //Get Active quizzes of category
    @GetMapping(value = "/category/active/{cid}")
    public ResponseEntity<?> getActiveQuizzesOfCategory(@PathVariable("cid") Long cid){

        List<Quiz> allActiveQuizOfCategory = quizService.getActiveQuizzesOfCategory(cid);
        ApiResponse<List<Quiz>> response = new ApiResponse<>("All active quizzes of a category fetched successfully",allActiveQuizOfCategory);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    //soft delete quiz
    @DeleteMapping(path ="/soft-delete/by-category", params = {"cid","qId"})
    public ResponseEntity<?> softDeleteQuiz(
            @RequestParam("cid")Long cid,
            @RequestParam("qId") Long qId
            ){
        QuizDTO deleteQuiz = quizService.softDeleteQuiz(qId,cid);
        ApiResponse<QuizDTO> response = new ApiResponse<>("Quiz soft delete successful",deleteQuiz);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    //get all quizzes by cid and qId
    @GetMapping(value = "/")
    public ResponseEntity<?> getAllQuizzes(
            @RequestParam("cid") Long cid,
            @RequestParam("qId") Long qId
    ){
        QuizDTO quizDTO = quizService.getQuizByCidAndQid(qId,cid);
        ApiResponse<?> response = new ApiResponse<>("Quiz fetched successfully",quizDTO);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
