package validator.sealed

import delayUpTo
import validator.Validatable
import validator.ValidatorResult
import validator.tempWarn

data class WhyIsTheServerARaspberryPiValidator(override var enabled: Boolean = true): Validator {
  override suspend fun validate(item: Validatable): Collection<ValidatorResult> {
    delayUpTo(5)
    return listOf(tempWarn("Unable to connect", "Unable to connect to remote server. Try again later."))
  }
}