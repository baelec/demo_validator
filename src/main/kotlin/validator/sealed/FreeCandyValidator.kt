package validator.sealed

import delayUpTo
import validator.Validatable
import validator.ValidatorResult
import validator.success

data class FreeCandyValidator(override var enabled: Boolean = true): Validator {
  override suspend fun validate(item: Validatable): Collection<ValidatorResult> {
    delayUpTo(5)
    return listOf(success("Free candy", "FREE CANDY!"))
  }
}