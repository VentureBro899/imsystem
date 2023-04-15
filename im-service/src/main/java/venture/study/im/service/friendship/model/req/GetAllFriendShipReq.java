package venture.study.im.service.friendship.model.req;

import lombok.Data;
import venture.study.im.common.model.RequestBase;

import javax.validation.constraints.NotEmpty;

/*
 * @author: Venture
 * @description: TODO
 */
@Data
public class GetAllFriendShipReq extends RequestBase {
    @NotEmpty(message = "用户id不能为空")
    private String fromId;
}
