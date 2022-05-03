package wealthrating.dal;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.Id;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
@Entity
public class Person {

    @Id
    private Integer id;
    private String firstName;
    private String lastName;
    private String city;
    private Long cash;
    private Integer numberOfAssets;


}
