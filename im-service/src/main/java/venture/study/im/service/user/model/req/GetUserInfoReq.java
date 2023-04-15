/*
 * @author: Venture
 * @description:
 */

package venture.study.im.service.user.model.req;

import lombok.Data;
import venture.study.im.common.model.RequestBase;

import java.util.List;


@Data
public class GetUserInfoReq extends RequestBase {
    private List<String> userIds;
}
