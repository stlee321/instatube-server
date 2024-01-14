package me.stlee321.instatube.jwt;

public class PassSubjectValidator implements SubjectValidator {

    @Override
    public boolean isValidSubject(String subject) {
        return true;
    }
}
