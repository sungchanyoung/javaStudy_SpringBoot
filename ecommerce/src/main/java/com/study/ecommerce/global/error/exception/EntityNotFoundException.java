package com.study.ecommerce.global.error.exception;

import com.study.ecommerce.global.error.ErrorCode;
//BusinessException 형태로 동일 ->  최종적으로 BusinessException만 바라본다

public class EntityNotFoundException extends BusinessException {
    public EntityNotFoundException(String message) {
        super(message, ErrorCode.RESOURCE_NOT_FOUND);
    }


    public EntityNotFoundException(ErrorCode errorCode) {
        super(ErrorCode.RESOURCE_NOT_FOUND);

    }
}
