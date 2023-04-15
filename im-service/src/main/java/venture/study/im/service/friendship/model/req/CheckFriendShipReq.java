package venture.study.im.service.friendship.model.req;

import lombok.Data;
import venture.study.im.common.model.RequestBase;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

/*
 * @author: Venture
 * @description: TODO
 */
@Data
public class CheckFriendShipReq extends RequestBase {
    @NotEmpty(message = "用户id不能为空")
    private String fromId;
    @NotEmpty(message = "用户id不能为空")
    private List<String> toIds;
    @NotNull(message = "checkType不能为空")
    private Integer checkType;

}
