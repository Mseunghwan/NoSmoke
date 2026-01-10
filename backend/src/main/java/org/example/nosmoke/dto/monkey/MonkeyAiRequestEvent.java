package org.example.nosmoke.dto.monkey;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MonkeyAiRequestEvent {

    private Long userId;
    private String prompt;

}
