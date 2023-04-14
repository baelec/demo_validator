import gate.GateType
import validator.Severity
import validator.Validatable
import gate.ValidationGate
import gate.evaluate
import validator.sealed.FreeCandyValidator
import validator.sealed.GTFOValidator
import validator.sealed.WhyIsTheServerARaspberryPiValidator

suspend fun main() {
  /*
   * There is an argument to be made for this to be a ValidationPipeline class / Map<GateType, List<Validator>> rather than a list.
   * A list allows multiple gates of the same type which doesn't really make sense (or for gates to be in the wrong order).
   * If it's an object, we have an API that guarantees that everything looks exactly as it should.
   * Otherwise, we need to check for the following errors.
   */
  val validationPipelineJson = jsonWriter.writeValueAsString(listOf(
    ValidationGate(
      GateType.DRAFT,
      listOf() // It doesn't make sense for DRAFT to have validators
    ),
    ValidationGate(
      GateType.MIDDLE_MANAGER,
      listOf(
        FreeCandyValidator(),
        GTFOValidator(enabled = false),
        WhyIsTheServerARaspberryPiValidator()
      ),
    ),
    ValidationGate(
      GateType.BIG_BOSS,
      listOf(
        FreeCandyValidator(),
        GTFOValidator(),
      ),
    ),
    ValidationGate(
      GateType.APPROVED,
      listOf()
    )
  ).checkPipelineConfig())

  println(validationPipelineJson)
  println()

  val validationPipeline = jsonReader.readValue<Collection<ValidationGate>>(validationPipelineJson)

  val itemToValidate = object : Validatable {}
  val currentGate = GateType.values().random()
  println("Current gate: $currentGate")

  val nextGate = GateType.values().getOrNull(currentGate.ordinal + 1) ?: run {
    println("Already approved")
    return
  }
  println("Next gate: $nextGate")

  val gateResult = validationPipeline.first { it.type === nextGate }.evaluate(itemToValidate)

  val message = when (gateResult.severity) {
    Severity.INFO, Severity.SUCCESS, null -> "Can continue without issue"
    Severity.WARNING -> "Can continue with warnings"
    Severity.TEMPORARY_WARNING -> "Can continue because we don't really trust how reliable this is"
    Severity.ERROR -> "Can not continue"
    Severity.TEMPORARY_ERROR -> "Can not continue now but we might be able to at a later date"
  }
  println("Validation result: ${gateResult.severity} - $message")
  println("\nIndividual rule results:")
  gateResult.validatorResults.forEach { println(it) }
}