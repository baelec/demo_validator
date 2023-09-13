package validator

sealed class ValidatorResult(val severity: Severity, val title: String, val message: String, val items: Collection<String>? = null) {
  override fun toString(): String {
    return "$severity: $title - $message - $items"
  }
}

class ErrorValidatorResult(title: String, message: String, items: Collection<String>? = null):
  ValidatorResult(title = title, message = message, severity = Severity.ERROR, items = items)
class WarningValidatorResult(title: String, message: String, items: Collection<String>? = null):
  ValidatorResult(title = title, message = message, severity = Severity.WARNING, items = items)
class InfoValidatorResult(title: String, message: String, items: Collection<String>? = null):
  ValidatorResult(title = title, message = message, severity = Severity.INFO, items = items)
class SuccessValidatorResult(title: String, message: String, items: Collection<String>? = null):
  ValidatorResult(title = title, message = message, severity = Severity.SUCCESS, items = items)
class TemporaryErrorValidatorResult(title: String, message: String, items: Collection<String>? = null):
  ValidatorResult(title = title, message = message, severity = Severity.TEMPORARY_ERROR, items = items)
class TemporaryWarningValidatorResult(title: String, message: String, items: Collection<String>? = null):
  ValidatorResult(title = title, message = message, severity = Severity.TEMPORARY_WARNING, items = items)

fun error(title: String, message: String, items: Collection<String>? = null): ErrorValidatorResult {
  return ErrorValidatorResult(
    title = title,
    message = message,
    items = items,
  )
}

fun warn(title: String, message: String, items: Collection<String>? = null): WarningValidatorResult {
  return WarningValidatorResult(
    title = title,
    message = message,
    items = items,
  )
}

fun info(title: String, message: String, items: Collection<String>? = null): InfoValidatorResult {
  return InfoValidatorResult(
    title = title,
    message = message,
    items = items,
  )
}

fun success(title: String, message: String, items: Collection<String>? = null): SuccessValidatorResult {
  return SuccessValidatorResult(
    title = title,
    message = message,
    items = items,
  )
}

fun tempError(title: String, message: String, items: Collection<String>? = null): TemporaryErrorValidatorResult {
  return TemporaryErrorValidatorResult(
    title = title,
    message = message,
  )
}

fun tempWarn(title: String, message: String, items: Collection<String>? = null): TemporaryWarningValidatorResult {
  return TemporaryWarningValidatorResult(
    title = title,
    message = message,
    items = items,
  )
}