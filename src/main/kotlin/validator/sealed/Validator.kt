package validator.sealed

import com.fasterxml.jackson.annotation.JsonTypeInfo
import validator.Validatable
import validator.ValidatorResult

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME)
sealed interface Validator {
  suspend fun validate(item: Validatable): Collection<ValidatorResult>
  var enabled: Boolean
}