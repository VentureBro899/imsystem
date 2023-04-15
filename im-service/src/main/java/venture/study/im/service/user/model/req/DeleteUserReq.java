/*
 * @author: Venture
 * @description:
 */

package venture.study.im.service.user.model.req;

import lombok.Data;
import venture.study.im.common.model.RequestBase;

import javax.validation.constraints.NotEmpty;
import java.util.List;

@Data
public class DeleteUserReq extends RequestBase {
    @NotEmpty(message = "用户id不能为空")
    private List<String> userIds;
}
