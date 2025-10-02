package com.exam.examserver.mapper;

import com.exam.examserver.dto.QuizDTO;
import com.exam.examserver.entity.exam.Category;
import com.exam.examserver.entity.exam.Quiz;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.stereotype.Component;

//@Mapper(componentModel = "spring")
@Component
public class QuizMapper {

   // entity->dto
//   @Mapping(source = "category.cid", target = "categoryId")
    public static QuizDTO toDto(Quiz q){
        if(q==null) return null;
        QuizDTO quizDTO = new QuizDTO();
        quizDTO.setqId(q.getqId());
        quizDTO.setTitle(q.getTitle());
        quizDTO.setDescription(q.getDescription());
        quizDTO.setMaxMarks(q.getMaxMarks());
        quizDTO.setNumberOfQuestions(q.getNumberOfQuestions());
        quizDTO.setActive(q.isActive());
        if(q.getCategory()!=null){
            quizDTO.setCategoryId(q.getCategory().getCid());
        }
        return quizDTO;
    }

    //dto->entity
//    @Mapping(target = "category", expression = "java(toCategory(dto.getCategoryId()))")
    public static Quiz toEntity (QuizDTO dto){
        if(dto == null ) return null;

        Quiz q = new Quiz();
        q.setqId(dto.getqId());
        q.setTitle(dto.getTitle());
        q.setDescription(dto.getDescription());
        q.setMaxMarks(dto.getMaxMarks());
        q.setNumberOfQuestions(dto.getNumberOfQuestions());
        q.setActive(dto.isActive());

        if(dto.getCategoryId() != null){
            Category category = new Category();
            category.setCid(dto.getCategoryId());
            q.setCategory(category);
        }
        return q;
    }
}
