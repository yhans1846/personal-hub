package com.personalhub.common.exception;

import com.personalhub.common.result.Result;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void multipartException_returnsBadRequest() {
        Result<Void> result = handler.handleMultipart(new MultipartException("not a multipart request"));
        assertEquals(400, result.getCode());
        assertEquals("请上传文件", result.getMessage());
    }

    @Test
    void missingPart_returnsBadRequest() throws Exception {
        Result<Void> result = handler.handleMultipart(new MissingServletRequestPartException("file"));
        assertEquals(400, result.getCode());
        assertEquals("请上传文件", result.getMessage());
    }

    @Test
    void unsupportedMediaType_returnsBadRequest() {
        Result<Void> result = handler.handleMultipart(
                new HttpMediaTypeNotSupportedException(MediaType.APPLICATION_JSON, List.of(MediaType.MULTIPART_FORM_DATA)));
        assertEquals(400, result.getCode());
        assertEquals("请上传文件", result.getMessage());
    }

    @Test
    void constraintViolation_returnsBadRequest() {
        @SuppressWarnings("unchecked")
        ConstraintViolation<Object> violation = mock(ConstraintViolation.class);
        when(violation.getMessage()).thenReturn("每页条数最大为 100");
        Result<Void> result = handler.handleConstraintViolation(
                new ConstraintViolationException(Set.of(violation)));
        assertEquals(400, result.getCode());
        assertEquals("每页条数最大为 100", result.getMessage());
    }
}
