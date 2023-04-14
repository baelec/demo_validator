package gate

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import validator.Validatable
import validator.sealed.Validator

data class ValidationGate(
  val type: GateType,
  val validators: Collection<Validator>,
)

suspend fun ValidationGate.evaluate(validatable: Validatable): GateValidationResult {
  return GateValidationResult(
    this.type,
    coroutineScope {
      validators.filter { it.enabled }.map { async { it.validate(validatable) } }
        .awaitAll()
        .flatten()
    },
  )
}