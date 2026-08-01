package edu.university.system.controller;

import java.math.BigDecimal;
import java.time.LocalDate;

abstract class ControllerSupport {

    protected void requireObject(Object value, String fieldName) {
        if (value == null) {
            throw new ValidationException(fieldName + " es requerido.");
        }
    }

    protected void requireId(Long id, String fieldName) {
        if (id == null || id <= 0) {
            throw new ValidationException(fieldName + " debe tener un identificador valido.");
        }
    }

    protected void requireText(String value, String fieldName, int minimumLength) {
        if (value == null || value.trim().length() < minimumLength) {
            throw new ValidationException(fieldName + " debe tener al menos " + minimumLength + " caracteres.");
        }
    }

    protected void validateOptionalEmail(String email, String fieldName) {
        if (email != null && !email.isBlank() && !email.contains("@")) {
            throw new ValidationException(fieldName + " debe tener un formato valido.");
        }
    }

    protected void validateOptionalPhone(String phone, String fieldName) {
        if (phone == null || phone.isBlank()) {
            return;
        }
        String trimmedPhone = phone.trim();
        if (!trimmedPhone.matches("[0-9+()\\-\\s]{7,20}")) {
            throw new ValidationException(fieldName + " debe tener un formato valido.");
        }
    }

    protected void validateNonNegative(BigDecimal value, String fieldName) {
        if (value != null && value.compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidationException(fieldName + " no puede ser negativo.");
        }
    }

    protected void validateNonNegative(int value, String fieldName) {
        if (value < 0) {
            throw new ValidationException(fieldName + " no puede ser negativo.");
        }
    }

    protected void requireDate(LocalDate value, String fieldName) {
        if (value == null) {
            throw new ValidationException(fieldName + " es requerida.");
        }
    }

    protected void validateDateRange(LocalDate startDate, LocalDate endDate, String startField, String endField) {
        if (startDate != null && endDate != null && endDate.isBefore(startDate)) {
            throw new ValidationException(endField + " no puede ser anterior a " + startField + ".");
        }
    }
}
