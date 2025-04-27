package com.mojian.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @className: ResponseDTO
 * @author: Icw
 * @date: 2025/4/26 21:47
 * @Version: 1.0
 * @description:
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResponseDTO {
        private String status;
        private String message;
        private Object data;

       public ResponseDTO(String status, Object data) {
               this.status = status;
               this.data = data;
       }
       public static   ResponseDTO success(Object data) {
               return new ResponseDTO("CONFIRMED", data);
       }

}
