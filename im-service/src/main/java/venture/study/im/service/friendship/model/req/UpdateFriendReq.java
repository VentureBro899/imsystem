package venture.study.im.service.friendship.model.req;

import lombok.Data;
import venture.study.im.common.model.RequestBase;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/*
 * @author: Venture
 * @description: TODO
 */
@Data
public class UpdateFriendReq extends RequestBase {
    @NotBlank(message = "fromId不能为空")
    private String fromId;

    @NotNull(message = "toItem不能为空")
    private FriendDto toItem;
}
