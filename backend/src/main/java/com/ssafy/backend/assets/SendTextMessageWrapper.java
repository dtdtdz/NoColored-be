package com.ssafy.backend.assets;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Stack;

public class SendTextMessageWrapper {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final Wrapper wrapper = new Wrapper();

    public static String wrapAndConvertToJson(Object dto) throws JsonProcessingException {

        // "type"과 "data"를 가진 객체 생성
        if (dto instanceof String str){
            wrapper.setType(str);
        } else {
            // DTO 클래스 이름을 소문자로 시작하게 변환 (예: MyDto -> myDto)
            String typeName = dto.getClass().getSimpleName();
//        typeName = Character.toLowerCase(typeName.charAt(0)) + typeName.substring(1);

            wrapper.setType(typeName);
            wrapper.setData(dto);
        }

        // 객체를 JSON 문자열로 변환
        return objectMapper.writeValueAsString(wrapper);
    }

    // "type"과 "data"를 갖는 Wrapper 클래스
    @NoArgsConstructor
    @Setter
    @Getter
    private static class Wrapper {
        private String type;
        private Object data;

        public Wrapper(String type, Object data) {
            this.type = type;
            this.data = data;
        }

        // getter, setter 생략
    }
}
