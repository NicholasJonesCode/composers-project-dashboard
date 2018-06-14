package org.launchcode.projectmanager;

import org.launchcode.projectmanager.models.data.ProjectDao;
import org.springframework.beans.factory.annotation.Autowired;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class UniqueProjectTitleValidator implements ConstraintValidator<UniqueProjectTitle, String> {

    @Autowired
    private ProjectDao projectDao;

    public UniqueProjectTitleValidator(ProjectDao projectDao) {
        this.projectDao = projectDao;
    }

    public UniqueProjectTitleValidator() {
    }

    @Override
    public void initialize(UniqueProjectTitle constraintAnnotation) {

    }

    @Override
    public boolean isValid(String title, ConstraintValidatorContext context) {
        if (projectDao.findByTitle(title).isEmpty() || projectDao.findByTitle(title) == null) {
            return true;
        }
        return false;
    }
}
