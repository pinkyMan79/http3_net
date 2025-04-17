package one.terenin.srv_common.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor(force = true)
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DataBundle {

    String uuid;
    String name;
    String description;
    String type;
    String mainCategory;
    String price;
    String productOwner;
    List<String> slaveCategories;
    Map<String, String> options;
    Map<String, String> characteristics;

}
