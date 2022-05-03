package wealthrating.boundary;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FinancialInfo {

    private Long cash;
    private Integer numberOfAssets;

}
