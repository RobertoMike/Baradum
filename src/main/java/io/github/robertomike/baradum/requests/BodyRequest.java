package io.github.robertomike.baradum.requests;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class BodyRequest {
    private List<FilterRequest> filters = new ArrayList<>();
    private List<OrderRequest> sorts = new ArrayList<>();
}
