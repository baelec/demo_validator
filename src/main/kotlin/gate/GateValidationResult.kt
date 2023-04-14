package gate

import validator.ValidatorResult

data class GateValidationResult(
  val type: GateType,
  val validatorResults: Collection<ValidatorResult>
) {
  val severity by lazy {
    validatorResults.maxOfOrNull { it.severity }
  }
}