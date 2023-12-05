package com.test.service;

import com.jeequan.jeepay.core.exception.BizException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.Set;

/*
 * 通用 Validator
 * @date 2021/6/8 17:47
 */
@Service
public class ValidateService {

    @Autowired
    private Validator validator;

    public void validate(Object obj) {

        Set<ConstraintViolation<Object>> resultSet = validator.validate(obj);
        if (resultSet == null || resultSet.isEmpty()) {
            return;
        }
        resultSet.stream().forEach(item -> {
            throw new BizException(item.getMessage());
        });
    }

}
