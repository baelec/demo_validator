import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.ObjectReader
import com.fasterxml.jackson.databind.ObjectWriter
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import kotlinx.coroutines.delay
import gate.GateType
import gate.ValidationGate
import java.time.Duration
import kotlin.random.Random

fun throwError() {
  throw RuntimeException("WTF BRO")
}

suspend fun delayUpTo(maxDelaySeconds: Long) {
  val delay = Duration.ofSeconds(Random.nextLong(1, maxDelaySeconds))
  println("Delaying validator ${delay.seconds} seconds(s) to simulate async validators")
  delay(delay.toMillis())
}

fun Collection<ValidationGate>.checkPipelineConfig(): Collection<ValidationGate> {
  when {
    this.isEmpty() -> {
      // Check that the pipeline has gates
      throwError()
    }
    this.first().type != GateType.DRAFT -> {
      // Check that the first gate is DRAFT
      throwError()
    }
    this.last().type != GateType.APPROVED -> {
      // Check that the last gate is APPROVED
      throwError()
    }
    this.first().validators.isNotEmpty() -> {
      // It doesn't make sense for DRAFT to have validators
      throwError()
    }
  }
  var latestGate = GateType.DRAFT
  // Check that gates are in order
  for (gate in this) {
    if (gate.type > latestGate) {
      latestGate = gate.type
    } else if (gate.type < latestGate) {
      throwError()
    }
  }
  return this
}

val jsonMapper = ObjectMapper().registerKotlinModule()
val jsonReader: ObjectReader = jsonMapper.readerForListOf(ValidationGate::class.java)
val jsonWriter: ObjectWriter = jsonMapper.writerFor(object : TypeReference<List<ValidationGate>>() {}).withDefaultPrettyPrinter()
