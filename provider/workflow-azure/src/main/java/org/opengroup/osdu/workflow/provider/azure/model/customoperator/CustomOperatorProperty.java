package org.opengroup.osdu.workflow.provider.azure.model.customoperator;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CustomOperatorProperty {
  @NotEmpty(message = "Custom property name should not be null or empty")
  private String name;

  @NotNull
  private String description;

  @NotNull
  private Boolean mandatory;
}
