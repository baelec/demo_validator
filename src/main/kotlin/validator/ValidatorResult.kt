package validator

sealed class ValidatorResult(val severity: Severity, val title: String, val message: String) {
  override fun toString(): String {
    return "$severity: $title - $message"
  }
}

class ErrorValidatorResult(title: String, message: String): ValidatorResult(title = title, message = message, severity = Severity.ERROR)
class WarningValidatorResult(title: String, message: String): ValidatorResult(title = title, message = message, severity = Severity.WARNING)
class InfoValidatorResult(title: String, message: String): ValidatorResult(title = title, message = message, severity = Severity.INFO)
class SuccessValidatorResult(title: String, message: String): ValidatorResult(title = title, message = message, severity = Severity.SUCCESS)
class TemporaryErrorValidatorResult(title: String, message: String): ValidatorResult(title = title, message = message, severity = Severity.TEMPORARY_ERROR)
class TemporaryWarningValidatorResult(title: String, message: String): ValidatorResult(title = title, message = message, severity = Severity.TEMPORARY_WARNING)

fun error(title: String, message: String): ErrorValidatorResult {
  return ErrorValidatorResult(
    title = title,
    message = message,
  )
}

fun warn(title: String, message: String): WarningValidatorResult {
  return WarningValidatorResult(
    title = title,
    message = message,
  )
}

fun info(title: String, message: String): InfoValidatorResult {
  return InfoValidatorResult(
    title = title,
    message = message,
  )
}

fun success(title: String, message: String): SuccessValidatorResult {
  return SuccessValidatorResult(
    title = title,
    message = message,
  )
}

fun tempError(title: String, message: String): TemporaryErrorValidatorResult {
  return TemporaryErrorValidatorResult(
    title = title,
    message = message,
  )
}

fun tempWarn(title: String, message: String): TemporaryWarningValidatorResult {
  return TemporaryWarningValidatorResult(
    title = title,
    message = message,
  )
}