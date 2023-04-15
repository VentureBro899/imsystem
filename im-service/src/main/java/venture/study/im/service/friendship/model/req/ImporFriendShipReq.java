/*
 * @author: Venture
 * @description:
 */

/*
 * @author: Venture
 * @description:
 */

package venture.study.im.service.friendship.model.req;

import lombok.Data;
import venture.study.im.common.enums.FriendShipStatusEnum;
import venture.study.im.common.model.RequestBase;

import javax.validation.constraints.NotEmpty;
import java.util.List;

/*
 * @author: Venture
 * @description: TODO
 */
@Data
public class ImporFriendShipReq extends RequestBase {
    @NotEmpty(message = "用户id不能为空")
    private String fromId;

    private List<ImportFriendDto> friendItem;

    @Data
    public static class ImportFriendDto{

        private String toId;

        private String remark;

        private String addSource;

        // 默认未非朋友
        private Integer status = FriendShipStatusEnum.FRIEND_STATUS_NO_FRIEND.getCode();

        // 默认为未拉黑
        private Integer black = FriendShipStatusEnum.BLACK_STATUS_NORMAL.getCode();
    }
}
