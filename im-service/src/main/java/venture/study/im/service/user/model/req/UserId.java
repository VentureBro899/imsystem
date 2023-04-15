/*
 * @author: Venture
 * @description:
 */

package venture.study.im.service.user.model.req;

import lombok.Data;
import venture.study.im.common.model.RequestBase;

import javax.validation.constraints.NotEmpty;

/*
 * @author: Venture
 * @description: TODO
 */

@Data
public class UserId extends RequestBase {
    @NotEmpty
    private String userId;
}
