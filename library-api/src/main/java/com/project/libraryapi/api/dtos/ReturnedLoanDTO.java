package com.project.libraryapi.api.dtos;

import javax.validation.constraints.NotEmpty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReturnedLoanDTO {

    @NotEmpty
    private Boolean returned;
}
