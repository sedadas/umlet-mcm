package at.ac.tuwien.model.change.management.core.model;

import lombok.*;

@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class Filter {
    private String key;
    private String value;
}
