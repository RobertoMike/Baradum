package io.github.robertomike.baradum.requests;

import io.github.robertomike.hefesto.enums.Sort;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OrderRequest {
    private String field;
    private Sort sort = Sort.ASC;
}
