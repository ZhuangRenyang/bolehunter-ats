package com.bolehunter.dto.task;

import lombok.Data;
import lombok.NoArgsConstructor;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.Date;

/**
 * 新增任务
 */
@Data
@NoArgsConstructor
public class CreateTaskDTO {

    @NotBlank(message = "{task.title.not-blank}")
    @Size(max = 100, message = "{task.title.not-blank}")
    private String title;

    @Size(max = 500, message = "{task.content.length}")
    private String content;

    /**
     * 到期时间
     */
    @NotNull(message = "{task.due-time.not-null}")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date dueTime;

    /**
     * 关联对象类型（可选）
     */
    @Pattern(regexp = "recommendation|client|candidate", message = "{candidate.source.illegal}")
    private String relatedType;

    private Integer relatedId;
}
