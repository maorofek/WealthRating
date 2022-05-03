package wealthrating.boundary;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class PersonBoundary {

    private Integer id;
    private PersonalInfo personalInfo;
    private FinancialInfo financialInfo;
}