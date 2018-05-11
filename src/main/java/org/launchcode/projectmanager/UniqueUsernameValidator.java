package org.launchcode.projectmanager;

import org.launchcode.projectmanager.models.data.UserDao;
import org.springframework.beans.factory.annotation.Autowired;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class UniqueUsernameValidator implements ConstraintValidator<UniqueUsername, String> {

    @Autowired
    private UserDao userDao;

    public UniqueUsernameValidator(UserDao userDao) {
        this.userDao = userDao;
    }

    public UniqueUsernameValidator() {
    }

    @Override
    public void initialize(UniqueUsername constraintAnnotation) {
    }

    @Override
    public boolean isValid(String username, ConstraintValidatorContext context) {
        if (userDao.findByUsername(username).isEmpty() || userDao.findByUsername(username) == null) {
            return true;
        }
        return false;
    }
}
