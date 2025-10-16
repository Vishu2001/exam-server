package com.exam.examserver.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class QuestionDTO {
    private Long questionId; // optional for update, ignored during create

    @NotBlank (message = "content must not be blank")
    @Size(max = 5000)
    private String content;

    private String image;

    @Size(max = 5000)
    private String option1;
    @Size(max = 5000)
    private String option2;
    @Size(max = 5000)
    private String option3;
    @Size(max = 5000)
    private String option4;

    @NotBlank(message = "answer must not be blank")
    private String answer;

    @NotNull(message = "quizId is required")
    private Long quizId;

    public QuestionDTO() {}

    public QuestionDTO(Long questionId, String content, String image, String option1, String option2, String option3, String option4, String answer, Long quizId) {
        this.questionId = questionId;
        this.content = content;
        this.image = image;
        this.option1 = option1;
        this.option2 = option2;
        this.option3 = option3;
        this.option4 = option4;
        this.answer = answer;
        this.quizId = quizId;
    }

    public QuestionDTO(Long questionId, String content) {
        this.questionId = questionId;
        this.content = content;
    }

    // getters & setters
    public Long getQuestionId() { return questionId; }
    public void setQuestionId(Long questionId) { this.questionId = questionId; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }

    public String getOption1() { return option1; }
    public void setOption1(String option1) { this.option1 = option1; }

    public String getOption2() { return option2; }
    public void setOption2(String option2) { this.option2 = option2; }

    public String getOption3() { return option3; }
    public void setOption3(String option3) { this.option3 = option3; }

    public String getOption4() { return option4; }
    public void setOption4(String option4) { this.option4 = option4; }

    public String getAnswer() { return answer; }
    public void setAnswer(String answer) { this.answer = answer; }

    public Long getQuizId() { return quizId; }
    public void setQuizId(Long quizId) { this.quizId = quizId; }
}
