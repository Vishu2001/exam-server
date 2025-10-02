package com.exam.examserver.mapper;

import com.exam.examserver.dto.QuestionDTO;
import com.exam.examserver.entity.exam.Question;
import com.exam.examserver.entity.exam.Quiz;

public class QuestionMapper {

    //entity -> dto
    public static QuestionDTO toDto(Question q){
        if(q==null) return null;
        QuestionDTO questionDTO = new QuestionDTO();
        questionDTO.setQuestionId(q.getQuestionId());
        questionDTO.setImage(q.getImage());
        questionDTO.setOption1(q.getOption1());
        questionDTO.setOption2(q.getOption2());
        questionDTO.setOption3(q.getOption3());
        questionDTO.setOption4(q.getOption4());
        questionDTO.setAnswer(q.getAnswer());

        if(q.getQuiz() != null ) questionDTO.setQuizId(q.getQuiz().getqId());
        return questionDTO;
    }

    //dto-> entity
    public static Question toEntity(QuestionDTO dto){
        if (dto == null) return null;
        Question q = new Question();
        q.setQuestionId(dto.getQuestionId());
        q.setContent(dto.getContent());
        q.setImage(dto.getImage());
        q.setOption1(dto.getOption1());
        q.setOption2(dto.getOption2());
        q.setOption3(dto.getOption3());
        q.setOption4(dto.getOption4());
        q.setAnswer(dto.getAnswer());

        if (dto.getQuizId() != null) {
            Quiz quiz = new Quiz();
            quiz.setqId(dto.getQuizId());
            q.setQuiz(quiz); // transient quiz; controller should set managed quiz if available
        }
        return q;
    }
}
