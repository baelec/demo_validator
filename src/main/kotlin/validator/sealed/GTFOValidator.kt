package validator.sealed

import delayUpTo
import kotlinx.coroutines.coroutineScope
import validator.Validatable
import validator.ValidatorResult
import validator.warn

data class GTFOValidator(
  override var enabled: Boolean = true
): Validator {
  override suspend fun validate(item: Validatable): Collection<ValidatorResult> {
    coroutineScope {
      delayUpTo(5)
    }
    return listOf(
      validator.error("GTFO", "You shall not pass!"),
      warn("Sorry", "It's not you. It's me.")
    )
  }
}